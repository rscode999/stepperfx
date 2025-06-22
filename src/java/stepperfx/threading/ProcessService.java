package stepperfx.threading;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 * Schedules multithreaded tasks for the app.<br><br>
 *
 * IMPORTANT: The service must be re-initialized before each run. The service's fields are set to {@code null}
 * during each run. Initialize using the service's {@code initializeService} method.
 */
public class ProcessService extends Service<String> {

    /**
     * Input text for the service to process
     */
    private String input;

    /**
     * Key to process the input with
     */
    private String key;

    /**
     * Number of threads to use during processing
     */
    private int nThreads;


    // //////////////////////////////////////////////////////////////////////////////////////////////////////////


    //CONSTRUCTOR
    /**
     * Creates a new ProcessService with uninitialized fields
     */
    public ProcessService() {
        //An explicitly defined constructor is required by the rules
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////
    //CREATE TASK

    /**
     * Creates and runs a ProcessTask. The Service's fields are set to null to save memory.
     */
    public Task<String> createTask() {
        ProcessTask task = new ProcessTask(input, key, nThreads);

        input = null;
        key = null;
        nThreads = Integer.MIN_VALUE;

        return task;
    }


    // //////////////////////////////////////////////////////////////////////////////////////////////////////////
    //SETTERS

    /**
     * Sets the input and amount of threads for input processing.<br>
     * The inputs to this method will be passed to the service's Task to execute.<br><br>
     *
     * @param input input text to process
     * @param key key to process the input with
     * @param nThreads number of threads to use during processing
     */
    public void initializeService(String input, String key, int nThreads) {
        this.input = input;
        this.key = key;
        this.nThreads = nThreads;
    }


}
