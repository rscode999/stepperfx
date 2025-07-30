package stepperfx.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import stepperfx.integration.ScreenName;
import stepperfx.integration.StepperFields;
import stepperfx.integration.IntegratedController;
import stepperfx.integration.StyledDialogs;

import java.util.Optional;

/**
 * Controller for the settings screen (accessed through the input screen).
 * Responsible for taking and updating the user's preferences.
 */
public final class SettingsController extends IntegratedController {

    /**
     * Shows a prompt to enter a product key
     */
    @FXML
    private Button productKeyButton;

    /**
     * Allows the user to select high-contrast styles
     */
    @FXML
    private CheckBox highContrastStyleSelector;

    /**
     *  Label for the block count input
     */
    @FXML
    private Label blockCountInputText;

    /**
     *  Label for the block length input
     */
    @FXML
    private Label blockLengthInputText;

    /**
     * Displays text if the user's settings update was successful
     */
    @FXML
    private Label statusText;

    /**
     * Text input for block count
     */
    @FXML
    private TextField blockCountInput;

    /**
     * Text input for block length
     */
    @FXML
    private TextField blockLengthInput;


    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //METHODS


    /**
     * Applies the user's settings. If a setting is invalid, displays a dialog with an error message.
     */
    @FXML
    private void applySettings() {

        int newBlockCount = fields.getBlockCount();
        int newBlockLength = fields.getBlockLength();

        //Get new block count
        if(!blockCountInput.getText().isEmpty()) {
            try {
                newBlockCount = Integer.parseInt(blockCountInput.getText());
            }
            catch (NumberFormatException e) {
                StyledDialogs.showAlertDialog("Invalid input", "Invalid input", "New block count must be an integer");
                return;
            }

            if(newBlockCount<=0 || newBlockCount>StepperFields.MAX_BLOCK_COUNT) {
                StyledDialogs.showAlertDialog("Invalid input", "Invalid input",
                        "New block count must be an integer between 1 and " + StepperFields.MAX_BLOCK_COUNT);
                return;
            }
        }

        //Get new block length
        if(!blockLengthInput.getText().isEmpty()) {
            try {
                newBlockLength = Integer.parseInt(blockLengthInput.getText());
            }
            catch (NumberFormatException e) {
                StyledDialogs.showAlertDialog("Invalid input", "Invalid input", "New block length must be an integer");
                return;
            }

            if(newBlockLength<=0 || newBlockLength>StepperFields.MAX_BLOCK_LENGTH) {
                StyledDialogs.showAlertDialog("Invalid input", "Invalid input",
                        "New block length must be an integer between 1 and " + StepperFields.MAX_BLOCK_LENGTH);
                return;
            }
        }

        //Update the "changes applied" label, if changes were made
        if(!blockCountInput.getText().isEmpty()
                || !blockLengthInput.getText().isEmpty()
                || highContrastStyleSelector.isSelected() != screenManager.usingAlternateStyles()) {
            statusText.setText("Changes applied");
        }
        else {
            statusText.setText(" ");
        }

        //Update the settings
        fields.setBlockCount(newBlockCount);
        fields.setBlockLength(newBlockLength);
        screenManager.setAlternateStyles(highContrastStyleSelector.isSelected());

        //Update the labels
        blockCountInputText.setText("Number of blocks (current: " + fields.getBlockCount() + ")");
        blockLengthInputText.setText("Block length (current: " + fields.getBlockLength() + ")");

        blockCountInput.setText("");
        blockLengthInput.setText("");
    }



    /**
     * Removes the "Changes applied" status text
     */
    @FXML
    private void clearStatusText() {
        statusText.setText(" ");
    }



    /**
     * Prepares the settings screen for view. Updates the high-contrast selector, block count, and block length displayed.
     */
    @Override
    protected void prepareScreen() {
        highContrastStyleSelector.setSelected(screenManager.usingAlternateStyles());
        blockCountInputText.setText("Number of blocks (current: " + fields.getBlockCount() + ")");
        blockLengthInputText.setText("Block length (current: " + fields.getBlockLength() + ")");
    }



    /**
     * Changes to the input screen. Clears all data from the settings screen.
     */
    @FXML
    private void showInputScreen() {
        screenManager.showScreen(ScreenName.INPUT);
        statusText.setText(" ");
        blockCountInput.setText("");
        blockLengthInput.setText("");
    }



    /**
     * Shows a dialog to get the product key from the user.
     * If the product key is valid, decreases the probability of seeing sponsored content.
     */
    @FXML
    private void takeProductKey() {
        //Get user input with a dialog
        Optional<String> rawKey = StyledDialogs.showTextInputDialog("Enter product key", "Enter product key",
                "All letters in the product key must be uppercase");

        //Get the user input. If not given, return
        if(rawKey.isEmpty()) {
            return;
        }
        String key = rawKey.get();

        if(key.equals("PI-EQUALS-3")) {
            fields.setSponsoredContentProbability(0.2);
            StyledDialogs.showAlertDialog("Key accepted", "Key accepted", "Enjoy seeing less sponsored content!");
            productKeyButton.setDisable(true);
        }
        else {
            StyledDialogs.showInfoDialog("Error", "Key not accepted", "The given product key is invalid");
        }
    }
}