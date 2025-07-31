package stepperfx.threading;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import stepperfx.integration.StepperFields;

import static stepperfx.integration.StepperFields.MAX_BLOCK_COUNT;
import static stepperfx.integration.StepperFields.MAX_BLOCK_LENGTH;

/**
 * Schedules multithreaded tasks for the app.<br><br>
 *
 * The output of the Service is retrieved using a ValuePropertyListener set on it.<br><br>
 *
 * The output is an array of 4 Strings: {result, formatted key, error type, error messages}.<br>
 * If a user-produced error stops processing, the result and key will be null, with a non-null error type and message.<br>
 * If the process is cancelled, all output indices are null.<br>
 * In the case of normal execution, the result and key are the only non-null indexes of the output.<br><br>
 *
 * IMPORTANT: The service must be re-initialized before each run. The service's fields are set to {@code null}
 * during each run. Initialize using the service's {@code initializeService} method.
 */
final public class ProcessService extends Service<String[]> {

    /**
     * Number of blocks to use in processes
     */
    private int blocks;

    /**
     * Number of characters per block to use in processes
     */
    private int charsPerBlock;

    /**
     * True if the service is encrypting, false if the service is decrypting
     */
    private boolean encrypting;

    /**
     * Input text for the service to process. If loading from a file, contains a filepath to load from.
     */
    private String input;

    /**
     * Key to process the input with
     */
    private String key;

    /**
     * Whether the service's task will load its input from a file
     */
    private boolean loadingFromFile;

    /**
     * Number of threads to use during processing
     */
    private int nThreads;

    /**
     * 0 if the task removes all punctuation from the input.<br>
     * 1 if the task removes spaces from the input.<br>
     * 2 if the task processes the input with all punctuation.<br><br>
     *
     * No other value is allowed.
     */
    private int punctMode;

    /**
     * True if using version 2 processes, false otherwise
     */
    private boolean usingV2Process;


    // //////////////////////////////////////////////////////////////////////////////////////////////////////////


    //CONSTRUCTOR
    /**
     * Creates a new ProcessService with uninitialized fields
     */
    public ProcessService() {
        super();
        //An explicitly defined constructor is required by the rules
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////
    //CREATE TASK

    /**
     * Creates and runs a ProcessTask. The Service's fields are set to null to save memory.
     */
    @Override
    public Task<String[]> createTask() {
        ProcessTask task = new ProcessTask(input, key, encrypting, usingV2Process,
                blocks, charsPerBlock,
                punctMode, loadingFromFile, nThreads);

        input = null;
        key = null;

        return task;
    }


    // //////////////////////////////////////////////////////////////////////////////////////////////////////////
    //METHODS

    /**
     * Sets all inputs for processing.<br>
     * The inputs to this method will be passed to the service's Task to execute.
     *
     * @param input input text to process, or a filepath to load from. Cannot be null
     * @param key key to process the input with. Cannot be null
     * @param encrypting true if the service encrypts, false if the service decrypts
     * @param usingV2Process true if the service uses version 2 processes
     * @param blocks number of blocks to use. Must be positive
     * @param charsPerBlock number of characters in each block to use. Must be positive
     * @param punctMode 0 if the task removes all punctuation from the input, 1 if the task removes spaces from the input,
     *                  2 if the task processes the input with all punctuation
     * @param loadingFromFile whether to load input from a file
     * @param nThreads number of threads to use during processing. Must be on the interval [0, StepperFields.MAX_THREADS]
     */
    public void initializeService(String input, String key, boolean encrypting, boolean usingV2Process,
                                  int blocks, int charsPerBlock,
                                  int punctMode, boolean loadingFromFile, int nThreads) {

        if(input==null) throw new AssertionError("Input cannot be null");
        if(key==null) throw new AssertionError("Key cannot be null");
        if(blocks<=0 || blocks>MAX_BLOCK_COUNT) throw new AssertionError("Blocks must be on the interval [1, " + MAX_BLOCK_COUNT + "]- instead received " + blocks);
        if(charsPerBlock<=0 || charsPerBlock>MAX_BLOCK_LENGTH) throw new AssertionError("Chars per block must be on the interval [1, " + MAX_BLOCK_LENGTH + "]- instead received " + charsPerBlock);
        if(punctMode<0 || punctMode>2) throw new AssertionError("Punctuation mode must be 0, 1, or 2: instead received " + punctMode);
        if(nThreads<0 || nThreads>StepperFields.MAX_THREADS) throw new AssertionError("Number of threads must be on the interval [0, " + StepperFields.MAX_THREADS + "]- instead received " + nThreads);

        this.input = input;
        this.key = key;
        this.encrypting = encrypting;
        this.usingV2Process = usingV2Process;
        this.blocks = blocks;
        this.charsPerBlock = charsPerBlock;
        this.punctMode = punctMode;
        this.loadingFromFile = loadingFromFile;
        this.nThreads = nThreads;
    }


}
