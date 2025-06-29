package stepperfx.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import stepperfx.StepperFields;
import stepperfx.administration.IntegratedController;
import stepperfx.administration.ScreenManager;
import stepperfx.threading.ProcessTask;

/**
 * Controller for the loading screen. Responsible for stopping the Service and tracking its progress.
 */
final public class LoadingController extends IntegratedController {

    /**
     * Cancels the ongoing process when clicked
     */
    @FXML
    private Button cancelButton;

    /**
     * Displays general information about the ongoing processes
     */
    @FXML
    private Label loadStatus;

    /**
     * Sets and configures the fields of this controller
     * @param manager ScreenManager controlling screen transitions
     * @param fields shared fields
     */
    @Override
    public void initializeController(ScreenManager manager, StepperFields fields) {
        this.screenManager = manager;
        this.fields = fields;

        fields.addServiceMessageListener((obs, oldValue, newValue) -> {
//            System.out.println("Listener triggered: " + oldValue + "->" + newValue);
            loadStatus.setText(newValue);

            //Disable the cancel button when a cancel operation is impossible
            cancelButton.setDisable(newValue.equals(ProcessTask.LOADING_STATE_NAMES[3]));
        });
    }


    /**
     * Stops the Service's execution, putting it in its READY state.
     * Sets the screen to the input screen.
     */
    @FXML
    public void stopService() {
        screenManager.showScreen("input");
        fields.stopService();
    }
}
