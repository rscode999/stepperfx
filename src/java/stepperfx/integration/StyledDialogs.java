package stepperfx.integration;

import javafx.scene.control.Alert;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextInputDialog;

import java.net.URL;
import java.util.Optional;

/**
 * Class containing static methods to show styled dialogs.<br>
 * Dialog styles are loaded from the CSS file at {@code StyledDialogs.STYLESHEET_FILEPATH}, from the "dialog" style class.
 */
public final class StyledDialogs {

    /**
     * Filepath to load dialog stylesheets from. Must point to a valid CSS file.<br><br>
     * A style class called "dialog" governs dialog styles.
     */
    final public static String STYLESHEET_FILEPATH = "/views/main-styles.css";


    // //////////////////////////////////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////////////////////////////////
    //HELPERS

    /**
     * Configures and loads {@code dialog} with the stylesheet at {@code StyledDialogs.STYLESHEET_FILEPATH}.
     * Sets the dialog's title, header, and text.<br><br>
     *
     * Upon completion of this method, {@code dialog} will be mutated.
     *
     * @param dialog dialog to configure
     * @param title title of the dialog window
     * @param header large text explaining the main message of the dialog
     * @param text small text below the header providing more information
     */
    private static void loadDialog(Dialog<?> dialog, String title, String header, String text) {
        //Set the stylesheet
        DialogPane dialogPane = dialog.getDialogPane();
        URL styles = StyledDialogs.class.getResource(STYLESHEET_FILEPATH);
        if(styles==null) throw new AssertionError("The dialog stylesheet at \"" + STYLESHEET_FILEPATH + "\" does not exist");
        dialogPane.getStylesheets().add(styles.toExternalForm());
        dialogPane.getStyleClass().add("dialog");

        //configure the dialog
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(text);
    }


    // //////////////////////////////////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////////////////////////////////
    //METHODS

    /**
     * Shows a standalone modal alert dialog.
     * @param title title of the dialog window. Cannot be null
     * @param header large text explaining the main message of the dialog. Cannot be null
     * @param text small text below the header providing more information. Cannot be null
     */
    public static void showAlertDialog(String title, String header, String text) {
        if(title==null) throw new AssertionError("Title cannot be null");
        if(header==null) throw new AssertionError("Header cannot be null");
        if(text==null) throw new AssertionError("Text cannot be null");

        //create, load and show the dialog
        Alert alert = new Alert(Alert.AlertType.WARNING);
        loadDialog(alert, title, header, text);
        alert.showAndWait();
    }


    /**
     * Shows a standalone modal dialog with a text field, a Cancel button, and an OK button.<br>
     * Returns the value received from the dialog, or {@code null} if the value is cancelled.
     * @param title title of the dialog window. Cannot be null
     * @param header large text explaining the main message of the dialog. Cannot be null
     * @param text small text below the header providing more information. Cannot be null
     * @return user's input from the dialog
     */
    public static Optional<String> showTextDialog(String title, String header, String text) {
        if(title==null) throw new AssertionError("Title cannot be null");
        if(header==null) throw new AssertionError("Header cannot be null");
        if(text==null) throw new AssertionError("Text cannot be null");

        TextInputDialog dialog = new TextInputDialog();

        dialog.setGraphic(null); //Remove '?' picture
        loadDialog(dialog, title, header, text);

        return dialog.showAndWait();
    }
}
