package stepperfx.integration;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.*;


/**
 * Centralized manager for screen transitions.<br><br>
 *
 * The {@code IntegratedController} class uses a {@code ScreenManager} to change the displayed screen.<br><br>
 *
 * A {@code ScreenManager} can load new screens until the {@code finishLoading} method is called.<br>
 * Afterward, the manager can change screens, and can no longer load new screens.<br><br>
 *
 * One default and one alternate style may be linked to each screen name.<br>
 * The default style is loaded from an FXML file, or is loaded by the {@code addAlternateStylesheet} method if no default exists.
 * An alternate style may be added through {@code addAlternateStylesheet}.<br>
 * Calling {@code useAlternateStyles} displays any alternate styles, if they exist.
 */
final public class ScreenManager {

    /**
     * Stores controllers for each of the Manager's screens
     */
    private final HashMap<ScreenName, IntegratedController> controllerMap;

    /**
     * Holds the application's shared state
     */
    private final StepperFields fields;

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
     * Stores root nodes of the ScreenManager's FXML files.
     * Keys, assigned during the {@code addScreen} method, are the screen's names
     */
    private final HashMap<ScreenName, Parent> rootMap;

    /**
     * True if the manager is currently displaying alternate screens, false otherwise
     */
    private boolean usingAlternateStyles;


    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //CONSTRUCTOR

    /**
     * Creates a new ScreenManager with a stage and an instance eof shared fields.
     * @param primaryStage stage to load. Can't be null
     * @param fields shared fields to load. Can't be null
     */
    public ScreenManager(Stage primaryStage, StepperFields fields) {
        if(primaryStage == null) throw new AssertionError("Primary stage cannot be null");
        if(fields == null) throw new AssertionError("Shared fields cannot be null");

        this.primaryStage = primaryStage;
        this.fields = fields;

        usingAlternateStyles = false;

        //Initialize the map and path set
        controllerMap = new HashMap<>(6);
        loadedFxmlPaths = new HashSet<>(10);
        rootMap = new HashMap<>();
    }


    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //GETTERS AND SETTERS


    /**
     * Returns whether the manager is displaying its alternate screens
     * @return true if alternate screens are shown
     */
    public boolean usingAlternateStyles() {
        return usingAlternateStyles;
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
     * @param name associated name of the FXML file. Cannot be null. Must not match any other screen names tracked by this ScreenManager
     * @param fxmlPath path to FXML file, relative to the project's resources root. Cannot be null.
     *                 Cannot match any previously loaded filepath. Associated controller must be an IntegratedController.
     * @param fields shared fields to load into the controller of the scene. Cannot be null
     * @throws IllegalStateException if a load is attempted after {@code finishLoading} is called
     * @throws IOException if the given FXML file path is invalid
     */
    public void addScreen(ScreenName name, String fxmlPath, StepperFields fields) throws IOException {
        if(name==null) {
            throw new AssertionError("Name cannot be null");
        }

        if(rootMap.containsKey(name)) {
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
        rootMap.put(name, sceneGraphRoot);
        //Add to the loaded set
        loadedFxmlPaths.add(fxmlPath);

        //Assign this ScreenManager object to manage the FXML's controller and pass shared fields into the controller
        //IMPORTANT: This step must be done after updating the screen map. `addEventFilter` depends on the screen map being updated.
        Object controller = loader.getController();
        if(controller instanceof IntegratedController) {
            ((IntegratedController) controller).name = name;
            ((IntegratedController) controller).initializeController(this, fields);

            //add to controller list
            controllerMap.put(name, (IntegratedController)controller);
        }
        else {
            throw new AssertionError("FXML controller must be a subclass of IntegratedController");
        }

        //Swap roots
        if (primaryStage.getScene() == null) {
            primaryStage.setScene(new Scene(sceneGraphRoot));
        }
    }



    /**
     * Adds an alternate style defined at {@code stylesheetPath} to the screen {@code screenName}.<br><br>
     *
     * The default stylesheet (as opposed to the alternate stylesheet) is the one defined in the screen's FXML document.<br>
     * Alternate styles are shown and hidden with {@code useAlternateStyles}.<br><br>
     *
     * If there is no default stylesheet defined for {@code screenName} (i.e. defined in the screen's FXML),
     * the stylesheet at {@code stylesheetPath} becomes the default stylesheet.<br>
     * If a default and alternate stylesheet are already defined, the existing alternate stylesheet is replaced.<br><br>
     *
     * Paths to CSS stylesheets are relative to the directory marked as the `resources` root.<br>
     * To load a stylesheet at src/main/resources/views/alternate.css, the accepted load path is /views/alternate.css. This
     * assumes that the resources root is `resources`.<br>
     * Important: Load paths must begin with a slash.<br>
     *
     * @param screenName name of the screen to add the alternate CSS file to. Corresponding root must contain at most 2 stylesheets.
     * @param stylesheetPath path to the CSS file, relative to the `resources` root. Cannot be null. Must end in '.css'
     * @throws IOException if the file at {@code stylesheetPath} does not exist
     * @throws IllegalStateException if the manager is done loading, i.e. {@code finishLoading} was called
     */
    public void addAlternateStylesheet(ScreenName screenName, String stylesheetPath) throws IOException {
        if(stylesheetPath==null) throw new AssertionError("CSS path cannot be null");

        //Check state
        if(loadedFxmlPaths == null) throw new IllegalStateException("Cannot load when the manager is finished loading");

        //Get the currently used stylesheets for the screen
        ObservableList<String> stylesheets = rootMap.get(screenName).getStylesheets();
        //NOTE: The stylesheet shown to the user is in the last index of `stylesheets`


        //Get the path to the new stylesheet
        URL resource = getClass().getResource(stylesheetPath);
        if(resource==null) {
            throw new IOException("The path at \"" + stylesheetPath + "\" does not lead to a file");
        }


        //Zero or one stylesheets loaded: add new stylesheet as the alternate
        if(stylesheets.size()==1 || stylesheets.isEmpty()) {
            stylesheets.addFirst(resource.toExternalForm());
        }
        //Two stylesheets: replace the existing alternate stylesheet
        else if(stylesheets.size()==2) {
            stylesheets.set((usingAlternateStyles ? 1 : 0), resource.toExternalForm());
        }
        //This should never happen
        else {
            throw new AssertionError("Number of loaded stylesheets must be at most 2- instead found "
                    + stylesheets.size() + " stylesheets");
        }
    }


    /**
     * Adds a key event filter with the handler {@code eventHandler} to the screen whose name is {@code screenName}.<br><br>
     *
     * This method may be safely called during the {@code initializeController} method of an {@code IntegratedController}.
     *
     * @param screenName name of the screen to use. Cannot be null. Must be a screen name managed by this ScreenManager
     * @param eventHandler event handler to place on the desired screen. Cannot be null
     */
    public void addKeyEventFilter(ScreenName screenName, javafx.event.EventHandler<? super KeyEvent> eventHandler) {
        if(screenName==null) throw new AssertionError("Screen name cannot be null");
        if(eventHandler==null) throw new AssertionError("Event handler cannot be null");
        if(!rootMap.containsKey(screenName)) {
            throw new AssertionError("The screen name \"" + screenName + "\" is not managed by this ScreenManager");
        }

        Parent sceneGraphRoot = rootMap.get(screenName);
        sceneGraphRoot.addEventFilter(KeyEvent.KEY_PRESSED, eventHandler);
    }



    /**
     * Prevents the ScreenManager from loading or altering screens.<br>
     * Enables screen changing.<br><br>
     *
     * The effects of this method cannot be reversed.
     */
    public void finishLoading() {
        loadedFxmlPaths = null;
    }



    /**
     * If {@code usingAlternateStyles} is true, the manager displays the alternate style of each screen.<br>
     * Otherwise, the manager displays default styles.<br>
     * The update appears instantly.<br><br>
     *
     * For each screen with only one style loaded, the method does not affect those screens.<br>
     *
     * @param usingAlternateStyles whether to display the manager's alternate styles
     * @throws IllegalStateException if the manager is not finished loading, i.e. {@code finishLoading} was not called
     */
    public void setAlternateStyles(boolean usingAlternateStyles) {
        if(loadedFxmlPaths != null) throw new IllegalStateException("Cannot set alternate styles when the manager is not done loading. Call {managerName}.finishLoading() first");

        if(this.usingAlternateStyles != usingAlternateStyles) {
            for(ScreenName name : rootMap.keySet()) {
                //Reverse every stylesheet list for each screen (activating the alternate stylesheet)
                ObservableList<String> stylesheets = rootMap.get(name).getStylesheets();
                FXCollections.reverse(stylesheets);
            }

            this.usingAlternateStyles = !this.usingAlternateStyles;
        }
    }



    /**
     * Sets the ScreenManager's stage with a screen whose name is {@code name}.<br><br>
     *
     * This method has a {@code fields.getSponsoredContentProbability()} probability of showing a sponsored content dialog.<br>
     *
     * @param name name of the screen. Must be the name of a screen managed by this ScreenManager
     * @throws IllegalStateException if the manager is not finished loading screens
     */
    public void showScreen(ScreenName name) {
        showScreen(name, true);
    }


    /**
     * Sets the ScreenManager's stage with a screen whose name is {@code name}.<br><br>
     *
     * If {@code showingSponsoredContent} is true, this method has a {@code fields.getSponsoredContentProbability()}
     * probability of showing a sponsored content dialog.<br>
     * If false, a sponsored dialog is never shown,
     *
     * @param name name of the screen. Must be the name of a screen managed by this ScreenManager
     * @param showSponsoredContent whether to have a chance of showing sponsored content
     * @throws IllegalStateException if the manager is not finished loading screens
     */
    public void showScreen(ScreenName name, boolean showSponsoredContent) {
        if(name == null) {
            throw new AssertionError("Screen name cannot be null");
        }

        //Check that the manager actually contains the screen
        if(!rootMap.containsKey(name)) {
            throw new AssertionError("The screen \"" + name + "\" is not tracked by the manager");
        }

        //Check if the manager is loadable, i.e. the set of loaded FXML paths is not null
        if(loadedFxmlPaths != null) {
            throw new IllegalStateException("Cannot show new screens before the manager finishes loading. Call {managerName}.finishLoading() to allow screen changing");
        }


        //Replace the root of the existing Scene
        if (primaryStage.getScene() == null) {
            primaryStage.setScene(new Scene(rootMap.get(name)));
        }
        else {
            primaryStage.getScene().setRoot(rootMap.get(name));
        }

        //Prepare the screen for view
        IntegratedController controller = controllerMap.get(name);
        controller.prepareScreen();

        primaryStage.sizeToScene(); //Adjust window size to fit new content
        primaryStage.show();

        //Show the sponsored content, if desired
        if(showSponsoredContent && (float)Math.random() < fields.getSponsoredContentProbability()) {
            StyledDialogs.showSponsoredDialog();
        }
    }

}