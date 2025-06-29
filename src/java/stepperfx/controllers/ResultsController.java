package stepperfx.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import stepperfx.StepperFields;
import stepperfx.administration.IntegratedController;
import stepperfx.administration.ScreenManager;

import java.util.*;

/**
 * Controller for the results screen. Responsible for handling the output and errors of the shared Service.
 */
final public class ResultsController extends IntegratedController {

    /**
     * Holds the result in divisions
     */
    private ArrayList<String> resultPages;

    /**
     * Holds the index of the current page. Uses 0-based indexing.
     */
    private int currentResultPage;


    /**
     * Allows the user to copy the entire text to the system clipboard
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
    private Label pageDisplay;

    /**
     * Shows the amount of characters displayed per page
     */
    @FXML
    private Label pageLengthDisplay;

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
     * Sets the app's fields. Also attaches a value listener to the app's shared Service to load the Service's output.
     * @param manager ScreenManager for screen changes
     * @param fields shared fields
     */
    @Override
    public void initializeController(ScreenManager manager, StepperFields fields) {
        this.fields = fields;
        this.screenManager = manager;

        //Attach value listener to the service to handle its output
        fields.addServiceValueListener((obs, oldValue, newValue) -> {

            //runs the code on the main FX app thread, not a worker thread
            Platform.runLater(() -> {

                //This occurs when the Service is reset
                if(newValue == null) {
                    return;
                }

                if(newValue.length != 3) {
                    throw new AssertionError("Service output's length must be 3. Actual length is " + newValue.length);
                }

                //Error: display the dialog (dialog creation works on any screen)
                if(newValue[0]==null && newValue[1]==null && newValue[2]!=null) {

                    screenManager.showScreen("input");
                    fields.resetService();

                    Dialog<String> dialog = new Dialog<>();
                    //Setting the title
                    dialog.setTitle("Error");
                    ButtonType type = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
                    //Setting the content of the dialog
                    dialog.setContentText(newValue[2]);
                    //Adding buttons to the dialog pane
                    dialog.getDialogPane().getButtonTypes().add(type);

                    dialog.showAndWait();
                }

                //No error: configure and display results screen
                else if(newValue[0]!=null && newValue[1]!=null && newValue[2]==null) {
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
                    pageDisplay.setText("Page " + (currentResultPage+1) + " of " + resultPages.size());
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
        pageDisplay.setText("Result copied to clipboard");
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
     * Reconfigures the UI to display the next page of result text
     */
    @FXML
    private void setNextPage() {
        currentResultPage++;
        pageForwardButton.setDisable(currentResultPage+1 == resultPages.size());
        pageBackwardButton.setDisable(currentResultPage == 0);

        pageDisplay.setText("Page " + (currentResultPage+1) + " of " + resultPages.size());
        resultArea.setText(resultPages.get(currentResultPage));
    }


    /**
     * Reconfigures the UI to display the previous page of result text
     */
    @FXML
    private void setPreviousPage() {
        currentResultPage--;
        pageForwardButton.setDisable(currentResultPage+1 == resultPages.size());
        pageBackwardButton.setDisable(currentResultPage == 0);

        pageDisplay.setText("Page " + (currentResultPage+1) + " of " + resultPages.size());
        resultArea.setText(resultPages.get(currentResultPage));
    }

}
