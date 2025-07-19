package stepperfx.integration;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.*;


/**
 * Centralized manager for screen transitions.<br><br>
 *
 * A {@code ScreenManager} can load new screens until the {@code finishLoading} method is called.<br>
 * Afterward, the manager can change screens, and can no longer load new screens.<br><br>
 *
 * The {@code IntegratedController} class uses a {@code ScreenManager} to change the displayed screen.
 */
final public class ScreenManager {

    /**
     * Reference to the main application window where the screens will be displayed
     */
    private final Stage primaryStage;

    /**
     * Set containing previously loaded FXML paths.<br><br>
     *
     * Used to check if a FXML file has been previously loaded.<br>
     * Becomes null when the ScreenManager is finished loading.
     */
    private HashSet<String> loadedFxmlPaths;

    /**
     * Stores root nodes of the ScreenController's FXML files.
     * Keys, assigned during the {@code addScreen} method, are the screen's names
     */
    private final HashMap<String, Parent> screenMap;


    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //CONSTRUCTOR

    /**
     * Initializes the ScreenManager with a stage.
     * @param primaryStage stage to load
     */
    public ScreenManager(Stage primaryStage) {
        this.primaryStage = primaryStage;

        //Initialize the map and path set
        loadedFxmlPaths = new HashSet<>(10);
        screenMap = new HashMap<>();
    }


    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //METHODS


    /**
     * Adds a FXML file (controlled by an {@code IntegratedController}) dictating a scene at path {@code fxmlPath},
     * giving it the name {@code name} and loading it with the shared fields {@code fields}.<br><br>
     *
     * Paths to FXMLs are relative to the directory marked as the `resources` root.<br>
     * To load a FXML at src/main/resources/views/view.fxml, the accepted load path is /views/view.fxml. This
     * assumes that the resources root is `resources`.<br>
     * Important: Load paths must begin with a slash.<br><br>
     *
     * Adding a screen after calling {@code finishLoading}, causes an IllegalStateException.<br>
     * Passing an invalid FXML path causes an IOException.
     *
     * @param name associated name of the FXML file. Cannot be null. Must not match any other screen names tracked by this ScreenManager
     * @param fxmlPath path to FXML file, relative to the project's resources root. Cannot be null.
     *                 Cannot match any previously loaded filepath. Associated controller must be an IntegratedController.
     * @param fields shared fields to load into the controller of the scene. Cannot be null
     * @throws IllegalStateException if a load is attempted after the manager has finished loading screens
     * @throws IOException if the given FXML file path is invalid
     */
    public void addScreen(String name, String fxmlPath, StepperFields fields) throws IOException {
        if(name==null) {
            throw new AssertionError("Name cannot be null");
        }
        if(screenMap.containsKey(name)) {
            throw new AssertionError("The screen name \"" + name + "\" cannot match any other screen names");
        }
        if(fxmlPath==null) {
            throw new AssertionError("FXML path cannot be null");
        }
        if(fields==null) {
            throw new AssertionError("Fields cannot be null");
        }

        //Check if the manager is done
        if(loadedFxmlPaths == null) {
            throw new IllegalStateException("Cannot load new screens after the manager finishes loading");
        }

        //Check if the filepath has been loaded before
        if(loadedFxmlPaths.contains(fxmlPath)) {
            throw new AssertionError("The file at \"" + fxmlPath + "\" has already been loaded by the manager");
        }


        URL resource = getClass().getResource(fxmlPath);
        //Check if no null
        if(resource == null) {
            throw new IOException("The path \"" + fxmlPath + "\" does not lead to a FXML file");
        }

        //Load the FXML file (specifically the file's graph's root) from the path
        FXMLLoader loader = new FXMLLoader(resource);

        Parent sceneGraphRoot = loader.load(); //This line throws an IOException

        //Put the root into the map
        screenMap.put(name, sceneGraphRoot);

        //Assign this ScreenManager object to manage the FXML's controller and pass shared fields into the controller
        Object controller = loader.getController();
        if(controller instanceof IntegratedController) {
            ((IntegratedController) controller).initializeController(this, sceneGraphRoot, fields);
        }
        else {
            throw new AssertionError("FXML controller must be a subclass of IntegratedController");
        }

        // If you want to use the same Scene and just swap roots
        if (primaryStage.getScene() == null) {
            primaryStage.setScene(new Scene(sceneGraphRoot));
        }

        //Add to the loaded set
        loadedFxmlPaths.add(fxmlPath);
    }



    /**
     * Prevents the ScreenManager from loading more screens. Enables screen changing.
     */
    public void finishLoading() {
        loadedFxmlPaths = null;
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
     * Sets the ScreenManager's stage with a screen whose name is {@code name}.
     * @param name name of the screen. Cannot be null. Must be the name of a screen managed by this ScreenManager
     * @throws IllegalStateException if the manager is not finished loading screens
     */
    public void showScreen(String name) {
        if(name == null) {
            throw new AssertionError("Screen name cannot be null");
        }

        //Check that the manager actually contains the screen
       if(!screenMap.containsKey(name)) {
           throw new AssertionError("The screen \"" + name + "\" is not tracked by the manager");
       }

       //Check if the manager is loadable, i.e. the set of loaded FXML paths is not null
        if(loadedFxmlPaths != null) {
            throw new IllegalStateException("Cannot show new screens before the manager finishes loading. Call {managerName}.finishLoading() to allow screen changing");
        }

        //Replace the root of the existing Scene
        if (primaryStage.getScene() == null) {
            primaryStage.setScene(new Scene(screenMap.get(name)));
        }
        else {
            primaryStage.getScene().setRoot(screenMap.get(name));
        }

        primaryStage.sizeToScene(); //Adjust window size to fit new content
        primaryStage.show();
    }

}
