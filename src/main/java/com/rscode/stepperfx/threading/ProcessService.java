package com.rscode.stepperfx.threading;

import com.rscode.stepperfx.integration.OperationSelection;
import com.rscode.stepperfx.integration.PunctuationSelection;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import com.rscode.stepperfx.integration.StepperFields;

import static com.rscode.stepperfx.integration.StepperFields.MAX_BLOCK_COUNT;
import static com.rscode.stepperfx.integration.StepperFields.MAX_BLOCK_LENGTH;

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
     * Which operation the Service carries out. Example: Stepper 2, encryption.
     */
    private OperationSelection operationSelection;

    /**
     * Punctuation preferences for the output's operation
     */
    private PunctuationSelection punctSelection;

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
        ProcessTask task = new ProcessTask(input, key, operationSelection, punctSelection,
                blocks, charsPerBlock,
                loadingFromFile, nThreads);

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
     * @param operationSelection operation to do, as a OperationSelection object (i.e. Stepper 2, encrypt)
     * @param punctSelection punctuation preferences, as a PunctuationSelection object
     * @param blockCount number of blocks to use. Must be on the interval [1, {@code StepperFields.MAX_BLOCK_COUNT}]
     * @param blockLength number of characters in each block to use. Must be on the interval [1, {@code StepperFields.MAX_BLOCK_LENGTH}]
     * @param loadingFromFile whether to load input from a file
     * @param nThreads number of threads to use during processing. Must be on the interval [0, {@code StepperFields.MAX_THREADS}]
     */
    public void initializeService(String input, String key, OperationSelection operationSelection, PunctuationSelection punctSelection,
                                  int blockCount, int blockLength,
                                  boolean loadingFromFile, int nThreads) {

        if(input == null) throw new AssertionError("Input cannot be null");
        if(key == null) throw new AssertionError("Key cannot be null");
        if(blockCount<=0 || blockCount>MAX_BLOCK_COUNT) throw new AssertionError("Block count must be on the interval [1, " + MAX_BLOCK_COUNT + "]- instead received " + blockCount);
        if(blockLength<=0 || blockLength>MAX_BLOCK_LENGTH) throw new AssertionError("Block length must be on the interval [1, " + MAX_BLOCK_LENGTH + "]- instead received " + blockLength);
        if(nThreads<0 || nThreads>StepperFields.MAX_THREADS) throw new AssertionError("Number of threads must be on the interval [0, " + StepperFields.MAX_THREADS + "]- instead received " + nThreads);

        this.input = input;
        this.key = key;
        this.operationSelection = operationSelection;
        this.punctSelection = punctSelection;
        this.blocks = blockCount;
        this.charsPerBlock = blockLength;
        this.loadingFromFile = loadingFromFile;
        this.nThreads = nThreads;
    }


}
