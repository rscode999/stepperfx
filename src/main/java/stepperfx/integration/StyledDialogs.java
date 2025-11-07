package stepperfx.integration;

import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Optional;

/**
 * Class containing static methods to show styled dialogs.<br>
 * Dialog styles are loaded from the CSS file at {@code StyledDialogs.STYLESHEET_FILEPATH}, from the "dialog" style class.
 */
public final class StyledDialogs {

    /**
     * Path to the preferred directory containing images for sponsored content.<br><br>
     *
     * When run in the IntelliJ IDE, this path is relative to the project root directory.<br>
     * When run as a standalone executable, the path is relative to the app.
     */
    private static String SPONSORED_CONTENT_DIRECTORY = "src/main/resources/images";

    /**
     * Path to a backup directory containing images for sponsored content.<br><br>
     *
     * When run in the IntelliJ IDE, this path is relative to the project root directory.<br>
     * When run as a standalone executable, the path is relative to the app.
     */
    final private static String SPONSORED_CONTENT_DIRECTORY_ALTERNATE = "images";

    /**
     * Filepath to load dialog stylesheets from, relative to the directory marked as the "resources" root.
     * Must point to a valid CSS file containing a style class called "dialog".<br><br>
     * A style class called "dialog" governs dialog styles.
     */
    final public static String STYLESHEET_FILEPATH = "/views/main-styles.css";

    // //////////////////////////////////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////////////////////////////////
    //HELPERS

    /**
     * Returns a randomly selected image from the directory {@code directoryPath}.<br><br>
     *
     * This method has 2 illegal states: If {@code directoryPath} does not exist,
     * or there are no files in {@code directoryPath}.<br><br>
     *
     * In the case of an illegal state, the method throws a FileNotFoundException.<br><br>
     *
     * When run in the IntelliJ IDE, the sponsored content directories are relative to the project root directory.<br>
     * When run as a standalone executable, they're relative to the app.
     *
     * @param directoryPath directory to choose a file from. Must contain at least 1 image,
     *                      where all files inside {@code directoryPath} make {@code imageValid} return true
     * @return randomly selected image from {@code directoryPath}
     * @throws FileNotFoundException if {@code directoryPath} doesn't exist or is empty
     */
    private static Image fetchRandomImage(String directoryPath) throws FileNotFoundException {
        File dir = new File(directoryPath);

        if(!(dir.exists() || dir.isDirectory())) {
            throw new FileNotFoundException("The folder \"" + directoryPath + "\" (path relative to the app) does not exist");
        }

        // Get all the files in the directoryPath
        File[] files = dir.listFiles();
        if(files == null) {
            throw new AssertionError("I/O error when accessing \"" + directoryPath + "\"");
        }
        if(files.length == 0) {
            throw new FileNotFoundException("The folder \"" + directoryPath + "\" (path relative to the app) must contain an image");
        }

        //Pick a random index from the directoryPath
        int randIndex = (int) (Math.random() * (files.length));
        File selectedImage = files[randIndex];

        //Check if the file is valid
        if(!imageValid(selectedImage)) {
            throw new AssertionError("The file \"" + selectedImage.getName() + "\" inside the folder \"" + directoryPath +
                    "\" must be a JPG, PNG, BMP, or GIF image");
        }

        // Create an Image object and add it to the list
        return new Image(selectedImage.toURI().toString());
    }



    /**
     * Returns true if {@code testFile} exists and has a supported image file extension.<br>
     * Supported file extensions are jpg, jpeg, png, bmp, or gif.
     *
     * @param testFile file to test
     * @return true if the file is valid, false otherwise
     */
    private static boolean imageValid(File testFile) {
        //Check if the file exists
        if(!testFile.exists()) {
            return false;
        }

        String testFilename = testFile.getName().toLowerCase();

        //Check all valid file extensions
        return (testFilename.endsWith(".jpg") ||
                testFilename.endsWith(".jpeg") ||
                testFilename.endsWith(".png") ||
                testFilename.endsWith(".bmp") ||
                testFilename.endsWith(".gif"));
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

        if(styles==null) throw new AssertionError("The file at \"" + STYLESHEET_FILEPATH + "\" " +
                "is not a CSS stylesheet containing the 'dialog' style class");

        dialogPane.getStylesheets().add(styles.toExternalForm());
        dialogPane.getStyleClass().add("dialog");

        //configure the dialog
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(text);
    }


    // //////////////////////////////////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////////////////////////////////
    //GETTERS

    /**
     * Returns the path to the sponsored content directory.<br><br>
     *
     * When run in the IntelliJ IDE, this path is relative to the project root directory.<br>
     * When run as a standalone executable, the path is relative to the app.<br>
     *
     * @return path to sponsored content
     */
    public static String getSponsoredContentDirectory() {
        return SPONSORED_CONTENT_DIRECTORY;
    }


    /**
     * Returns the path to the alternate sponsored content directory.<br><br>
     *
     * When run in the IntelliJ IDE, this path is relative to the project root directory.<br>
     * When run as a standalone executable, the path is relative to the app.<br><br>
     *
     * This directory is used in case the sponsored content directory doesn't exist or is invalid.
     *
     * @return path to alternate sponsored content
     */
    public static String getSponsoredContentDirectoryAlternate() {
        return SPONSORED_CONTENT_DIRECTORY_ALTERNATE;
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
     * from the directory {@code StyledDialogs.getSponsoredContentDirectory()}.<br><br>
     *
     * If the image load from {@code StyledDialogs.getSponsoredContentDirectory()} fails, the method shows an error dialog.
     * The method then attempts a second load from {@code StyledDialogs.getSponsoredContentDirectoryAlternate()}.
     * If the second load fails, another dialog is shown, then {@code System.exit(0)} is called.<br><br>
     *
     * When run in the IntelliJ IDE, the sponsored content directories are relative to the project root directory.<br>
     * When run as a standalone executable, they're relative to the app.
     */
    public static void showSponsoredDialog() {

        Image image = null;

        //Attempt to load from the primary content directory
        try {
            image = fetchRandomImage(SPONSORED_CONTENT_DIRECTORY);
        }
        //Load fails: show dialog, then attempt alternate directory load
        catch (FileNotFoundException e) {
            showAlertDialog("Configuration error", "Folder not found", e.getMessage());

            //Attempt second load. If successful, make the alternate directory into the primary
            try {
                image = fetchRandomImage(SPONSORED_CONTENT_DIRECTORY_ALTERNATE);
                SPONSORED_CONTENT_DIRECTORY = SPONSORED_CONTENT_DIRECTORY_ALTERNATE;
            }
            //Second load fails: show dialog, then exit the app
            catch (FileNotFoundException e2) {
                showAlertDialog("Configuration error", "Folder not found", e2.getMessage());
                System.exit(0);
            }
        }

        //Put the loaded image in a viewable format
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