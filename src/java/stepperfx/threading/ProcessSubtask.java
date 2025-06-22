package stepperfx.threading;

import javafx.concurrent.Task;

/**
 * Performs part of the work of a ProcessTask
 */
public class ProcessSubtask extends Task<String> {

    /**
     * True if this worker is encrypting its text, false otherwise
     */
    final private boolean encrypting;

    /**
     * Holds the first nonzero number in the text. Must be on the interval [1, 9] or be Byte.MIN_VALUE.
     * If Byte.MIN_VALUE, the Worker will not perform operations on numbers.
     */
    final private byte firstNumber;

    /**
     * The String to process. Can't be null
     */
    private String inputPiece;

    /**
     * The key to process the input with. Can't be null
     */
    private final byte[][] key;

    /**
     * Allowed values: 0 if including punctuation, 1 if excluding spaces, 2 if alphabetic characters only
     */
    final private byte punctMode;

    /**
     * The segment number in the Boss's input string. Can't be negative
     */
    final private int startSegment;

    /**
     * True if using version 2 processes, false otherwise
     */
    final private boolean usingV2Process;

    /**
     * Creates a ParsingOperationsWorker and loads its fields.
     * @param input the substring it should process. Can't be null
     * @param key the key to process the substring with. Can't be null. No subarrays can be null. All indices must be on [0,25]
     * @param encrypting true if this Worker should encrypt its text, false otherwise
     * @param usingV2Process true if using enhanced (v2) process, false otherwise
     * @param punctMode 0 if including punctuation, 1 if excluding spaces, 2 if alphabetic characters only
     * @param textFirstNumber first nonzero number in the text. Must be on the interval [1, 9]
     *          or Integer.MIN_VALUE if not operating on numbers
     * @param startSegment where to start processing the input at. Must be at least 0
     */
    public ProcessSubtask(String input, byte[][] key, boolean encrypting, boolean usingV2Process,
                          byte punctMode, int textFirstNumber, int startSegment) {

        if(input==null || key==null) throw new AssertionError("Input text and key cannot be null");
        if(punctMode<0 || punctMode>2) throw new AssertionError("Punctuation mode must be on the interval [0,2]");
        if((textFirstNumber<=0 || textFirstNumber>9) && !(textFirstNumber==Integer.MIN_VALUE)) throw new AssertionError("First text number must be on the interval [1,9] or be Integer.MIN_VALUE");
        if(startSegment<0) throw new AssertionError("Start segment cannot be negative");

        this.inputPiece = input;

        //Make a deep copy of the key
        if(key[0]==null) throw new AssertionError("All indices in the key cannot be null");
        this.key = new byte[key.length][key[0].length];
        for(int a=0; a<key.length; a++) {
            if(key[a]==null) throw new AssertionError("All indices in the key cannot be null");

            for(int i=0; i<key[0].length; i++) {
                if(key[a][i]<0 || key[a][i]>25) throw new AssertionError("All indices in the key must be on the interval [0,25]");
                this.key[a][i] = key[a][i];
            }
        }

        this.encrypting = encrypting;
        this.firstNumber = (textFirstNumber==Integer.MIN_VALUE) ? Byte.MIN_VALUE : (byte)textFirstNumber;
        this.punctMode = punctMode;
        this.startSegment = startSegment;
        this.usingV2Process = usingV2Process;
    }


    /**
     * NOT IMPLEMENTED!
     */
    public String call() {
        throw new UnsupportedOperationException();
        /*
        To do:
        -convert all characters to lowercase, remove diacritics
        -remove non-alphabetic characters
        -do process
        -recombine non-alphabetic characters
        */
    }

}
