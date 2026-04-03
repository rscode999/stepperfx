package com.rscode.stepperfx.integration;


/**
 * Base class for GUI controllers.
 * Allows for management by a ScreenControl.<br><br>
 */
public class IntegratedController {

    /**
     * The name of the screen that the Controller controls. Assigned upon addition to a ScreenControl.<br>
     * Used to access the Controller's screen when using a ScreenControl.
     */
    protected ScreenName name;


    // //////////////////////////////////////////////////////////////////////////////////////////
    //  /////////////////////////////////////////////////////////////////////////////////////////
    //CONSTRUCTOR

    /**
     * Creates a new IntegratedController. Its ScreenControl and StepperFields will remain uninitialized.<br><br>
     *
     * Initialization occurs in the {@code initializeController} method inside a ScreenControl.
     */
    public IntegratedController() {
        //Must have an explicitly defined constructor to conform to the project rules
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
     * Configures GUI elements, to guarantee that the elements are not null.
     * Overridden when GUI elements require more configuration than what is specified in their FXML files.
     */
    protected void initializeController() {
        //Base class's method does nothing. Subclasses may override this method.
    }



    /**
     * Performs any necessary work to prepare the controlled screen for view.<br>
     * If not overridden, this method does nothing.<br><br>
     *
     * This method is called when a ScreenControl transitions to the controller's screen.
     */
    protected void prepareScreenTransition() {
        //Base class's method does nothing. Subclasses may override this method.
    }
}
