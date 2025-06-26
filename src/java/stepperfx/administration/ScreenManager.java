package stepperfx.administration;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import stepperfx.StepperFields;

/**
 * Centralized manager for screen transitions.
 * All Controller classes will be managed by an instance of this class.
 */
final public class ScreenManager {

    /**
     * Reference to the main application window where the screens will be displayed
     */
    private final Stage primaryStage;

    /**
     * Stores root nodes of the ScreenController's FXML files.
     * Keys, assigned during the {@code addScreen} method, are the screen's names
     */
    private final Map<String, Parent> screenMap;



    /**
     * Initializes the ScreenManager with a stage.
     * @param primaryStage stage to load
     */
    public ScreenManager(Stage primaryStage) {
        this.primaryStage = primaryStage;

        //Initialize the map
        screenMap = new HashMap<>();
    }


    /**
     * Adds a FXML file dictating a scene at path {@code fxmlPath},
     * giving it the name {@code name} and loading it with the shared fields {@code fields}.<br><br>
     *
     * Paths to FXMLs are relative to the directory marked as the `resources` root.<br>
     * To load a FXML at src/main/resources/views/view.fxml, the accepted load path is /views/view.fxml. This
     * assumes that the resources root is `resources`.<br>
     * Important: Load paths must begin with a slash.<br><br>
     *
     * Passing an invalid FXML path causes an IOException. If the FXML's controller is not an instance
     * of {@code IntegratedController}, throws IllegalArgumentException.<br><br>
     *
     * All FXML names must be unique.
     *
     * @param name associated name of the FXML file. Must not match any other screen names tracked by this ScreenManager
     * @param fxmlPath path to FXML file, relative to the `resources` directory
     * @param fields shared fields to load into the controller of the scene
     * @throws IllegalArgumentException if the FXML's controller is not an instance of IntegratedController
     * @throws IOException if the FXML file path is invalid
     */
    public void addScreen(String name, String fxmlPath, StepperFields fields) throws IOException {
        if(screenMap.containsKey(name)) {
            throw new AssertionError("Duplicate screen names not allowed");
        }

        URL resource = getClass().getResource(fxmlPath);
        //Check if no null
        if(resource == null) {
            throw new IOException("Invalid FXML path");
        }

        //Load the FXML file (specifically the file's graph's root) from the path
        FXMLLoader loader = new FXMLLoader(resource);

        Parent root = loader.load(); //This line throws an IOException

        //Put the root into the map
        screenMap.put(name, root);

        //Assign this ScreenManager object to manage the FXML's controller and pass shared fields into the controller
        Object controller = loader.getController();
        if(controller instanceof IntegratedController) {
            ((IntegratedController) controller).initializeController(this, fields);
        }
        else {
            throw new IllegalArgumentException("FXML controller must be a subclass of IntegratedController");
        }

        // Optional: If you want to use the same Scene and just swap roots
        if (primaryStage.getScene() == null) {
            primaryStage.setScene(new Scene(root));
        }
    }


    /**
     * Returns an array containing all screen names tracked by the ScreenManager.<br><br>
     *
     * The elements in the array are deep copies of the ScreenManager's screen names,
     * so they may be changed without affecting the ScreenManager.
     *
     * @return deep copy array of screen names
     */
    public String[] getScreens() {
        Set<String> set = screenMap.keySet();
        String[] output = new String[set.size()];
        int i = 0;
        for(String s : set) {
            output[i] = s;
            i++;
        }
        return output;
    }


    /**
     * Sets the ScreenController's stage with a screen whose name is {@code name}.
     * @param name name of the screen. Must be the name of a screen managed by this ScreenManager
     */
    public void showScreen(String name) {

        //Check that the manager actually contains the screen
       if(!screenMap.containsKey(name)) {
           throw new AssertionError("The screen \"" + name + "\" is not tracked by this manager");
       }

        //Replace the root of the existing Scene
        if (primaryStage.getScene() == null) {
            primaryStage.setScene(new Scene(screenMap.get(name)));
        }
        else {
            primaryStage.getScene().setRoot(screenMap.get(name));
            // System.out.println(screenMap.get(name).hashCode());
        }

        primaryStage.sizeToScene(); //Adjust window size to fit new content
        primaryStage.show();
    }

}
