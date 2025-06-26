package stepperfx.controllers;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;
import stepperfx.StepperFields;
import stepperfx.administration.IntegratedController;
import stepperfx.administration.ScreenManager;

import java.util.Optional;

/**
 * Controller for the user input screen
 */
final public class InputController extends IntegratedController {

    /**
     * Options for the app's input selector. The first option should be treated as equal to the second option.
     */
    final private String[] INPUT_SELECTION_OPTIONS = {"Select input mode", "Text", "File"};

    /**
     * Options for the app's operation mode selector. The first option should be treated as equal to the second option.
     */
    final private String[] MODE_OPTIONS = {"Select process", "Forward", "Reverse"};

    /**
     * Options for the app's punctuation selector. The first option should be treated as equal to the second option.
     */
    final private String[] PUNCT_OPTIONS = {"Select punctuation", "Remove All Punctuation", "Remove Spaces", "Use All Punctuation"};

    /**
     * Options for the app's thread selector.<br><br>
     *
     * First option ("Number of threads: 1") >>> app uses 1 thread<br>
     *
     * Second option ("Custom") >>> displays dialog asking user for any amount of threads.
     * Starting the operation with "Custom..." selected should never occur.<br>
     *
     * All other options >>> app uses amount of threads after the phrase "Threads: "<br><br>
     *
     * All options except for the first two should be in the format "Threads: {integer on the interval [1, StepperFields.MAX_THREADS]}"
     */
    final private String[] THREAD_OPTIONS = {"Number of threads: 1", "Custom...", "Threads: 2", "Threads: 4", "Threads: 6",
            "Threads: 8", "Threads: 10", "Threads: 12", "Threads: 16", "Threads: 32"};


    // ////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////////////////////////////////////
    //FXML ELEMENTS


    /**
     * Starts the operations. Changes its text depending on the operation selected.
     */
    @FXML
    private Button startButton;

    /**
     * Allows the user to choose between normal and enhanced (v2) operations
     */
    @FXML
    private CheckBox v2Selector;

    /**
     * Allows the user to select the operation mode (text or file)<br><br>
     *
     * Contents are specified by {@code INPUT_SELECTION_OPTIONS}
     */
    @FXML
    private ChoiceBox<String> inputSelector;

    /**
     * Allows the user to select the operation mode (forward or reverse process)<br><br>
     *
     * Contents are specified by {@code MODE_OPTIONS}
     */
    @FXML
    private ChoiceBox<String> modeSelector;

    /**
     * Allows the user to select the punctuation mode<br><br>
     *
     * Contents are specified by {@code PUNCT_OPTIONS}
     */
    @FXML
    private ChoiceBox<String> punctSelector;

    /**
     * Allows the user to select the number of threads used<br><br>
     *
     * Contents are specified by {@code THREAD_OPTIONS}
     */
    @FXML
    private ComboBox<String> threadSelector;

    /**
     * Label for the top text input that receives the plaintext or ciphertext.
     */
    @FXML
    private Label textInputLabel;

    /**
     * Text area where the user inputs text. Larger than the key input
     */
    @FXML
    private TextArea textInput;

    /**
     * Text are where the user inputs the key
     */
    @FXML
    private TextArea keyInput;

    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //CONSTRUCTOR



    /**
     * Initializes the controller with {@code manager} and {@code fields}.
     * Also configures GUI elements.
     *
     * @param manager the ScreenManager responsible for the controller
     * @param fields reference to shared fields between controllers
     */
    @Override
    public void initializeController(ScreenManager manager, StepperFields fields) {
        //Check thread option formatting
        for(int v=0; v<THREAD_OPTIONS.length; v++) {
            if(v>1) {
                if(!THREAD_OPTIONS[v].startsWith("Threads: ")) {
                    throw new AssertionError("All thread option elements except for the first two must start with \"Threads: \"");
                }

                String currentElement = THREAD_OPTIONS[v];
                try {
                    String substr = currentElement.substring(9);
                    int output = Integer.parseInt(substr);
                    if(output<1 || output>StepperFields.MAX_THREADS) {
                        throw new AssertionError("Number of threads specified by option " + v + " in the thread options array (" +
                                output + ") must be on the interval [1, StepperFields.MAX_THREADS]");
                    }
                }
                catch(NumberFormatException e) {
                    throw new AssertionError("Thread option element " + v + " (\"" + currentElement + "\") " +
                            "must be in the format \"Threads: <integer>\", where <integer> does not have a decimal point");
                }
            }
        }

        //Initialize controller's fields
        this.screenManager = manager;
        this.fields = fields;

        //Set choice boxes and combo box. Then select their first option
        inputSelector.setItems(FXCollections.observableArrayList(INPUT_SELECTION_OPTIONS));
        inputSelector.getSelectionModel().selectFirst();
        modeSelector.setItems(FXCollections.observableArrayList(MODE_OPTIONS));
        modeSelector.getSelectionModel().selectFirst();
        punctSelector.setItems(FXCollections.observableArrayList(PUNCT_OPTIONS));
        punctSelector.getSelectionModel().selectFirst();
        threadSelector.setItems(FXCollections.observableArrayList(THREAD_OPTIONS));
        threadSelector.getSelectionModel().selectFirst();

        //Set button and label changing when the mode selector changes
        modeSelector.valueProperty().addListener((obs, oldValue, newValue) -> {
            if(newValue.equals(MODE_OPTIONS[2])) {
                startButton.setText("Decrypt");
                textInputLabel.setText("Ciphertext");
            }
            else {
                startButton.setText("Encrypt");
                textInputLabel.setText("Plaintext");
            }
        });

        //Set listener on the shared service to clear the inputs when completed
        fields.addServiceValueListener((obs, oldValue, newValue) -> {
            if(newValue != null) {
                textInput.setText("");
                keyInput.setText("");
            }
        });

        //Set "Custom" value insertion option
        threadSelector.valueProperty().addListener((obs, oldValue, newValue) -> {
            if(newValue.equals("Custom...")) {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Custom thread input");
                dialog.setHeaderText("Enter number of threads");
                dialog.setContentText("Example: if your computer has 4 cores,\nuse 4 threads to use all the cores");

                //At least try to set the background color
//                DialogPane pane = dialog.getDialogPane();
//                pane.setStyle("-fx-background-color: green;");

                Optional<String> userInput = dialog.showAndWait();
                if(userInput.isPresent()) {

                    //Take the user input as an integer
                    int newThreadCount = Integer.MIN_VALUE;
                    try {
                        newThreadCount = (int)Float.parseFloat(userInput.get());
                    }
                    //Not a number: show error dialog
                    catch(NumberFormatException e) {
                        threadSelector.getSelectionModel().selectFirst();
                        threadSelector.getSelectionModel().selectFirst();
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Error");
                        alert.setHeaderText("Invalid number of threads");
                        alert.setContentText("Number of threads must be an integer");
                        alert.showAndWait();
                        return;
                    }

                    //Invalid number of threads: show dialog
                    if(newThreadCount<1 || newThreadCount>StepperFields.MAX_THREADS) {
                        threadSelector.getSelectionModel().selectFirst();
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Error");
                        alert.setHeaderText("Invalid number of threads");
                        alert.setContentText("Number of threads must be an integer\n" +
                                "between 1 and " + StepperFields.MAX_THREADS);
                        alert.showAndWait();
                        return;
                    }

                    //User wants 1 thread: set value to the default
                    if(newThreadCount == 1) {
                        threadSelector.getSelectionModel().selectFirst();
                        return;
                    }

                    ObservableList<String> threadSelectorContents = threadSelector.getItems();
                    //Look through the list for matching values
                    for(int i=2; i<threadSelectorContents.size(); i++) {
                        int currentThreadCount = Integer.parseInt( threadSelectorContents.get(i).substring(9) );

                        //User input equals current value: set combo box to inputted value
                        if(currentThreadCount == newThreadCount) {
                            threadSelector.setValue("Threads: " + newThreadCount);
                            return;
                        }
                        //User input doesn't equal current value: add inputted value to combobox
                        else if(currentThreadCount > newThreadCount) {
                            threadSelector.getItems().add(i, "Threads: " + newThreadCount);
                            threadSelector.setValue("Threads: " + newThreadCount);
                            return;
                        }
                    }
                    //No matches: add the value to the end
                    threadSelector.getItems().add(threadSelectorContents.size(), "Threads: " + newThreadCount);
                    threadSelector.setValue("Threads: " + newThreadCount);
                }
                else {
                    threadSelector.getSelectionModel().selectFirst();
                }

                //END OF USER INPUT PROCESSING
            }

            //END OF USER INPUT DIALOG DEFINITION
        });

    }



    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //METHODS


    /**
     * Displays the login screen
     */
    @FXML
    private void setLoginScreen() {
        textInput.setText("");
        keyInput.setText("");
        screenManager.showScreen("login");
    }


    /**
     * Loads the shared Service with the user's selected settings and runs the Service.<br>
     * Transitions to the loading screen.
     */
    @FXML
    private void startProcess() {

        screenManager.showScreen("loading");

        //Set mode options
        boolean encrypting = !modeSelector.getValue().equals(MODE_OPTIONS[2]);

        //Set punctuation mode
        byte punctMode = 0;
        if (punctSelector.getValue().equals(INPUT_SELECTION_OPTIONS[2])) {
            punctMode = 2;
        } else if (punctSelector.getValue().equals(INPUT_SELECTION_OPTIONS[1])) {
            punctMode = 1;
        }

        //Set file loading mode
        boolean loadingFromFile = inputSelector.getValue().equals(INPUT_SELECTION_OPTIONS[2]);

        //Set thread count
        int threadCount = 0;
        if (threadSelector.getValue().equals(THREAD_OPTIONS[0])) {
            threadCount = 1;
        } else if (threadSelector.getValue().equals(THREAD_OPTIONS[1])) {
            throw new IllegalArgumentException("Invalid choice");
        } else {
            //Remove the "Threads: " part of the string
            String substr = threadSelector.getValue();
            substr = substr.substring(9);
            threadCount = Integer.parseInt(substr);
        }


        fields.startService(textInput.getText().strip(), keyInput.getText().strip(), encrypting, v2Selector.isSelected(),
                punctMode, loadingFromFile, threadCount);

    }
}
