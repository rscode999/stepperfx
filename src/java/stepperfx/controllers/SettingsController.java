package stepperfx.controllers;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import stepperfx.integration.StepperFields;
import stepperfx.integration.IntegratedController;
import stepperfx.integration.ScreenManager;
import stepperfx.integration.StyledDialogs;

/**
 * Controller for the settings screen (accessed through the input screen).
 * Responsible for taking and updating the user's preferences.
 */
public final class SettingsController extends IntegratedController {

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
    //"CONSTRUCTOR"

    /**
     *
     * @param manager the ScreenManager responsible for the controller. Cannot be null
     * @param sceneGraphRoot root of the controller's scene. Cannot be null
     * @param fields reference to shared fields between controllers. Cannot be null
     */
    @Override
    public void initializeController(ScreenManager manager, Parent sceneGraphRoot, StepperFields fields) {
        assertInitializeController(manager, sceneGraphRoot, fields);
        this.screenManager = manager;
        this.sceneGraphRoot = sceneGraphRoot;
        this.fields = fields;

        blockCountInputText.setText("Number of blocks (current: " + fields.getBlockCount() + ")");
        blockLengthInputText.setText("Block length (current: " + fields.getBlockLength() + ")");

        assertInitializeController(manager, sceneGraphRoot, fields);
    }


    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //METHODS


    /**
     * Applies the user's settings. If a setting is invalid, displays a dialog with an error message.
     */
    @FXML
    private void applySettings() {
        //Take input and throw exception if something goes wrong
        int newBlockCount;
        int newBlockLength;
        try {
            newBlockCount = Integer.parseInt(blockCountInput.getText());
        }
        catch(NumberFormatException e) {
            StyledDialogs.showAlertDialog("Invalid input", "Invalid input", "New block count must be an integer");
            return;
        }
        try {
            newBlockLength = Integer.parseInt(blockLengthInput.getText());
        }
        catch(NumberFormatException e) {
            StyledDialogs.showAlertDialog("Invalid input", "Invalid input", "New block length must be an integer");
            return;
        }

        //Ensure the inputs are in valid range
        if(newBlockCount<=0 || newBlockCount>StepperFields.MAX_BLOCK_COUNT) {
            StyledDialogs.showAlertDialog("Invalid input", "Invalid input",
                    "New block count must be an integer between 1 and " + StepperFields.MAX_BLOCK_COUNT);
            return;
        }
        if(newBlockLength<=0 || newBlockLength>StepperFields.MAX_BLOCK_LENGTH) {
            StyledDialogs.showAlertDialog("Invalid input", "Invalid input",
                    "New block length must be an integer between 1 and " + StepperFields.MAX_BLOCK_LENGTH);
            return;
        }

        //Update the settings
        fields.setBlockCount(newBlockCount);
        fields.setBlockLength(newBlockLength);

        //Update the labels
        blockCountInputText.setText("Number of blocks (current: " + fields.getBlockCount() + ")");
        blockLengthInputText.setText("Block length (current: " + fields.getBlockLength() + ")");

        //Update the success label
        statusText.setText("Changes applied");
    }



    /**
     * Removes the status text ("Changes applied")
     */
    @FXML
    private void clearStatusText() {
        statusText.setText(" ");
    }



    /**
     * Changes to the input screen
     */
    @FXML
    private void setInputScreen() {
        screenManager.showScreen("input");
        statusText.setText(" ");
        blockCountInput.setText("");
        blockLengthInput.setText("");
    }
}
