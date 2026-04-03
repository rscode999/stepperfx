package com.rscode.stepperfx.integration;


/**
 * Base class for GUI controllers.
 * Allows for management by a ScreenManager.<br><br>
 *
 * The ScreenManager is stored in a field called {@code screenManager}.<br>
 */
public class IntegratedController {

    /**
     * The name of the screen that the Controller controls. Assigned upon addition to a ScreenManager.<br>
     * Used to access the Controller's screen when using a ScreenManager.
     */
    protected ScreenName name;

    /**
     * The ScreenManager that controls screen transitions for the Controller.
     * All Controllers in an app share a single ScreenManager.
     */
    protected ScreenManager screenManager;


    // //////////////////////////////////////////////////////////////////////////////////////////
    //  /////////////////////////////////////////////////////////////////////////////////////////
    //CONSTRUCTOR

    /**
     * Creates a new IntegratedController. Its ScreenManager and StepperFields will remain uninitialized.<br><br>
     *
     * Initialization occurs in the {@code initializeController} method inside a ScreenManager.
     */
    public IntegratedController() {
        //Must have an explicitly defined constructor to conform to the project rules
    }


    // //////////////////////////////////////////////////////////////////////////////////////////
    //  /////////////////////////////////////////////////////////////////////////////////////////
    //HELPERS

    /**
     * Throws an AssertionError if any inputs break {@code initializeController}'s preconditions.<br><br>
     * Call manually when a subclass overrides {@code initializeController}.
     * @param manager ScreenManager to check
     */
    protected final void assertInitializeController(ScreenManager manager) {
        if(manager == null) {
            throw new AssertionError("Manager cannot be null");
        }
    }



    // //////////////////////////////////////////////////////////////////////////////////////////
    //  /////////////////////////////////////////////////////////////////////////////////////////
    //UTILITIES

    /**
     * Returns a String representation of this controller, including its name
     * @return controller as a String
     */
    @Override
    public String toString() {
        return super.toString() + " (" + name + ")";
    }


    // //////////////////////////////////////////////////////////////////////////////////////////
    //  /////////////////////////////////////////////////////////////////////////////////////////
    //METHODS

    /**
     * Loads the controller with {@code manager}.<br>
     * Serves as the controller's de facto constructor.<br><br>
     *
     * A ScreenManager uses this method to initialize newly added controllers.<br><br>
     *
     * This method may be overridden where the controller attaches listeners to {@code screenManager},
     * or otherwise must guarantee that they are not null.<br>
     * If overridden, the method should initialize the controller's
     * {@code screenManager}.<br>
     *
     * @param manager the ScreenManager responsible for the controller. Cannot be null
     */
    protected void initializeController(ScreenManager manager) {
        assertInitializeController(manager);
        this.screenManager = manager;
    }



    /**
     * Performs any necessary work to prepare the controlled screen for view.<br>
     * If not overridden, this method does nothing.<br><br>
     *
     * This method is called when a ScreenManager transitions to the controller's screen.
     */
    protected void prepareScreenTransition() {
        //Base class's method does nothing. Subclasses may override this method.
    }
}
