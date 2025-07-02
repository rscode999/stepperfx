package stepperfx.threading;


import javafx.concurrent.Task;
import stepperfx.StepperFields;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Processes the input on a separate thread.<br><br>
 *
 * The output is an array of length 4. The indices are: {output, formatted key, error type (from [error].getClass().toString),
 * error message (from [error].getMessage)}<br>
 * If operations complete successfully, the output and key will be non-null, with a null error type and message.<br>
 * If not, the error type and message will be non-null, with the first two indices null.<br><br>
 *
 * If the task is cancelled, the return value will have all indices as null.<br><br>
 *
 * The output is received with a ValueProperty listener assigned to the Service that deployed the Task.
 */
public class ProcessTask extends Task<String[]> {

    /**
     * Names for each of the possible loading states that the task can be in.
     * Progression through the states starts at index 0, then index 1, and so on
     */
    public final static String[] LOADING_STATE_NAMES =
            new String[] {"Loading input...", "Formatting...", "Executing...", "Finalizing..."};


    // //////////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * True if the service is encrypting, false if the service is decrypting
     */
    private boolean encrypting;

    /**
     * Input text for the service to process
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
    private int nWorkerThreads;

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


    /**
     * Creates a new ProcessTask, initializing its inputs.
     *
     * @param input input text to process, or a filepath to load from. Cannot be null
     * @param key key to process the input with. Cannot be null
     * @param encrypting true if the service encrypts, false if the service decrypts
     * @param usingV2Process true if using enhanced (v2) processes, false otherwise
     * @param punctMode 0 if the task removes all punctuation from the input, 1 if the task removes spaces from the input,
     *                  2 if the task processes the input with all punctuation
     * @param loadingFromFile whether to load input from a file
     * @param nWorkerThreads number of threads to use during processing. Must be on the interval [0, StepperFields.MAX_THREADS]
     */
    public ProcessTask(String input, String key, boolean encrypting, boolean usingV2Process,
                       byte punctMode, boolean loadingFromFile, int nWorkerThreads) {
        if(input==null) throw new AssertionError("Input cannot be null");
        if(key==null) throw new AssertionError("Key cannot be null");
        if(punctMode<0 || punctMode>2) throw new AssertionError("Punctuation mode must be on the interval [0,2]- received " + punctMode);
        if(nWorkerThreads<0 || nWorkerThreads>StepperFields.MAX_THREADS)
            throw new AssertionError("Number of worker threads must be on the interval [0, " + StepperFields.MAX_THREADS
            + "]- received " + nWorkerThreads);

        this.input = input;
        this.key = key;
        this.encrypting = encrypting;
        this.usingV2Process = usingV2Process;
        this.loadingFromFile = loadingFromFile;
        this.punctMode = punctMode;
        this.nWorkerThreads = nWorkerThreads;
    }

    /**
     * FOR UNIT TESTS ONLY! Creates a new ProcessTask and loads it with values against operation preconditions
     */
    public ProcessTask() {
        this.input = null;
        this.key = null;
        this.encrypting = false;
        this.usingV2Process = false;
        this.loadingFromFile = false;
        this.punctMode = -127;
        this.nWorkerThreads = -1;
    }


    // ////////////////////////////////////////////////////////////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////////////////////////////////



    /**
     * Returns the output of the Task's processing.<br><br>
     *
     * The output is an array of length 4. The indices are: {output, formatted key, error type (from [error].getClass().toString),
     * error message (from [error].getMessage)}<br>
     * If operations complete successfully, the output and key will be non-null, with a null error type and message.<br>
     * If not, the error type and message will be non-null, with the first two indices null.<br><br>
     *
     * If the task is cancelled, the return value will have all four indices as null.<br><br>
     *
     * The output is received with a ValueProperty listener assigned to the Service that deployed the Task.
     *
     * @return result of transforming the input with the given parameters
     */
    @Override
    protected String[] call() {
        try {

            //Constructor check
            if (input == null || key == null) {
                throw new AssertionError("WRONG CONSTRUCTOR USED");
            }

            updateMessage(LOADING_STATE_NAMES[0]);

            //Prevent the user from getting epilepsy
            for (long s = 0; s < 400000000L; s++) {
                if (isCancelled()) {
                    return new String[]{null, null, null, null};
                }
            }


            //Get the input from a file, if chosen. Upon failure, present the error message
            if (loadingFromFile) {
                try {
                    input = readFile(input);
                }
                catch (FileNotFoundException e) {
                    return new String[]{null, null, e.getClass().toString(), e.getMessage()};
                }
            }
            //`input` now contains the text to be loaded


            //Make the key
            byte[][] formattedKey = createKeyBlocks(key, StepperFields.BLOCK_COUNT, StepperFields.BLOCK_LENGTH);

            StringBuilder runResult = new StringBuilder(); //Intermediate result
            for (int run = 1; run <= 2; run++) { //run 1 -> diacritics workers, run 2 -> main process workers

                updateMessage(LOADING_STATE_NAMES[run]);

                //Bug fix
                if (nWorkerThreads <= 0) {
                    return new String[]{"", "", null, null};
                }

                //Create subtasks and workloads
                ExecutorService executorService = Executors.newFixedThreadPool(nWorkerThreads);
                String[] subtaskWorkloads = setWorkerLoads(input, nWorkerThreads, StepperFields.BLOCK_LENGTH);

                //Assign worker threads. Run 1 -> diacritics workers, run 2 -> main process workers
                Task<String>[] subtasks = (run == 1)
                        ? new ProcessSubtaskDiacritics[nWorkerThreads]
                        : new ProcessSubtaskMain[nWorkerThreads];

                //Assign work to each subtask and create each subtask
                int startingSegment = 0;
                for (int i = 0; i < nWorkerThreads; i++) {
                    subtasks[i] = (run == 1)
                            ? new ProcessSubtaskDiacritics(subtaskWorkloads[i])
                            : new ProcessSubtaskMain(subtaskWorkloads[i], formattedKey, encrypting, usingV2Process,
                            punctMode, startingSegment);

                    //Advance starting segment
                    int charCounts = countAlphaChars(subtaskWorkloads[i]);
                    startingSegment += charCounts / StepperFields.BLOCK_LENGTH;

                    if (isCancelled()) {
                        return new String[]{null, null, null, null};
                    }
                }

                //The subtask workloads are no longer needed now
                subtaskWorkloads = null;
                System.gc();

                //Start the subtasks
                for (Task<String> subtask : subtasks) {
                    executorService.submit(subtask);
                }


                //Get the results
                runResult = new StringBuilder(100);
                for (int s = 0; s < nWorkerThreads; s++) {

                    //when the current worker finishes, load its result
                    try {
                        if (this.isCancelled()) {
                            for (Task<String> subtask : subtasks) {
                                subtask.cancel();
                            }
                            executorService.shutdownNow();
//                            System.out.println("MULTITHREADED PROCESS CANCELLED");
                            return new String[]{null, null, null, null};
                        }

                        runResult.append(subtasks[s].get());
                    }
                    //If cancelled while waiting
                    catch (InterruptedException e) {
                        return new String[]{null, null, null, null};
                    }
                    //ExecutionExceptions are caught by the outside try/catch

                }

                executorService.shutdown(); //needed to free threads from memory

                //reload the input if run 2 still needs to go
                if (run == 1) {
                    input = runResult.toString();
                    runResult = null;
                }
            }


            //change the message to "Finalizing..." (which disables the cancel button through the loading controller's listener)
            updateMessage(LOADING_STATE_NAMES[3]);
            Thread.sleep(100); //give the FX app thread time to update

            return new String[] {runResult.toString(), createKeyBlocksReverse(formattedKey), null, null};
        }
        catch(Throwable t) {
            t.printStackTrace();
            return new String[] {null, null, t.getClass().toString(), t.getMessage()};
        }
    }




    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //METHODS


    /**
     * Returns the amount of English ASCII characters in {@code input}.
     * If cancelled, returns 0.
     *
     * @param input String to count alphabetic and numeric characters in. Cannot be null
     * @return number of alphabetic chars in the input
     */
    private int countAlphaChars(String input) {
        if(input == null) throw new AssertionError("Input cannot be null");

        int output = 0;

        for(int i=0; i<input.length(); i++) {
            //alphabetic character: +output
            if(((int)input.charAt(i)>=97 && (int)input.charAt(i)<=122) ||
                (int)input.charAt(i)>=65 && (int)input.charAt(i)<=90) {
                output++;
            }

            //cancel check
            if(isCancelled()) {
                return 0;
            }
        }

        return output;
    }



    /**
     * Returns a byte[][] array with {@code blocks} indices, each with {@code charsPerBlock} characters,
     * containing the text from {@code input} as numerical values.<br><br>
     *
     * -Numerical values: a=0, b=1, c=2... z=25. A=0, B=1, C=2... Z=25. Note: uppercase letters are the same as lowercase letters<br>
     *
     * -All non-letters (any character that is not an English ASCII letter after removeDiacritics is used) are ignored.<br>
     *
     * -Index [0][0] of the output is filled first, followed by index [0][1]. When the first subarray is filled, index [1][0]
     * is next, followed by [1][1], and so on.<br>
     *
     * -If {@code input} contains less than {@code blocks}*{@code charsPerBlock} English ASCII letters, any character not filled by `input`
     * becomes a random value on the interval [0,25]. If {@code input} contains more than {@code blocks}*{@code charsPerBlock}
     * English ASCII letters, any letter past index {@code blocks}*{@code charsPerBlock} in the input is ignored.<br>
     *
     * @param input the input text. Can't be null
     * @param blocks number of indices in the output array. Must be positive
     * @param charsPerBlock number of indices in each of the output's subarrays. Must be positive
     * @return {@code blocks} by {@code charsPerBlock} byte[][] array loaded with text from {@code input}
     */
    private byte[][] createKeyBlocks(String input, int blocks, int charsPerBlock) {
        if(input==null) {
            throw new AssertionError("Input string cannot be null");
        }
        if(blocks<=0) {
            throw new AssertionError("Blocks must be positive");
        }
        if(charsPerBlock<=0) {
            throw new AssertionError("Chars per block must be positive");
        }

        //The formatted key will have all lowercase ASCII characters in it
        StringBuilder formattedKey = new StringBuilder();

        //Create the formatted key. Fill until every input character is loaded
        for(int i=0; i<input.length(); i++) {
            char currentChar = Character.toLowerCase(input.charAt(i));
            currentChar = removeDiacritics(currentChar);

            if(currentChar>=97 && currentChar<=122) {
                formattedKey.append(currentChar);
            }
        }

        //Create a sequence of random numbers
        SecureRandom rng = new SecureRandom();
        rng.nextInt();

        //If the output is not filled, load with random characters
        while(formattedKey.length() < blocks*charsPerBlock) {
            for(int r=0; r<rng.nextInt(); r++) {
                rng.nextInt();
            }
            int currentRandChar = rng.nextInt();

            //Convert the next random number to a non-negative number
            if(currentRandChar < 0) {
                currentRandChar = currentRandChar*-1;
            }

            //Convert the number to a lowercase character ASCII value
            currentRandChar = (currentRandChar%26 + 97);

            //Add random character to the formatted key
            formattedKey.append((char) currentRandChar);
        }

        //At this point, the formatted key should contain blocks*charsPerBlock characters.
        byte[][] output = new byte[blocks][charsPerBlock];
        int inputIndex=0;
        //Load the output with the input's indices
        for(int a=0; a<blocks; a++) {
            for(int i=0; i<charsPerBlock; i++) {
                output[a][i]=(byte)(formattedKey.charAt(inputIndex) - 97);
                inputIndex++;
            }
        }

        return output;
    }


    /**
     * Returns a string representation of the input byte array.<br><br>
     *
     * The given array should represent a valid key configuration from processes.
     *
     * @param input input array. Cannot be null. No subarrays can be null. All indices must be on the interval [0, 25]
     * @return String representation of the input
     */
    private String createKeyBlocksReverse(byte[][] input) {
        if(input == null) {
            throw new AssertionError("Input cannot be null");
        }

        StringBuilder output = new StringBuilder(input.length * input[0].length);

        //move through the blocks
        for (int b=0; b<input.length; b++) {
            if(input[b] == null) {
                throw new AssertionError("Block index " + b + " cannot be null");
            }

            //append each value in the block to the output
            for(int c=0; c<input[b].length; c++) {
                if((int)input[b][c]<0 || (int)input[b][c]>25) {
                    throw new AssertionError("Index [" + b + "][" + c + "] ( " + input[b][c] + ") must be on the interval [0, 25]");
                }

                output.append((char)(input[b][c] + 97));
            }
        }

        return output.toString();
    }


    /**
     * Returns a byte[][] array with `blocks` indices, each with `charsPerBlock` characters,
     * containing the text from `input` as numerical values.<br><br>
     *
     * -Numerical values: a=0, b=1, c=2... z=25. A=0, B=1, C=2... Z=25. Note: uppercase letters are the same as lowercase letters<br>
     *
     * -Before the input can be processed, removeDiacritics must be called on each character of the input.<br>
     *
     * -All non-letters (any character that is not an English ASCII letter after removeDiacritics is called) are to be ignored.<br>
     *
     * -If `input` contains less than `blocks`*`charsPerBlock` English ASCII letters, any character not filled by `input`
     * becomes a random value on the interval [0,25]. If `input` contains more than `blocks`*`charsPerBlock` English ASCII letters,
     * any character past index `blocks`*`charsPerBlock` in the input is ignored.<br><br>
     *
     * FOR UNIT TESTING ONLY!!!
     *
     * @param input the input text. Can't be null
     * @param blocks number of indices in the output array. Must be positive
     * @param charsPerBlock number of indices in each of the output's subarrays. Must be positive
     * @return `blocks` by `charsPerBlock` byte[][] array loaded with text from `input`
     */
    public byte[][] createKeyBlocks_Testing(String input, int blocks, int charsPerBlock) {
        return createKeyBlocks(input, blocks, charsPerBlock);
    }



    /**
     * Returns all the text from a file whose name is {@code filepath}. If {@code filepath} is the empty string, loads from
     * {@code StepperFields.DEFAULT_INPUT_FILE}.<br><br>
     *
     * The input filepath must end with the ".txt" extension.<br>
     * If the input filepath is empty, does not end in ".txt", or the file could not be read,
     * throws a FileNotFoundException.<br>
     *
     * @param filepath name of the input file. Can't be null
     * @return contents from the given input filename, or the empty string if the Boss is cancelled
     * @throws FileNotFoundException if the file can't be read or the filename lacks the ".txt" extension.
     * Displays a descriptive error message, which is used in the main App, if thrown.
     */
    private String readFile(String filepath) throws FileNotFoundException {
        if(filepath==null) {
            throw new AssertionError("Filename cannot be null");
        }

        File inputFile;

        //Create file from default or the top text input
        if(filepath.isEmpty()) {
            inputFile = new File(StepperFields.DEFAULT_INPUT_FILENAME);
        }
        else {
            inputFile = new File(filepath);
        }

        StringBuilder output = new StringBuilder();

        //Check if the input file ends in .txt
        if(inputFile.getName().length()<=3 || !inputFile.getName().endsWith(".txt")) {
            throw new FileNotFoundException("The input file must have a .txt extension");
        }
        else {
            //Read the file and load it into the fields
            try {
                Scanner fileReader = new Scanner(inputFile);

                //Load all the lines
                while (fileReader.hasNextLine()) {
                    if(isCancelled()) {
                        return "";
                    }

                    output.append(fileReader.nextLine());
                    output.append("\n");
                }
            }
            //If error, create a nicer error message and throw an exception with it
            catch (FileNotFoundException e) {
                String fileErrorMsg = "The input file \"";

                fileErrorMsg += (inputFile.getName().length() < 20) ?
                        inputFile.getName() :
                        inputFile.getName().substring(0, 16) + "... .txt";

                fileErrorMsg += "\" does not exist\n";

                if(filepath.contains("\\") || filepath.contains("/")) {
                    fileErrorMsg += "at the given absolute path";
                }
                else {
                    fileErrorMsg += "in the folder containing the app";
                }

                throw new FileNotFoundException(fileErrorMsg);
            }
        }

        return output.toString();
    }



    /**
     * Returns a lowercase version of the input without accent marks or letter variants.<br><br>
     *
     * Helper to {@code createKeyBlocks}
     *
     * @param input letter to remove diacritics from
     * @return copy of input without diacritics
     */
    private char removeDiacritics(char input) {

        String a="" + input;
        a=a.toLowerCase();
        input=a.charAt(0);
        a=null;

        //Not like the 'final' declaration will save the array indices from tampering,
        //but I hope that it increases speed a little.
        final String[] outChars={"àáâãäå", "ç", "ð", "èéëêœæ", "ìíîï", "òóôõöø", "ǹńñň",
                "ß", "ùúûü", "ýÿ", "⁰₀", "¹₁", "²₂", "³₃", "⁴₄", "⁵₅", "⁶₆", "⁷₇", "⁸₈", "⁹₉", "—"};
        final char[] inChars={'a', 'c', 'd', 'e', 'i', 'o', 'n', 's', 'u', 'y',  '0', '1',
                '2', '3', '4', '5', '6', '7', '8', '9', '-'};
        char charReplacement='#';


        //loop through outChar strings
        for(int os=0; os<outChars.length; os++) {
            //loop through individual letters in the outChars string.
            //if match, set replacement char to ASCII replacement

            for(int oc=0; oc<outChars[os].length(); oc++) {
                if(outChars[os].charAt(oc)==input) {
                    charReplacement=inChars[os];
                    break;
                }
            }
        }

        if(charReplacement=='#') {
            return input;
        }

        return charReplacement;
    }

    /**
     * Returns a lowercase version of the input without accent marks or letter variants.<br><br>
     *
     * FOR UNIT TESTING ONLY!
     *
     * @param input letter to remove diacritics from
     * @return copy of input without diacritics
     */
    public char removeDiacritics_Testing(char input) {
        return removeDiacritics(input);
    }



    /**
     * Returns an array containing {@code text} split evenly into {@code threads} pieces.
     * The number of alphabetic characters of each piece is a multiple of {@code blockLength}, except for the last piece.<br><br>
     *
     * -Alphabetic characters are lowercase English ASCII characters.<br>
     *
     * -All indices except for the last one should have {@code blockLength}alphabetic characters or a multiple thereof.<br>
     *
     * -Any unused threads should be assigned the empty string, not null. Empty strings may occur at the beginning of the output array.<br>
     *
     * -Note: The final character of each output index (excluding the last index) should end in an alphabetic character.
     *
     * @param text the text to split. Non-null
     * @param threads how many pieces {@code text} should be split into. If zero, or the Task is cancelled, returns {""}. Cannot be negative
     * @param blockLength number of characters, or a multiple thereof, to put in each piece. Must be positive
     * @return array of Strings. There are {@code threads} total Strings whose alphabetic characters are
     * evenly split among the output's indices
     */
    private String[] setWorkerLoads(String text, int threads, int blockLength) {
        //Assert preconditions
        if (text==null) throw new AssertionError("Text cannot be null");
        if(threads<0) throw new AssertionError("Number of threads cannot be negative");
        if(blockLength<=0) throw new AssertionError("Block length must be positive");

        //Return the empty string if threads is 0
        if(threads==0) {
            return new String[] {""};
        }

        //Find effective length of the text to create the blocks without wasting memory
        int alphaChars = 0;
        for(int i=0; i<text.length(); i++) {
            if(text.charAt(i)>=97 && text.charAt(i)<=122) {
                alphaChars++;
            }

            if(isCancelled()) {
                return new String[] {""};
            }
        }


        //CALCULATE NUMBER OF BLOCKS PER THREAD

        //Number of blocks equals the number of alphabetic characters divided by the block length
        //If there is a remainder, there is an extra block
        int nBlocks = alphaChars / blockLength;
        if (alphaChars % blockLength != 0) nBlocks++;
        //Note: one block is a piece of length `blockLength` or shorter

        //Create the number of characters in each thread. Temporarily holds the number of blocks
        int[] charsPerThread = new int[threads];

        //The minimum number of blocks per piece is the number of blocks divided by the number of threads
        Arrays.fill(charsPerThread, nBlocks / threads);
        //The number of remaining blocks equals the remainder of the number of blocks divided by the number of threads
        for (int i = charsPerThread.length - 1; i >= charsPerThread.length - nBlocks % threads; i--) {
            charsPerThread[i]++;
        }


        //CALCULATE NUMBER OF CHARACTERS PER THREAD

        //Create a StringBuilder array to hold the result and load it. Also properly calculate number of characters per thread
        StringBuilder[] threadLoads = new StringBuilder[threads];
        for(int t=0; t<threadLoads.length; t++) {
            threadLoads[t] = new StringBuilder();
            charsPerThread[t] *= blockLength; //now, charsPerThread holds the number of characters per thread

            //Check if cancelled, abort if so
            if(isCancelled()) {
                return new String[] {""};
            }
        }


        //LOAD THE THREADS

        //Move through each thread, except for the last one, and load it
        int currentThread = 0;
        int currentTextIndex = 0;
        while(currentThread < threadLoads.length) {

            //If there are no more characters to load, move to the next thread
            if(charsPerThread[currentThread] == 0) {
                currentThread++;
            }
            //If not, load the character
            else {
                threadLoads[currentThread].append(text.charAt(currentTextIndex));

                //Update number of alphabetic characters if a letter was loaded
                if(text.charAt(currentTextIndex)>=97 && text.charAt(currentTextIndex)<=122) {
                    charsPerThread[currentThread]--;
                }

                //Move to the next index
                currentTextIndex++;
            }

            //Exit the loop if it will overrun the input text
            if(currentTextIndex >= text.length()) {
                break;
            }

            //Check if cancelled, abort if so
            if(isCancelled()) {
                return new String[] {""};
            }
        }

        //Load any non-alphabetic character that was not yet loaded into the last thread
        while(currentTextIndex < text.length()) {
            threadLoads[threadLoads.length-1].append(text.charAt(currentTextIndex));
            currentTextIndex++;

            //Check if cancelled, abort if so
            if(isCancelled()) {
                return new String[] {""};
            }
        }

        //Convert StringBuilders to proper strings
        String[] output = new String[threads];
        for(int l=0; l<threadLoads.length; l++) {
            output[l] = threadLoads[l].toString();

            //Check if cancelled, abort if so
            if(isCancelled()) {
                return new String[] {""};
            }
        }

        return output;
    }

    /**
     * FOR UNIT TESTING ONLY!<br><br>
     *
     * Returns an array containing {@code text} split evenly into {@code threads} pieces.
     * The number of alphabetic characters of each piece is a multiple of {@code blockLength}, except for the last piece.<br><br>
     *
     * -Alphabetic characters are lowercase English ASCII characters.<br>
     *
     * -All indices except for the last one should have {@code blockLength}alphabetic characters or a multiple thereof.<br>
     *
     * -Any unused threads should be assigned the empty string, not null. Empty strings may occur at the beginning of the output array.<br>
     *
     * -Note: The final character of each output index (excluding the last index) should end in an alphabetic character.
     *
     * @param text the text to split. Non-null
     * @param threads how many pieces {@code text} should be split into. If zero, or the Task is cancelled, returns {""}. Cannot be negative
     * @param blockLength number of characters, or a multiple thereof, to put in each piece. Must be positive
     * @return array of Strings. There are {@code threads} total Strings whose alphabetic characters are
     * evenly split among the output's indices
     */
    public String[] setWorkerLoads_Testing(String text, int threads, int blockLength) {
        return setWorkerLoads(text, threads, blockLength);
    }



    /**
     * EXPERIMENTAL
     * @param filepath filepath to write to. Cannot be null. If not the empty string, must end in ".txt"
     * @param contents what to write to the file. Cannot be null
     * @param doingDryRun true if checking if the file exists, false if writing to the file
     * @throws FileNotFoundException if the file write fails
     */
    private void writeFile(String filepath, String contents, boolean doingDryRun) throws FileNotFoundException {
        if(filepath==null) {
            throw new AssertionError("Filepath cannot be null");
        }
        if(contents == null) {
            throw new AssertionError("Contents cannot be null");
        }

        File inputFile;

        //Create file from default or the top text input
        if(filepath.isEmpty()) {
            inputFile = new File(StepperFields.DEFAULT_INPUT_FILENAME);
        }
        else {
            inputFile = new File(filepath);
        }

        //Check if the input file ends in .txt
        if(inputFile.getName().length()<=3 || !inputFile.getName().endsWith(".txt")) {
            throw new AssertionError("The input file must have a .txt extension");
        }

        try {
           FileWriter writer = new FileWriter(inputFile);
           if(doingDryRun) {
               return;
           }
           //Write an empty string to clear the file
           writer.write("");

           writer = new FileWriter(inputFile, true);

           //Write 50000 characters at a time
            int startIndex = 0;
            int endIndex = 50000;
            while(endIndex < contents.length()) {
                if(isCancelled()) {
                    return;
                }

                writer.write(contents.substring(startIndex, endIndex));

                startIndex += 50000;
                endIndex += 50000;
            }
            writer.write(contents.substring(startIndex));


           writer.close();
           System.out.println("WRITE FILE: Successfully wrote to the file.");
        }
        catch (IOException e) {
            throw new FileNotFoundException("An error occurred.");
        }


    }
}
