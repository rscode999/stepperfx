package stepperfx;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import stepperfx.threading.ProcessService;

import java.util.Arrays;

/**
 * Contains fields shared between the application's controllers.
 * One of the fields is a javafx.concurrent.Service used to do operations.
 */
final public class StepperFields {

    /**
     * Number of blocks in the key. Must be positive.
     */
    final public static int BLOCK_COUNT = 6;

    /**
     * Number of characters in each key block. Must be positive.<br><br>
     *
     * Highly recommended to be relatively prime with {@code BLOCK_COUNT}.
     */
    final public static int BLOCK_LENGTH = 25;

    /**
     * Filename for the input file if none is given. Cannot be null. Must end in ".txt"
     */
    final public static String DEFAULT_INPUT_FILENAME = "input.txt";

    /**
     * Filename for the output file if none is given. Cannot be null. Must end in ".txt"
     */
    final public static String DEFAULT_OUTPUT_FILENAME = "output.txt";

    /**
     * Amount to shift during v2 processes. Cannot be null. Length must equal BLOCK_COUNT
     */
    final private static byte[] KEY_BLOCK_INCREMENTS = {2,3,5,7,11,13};

    /**
     * The maximum amount of threads that the app can use. Must be at least 1
     */
    final public static int MAX_THREADS = 999;

    /**
     * Maximum number of characters that are displayed on a result page. Must be positive
     */
    final public static int RESULT_PAGE_LENGTH = 2;


    // ////////////////////////////////////////////////////////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////////////////////////////
    //VARIABLES

    /**
     * Holds the user's login credentials
     */
    private int loginCredentials;

    /**
     * A Service that controllers can start, cancel, and reset. Can never be null.
     */
    private final ProcessService service;



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
        assertConstantInvariants();

        loginCredentials = 1;
        service = new ProcessService();
    }


    /**
     * Throws an AssertionError if any constant's invariants are broken
     */
    private void assertConstantInvariants() {
        if(BLOCK_COUNT<=0) throw new AssertionError("Block count must be positive");
        if(BLOCK_LENGTH<=0) throw new AssertionError("Block length must be positive");
        if(DEFAULT_INPUT_FILENAME==null || DEFAULT_INPUT_FILENAME.length()<4 || !DEFAULT_INPUT_FILENAME.endsWith(".txt"))
            throw new AssertionError("Default input filename must end in \".txt\"");
        if(DEFAULT_OUTPUT_FILENAME==null || DEFAULT_OUTPUT_FILENAME.length()<4 || !DEFAULT_OUTPUT_FILENAME.endsWith(".txt"))
            throw new AssertionError("Default output filename must end in \".txt\"");
        if(KEY_BLOCK_INCREMENTS==null || KEY_BLOCK_INCREMENTS.length!=BLOCK_COUNT)
            throw new AssertionError("Key block increment length must equal BLOCK_COUNT");
        if(MAX_THREADS<1) throw new AssertionError("Max thread count must be positive");
        if(RESULT_PAGE_LENGTH<1) throw new AssertionError("Result page length must be positive");
    }



    // ///////////////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////////////
    //GETTERS


    /**
     * Returns the value at index {@code index} of the key block increments
     * @param index index to retrieve
     * @return index of block increments
     * @throws ArrayIndexOutOfBoundsException if the index is outside the array's range
     */
    public static byte getKeyBlockIncrementIndex(int index) {
        if(index<0 || index>=KEY_BLOCK_INCREMENTS.length) {
            throw new ArrayIndexOutOfBoundsException("The given index ( " + index +
                    ") must be in the interval [0, " + (KEY_BLOCK_INCREMENTS.length-1) + "]");
        }
        return KEY_BLOCK_INCREMENTS[index];
    }


    /**
     * Returns the user's stored login credentials
     * @return login credentials
     */
    public int loginCredentials() {
        return loginCredentials;
    }

    /**
     * Sets the stored login credentials to {@code newCredentials}.
     * @param newCredentials new credentials to set
     */
    public void setLoginCredentials(int newCredentials) {
        loginCredentials = newCredentials;
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
     * Assigns {@code listener} as a value property listener on the app's Service.<br><br>
     *
     * Value format: String array of length 3 containing {result, formatted key, error message}<br>
     * Possible configurations:<br>
     * - Result and key non-null, error message null: process completed successfully<br>
     * - Result and key null, error message non-null: process stopped with error<br>
     * - Result, key, and error message null: process was cancelled<br>
     * - Entire output is null: should be ignored<br>
     * If any other value is produced, there is a bug.
     *
     * @param listener listener to assign
     */
    public void addServiceValueListener(ChangeListener<? super String[]> listener) {
        service.valueProperty().addListener(listener);
    }


    /**
     * Sets the app's Service to its READY state, preparing it to be run again.<br><br>
     * This method works when the Service is in any state.
     */
    public void resetService() {
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
    public void startService(String input, String key, boolean encrypting, boolean usingV2Process, byte punctMode,
                             boolean loadingFromFile, int nThreads) {

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
     */
    public void stopService() {
        service.cancel();
        service.reset();
    }

}
