package stepperfx.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import stepperfx.StepperFields;
import stepperfx.screen_management.IntegratedController;
import stepperfx.screen_management.ScreenManager;

import java.util.*;

/**
 * Controller for the results screen. Responsible for handling the output and errors of the shared Service.
 */
final public class ResultsController extends IntegratedController {

    /**
     * Holds the index of the current page in {@code resultPages}. Uses 0-based indexing, so the first page is at index 0.
     */
    private int currentResultPage;

    /**
     * Holds the result in pages. Each index of this list is one page. The first page is at index 0.
     */
    private ArrayList<String> resultPages;


    /**
     * Allows the user to copy the entire text in {@code resultPages} to the system clipboard
     */
    @FXML
    private Button copyButton;

    /**
     * Allows the user to view the previous page of the output
     */
    @FXML
    private Button pageBackwardButton;

    /**
     * Allows the user to view the next page of the output
     */
    @FXML
    private Button pageForwardButton;

    /**
     * Shows the current page and total pages to the user
     */
    @FXML
    private Label pageDisplayText;

    /**
     * Displays the results of the app's processing
     */
    @FXML
    private TextArea resultArea;

    /**
     * Displays the key used to process the result
     */
    @FXML
    private TextArea keyArea;


    /**
     * Sets the app's fields.<br>
     * Attaches a value listener to the app's shared Service to load the Service's output.<br>
     * Attaches an event filter to the scene graph root to check for key presses.
     *
     * @param manager ScreenManager for screen changes
     * @param sceneGraphRoot root of the scene graph managed by the controller
     * @param fields shared fields
     */
    @Override
    public void initializeController(ScreenManager manager, Parent sceneGraphRoot, StepperFields fields) {
        assertInitializeController(manager, sceneGraphRoot, fields);
        this.fields = fields;
        this.sceneGraphRoot = sceneGraphRoot;
        this.screenManager = manager;

        //Configure key shortcuts
        sceneGraphRoot.addEventFilter(KeyEvent.KEY_PRESSED, event -> {

            //Move between pages with arrow keys
            if (KeyCode.RIGHT.equals(event.getCode()) || KeyCode.UP.equals(event.getCode())) {
                setNextPage();
            }
            else if(KeyCode.LEFT.equals(event.getCode()) || KeyCode.DOWN.equals(event.getCode())) {
                setPreviousPage();
            }
        });

        //Attach value listener to the service to handle its output
        fields.addServiceValueListener((obs, oldValue, newValue) -> {

            //runs the code on the main FX app thread, not a worker thread
            Platform.runLater(() -> {

                //Null new values occur when the Service is reset, so any null results should be ignored
                if(newValue == null) {
                    return;
                }

                //Check valid output
                if(newValue.length != 4) {
                    throw new AssertionError("Service output's length must be 4. Actual length is " + newValue.length);
                }

                //Error: display the dialog (dialog creation works on any screen)
                if(newValue[0]==null && newValue[1]==null && newValue[2]!=null && newValue[3]!=null) {

                    screenManager.showScreen("input");
                    fields.resetService();

                    //File loading exception: load with error message
                    if(newValue[2].equals("class java.io.FileNotFoundException")) {
                        Dialogs.showAlertDialog("File load error", "File load error", newValue[3]);
                    }
                    //Any other exception: load with exception's message
                    else {
                        Dialogs.showAlertDialog("Thread unhandled exception", newValue[2], newValue[3]);
                    }
                }

                //No error: configure and display results screen
                else if(newValue[0]!=null && newValue[1]!=null && newValue[2]==null && newValue[3]==null) {
                    resultPages = new ArrayList<>();

                    //Divide the result into pages
                    int startIndex = 0;
                    int endIndex = StepperFields.RESULT_PAGE_LENGTH;
                    while(endIndex < newValue[0].length()) {
                        resultPages.add(newValue[0].substring(startIndex, endIndex));
                        startIndex += StepperFields.RESULT_PAGE_LENGTH;
                        endIndex += StepperFields.RESULT_PAGE_LENGTH;
                    }
                    resultPages.add(newValue[0].substring(startIndex));
                    resultPages.trimToSize();

                    //configure UI variables
                    currentResultPage = 0;
                    pageDisplayText.setText("Page " + (currentResultPage+1) + " of " + resultPages.size() + ", displaying " +
                            ((StepperFields.RESULT_PAGE_LENGTH==100000) ? "100K" : StepperFields.RESULT_PAGE_LENGTH) +
                            " characters per page");
                    pageBackwardButton.setDisable(true);
                    pageForwardButton.setDisable(resultPages.size()<=1);

                    //load the result
                    resultArea.setText(resultPages.getFirst());
                    keyArea.setText(newValue[1]);

                    screenManager.showScreen("results");
                }

                //Something weird
                else {
                    throw new AssertionError("Illegal output configuration- output is " + Arrays.toString(newValue));
                }
            });
        });

        assertInitializeController(manager, sceneGraphRoot, fields);
    }



    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Copies the output to the system clipboard
     */
    @FXML
    private void copyResult() {
        copyButton.setDisable(true);

        //Get the clipboard and its contents
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();

        //Turn the clipboard contents into a single string
        StringBuilder combinedResult = new StringBuilder();
        for(String str : resultPages) {
            combinedResult.append(str);
        }

        //Export the result
        content.putString(combinedResult.toString());
        clipboard.setContent(content);

        copyButton.setDisable(false);
        pageDisplayText.setText("Result copied to clipboard");
    }


    /**
     * Sets the screen to the login screen. Resets the app's Service.
     */
    @FXML
    private void setLoginScreen() {
        screenManager.showScreen("login");
        fields.resetService();
        resultArea.setText("");
        keyArea.setText("");
    }



    /**
     * Reconfigures the UI to display the next page of result text.<br>
     * If the current result page equals the result page list's size (i.e. the last page is displayed), does nothing.
     */
    @FXML
    private void setNextPage() {
        if(currentResultPage+1 == resultPages.size()) {
            return;
        }

        currentResultPage++;
        pageForwardButton.setDisable(currentResultPage+1 == resultPages.size());
        pageBackwardButton.setDisable(currentResultPage == 0);

        pageDisplayText.setText("Page " + (currentResultPage+1) + " of " + resultPages.size());
        resultArea.setText(resultPages.get(currentResultPage));
    }


    /**
     * Reconfigures the UI to display the previous page of result text.<br>
     * If the current result page equals 0 (i.e. the first page is displayed), does nothing.
     */
    @FXML
    private void setPreviousPage() {
        if(currentResultPage == 0) {
            return;
        }

        currentResultPage--;
        pageForwardButton.setDisable(currentResultPage+1 == resultPages.size());
        pageBackwardButton.setDisable(currentResultPage == 0);

        pageDisplayText.setText("Page " + (currentResultPage+1) + " of " + resultPages.size());
        resultArea.setText(resultPages.get(currentResultPage));
    }

}
