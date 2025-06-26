package stepperfx.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import stepperfx.StepperFields;
import stepperfx.administration.IntegratedController;
import stepperfx.administration.ScreenManager;

/**
 * Controller for the loading screen. Responsible for stopping the Service.
 */
final public class LoadingController extends IntegratedController {

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
            loadStatus.setText(newValue);
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
