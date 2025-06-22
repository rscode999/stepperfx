package stepperfx.threading;

import javafx.concurrent.Task;

/**
 * Processes user input on a separate thread. Uses ProcessSubtask threads to help with processing.
 */
public class ProcessTask extends Task<String> {

    /**
     * Input text for the task to process
     */
    private String input;

    /**
     * Key to process the input with
     */
    private String key;

    /**
     * Number of worker threads to use during processing
     */
    final private int nWorkerThreads;

    /**
     * Creates a new ProcessTask, initializing its inputs.
     *
     * @param input input text to process
     * @param key key to process the input with
     * @param nWorkerThreads number of worker threads to use during processing
     */
    public ProcessTask(String input, String key, int nWorkerThreads) {
        this.input = input;
        this.key = key;
        this.nWorkerThreads = nWorkerThreads;
    }


    /**
     * NOT YET IMPLEMENTED! Returns the string "hallo world".
     * @return
     * @throws Exception
     */
    @Override
    protected String call() throws Exception {
        return "hallo world";
    }
}
