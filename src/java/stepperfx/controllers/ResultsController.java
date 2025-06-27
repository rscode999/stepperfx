package stepperfx.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import stepperfx.StepperFields;
import stepperfx.administration.IntegratedController;
import stepperfx.administration.ScreenManager;

import java.util.Optional;

/**
 * Controller for the results screen
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
     * Sets the app's fields. Also attaches a value listener to the app's shared Service.
     * @param fields shared fields
     */
    @Override
    public void initializeController(ScreenManager manager, StepperFields fields) {
        this.fields = fields;
        this.screenManager = manager;

        //Attach value listener to the service. If results are not null, sets the output areas
        fields.addServiceValueListener((obs, oldValue, newValue) -> {

            //runs the code on the main FX app thread, not a worker thread
            Platform.runLater(() -> {

                //Null check is important to ensure this is executed only if the result is set
                if(fields.result() != null) {

                    if(newValue.length > 1) {
                        screenManager.showScreen("results");
                        resultArea.setText(fields.result());
                        keyArea.setText(fields.key());
                    }
                    else {
                        screenManager.showScreen("input");
                        fields.resetService();

                        Dialog<String> dialog = new Dialog<>();
                        //Setting the title
                        dialog.setTitle("Error");
                        ButtonType type = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
                        //Setting the content of the dialog
                        dialog.setContentText(newValue[0]);
                        //Adding buttons to the dialog pane
                        dialog.getDialogPane().getButtonTypes().add(type);

                        dialog.showAndWait();
                    }
                }
                else {
                    System.out.println("Results Controller: result is not null, finished");
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
