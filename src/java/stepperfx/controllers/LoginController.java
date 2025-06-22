package stepperfx.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import stepperfx.administration.IntegratedController;


/**
 * Handles GUI actions on the login screen
 */
final public class LoginController extends IntegratedController {

    /**
     * Takes the user's inputted password
     */
    @FXML
    private PasswordField pwField;


    // /////////////////////////////////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////////////////////
    //METHODS


    /**
     * Gets and evaluates the user's password from {@code pwField}.<br>
     * If the password is correct, changes the screen to the input screen.<br>
     * If wrong, a "password incorrect" text is displayed.
     */
    @FXML
    private void login() {
        if("password".equals(pwField.getText()) || "".equals(pwField.getText())) {
            pwField.setText("");
            screenManager.showScreen("input");
        }
        else {
            screenManager.showScreen("login-reject");
        }
    }
}