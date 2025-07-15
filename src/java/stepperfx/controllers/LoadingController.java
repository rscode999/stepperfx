package stepperfx.controllers;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import stepperfx.StepperFields;
import stepperfx.screen_management.IntegratedController;
import stepperfx.screen_management.ScreenManager;
import stepperfx.threading.ProcessTask;

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
     * @param sceneGraphRoot root of the controller's scene graph
     * @param fields shared fields
     */
    @Override
    public void initializeController(ScreenManager manager, Parent sceneGraphRoot, StepperFields fields) {
        assertInitializeController(manager, sceneGraphRoot, fields);

        this.screenManager = manager;
        this.sceneGraphRoot = sceneGraphRoot;
        this.fields = fields;

        fields.addServiceMessageListener((obs, oldValue, newValue) -> {
//            System.out.println("Listener triggered: " + oldValue + "->" + newValue);
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
    public void stopService() {
        screenManager.showScreen("input");
        fields.stopService();
    }
}
