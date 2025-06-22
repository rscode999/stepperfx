package stepperfx.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import stepperfx.administration.IntegratedController;

final public class LoginRejectController extends IntegratedController {

    /**
     * Calls {@code System.exit(0)} to close the app.
     */
    @FXML
    private void closeApp() {
        System.exit(0);
    }
}
