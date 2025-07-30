package stepperfx.integration;

import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;

import java.io.File;
import java.net.URL;
import java.util.Optional;

/**
 * Class containing static methods to show styled dialogs.<br>
 * Dialog styles are loaded from the CSS file at {@code StyledDialogs.STYLESHEET_FILEPATH}, from the "dialog" style class.
 */
public final class StyledDialogs {

    /**
     * Path to a directory containing images for sponsored content
     */
    final public static String SPONSORED_CONTENT_DIRECTORY = "src/main/resources/images";

    /**
     * Filepath to load dialog stylesheets from. Must point to a valid CSS file.<br><br>
     * A style class called "dialog" governs dialog styles.
     */
    final public static String STYLESHEET_FILEPATH = "/views/main-styles.css";


    // //////////////////////////////////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////////////////////////////////
    //HELPERS

    /**
     * Returns true if {@code testFile} does not exist or has an unsupported image file extension.<br>
     * Supported file extensions are jpg, jpeg, png, bmp, or gif.
     *
     * @param testFile file to test
     * @return true if the file is invalid, false otherwise
     */
    private static boolean fileInvalid(File testFile) {
        //Check if the file exists
        if(!testFile.exists()) {
            return true;
        }

        String testFilename = testFile.getName().toLowerCase();

        //Check all valid file extensions
        return !(testFilename.endsWith("jpg") ||
                testFilename.endsWith("jpeg") ||
                testFilename.endsWith("png") ||
                testFilename.endsWith("bmp") ||
                testFilename.endsWith("gif"));
    }


    /**
     * Loads {@code dialog} with the stylesheet at {@code StyledDialogs.STYLESHEET_FILEPATH}.<br>
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
     * Shows a modal alert dialog.
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
     * Shows a modal information dialog.
     * @param title title of the dialog window. Cannot be null
     * @param header large text explaining the main message of the dialog. Cannot be null
     * @param text small text below the header providing more information. Cannot be null
     */
    public static void showInfoDialog(String title, String header, String text) {
        if(title==null) throw new AssertionError("Title cannot be null");
        if(header==null) throw new AssertionError("Header cannot be null");
        if(text==null) throw new AssertionError("Text cannot be null");

        //create, load and show the dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        loadDialog(alert, title, header, text);
        alert.showAndWait();
    }



    /**
     * Shows a modal dialog. The dialog contains one random sponsored image
     * from the directory {@code StyledDialogs.SPONSORED_CONTENT_DIRECTORY}.<br><br>
     *
     * If the {@code StyledDialogs.SPONSORED_CONTENT_DIRECTORY} directory does not exist, throws an AssertionError.<br>
     * If there are no images in {@code StyledDialogs.SPONSORED_CONTENT_DIRECTORY}, the method prints a warning to System.err
     * and returns.<br>
     * If the randomly selected image is not of a supported type, the method throws an AssertionError.
     */
    public static void showSponsoredDialog() {

        File dir = new File(SPONSORED_CONTENT_DIRECTORY);
        Image image = null;

        // Check if the directory exists and is a directory
        if(!dir.exists()) {
            throw new AssertionError("The directory \"" + SPONSORED_CONTENT_DIRECTORY + "\" does not exist");
        }
        if(!dir.isDirectory()) {
            throw new AssertionError("The directory \"" + SPONSORED_CONTENT_DIRECTORY + "\" is not a directory");
        }

        // Get all the files in the directory
        File[] files = dir.listFiles();
        assert files != null;
        if(files.length == 0) {
            System.err.println("Alert: \"" + SPONSORED_CONTENT_DIRECTORY + "\" is empty");
            return;
        }

        //Pick a random index from the directory
        int randIndex = (int) (Math.random() * (files.length));
        File selectedImage = files[randIndex];

        //Check if the file is valid
        if(fileInvalid(selectedImage)) {
            throw new AssertionError("The file \"" + selectedImage.getName() + "\" is of an unsupported image type");
        }

        // Create an Image object and add it to the list
        image = new Image(selectedImage.toURI().toString());


        //Put the image in a viewable format
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(300);  // Set a maximum width for the image
        imageView.setPreserveRatio(true);  // Preserve the aspect ratio of the image

        //Create the dialog and set it to modal
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Sponsored Content");
        dialog.initModality(Modality.APPLICATION_MODAL);

        //Put an exit button so the dialog can be closed, then hide the button
        ButtonType exitType = new ButtonType("exit", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().clear();
        dialog.getDialogPane().getButtonTypes().addAll(exitType);
        dialog.getDialogPane().lookupButton(exitType).setVisible(false); //hide exit button

        //load image, show dialog
        dialog.getDialogPane().setContent(imageView);
        dialog.showAndWait();
    }



    /**
     * Shows a standalone modal dialog with a text field, a Cancel button, and an OK button.<br>
     * Returns the value received from the dialog, as an Optional.
     * @param title title of the dialog window. Cannot be null
     * @param header large text explaining the main message of the dialog. Cannot be null
     * @param text small text below the header providing more information. Cannot be null
     * @return user's input from the dialog
     */
    public static Optional<String> showTextInputDialog(String title, String header, String text) {
        if(title==null) throw new AssertionError("Title cannot be null");
        if(header==null) throw new AssertionError("Header cannot be null");
        if(text==null) throw new AssertionError("Text cannot be null");

        TextInputDialog dialog = new TextInputDialog();

        dialog.setGraphic(null); //Remove '?' picture
        loadDialog(dialog, title, header, text);

        return dialog.showAndWait();
    }
}