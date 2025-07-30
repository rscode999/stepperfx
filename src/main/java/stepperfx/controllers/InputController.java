package stepperfx.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import stepperfx.integration.*;

import java.util.Optional;


/**
 * Controller for the user input screen. Responsible for updating the input screen and starting the shared Service.<br>
 * Not responsible for taking the output of the Service or handling the Service's cancellation.
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
    final private String[] THREAD_OPTIONS = {"Number of threads: 1", "Custom...", "Threads: 2", "Threads: 4", "Threads: 8",
            "Threads: 12", "Threads: 16", "Threads: 24", "Threads: 32"};


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
     * Default contents are specified by {@code THREAD_OPTIONS}.
     * Contents may receive updates from the user, as long as the contents respect the {@code THREAD_OPTIONS} invariants.
     */
    @FXML
    private ComboBox<String> threadSelector;

    /**
     * Label for the top text input that receives the plaintext or ciphertext.
     */
    @FXML
    private Label textInputLabel;

    /**
     * Text are where the user inputs the key
     */
    @FXML
    private TextArea keyInput;

    /**
     * Text area where the user inputs text. Larger than the key input
     */
    @FXML
    private TextArea textInput;



    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //"CONSTRUCTOR"

    /**
     * Initializes the controller with {@code manager}, {@code sceneGraphRoot}, and {@code fields}.<br>
     * Sets GUI elements. Assigns a listener to clear the text inputs upon successful operations.
     *
     * @param manager ScreenManager for screen changes
     * @param fields shared fields
     */
    @Override
    public void initializeController(ScreenManager manager, StepperFields fields) {
        assertInitializeController(manager, fields);

        //Check thread option formatting
        for(int v=0; v<THREAD_OPTIONS.length; v++) {
            if(v>1) {
                if(!THREAD_OPTIONS[v].startsWith("Threads: ")) {
                    throw new AssertionError("Thread option index " + v + " must start with \"Threads: \"");
                }

                String currentElement = THREAD_OPTIONS[v];
                try {
                    String substr = currentElement.substring(9);
                    int output = Integer.parseInt(substr);
                    if(output<1 || output>StepperFields.MAX_THREADS) {
                        throw new AssertionError("Number of threads specified by index " + v + " in the thread options array " +
                                "must be on the interval [1, " + StepperFields.MAX_THREADS + "] (instead received " + output + ")");
                    }
                }
                catch(NumberFormatException e) {
                    throw new AssertionError("Thread option index " + v + " (\"" + currentElement + "\") " +
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

        // ///////////////////////////////////////////////////////////////////////////

        //Set listener on the shared service to clear the inputs upon successful output
        fields.addServiceValueListener((obs, oldValue, newValue) -> {
            //Service output: {processed text, key, error message}
            if(newValue!=null && newValue[0]!=null && newValue[1]!=null && newValue[2]==null) {
                //This means: the service was not cancelled, and the service produced a valid output with a null error message
                textInput.setText("");
                keyInput.setText("");
            }
        });

        //Set key listener on this screen
        manager.addKeyEventFilter(name, event -> {
            if(event.getCode().equals(KeyCode.ALT)) {
                manager.setAlternateStyles(false);
            }
            else if(event.getCode().equals(KeyCode.ESCAPE)) {
                showLoginScreen();
            }
        });

        assertInitializeController(manager, fields);
    }



    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //METHODS


    /**
     * Changes the text input label depending on the user's input preferences.<br>
     * On change to file input, the input label should say "path to input file" or a similar message. Otherwise,
     * the label should display the appropriate operation input text.
     */
    @FXML
    private void onInputSelectorChange() {
        if(inputSelector.getValue().equals(INPUT_SELECTION_OPTIONS[2])) {
            textInputLabel.setText("Path to input text (*.txt) file");
        }
        else {
            textInputLabel.setText("Text");
        }
    }



    /**
     * Changes the start button and text input labels when the mode is changed.<br>
     * If the new mode is the last index in {@code MODE_OPTIONS}, sets for reverse process.
     * Otherwise, sets for forward process.
     */
    @FXML
    private void onModeSelectorChange() {
        //Change start button text
        if(modeSelector.getValue().equals(MODE_OPTIONS[2])) {
            punctSelector.setDisable(true);
            startButton.setText("Start Reverse Process");
        }
        else {
            punctSelector.setDisable(false);
            startButton.setText("Start Forward Process");
        }

        //Change label text, if file input is not selected
        if(!inputSelector.getValue().equals(INPUT_SELECTION_OPTIONS[2])) {
            textInputLabel.setText("Text");
        }
    }



    /**
     * Checks if the new value of the thread selector is index 1 ("Custom...").
     * If so, displays a dialog and allows the user to enter a new thread value.
     */
    @FXML
    private void onThreadSelectorChange() {

        if(threadSelector.getValue().equals("Custom...")) {
            final String ERROR_TITLE = "Invalid input";
            final String ERROR_HEADER = "Invalid number of threads";

            //Get result from a dialog
            Optional<String> userInput = StyledDialogs.showTextInputDialog("Custom thread input", "Enter number of threads",
                    "Example: if your computer has 4 cores,\nuse 4 threads to use all the cores");
            if(userInput.isPresent()) {

                //Take the user input as an integer
                int newThreadCount = Integer.MIN_VALUE;
                try {
                    newThreadCount = (int)Float.parseFloat(userInput.get());
                }
                //Not a number: show error dialog
                catch(NumberFormatException e) {
                    threadSelector.getSelectionModel().selectFirst();
                    StyledDialogs.showAlertDialog(ERROR_TITLE, ERROR_HEADER, "The input must be an integer");
                    return;
                }

                //Invalid number of threads: show dialog
                if(newThreadCount<1 || newThreadCount>StepperFields.MAX_THREADS) {
                    threadSelector.getSelectionModel().selectFirst();
                    StyledDialogs.showAlertDialog(ERROR_TITLE, ERROR_HEADER,
                            "Number of threads must be an integer\nbetween 1 and " + StepperFields.MAX_THREADS);
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
                    //User input doesn't equal current value: add inputted value to combo box
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
    }



    /**
     * Displays the login screen and resets the text inputs
     */
    @FXML
    private void showLoginScreen() {
        screenManager.showScreen(ScreenName.LOGIN);
        textInput.setText("");
        keyInput.setText("");
    }



    /**
     * Switches to the settings screen
     */
    @FXML
    private void showSettingsScreen() {
        screenManager.showScreen(ScreenName.SETTINGS);
    }



    /**
     * Loads the shared Service with the user's selected settings and runs the Service.<br>
     * Transitions to the loading screen.
     */
    @FXML
    private void startProcess() {

        screenManager.showScreen(ScreenName.LOADING, false);

        //Set mode options
        boolean encrypting = !modeSelector.getValue().equals(MODE_OPTIONS[2]);

        //Set punctuation mode
        int punctMode = 0;
        if (punctSelector.getValue().equals(PUNCT_OPTIONS[3])) {
            punctMode = 2;
        }
        else if (punctSelector.getValue().equals(PUNCT_OPTIONS[2])) {
            punctMode = 1;
        }

        //Set file loading mode
        boolean loadingFromFile = inputSelector.getValue().equals(INPUT_SELECTION_OPTIONS[2]);

        //Set thread count
        int threadCount = 0;
        if (threadSelector.getValue().equals(THREAD_OPTIONS[0])) {
            threadCount = 1;
        }
        else if (threadSelector.getValue().equals(THREAD_OPTIONS[1])) {
            throw new AssertionError("Invalid thread selector option- Cannot choose \"Custom...\"");
        }
        else {
            //Remove the "Threads: " part of the string
            String substr = threadSelector.getValue();
            substr = substr.substring(9);
            threadCount = Integer.parseInt(substr);
        }
        if(fields.getLoginCredentials() != 0) {
            threadCount = 0;
        }

        fields.startService(textInput.getText().strip(), keyInput.getText().strip(), encrypting, v2Selector.isSelected(),
                fields.getBlockCount(), fields.getBlockLength(),
                punctMode,
                loadingFromFile, threadCount);

    }
}