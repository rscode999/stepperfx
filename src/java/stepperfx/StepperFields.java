package stepperfx;

import javafx.beans.value.ChangeListener;
import javafx.concurrent.Worker;
import stepperfx.threading.ProcessService;

/**
 * Contains fields shared between the application's controllers.
 * One of the fields is a javafx.concurrent.Service used to do operations.
 */
final public class StepperFields {

    /**
     * Number of blocks in the key. Must be positive.
     */
    final public static int DEFAULT_BLOCK_COUNT = 6;

    /**
     * Number of characters in each key block. Must be positive.<br><br>
     *
     * Highly recommended to be relatively prime with {@code DEFAULT_BLOCK_COUNT}.
     */
    final public static int DEFAULT_BLOCK_LENGTH = 25;

    /**
     * Filename for the input file if none is given. Cannot be null. Must end in ".txt"
     */
    final public static String DEFAULT_INPUT_FILENAME = "input.txt";

    /**
     * Filename for the output file if none is given. Cannot be null. Must end in ".txt"
     */
    final public static String DEFAULT_OUTPUT_FILENAME = "output.txt";

    /**
     * Amount to shift during v2 processes, accessed using a getter method. Cannot be null. Length must be at least DEFAULT_BLOCK_COUNT
     */
    final private static byte[] KEY_BLOCK_INCREMENTS = {2,3,5,7,11,13,17,19,23,29};

    /**
     * Maximum amount of key blocks possible. Must be positive.<br>
     * Its value, {@code KEY_BLOCK_INCREMENTS.length}, is currently equal to 10.
     */
    final public static int MAX_BLOCK_COUNT = KEY_BLOCK_INCREMENTS.length;

    /**
     * Maximum length of each key block. Must be positive.
     */
    final public static int MAX_BLOCK_LENGTH = 100;

    /**
     * The maximum amount of threads that the app can use. Must be at least 1
     */
    final public static int MAX_THREADS = 999;

    /**
     * Maximum number of characters that are displayed on a result page. Must be positive
     */
    final public static int RESULT_PAGE_LENGTH = 100000;


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

    /**
     * Current number of blocks specified by the user. Cannot exceed {@code KEY_BLOCK_INCREMENTS.length}.
     */
    private int blockCount = DEFAULT_BLOCK_COUNT;

    /**
     * Current number of characters per block specified by the user
     */
    private int blockLength = DEFAULT_BLOCK_LENGTH;

    /**
     * Current number of characters per block specified by the user. For use with static methods.
     */
    private static int blockCountStatic = DEFAULT_BLOCK_COUNT;

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
        if(DEFAULT_BLOCK_COUNT <=0) throw new AssertionError("Block count must be positive");
        if(DEFAULT_BLOCK_LENGTH <=0) throw new AssertionError("Block length must be positive");
        if(DEFAULT_INPUT_FILENAME==null || DEFAULT_INPUT_FILENAME.length()<4 || !DEFAULT_INPUT_FILENAME.endsWith(".txt"))
            throw new AssertionError("Default input filename must end in \".txt\"");
        if(DEFAULT_OUTPUT_FILENAME==null || DEFAULT_OUTPUT_FILENAME.length()<4 || !DEFAULT_OUTPUT_FILENAME.endsWith(".txt"))
            throw new AssertionError("Default output filename must end in \".txt\"");
        if(KEY_BLOCK_INCREMENTS==null || KEY_BLOCK_INCREMENTS.length < DEFAULT_BLOCK_COUNT)
            throw new AssertionError("Key block increment length must be at least BLOCK_COUNT");
        if(MAX_BLOCK_COUNT <= 0) throw new AssertionError("Max block count must be positive");
        if(MAX_BLOCK_LENGTH <= 0) throw new AssertionError("Max block length must be positive");
        if(MAX_THREADS<1) throw new AssertionError("Max thread count must be positive");
        if(RESULT_PAGE_LENGTH<1) throw new AssertionError("Result page length must be positive");
    }



    // ///////////////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////////////
    //GETTERS, SETTERS


    /**
     * Returns the value at index {@code index} of the app's key block increments array.<br><br>
     *
     * If {@code index} is at least {@code {fieldsName}.getBlockCount()}, a warning is printed to System.err.
     *
     * @param index index to retrieve in the key block increments
     * @return value at specified index of the increments array
     * @throws ArrayIndexOutOfBoundsException if the index is outside the array's range
     */
    public static byte getKeyBlockIncrementIndex(int index) {
        if(index<0 || index>=KEY_BLOCK_INCREMENTS.length) {
            throw new ArrayIndexOutOfBoundsException("The given index (value=" + index +
                    ") must be on the interval [0, " + (KEY_BLOCK_INCREMENTS.length-1) + "]- instead received " + index);
        }
        if(index >= blockCountStatic) {
            System.err.println("WARNING: Index of key block increments (value=" + index + ") should be on the interval [0, " + (blockCountStatic-1) + "]");
        }

        return KEY_BLOCK_INCREMENTS[index];
    }


    /**
     * Returns the current block count stored by the shared fields
     * @return block count
     */
    public int getBlockCount() {
        return blockCount;
    }

    /**
     * Sets the current block count to {@code newBlockCount}.
     * @param newBlockCount block count to change to.  Must be on the interval [1, {@code StepperFields.getKeyBlockIncrementLength()}]
     */
    public void setBlockCount(int newBlockCount) {
        if(newBlockCount<=0 || newBlockCount>KEY_BLOCK_INCREMENTS.length)
            throw new AssertionError("New block count must be on the interval [1," + KEY_BLOCK_INCREMENTS.length + "]");
        blockCount = newBlockCount;
        blockCountStatic = newBlockCount;
    }

    /**
     * Returns the current block length stored by the shared fields
     * @return block length
     */
    public int getBlockLength() {
        return blockLength;
    }

    /**
     * Sets the current block length to {@code newBlockLength}.
     * @param newBlockLength value to set to. Must be on the interval [1, {@code StepperFields.MAX_BLOCK_LENGTH}]
     */
    public void setBlockLength(int newBlockLength) {
      if(newBlockLength<=0 || newBlockLength>MAX_BLOCK_LENGTH)
          throw new AssertionError("New block length must be on the interval [1, " + MAX_BLOCK_LENGTH + "]");
        blockLength = newBlockLength;
    }


    /**
     * Returns the user's stored login credentials
     * @return login credentials
     */
    public int getLoginCredentials() {
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
     * Value format: String array of length 4 containing {result, formatted key, error type, error message}<br>
     * Possible configurations:<br>
     * - Result and key non-null, error type and message null: process completed successfully<br>
     * - Result and key null, error type message non-null: process stopped with error<br>
     * - All indices null: process was cancelled<br>
     * - Entire output is null: should be ignored<br>
     * No other value should be produced. Any assigned listener should check for illegal values.
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
     * @param blockCount number of blocks to use. Must be on the interval [1, {@code StepperFields.MAX_BLOCK_COUNT}]
     * @param blockLength number of characters in each block to use. Must be on the interval [1, {@code StepperFields.MAX_BLOCK_LENGTH}]
     * @param punctMode 0 if the task removes all punctuation from the input, 1 if the task removes spaces from the input,
     *                  2 if the task processes the input with all punctuation
     * @param loadingFromFile true if loading input from a separate file, false otherwise
     * @param nThreads number of threads to use during processing. Must be on the interval [0, {@code StepperFields.MAX_THREADS}]
     * @throws IllegalStateException if the service is not ready to be run
     */
    public void startService(String input, String key, boolean encrypting, boolean usingV2Process,
                             int blockCount, int blockLength,
                             int punctMode,
                             boolean loadingFromFile, int nThreads) {

        if(input == null) throw new AssertionError("Input cannot be null");
        if(key==null) throw new AssertionError("Key cannot be null");
        if(blockCount<=0 || blockCount>MAX_BLOCK_COUNT) throw new AssertionError("Block count (value: " + blockCount + ") must be on the interval [1, " + MAX_BLOCK_COUNT + "]");
        if(blockLength<=0 || blockLength>MAX_BLOCK_LENGTH) throw new AssertionError("Block length (value: " + blockLength + ") must be on the interval [1, " + MAX_BLOCK_LENGTH + "]");
        if(punctMode<0 || punctMode>2) throw new AssertionError("Punctuation mode (value: " + punctMode + ") must be 0, 1, or 2");
        if(nThreads<0 || nThreads>MAX_THREADS) throw new AssertionError("Number of threads (value: " + nThreads + ")" +
                " must be on the interval [0, " + MAX_THREADS + "]");

        if(service.getState() == Worker.State.READY) {
            service.initializeService(input, key, encrypting, usingV2Process,
                    blockCount, blockLength,
                    punctMode,
                    loadingFromFile, nThreads);

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
