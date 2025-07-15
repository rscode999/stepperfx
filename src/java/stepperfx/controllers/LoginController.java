package stepperfx.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import stepperfx.screen_management.IntegratedController;


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
        final String[] validPasswords = {"a", "asdf", "password", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "", " "};
        int index = -1;
        String enteredPW = pwField.getText();

        for(int i=0; i<validPasswords.length; i++) {
            if(validPasswords[i].equals(enteredPW)) {
                index = i;
                break;
            }
        }
        if(index == validPasswords.length-1) {
            index = 0;
        }
        else if(index != -1) {
            index = 1;
        }

        if(index == -1) {
            screenManager.showScreen("login-reject");
        }
        else {
            screenManager.showScreen("input");
            fields.setLoginCredentials(index);
            pwField.setText("");
        }
    }
}