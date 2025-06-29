package stepperfx.administration;


import stepperfx.StepperFields;

/**
 * Base class for the formatting project's GUI controllers.
 * Allows for management by a ScreenManager and access to a StepperFields instance
 * shared between controllers.<br><br>
 *
 * The ScreenManager is stored in a field called {@code screenManager}.<br>
 * The StepperFields is in {@code fields}.
 */
public class IntegratedController {

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
     * Creates a new IntegratedController. Its ScreenManager and StepperFields will remain uninitialized.<br><br>
     *
     * Initialization occurs in the {@code initializeController} method inside the ScreenManager class definition.
     */
    public IntegratedController() {
        //Must have an explicitly defined constructor to conform to the project rules
    }


    // /////////////////////////////////////////////////////////////////////////////////////////
    //  /////////////////////////////////////////////////////////////////////////////////////////
    //METHODS

    /**
     * Loads the controller with {@code manager} and {@code fields}.<br><br>
     *
     * A ScreenManager uses this method to initialize newly added controllers.<br><br>
     *
     * This method may be overridden where the controller's {@code screenManager} and {@code fields}
     * must not be null. If overridden, the method should initialize the controller's {@code screenManager} and {@code fields}.
     *
     * @param manager the ScreenManager responsible for the controller. Cannot be null
     * @param fields reference to shared fields between controllers. Cannot be null
     */
    public void initializeController(ScreenManager manager, StepperFields fields) {
        if(manager==null) {
            throw new AssertionError("Manager cannot be null");
        }
        if(fields == null) {
            throw new AssertionError("Fields cannot be null");
        }

        this.screenManager = manager;
        this.fields = fields;
    }

}
