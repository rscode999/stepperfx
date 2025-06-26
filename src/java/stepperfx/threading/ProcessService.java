package stepperfx.threading;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import stepperfx.StepperFields;

/**
 * Schedules multithreaded tasks for the app.<br><br>
 *
 * IMPORTANT: The service must be re-initialized before each run. The service's fields are set to {@code null}
 * during each run. Initialize using the service's {@code initializeService} method.
 */
public class ProcessService extends Service<String[]> {

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
    private byte punctMode;

    /**
     * True if using version 2 processes, false otherwise
     */
    private boolean usingV2Process;


    // //////////////////////////////////////////////////////////////////////////////////////////////////////////


//    //CONSTRUCTOR
//    /**
//     * Creates a new ProcessService with uninitialized fields
//     */
//    public ProcessService() {
//        super();
//        //An explicitly defined constructor is required by the rules
//    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////
    //CREATE TASK

    /**
     * Creates and runs a ProcessTask. The Service's fields are set to null to save memory.
     */
    public Task<String[]> createTask() {
        ProcessTask task = new ProcessTask(input, key, encrypting, usingV2Process, punctMode, loadingFromFile, nThreads);

//        input = null;
//        key = null;
//        nThreads = Integer.MIN_VALUE;

        return task;
    }


    // //////////////////////////////////////////////////////////////////////////////////////////////////////////
    //SETTERS

    /**
     * Sets the input and amount of threads for input processing.<br>
     * The inputs to this method will be passed to the service's Task to execute.
     *
     * @param input input text to process, or a filepath to load from. Cannot be null
     * @param key key to process the input with. Cannot be null
     * @param encrypting true if the service encrypts, false if the service decrypts
     * @param usingV2Process true if the service uses version 2 processes
     * @param punctMode 0 if the task removes all punctuation from the input, 1 if the task removes spaces from the input,
     *                  2 if the task processes the input with all punctuation
     * @param loadingFromFile whether to load input from a file
     * @param nThreads number of threads to use during processing. Must be on the interval [0, StepperFields.MAX_THREADS]
     */
    public void initializeService(String input, String key, boolean encrypting, boolean usingV2Process,
                                  byte punctMode, boolean loadingFromFile, int nThreads) {
        if(input==null) throw new AssertionError("Input cannot be null");
        if(key==null) throw new AssertionError("Key cannot be null");
        if(punctMode<0 || punctMode>2) throw new AssertionError("Punctuation mode must be 0, 1, or 2");
        if(nThreads<0 || nThreads>StepperFields.MAX_THREADS) throw new AssertionError("Number of threads must be on the interval [0, " + StepperFields.MAX_THREADS + "]");

        this.input = input;
        this.key = key;
        this.encrypting = encrypting;
        this.usingV2Process = usingV2Process;
        this.punctMode = punctMode;
        this.loadingFromFile = loadingFromFile;
        this.nThreads = nThreads;
    }


}
