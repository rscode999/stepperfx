package stepperfx.administration;


import javafx.scene.Parent;
import stepperfx.StepperFields;

/**
 * Base class for GUI controllers.
 * Allows for management by a ScreenManager, access to the controlled Scene's root (as a Parent),
 * and access to a StepperFields instance shared between controllers.<br><br>
 *
 * The ScreenManager is stored in a field called {@code screenManager}.<br>
 * The root is in {@code sceneGraphRoot}.<br>
 * The StepperFields is in {@code fields}.
 */
public class IntegratedController {

    /**
     * Root (Parent) of the scene graph that the controller manages.
     */
    protected Parent sceneGraphRoot;

    /**
     * The ScreenManager that controls screen transitions for the Controller.
     * All Controllers share a single ScreenManager.
     */
    protected ScreenManager screenManager;

    /**
     * Contains a reference to fields that are shared between other controllers.
     * The shared fields belong to all controllers.
     */
    protected StepperFields fields;


    //CONSTRUCTOR

    /**
     * Creates a new IntegratedController. Its ScreenManager, root, and StepperFields will remain uninitialized.<br><br>
     *
     * Initialization occurs in the {@code initializeController} method inside the ScreenManager class definition.
     */
    public IntegratedController() {
        //Must have an explicitly defined constructor to conform to the project rules
    }



    // //////////////////////////////////////////////////////////////////////////////////////////
    //  /////////////////////////////////////////////////////////////////////////////////////////
    //HELPERS

    /**
     * Throws an AssertionError if any inputs break {@code initializeController}'s preconditions.<br><br>
     * Use when a subclass overrides {@code initializeController}.
     * @param manager ScreenManager to check
     * @param sceneGraphRoot Parent to check
     * @param fields StepperFields to check
     */
    protected void assertInitializeController(ScreenManager manager, Parent sceneGraphRoot, StepperFields fields) {
        if(manager == null) {
            throw new AssertionError("Manager cannot be null");
        }
        if(sceneGraphRoot == null) {
            throw new AssertionError("Scene graph root cannot be null");
        }
        if(fields == null) {
            throw new AssertionError("Fields cannot be null");
        }
    }


    // //////////////////////////////////////////////////////////////////////////////////////////
    //  /////////////////////////////////////////////////////////////////////////////////////////
    //METHODS

    /**
     * Loads the controller with {@code manager}, {@code sceneGraphRoot}, and {@code fields}.<br><br>
     *
     * A ScreenManager uses this method to initialize newly added controllers.<br><br>
     *
     * This method may be overridden where the controller attaches listeners to {@code screenManager}, {@code sceneGraphRoot},
     * and {@code fields}, or otherwise must guarantee that they are not null.<br>
     * If overridden, the method should initialize the controller's
     * {@code screenManager}, {@code sceneGraphRoot}, and {@code fields}.<br>
     *
     * @param manager the ScreenManager responsible for the controller. Cannot be null
     * @param sceneGraphRoot root of the controller's scene. Cannot be null
     * @param fields reference to shared fields between controllers. Cannot be null
     */
    public void initializeController(ScreenManager manager, Parent sceneGraphRoot, StepperFields fields) {
        assertInitializeController(manager, sceneGraphRoot, fields);

        this.screenManager = manager;
        this.sceneGraphRoot = sceneGraphRoot;
        this.fields = fields;
    }
}
