package com.rscode.stepperfx.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import com.rscode.stepperfx.integration.ScreenName;
import com.rscode.stepperfx.integration.StepperFields;
import com.rscode.stepperfx.integration.IntegratedController;
import com.rscode.stepperfx.integration.ScreenManager;
import com.rscode.stepperfx.threading.ProcessTask;

/**
 * Controller for the loading screen. Responsible for stopping the Service and tracking its progress.
 */
final public class LoadingController extends IntegratedController {

    /**
     * Cancels the ongoing process when clicked. Disabled when the Service sends its output to the app.
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
     *
     * @param manager ScreenManager for screen changes
     */
    @Override
    public void initializeController(ScreenManager manager) {
        assertInitializeController(manager);

        this.screenManager = manager;

        StepperFields.addServiceMessageListener((obs, oldValue, newValue) -> {
            loadStatus.setText(newValue);

            //Disable the cancel button when a cancel operation is impossible
            cancelButton.setDisable(newValue.equals(ProcessTask.LOADING_STATE_NAMES[4]));
        });
    }


    /**
     * Stops the Service's execution, putting it in its READY state.
     * Sets the screen to the input screen.
     */
    @FXML
    public void stopLoading() {
        //IMPORTANT: Stop the Service before changing the screen. If not, the worker threads will continue running while a sponsored content dialog is showing.
        //If background threads keep running, the screen may unexpectedly change to the results screen.
        StepperFields.stopService();
        screenManager.showScreen(ScreenName.INPUT);
    }
}