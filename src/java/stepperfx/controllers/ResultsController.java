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

                    Alert alert = new Alert(Alert.AlertType.WARNING);

                    //File loading exception: load with error message
                    if(newValue[2].equals("class java.io.FileNotFoundException")) {
                        alert.setTitle("File load error");
                        alert.setHeaderText("File load error");
                        alert.setContentText(newValue[3]); //Note: The error message should be trimmed
                    }
                    //Any other exception: load with exception's message
                    else {
                        alert.setTitle("Thread unhandled exception");
                        alert.setHeaderText(newValue[2]);
                        alert.setContentText(newValue[3]);
                    }
                    alert.showAndWait();
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
                    pageDisplayText.setText("Page " + (currentResultPage+1) + " of " + resultPages.size());
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
     * Reconfigures the UI to display the next page of result text
     */
    @FXML
    private void setNextPage() {
        currentResultPage++;
        pageForwardButton.setDisable(currentResultPage+1 == resultPages.size());
        pageBackwardButton.setDisable(currentResultPage == 0);

        pageDisplayText.setText("Page " + (currentResultPage+1) + " of " + resultPages.size());
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

        pageDisplayText.setText("Page " + (currentResultPage+1) + " of " + resultPages.size());
        resultArea.setText(resultPages.get(currentResultPage));
    }

}
