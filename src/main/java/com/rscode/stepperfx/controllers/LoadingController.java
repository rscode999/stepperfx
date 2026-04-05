package com.rscode.stepperfx.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import com.rscode.stepperfx.integration.ScreenName;
import com.rscode.stepperfx.integration.StepperFields;
import com.rscode.stepperfx.integration.IntegratedController;
import com.rscode.stepperfx.integration.ScreenControl;
import com.rscode.stepperfx.threading.ProcessTask;

/**
 * Controller for the loading screen. Responsible for stopping the Service and tracking its progress.
 */
final public class LoadingController extends IntegratedController {

    /**
     * Names for each of the possible loading states that the task can be in.
     * Progression through the states starts at index 0, then index 1, and so on.<br><br>
     *
     * Used by a {@code ProcessTask} to update the loading screen text.<br><br>
     *
     * Index 3 ("Writing to file...") is currently not used.
     */
    public final static String[] LOADING_STATE_NAMES =
            new String[] {"Loading input...", "Formatting...", "Executing...", "Writing to file...", "Finalizing..."};


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
     */
    @Override
    public void initializeController() {

        StepperFields.addServiceMessageListener((obs, oldValue, newValue) -> {
            loadStatus.setText(newValue);

            //Disable the cancel button when a cancel operation is impossible
            cancelButton.setDisable(newValue.equals(LOADING_STATE_NAMES[4]));
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
        ScreenControl.showScreen(ScreenName.INPUT);
    }
}