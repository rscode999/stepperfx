package stepperfx;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import stepperfx.threading.ProcessService;

/**
 * Contains fields shared between the application's controllers.
 * One of the fields is a javafx.concurrent.Service used to do operations.
 */
public class StepperFields {

    /**
     * Number of blocks in the key
     */
    final public static int BLOCK_COUNT = 6;

    /**
     * Number of characters in each key block
     */
    final public static int BLOCK_LENGTH = 25;

    /**
     * Filename for the input file if none is given
     */
    final public static String DEFAULT_INPUT_FILENAME = "input.txt";

    /**
     * The maximum amount of threads that the app can use
     */
    final public static int MAX_THREADS = 9999;

    /**
     * Holds the result of the ProcessService's work. May be null.<br><br>
     *
     * A change from null to non-null indicates that processing finished.<br>
     * A change from non-null to null means the results finished displaying.
     */
    private String result;

    /**
     * Key for processing the input. May be null.
     */
    private String key;

    /**
     * A Service that controllers can start, cancel, and reset. Can never be null.
     */
    private ProcessService service;



    // ///////////////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////////////
    //CONSTRUCTOR

    /**
     * Creates a new instance of StepperFields. All fields inside
     * are guaranteed to be initialized upon completion of this method.<br><br>
     *
     * A new instance should be created only once.
     */
    public StepperFields() {
        service = new ProcessService();

        //Set the service to update the result each time a value property is changed
        service.valueProperty().addListener(new ChangeListener<String>() {
            public void changed(ObservableValue<? extends String> obs, String oldValue, String newValue) {
                result = newValue;
                System.out.println("VALUE PROPERTY CHANGE as tracked by StepperFields instance");
            }
        });
    }

    // ///////////////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////////////
    //GETTERS

    /**
     * Returns the processing result stored in the fields
     * @return result text (may be null)
     */
    public String result() {
        return result;
    }

    /**
     * Returns the key used to process the input
     * @return key text (may be null)
     */
    public String key() {
        return key;
    }


    // ///////////////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////////////
    //SERVICE METHODS

    /**
     * Assigns {@code listener} as a message property listener on the app's Service.
     * @param listener listener to assign
     */
    public void addServiceMessageListener(ChangeListener<? super String> listener) {
        service.messageProperty().addListener(listener);
    }

    /**
     * Assigns {@code listener} as a progress property listener on the app's Service.
     * @param listener listener to assign
     */
    public void addServiceProgressListener(ChangeListener<? super Number> listener) {
        service.progressProperty().addListener(listener);
    }


    /**
     * Assigns {@code listener} as a value property listener on the app's Service.
     * @param listener listener to assign
     */
    public void addServiceValueListener(ChangeListener<? super String> listener) {
        service.valueProperty().addListener(listener);
    }


    /**
     * Sets the app's Service to its READY state, preparing it to be run again.
     * The result and key are set to {@code null} to indicate a restart.
     */
    public void resetService() {
        result = null;
        key = null;
        service.reset();
    }


    /**
     * Starts the app's Service, loading it with the given data.
     * If the Service is not in the READY state, throws an IllegalStateException.
     *
     * @param input what the Service should process, or a filepath to the input. Cannot be null
     * @param key key for processing the input. Cannot be null
     * @param encrypting true if the service will encrypt, false if the service will decrypt
     * @param usingV2Process true if using enhanced (v2) processes, false otherwise
     * @param punctMode 0 if the task removes all punctuation from the input, 1 if the task removes spaces from the input,
     *                  2 if the task processes the input with all punctuation
     * @param loadingFromFile true if loading input from a separate file, false otherwise
     * @param nThreads number of threads to use during processing. Must be on the interval [0, MAX_THREADS]
     * @throws IllegalStateException if the service is not ready to be run
     */
    public void startService(String input, String key, boolean encrypting, boolean usingV2Process, byte punctMode, boolean loadingFromFile, int nThreads) {
        if(input == null) throw new AssertionError("Input cannot be null");
        if(key==null) throw new AssertionError("Key cannot be null");
        if(nThreads<0 || nThreads>MAX_THREADS) throw new AssertionError("Number of threads (" + nThreads + ")" +
                " must be on the interval [0, " + MAX_THREADS + "]");

        if(service.getState() == Worker.State.READY) {
            service.initializeService(input, key, encrypting, usingV2Process, punctMode, loadingFromFile, nThreads);
            service.start();
        }
        else {
            throw new IllegalStateException("Cannot start the shared Service when not in the READY state. " +
                    "Reset the Service before starting it.");
        }
    }


    /**
     * Stops the Service's execution and puts it in the READY state.
     * The result is also reset to null to indicate a restart.
     */
    public void stopService() {
        service.cancel();
        result = null;
        service.reset();
    }

}
