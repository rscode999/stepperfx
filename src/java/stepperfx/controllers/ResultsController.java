package stepperfx.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import stepperfx.StepperFields;
import stepperfx.administration.IntegratedController;
import stepperfx.administration.ScreenManager;

import java.util.Arrays;
import java.util.Optional;

/**
 * Controller for the results screen. Responsible for processing the output of the shared Service.
 */
final public class ResultsController extends IntegratedController {

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
                    ButtonType type = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
                    //Setting the content of the dialog
                    dialog.setContentText(newValue[2]);
                    //Adding buttons to the dialog pane
                    dialog.getDialogPane().getButtonTypes().add(type);

                    dialog.showAndWait();
                }

                //No error: display results screen
                else if(newValue[0]!=null && newValue[1]!=null && newValue[2]==null) {
                    resultArea.setText(newValue[0]);
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


    /**
     * Sets the screen to the login screen. Resets the app's Service.
     */
    @FXML
    private void setLoginScreen() {
        screenManager.showScreen("login");
        fields.resetService();
    }
}
