package stepperfx.threading;

import javafx.concurrent.Task;
import stepperfx.StepperFields;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
    private String textPiece;

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
     * Creates a ProcessSubtask and loads its fields.
     * @param input the substring it should process. Can't be null
     * @param key the key to process the substring with. Can't be null. No subarrays can be null. All indices must be on [0,25]
     * @param encrypting true if this Worker should encrypt its text, false otherwise
     * @param usingV2Process true if using enhanced (v2) process, false otherwise
     * @param punctMode 0 if the task removes all punctuation from the input, 1 if the task removes spaces from the input,
     *            2 if the task processes the input with all punctuation
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

        this.textPiece = input;

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
     * Processes the subtask's inputs, returning an output.
     * @return the output of processing
     */
    public String call() {

        //lowercase and remove diacritics
        textPiece = removeDiacritics(textPiece);

        //remove spaces (if specified)
        if(punctMode == 1) {
            textPiece = removeSpaces(textPiece);
        }

        //split non-alphas, then remove from the input
        char[] nonAlphas = findNonAlphaPositions(textPiece);
        textPiece = removeNonAlphas(textPiece);

        //do the numbers
        if(firstNumber != Byte.MIN_VALUE) {
            nonAlphas = encrypting ? encryptNumbers(nonAlphas, firstNumber) : decryptNumbers(nonAlphas, firstNumber);
        }

        //do the specified process
        if(usingV2Process) {
            textPiece = encrypting ? encrypt2(textPiece, key, startSegment) : decrypt2(textPiece, key, startSegment);
        }
        else {
            textPiece = encrypting ? encrypt(textPiece, key, startSegment) : decrypt(textPiece, key, startSegment);
        }

        //reinsert non-alphas
        textPiece = recombineNonAlphas(textPiece, nonAlphas, punctMode>=1);

        return textPiece;
    }




    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //METHODS




    /**
     * Returns the decrypted version of {@code text} using the given key beginning at index {@code startSegment}.<br><br>
     *
     * The result should be as if the entire text was decrypted, then only the substring starting at {@code startSegment} is
     * included in the final result.<br><br>
     *
     * Algorithm first implemented on February 26-29, 2024. By Chris P Bacon
     *
     * @param text text to decrypt. Must contain all lowercase English ASCII characters. Can't be null
     * @param key key to decrypt with. Can't be null. All indices must be on [0,25]
     * @param startSegment index to start decrypting from. Must be non-negative
     * @return decrypted version of text
     */
    private String decrypt(String text, byte[][] key, int startSegment) {
        //Enforce preconditions

        //Check that both inputs are not null
        if(text == null || key == null) {
            throw new AssertionError("Text and key cannot be null");
        }
        for(byte[] k : key) {
            if(k==null) {
                throw new AssertionError("No index in the key can be null");
            }
        }

        //Check text contents: all alphabetic lowercase ASCII characters (done during decr. process)

        //Check key contents: all indices on [0,25]
        for(int a=0; a<key.length; a++) {
            for(int i=0; i<key[a].length; i++) {
                if(key[a][i]<0 || key[a][i]>25) {
                    throw new AssertionError("All key indices must be on the interval [0,25]");
                }
            }
        }

        //Check start index is non-negative
        if(startSegment < 0) {
            throw new AssertionError("Starting index must be non-negative");
        }


        // ////////////////////////

        //Configure block positions
        byte[] keyBlockPositions=setKeyBlockPositions((long)startSegment * StepperFields.BLOCK_LENGTH + text.length());
        StringBuilder output = new StringBuilder(text.length());

        int currentChar=0;

        byte[] currentKeyBlockPositions=new byte[StepperFields.BLOCK_COUNT];
        for(int s=0; s<currentKeyBlockPositions.length; s++) {
            currentKeyBlockPositions[s]=keyBlockPositions[s];
        }

        for(int m = 0; m<(text.length() % StepperFields.BLOCK_LENGTH); m++) {
            for(int a=0; a<currentKeyBlockPositions.length; a++) {
                currentKeyBlockPositions[a]++;
                if(currentKeyBlockPositions[a] >= StepperFields.BLOCK_LENGTH) {
                    currentKeyBlockPositions[a]=0;
                }
            }
        }

        for(int t = text.length()-1; t>=text.length()-(text.length() % StepperFields.BLOCK_LENGTH); t--) {
            if(isCancelled()) {
                return "";
            }

            for(int d=0; d<currentKeyBlockPositions.length; d++) {
                currentKeyBlockPositions[d]--;
                if(currentKeyBlockPositions[d] < 0) {
                    currentKeyBlockPositions[d] = StepperFields.BLOCK_LENGTH-1;
                }
            }

            if(!(text.charAt(t)>=97 && text.charAt(t)<=122)) {
                throw new IllegalArgumentException("Text must contain all lowercase English ASCII characters");
            }

            currentChar=text.charAt(t) - 97;

            for(int k=currentKeyBlockPositions.length-1; k>=0; k--) {
                currentChar = (currentChar - key[k][currentKeyBlockPositions[k]]) % 26;
                if(currentChar < 0) {
                    currentChar += 26;
                }
            }

            output.append((char)(currentChar + 97));

        }

        for(int seg = text.length()-(text.length() % StepperFields.BLOCK_LENGTH)-1; seg >= 0; seg -= StepperFields.BLOCK_LENGTH) {
            if(isCancelled()) {
                return "";
            }

            keyBlockPositions[0]--;

            for(int m=0; m<keyBlockPositions.length-1; m++) {

                if(keyBlockPositions[m]<0) {
                    for(int r=0; r<=m; r++) {
                        keyBlockPositions[m]= StepperFields.BLOCK_LENGTH-1;
                    }
                    keyBlockPositions[m+1]--;

                }
            }

            for(int s=0; s<currentKeyBlockPositions.length; s++) {
                currentKeyBlockPositions[s]=keyBlockPositions[s];
            }

            for(int t = seg; t>seg - StepperFields.BLOCK_LENGTH; t--) {

                for(int d=0; d<currentKeyBlockPositions.length; d++) {
                    currentKeyBlockPositions[d]--;
                    if(currentKeyBlockPositions[d] < 0) {
                        currentKeyBlockPositions[d]= StepperFields.BLOCK_LENGTH-1;
                    }
                }

                if(!(text.charAt(t)>=97 && text.charAt(t)<=122)) {
                    throw new IllegalArgumentException("Text must contain all lowercase English ASCII characters");
                }

                currentChar=(int)text.charAt(t) - 97;

                for(int k=currentKeyBlockPositions.length-1; k>=0; k--) {

                    currentChar = (currentChar - key[k][currentKeyBlockPositions[k]]) % 26;
                    if(currentChar < 0) {
                        currentChar += 26;
                    }

                }

                output.append((char)(currentChar + 97));

            }
        }

        //Reverse the output (the decryption process will make the text turn out backwards)
        output.reverse();

        return output.toString();
    }


    /**
     * Returns the Version 2 decrypted version of {@code text} using the given key.
     * Operations start after {@code startingSegment} segments have been decrypted.<br><br>
     *
     * Algorithm first implemented on February 26-29, 2024. Enhanced encryption finished on July 18, 2024. By Chris P Bacon
     *
     * @param text text to decrypt. Must contain all lowercase English ASCII characters. Can't be null
     * @param key key to decrypt with. Can't be null. All indices must be on [0,25]
     * @param startingSegment index to start decrypting from. Must be non-negative
     * @return decrypted version of text
     */
    private String decrypt2(String text, byte[][] key, int startingSegment) {
        //Enforce preconditions

        //Check that both inputs are not null
        if(text == null || key == null) {
            throw new AssertionError("Text and key cannot be null");
        }
        for(byte[] k : key) {
            if(k==null) {
                throw new AssertionError("No index in the key can be null");
            }
        }

        //Check text contents: all alphabetic lowercase ASCII characters
        for(int v=0; v<text.length(); v++) {
            if(!(text.charAt(v)>=97 && text.charAt(v)<=122)) {
                throw new AssertionError("Text must contain all lowercase English ASCII characters");
            }
        }

        //Check key contents: all indices on [0,25]
        for(byte[] block : key) {
            for(byte index : block) {
                if (index < 0 || index > 25) {
                    throw new AssertionError("All key indices must be on the interval [0,25]");
                }
            }
        }

        //Check start index is non-negative
        if(startingSegment < 0) {
            throw new AssertionError("Starting segment number must be non-negative");
        }


        // ////////////////////////

        //Configure positions
        byte[] keyBlockBasePositions=initializeKeyBlockPositions(startingSegment + text.length()/StepperFields.BLOCK_LENGTH);

        StringBuilder output = new StringBuilder(text.length());

        int currentChar=0;
        int currentBlock = (startingSegment + text.length()/StepperFields.BLOCK_LENGTH);

        byte[] keyBlockReadPositions=new byte[StepperFields.BLOCK_COUNT];
        System.arraycopy(keyBlockBasePositions, 0, keyBlockReadPositions, 0, keyBlockReadPositions.length);

        for(int m = 0; m<(text.length() % StepperFields.BLOCK_LENGTH); m++) {
            for(int a=0; a<keyBlockReadPositions.length; a++) {
                keyBlockReadPositions[a]++;
                if(keyBlockReadPositions[a] >= StepperFields.BLOCK_LENGTH) {
                    keyBlockReadPositions[a]=0;
                }
            }
        }

        if(isCancelled()) {
            return "";
        }

        for(int t = text.length()-1; t>=text.length()-(text.length() % StepperFields.BLOCK_LENGTH); t--) {

            for(int d=0; d<keyBlockReadPositions.length; d++) {
                keyBlockReadPositions[d] -= 1;
                if(keyBlockReadPositions[d] < 0) {
                    keyBlockReadPositions[d] = StepperFields.BLOCK_LENGTH-1;
                }
            }

            currentChar=text.charAt(t) - 97;
            for(int k=keyBlockReadPositions.length-1; k>=0; k--) {
                currentChar = (currentChar - key[k][keyBlockReadPositions[k]]) % 26;
                if(currentChar < 0) {
                    currentChar += 26;
                }
            }

            output.append((char)(currentChar+97));

        }


        for(int seg = text.length()-(text.length() % StepperFields.BLOCK_LENGTH)-1; seg>=0; seg-=StepperFields.BLOCK_LENGTH) {
            if(isCancelled()) {
                return "";
            }

            currentBlock--;
            if((currentBlock+1) % StepperFields.BLOCK_LENGTH==0) {
                keyBlockBasePositions = setKeyBlockPositions(currentBlock);
            }


            for(int m=0; m<keyBlockBasePositions.length; m++) {
                keyBlockBasePositions[m] -= StepperFields.getKeyBlockIncrementIndex(m);

                if(keyBlockBasePositions[m]<0) {
                    keyBlockBasePositions[m] += StepperFields.BLOCK_LENGTH;
                }
            }

            System.arraycopy(keyBlockBasePositions, 0, keyBlockReadPositions, 0, keyBlockReadPositions.length);

            for(int t = seg; t > seg - StepperFields.BLOCK_LENGTH; t--) {

                for(int d=0; d<keyBlockReadPositions.length; d++) {
                    keyBlockReadPositions[d]--;
                    if(keyBlockReadPositions[d] < 0) {
                        keyBlockReadPositions[d]= StepperFields.BLOCK_LENGTH-1;
                    }
                }

                currentChar=(int)text.charAt(t) - 97;

                for(int k=keyBlockReadPositions.length-1; k>=0; k--) {

                    currentChar = (currentChar - key[k][keyBlockReadPositions[k]]) % 26;
                    if(currentChar < 0) {
                        currentChar += 26;
                    }

                }

                output.append((char)(currentChar+97));

            }
        }

        output.reverse();

        return output.toString();
    }



    /**
     * Returns a copy of {@code input} with its numbers decrypted using {@code key}.<br><br>
     *
     * Any non-number is unchanged in the output.
     * @param input input array containing some numbers
     * @param key key to decrypt with. Must be on the interval [1,9]
     * @return copy of {@code input} with numbers decrypted
     */
    private char[] decryptNumbers(char[] input, byte key) {
        if(input==null) {
            throw new AssertionError("Input cannot be null");
        }
        if(key<=0 || key>9) throw new AssertionError("Key must be on the interval [1,9]");

        char[] output = new char[input.length];
        for(int i=0; i<input.length; i++) {
            if(!((int)input[i]>=48 && (int)input[i]<=57)) {
                output[i] = input[i];
            }
            else {
                int newChar = (int)input[i] - 48;
                newChar = (newChar - key) % 10;
                if(newChar < 0) {
                    newChar += 10;
                }
                output[i] = (char)(newChar + 48);
            }
        }

        return output;
    }



    /**
     * Returns the encrypted version of {@code text}, using {@code inputKey} as the key.
     * Encryption starts at index {@code startSegment}. <br><br>
     *
     * The result should be as if the entire text was encrypted, then only the substring starting at {@code startSegment} is
     * included in the final result.<br><br>
     *
     * Algorithm first implemented on February 26-29, 2024. By Chris P Bacon
     *
     * @param text text to encrypt. Must contain all lowercase English ASCII characters. Can't be null
     * @param key key to encrypt with. Can't be null. All indices must be on [0,25]
     * @param startSegment index to start encrypting from. Must be non-negative
     * @return encrypted version of text
     */
    private String encrypt(String text, byte[][] key, int startSegment) {
        //Enforce preconditions

        //Check that both inputs are not null
        if(text == null || key == null) {
            throw new AssertionError("Text and key cannot be null");
        }
        for(byte[] k : key) {
            if(k==null) {
                throw new AssertionError("No index in the key can be null");
            }
        }

        //Check text contents: all alphabetic lowercase ASCII characters
        for(int v=0; v<text.length(); v++) {
            if(!(text.charAt(v)>=97 && text.charAt(v)<=122)) {
                throw new AssertionError("Text must contain all lowercase English ASCII characters");
            }
        }

        //Check key contents: all indices on [0,25]
        for(int a=0; a<key.length; a++) {
            for(int i=0; i<key[a].length; i++) {
                if(key[a][i]<0 || key[a][i]>25) {
                    throw new AssertionError("All key indices must be on the interval [0,25]");
                }
            }
        }

        //Check start index is non-negative
        if(startSegment < 0) {
            throw new AssertionError("Starting segment must be non-negative");
        }


        // ////////////////////////
        //Start the process

        byte[] keyBlockPositions = setKeyBlockPositions((long)startSegment * (long)StepperFields.BLOCK_LENGTH);
        byte[] currentKeyBlockPositions = new byte[StepperFields.BLOCK_COUNT];
        for(int s=0; s<currentKeyBlockPositions.length; s++) {
            currentKeyBlockPositions[s] = keyBlockPositions[s];
        }

        StringBuilder output = new StringBuilder(text.length());
        int currentChar=0;

        for(int seg = 0; seg <= text.length()- StepperFields.BLOCK_LENGTH; seg += StepperFields.BLOCK_LENGTH) {
            if(isCancelled()) {
                return "";
            }

            for(int pos=0; pos<currentKeyBlockPositions.length; pos++) {
                currentKeyBlockPositions[pos] = keyBlockPositions[pos];
            }

            for(int t = seg; t<(seg + StepperFields.BLOCK_LENGTH); t++) {

                currentChar=(int)text.charAt(t) - 97;

                for(int k=0; k<currentKeyBlockPositions.length; k++) {
                    currentChar = (currentChar + key[k][currentKeyBlockPositions[k]]) % 26;
                }

                output.append((char)(currentChar+97));

                for(int a=0; a<currentKeyBlockPositions.length; a++) {
                    currentKeyBlockPositions[a]++;
                    if(currentKeyBlockPositions[a] >= StepperFields.BLOCK_LENGTH) {
                        currentKeyBlockPositions[a]=0;
                    }
                }
            }

            keyBlockPositions[0]++;

            for(int m=0; m<keyBlockPositions.length-1; m++) {

                if(keyBlockPositions[m] >= StepperFields.BLOCK_LENGTH) {

                    for(int r=0; r<=m; r++) {
                        keyBlockPositions[r]=0;
                    }
                    keyBlockPositions[m+1]++;
                }

            }

            if(keyBlockPositions[keyBlockPositions.length-1] >= StepperFields.BLOCK_LENGTH) {
                Arrays.fill(keyBlockPositions, (byte)0);
            }

        }

        for(int pos=0; pos<currentKeyBlockPositions.length; pos++) {
            currentKeyBlockPositions[pos] = keyBlockPositions[pos];
        }
        if(isCancelled()) {
            return "";
        }

        for(int t = text.length()-(text.length() % StepperFields.BLOCK_LENGTH); t<text.length(); t++) {

            currentChar=(int)text.charAt(t) - 97;

            for(int k=0; k<currentKeyBlockPositions.length; k++) {
                currentChar = (currentChar + key[k][currentKeyBlockPositions[k]]) % 26;
            }

            output.append((char)(currentChar+97));

            for(int a=0; a<currentKeyBlockPositions.length; a++) {
                currentKeyBlockPositions[a]++;
                if(currentKeyBlockPositions[a] >= StepperFields.BLOCK_LENGTH) {
                    currentKeyBlockPositions[a]=0;
                }
            }
        }

        return output.toString();
    }


    /**
     * Returns the Version 2 encrypted version of {@code text}, using {@code inputKey} as the key.
     * Encryption starts after {@code startingSegment} segments. <br><br>
     *
     * The result should be as if the entire text was encrypted, then only the substring starting after {@code startingSegment} is
     * in the final result.<br><br>
     *
     * Algorithm first implemented on February 26-29, 2024. Enhanced encryption finished on July 18, 2024. By Chris P Bacon
     *
     * @param text text to encrypt. Must contain all lowercase English ASCII characters. Can't be null
     * @param key key to encrypt with. Can't be null. All indices must be on [0,25]
     * @param startingSegment index to start encrypting from. Must be non-negative
     * @return encrypted version of text
     */
    private String encrypt2(String text, byte[][] key, int startingSegment) {
        //Enforce preconditions

        //Check that both inputs are not null
        if(text == null || key == null) {
            throw new AssertionError("Text and key cannot be null");
        }
        for(byte[] k : key) {
            if(k==null) {
                throw new AssertionError("No index in the key can be null");
            }
        }

        //Check text contents: all alphabetic lowercase ASCII characters
        for(int v=0; v<text.length(); v++) {
            if(!(text.charAt(v)>=97 && text.charAt(v)<=122)) {
                throw new AssertionError("Text must contain all lowercase English ASCII characters");
            }
        }

        //Check key contents: all indices on [0,25]
        for(byte[] block : key) {
            for(byte index : block) {
                if (index < 0 || index > 25) {
                    throw new AssertionError("All key indices must be on the interval [0,25]");
                }
            }
        }

        //Check start index is non-negative
        if(startingSegment < 0) {
            throw new AssertionError("Starting segment number must be non-negative");
        }


        // ////////////////////////
        //Start the process

        byte[] keyBlockBasePositions = initializeKeyBlockPositions(startingSegment);
        byte[] keyBlockReadPositions = new byte[StepperFields.BLOCK_COUNT];
        System.arraycopy(keyBlockBasePositions, 0, keyBlockReadPositions, 0, keyBlockReadPositions.length);


        StringBuilder output = new StringBuilder(text.length());
        int currentChar=0;
        int blocksEncrypted = startingSegment;

        for(int seg = 0; seg <= (text.length() - StepperFields.BLOCK_LENGTH); seg += StepperFields.BLOCK_LENGTH) {
            if(isCancelled()) {
                return "";
            }

            System.arraycopy(keyBlockBasePositions, 0, keyBlockReadPositions, 0, keyBlockReadPositions.length);

            for(int t = seg; t<(seg + StepperFields.BLOCK_LENGTH); t++) {

                currentChar=(int)text.charAt(t) - 97;

                for(int k=0; k<keyBlockReadPositions.length; k++) {
                    currentChar = (currentChar + key[k][keyBlockReadPositions[k]]) % 26;
                }

                output.append((char)(currentChar+97));

                for(int a=0; a<keyBlockReadPositions.length; a++) {
                    keyBlockReadPositions[a]++;
                    if(keyBlockReadPositions[a] >= StepperFields.BLOCK_LENGTH) {
                        keyBlockReadPositions[a]=0;
                    }
                }
            }

            for(int r=0; r<keyBlockBasePositions.length; r++) {
                keyBlockBasePositions[r] = (byte) ((keyBlockBasePositions[r] + StepperFields.getKeyBlockIncrementIndex(r)) % StepperFields.BLOCK_LENGTH);
            }

            if((blocksEncrypted+1) % StepperFields.BLOCK_LENGTH == 0) {
                keyBlockBasePositions = setKeyBlockPositions(blocksEncrypted+2);
            }

            blocksEncrypted++;
        }

        System.arraycopy(keyBlockBasePositions, 0, keyBlockReadPositions, 0, keyBlockReadPositions.length);

        if(isCancelled()) {
            return "";
        }

        for(int t = text.length()-(text.length() % StepperFields.BLOCK_LENGTH); t<text.length(); t++) {

            currentChar=(int)text.charAt(t) - 97;

            for(int k=0; k<keyBlockReadPositions.length; k++) {
                currentChar = (currentChar + key[k][keyBlockReadPositions[k]]) % 26;
            }

            output.append((char)(currentChar+97));

            for(int a=0; a<keyBlockReadPositions.length; a++) {
                keyBlockReadPositions[a]++;
                if(keyBlockReadPositions[a] >= StepperFields.BLOCK_LENGTH) {
                    keyBlockReadPositions[a]=0;
                }
            }
        }

        return output.toString();
    }


    /**
     * Returns a copy of {@code input}, but with numbers encrypted using {@code key}.<br><br>
     *
     * Any non-number is unchanged in the output.
     * @param input the input text segment. Cannot be null
     * @param key key to encrypt with. Must be on the interval [1, 9]
     * @return copy of input, but with numbers encrypted
     */
    private char[] encryptNumbers(char[] input, byte key) {
        if(input == null) throw new AssertionError("Input cannot be null");
        if(key<=0 || key>9) throw new AssertionError("Key must be on the interval [1,9]");

        char[] output = new char[input.length];
        for(int i=0; i<input.length; i++) {
            if(input[i]<48 || input[i]>57) {
                output[i] = input[i];
            }
            else {
                int newChar = (int)input[i] - 48;
                newChar = (newChar + (int)key) % 10;
                output[i] = (char)(newChar + 48);
            }
        }
        return output;
    }


    /**
     * Returns an array containing the positions of all non-alphabetic characters in {@code text}.
     * If there's an alphabetic character, puts a 0 in the output index.<br><br>
     *
     * Alphabetic characters are ASCII characters that belong to the English alphabet.<br>
     * Uppercase and lowercase letters are both treated as letters.<br><br>
     *
     * Example: if the text is "A1b2c3", the output, expressed as ints, should be {0, 48, 0, 49, 0, 50}.
     * Since indices 0, 2, and 4 in the input are alphabetic characters, the corresponding indices in the output is 0.
     * Indices 1, 3, and 5 hold the corresponding ASCII value in the corresponding output indices
     *
     * @param text text to find non-alphabetic characters in. Cannot be null
     * @return char array containing locations of non-alphabetic characters. Returns {@code {(char)0}} if the Worker is cancelled.
     */
    private char[] findNonAlphaPositions(String text) {
        if(text==null) {
            throw new AssertionError("Text cannot be null");
        }

        char[] nonAlphas = new char[text.length()];

        for(int i=0; i<text.length(); i++) {
            if(isCancelled()) {
                return new char[(char)0];
            }

            if((int)text.charAt(i)<65
                    || ((int)text.charAt(i)>90 && (int)text.charAt(i)<97)
                    || (int)text.charAt(i)>122) {

                nonAlphas[i] = text.charAt(i);
            }
            else {
                nonAlphas[i]=(char)0;
            }
        }
      /*
      for(int i=0; i<nonAlphas.length; i++) {
        System.out.print((int)nonAlphas[i] + " ");
      }
      System.out.println();
      */
        return nonAlphas;
    }


    /**
     * Returns an array of bytes representing the key block positions at the end of encryption,
     * if the input had {@code segments} segments<br><br>
     *
     * {@code segments} should equal the number of segments before the starting position.<br>
     * Example: if {@code segments}  equals 4, the output would be the block positions just after encrypting 4 segments.<br><br>
     *
     * Helper to the operation functions.
     *
     * @param segments number of blocks encrypted so far, non-negative
     * @return key block positions after encrypting {@code segments} segments
     */
    private byte[] initializeKeyBlockPositions(long segments) {
        assert segments >= 0;

        byte[] output = setKeyBlockPositions(segments);

        //Simulate moving through the remainder of the blocks
        for(int b = 0; b < segments%StepperFields.BLOCK_LENGTH; b++) {
            //Increment each index of the output
            for(int i=0; i<output.length; i++) {
                output[i] = (byte) ((output[i] + StepperFields.getKeyBlockIncrementIndex(i)) % StepperFields.BLOCK_LENGTH);
            }
        }

        return output;
    }


    /**
     * Returns {@code text}, with all characters from {@code nonAlphas} reinserted in their places.<br><br>
     *
     * -{@code text} represents an output without non-alphabetic characters.<br>
     *
     * -{@code nonAlphas} contains the Unicode values of characters that were removed from {@code text} at every index
     * containing a positive number<br>
     *
     * -If {@code reinsertPunctuation} is true, the text returned should contain all characters from nonAlphas
     * reinserted in their original places.
     * If not, the text returned should contain only alphanumeric characters.<br><br>
     *
     * Example: {@code text} is "abcdefg", {@code nonAlphas} is [0,0,0,32,0,0,0,48,49,50].<br>
     * {@code nonAlphas}'s positive entries are at positions where, in the original text, there were non-alphabetic
     * characters that were removed. A space (Unicode: 32) was at index 3 in the original text. Numbers '1', '2', and '3'
     * (Unicode: 48, 49, 50) were at indices 7, 8, and 9.<br>
     * Given the text, the non-alpha positions, and a {@code reinsertingPunctuation} value of true, the output would be
     * "abc defg123".<br>
     *
     * If {@code reinsertPunctuation} was false, the output would be "abcdefg123".<br><br>
     *
     * Undoes the separation of characters in {@code removeNonAlphas}.
     *
     * @param text input text without non-alphabetic characters
     * @param nonAlphas array containing locations of non-alphabetic characters
     * @param reinsertingPunctuation whether to include punctuation in the output;
     *                            if false, the function reinserts numbers only
     * @return version of text with non-alphabetic characters in their places
     */
    private String recombineNonAlphas(String text, char[] nonAlphas, boolean reinsertingPunctuation) {
        if(text==null) {
            throw new AssertionError("Text cannot be null");
        }
        if(nonAlphas==null) {
            throw new AssertionError("Non-alphas cannot be null");
        }

        for(int v=0; v<text.length(); v++) {
            if(text.charAt(v)<97 || text.charAt(v)>122) {
                throw new AssertionError("All indices in the text must be English lowercase letters");
            }
        }

        //make defensive copy of nonAlphasIn
        char[] nonAlphasWorking = new char[nonAlphas.length];
        for(int t=0; t<nonAlphasWorking.length; t++) {
            if(isCancelled()) {
                return "";
            }

            nonAlphasWorking[t]=nonAlphas[t];
        }

        StringBuilder output = new StringBuilder(text.length());
        int textIndex=0;
        int nonAlphasIndex=0;
        int outputLen=text.length();

        if(text.length() > nonAlphasWorking.length) {
            System.err.println("WARNING: does 'symbols' have blank spaces accounted for?");
        }


        //all characters from [0..nonAlphasIndex) in text should be already processed
        //outputLen should equal the input's length, plus the number of symbols added to the output
        while(nonAlphasIndex < outputLen) {
            if(isCancelled()) {
                return "";
            }

            //If there's a symbol in the current index
            if(nonAlphasWorking[nonAlphasIndex] > 0) {
                //If not an apostrophe (ignore the compiler warning)
                if(!(nonAlphasWorking[nonAlphasIndex]==(char)39 || nonAlphasWorking[nonAlphasIndex]==(char)96 || nonAlphasWorking[nonAlphasIndex]=='’' || nonAlphasWorking[nonAlphasIndex]=='`')) {

                    if( (reinsertingPunctuation) ||
                            (nonAlphasWorking[nonAlphasIndex]>=48 && nonAlphasWorking[nonAlphasIndex]<=57) ) {
                        //add to output
                        output.append((char)nonAlphasWorking[nonAlphasIndex]);
                    }

                    //empty the symbol
                    nonAlphasWorking[nonAlphasIndex]=0;
                }

                outputLen++;
            }

            //If there's no symbol
            else {
                output.append(text.charAt(textIndex));
                textIndex++;
            }

            nonAlphasIndex++;
        }


        //Add the rest of the symbols

        //all characters from [0..nonAlphasIndex) in text should be already processed
        while(nonAlphasIndex < nonAlphasWorking.length) {
            if(isCancelled()) {
                return "";
            }

            if((nonAlphasWorking[nonAlphasIndex]>0 && reinsertingPunctuation)
                    || (nonAlphasWorking[nonAlphasIndex])>=48 && nonAlphasWorking[nonAlphasIndex]<=57) {
                output.append(nonAlphasWorking[nonAlphasIndex]);
            }
            nonAlphasIndex++;
        }


        return output.toString();
    }


    /**
     * Returns a lowercased version of the input without accent marks or letter variants.
     *
     * @param input String to remove diacritics from
     * @return copy of input without diacritics
     */
    private String removeDiacritics(String input) {

        //These are the characters to remove. Corresponding indices in `replacementChars` are their replacements
        String[] accentedChars={"àáâãäå", "ç", "ð", "èéëêœæ", "ìíîï", "òóôõöø", "ǹńñň",
                "ß", "ùúûü", "ýÿ", "⁰₀", "¹₁", "²₂", "³₃", "⁴₄", "⁵₅", "⁶₆", "⁷₇", "⁸₈", "⁹₉", "—"};
        char[] replacementChars={'a', 'c', 'd', 'e', 'i', 'o', 'n', 's', 'u', 'y',  '0', '1',
                '2', '3', '4', '5', '6', '7', '8', '9', '-'};

        Map<Character, Character> charMap = new HashMap<>(accentedChars.length);

        //load the map
        for(int a=0; a<accentedChars.length; a++) {
            //assign each character in the current accented char string as a key to map the corresponding replacement char
            for(int r=0; r<accentedChars[a].length(); r++) {
                charMap.put(accentedChars[a].charAt(r), replacementChars[a]);
            }
        }

        accentedChars = null;
        replacementChars = null;
        StringBuilder output = new StringBuilder(input.length());

        //build the output
        char currentChar = (char)0;
        for(int i=0; i<input.length(); i++) {
            //lowercase the character
            currentChar = Character.toLowerCase(input.charAt(i));

            //check if the character is in the map: if so, convert it
            if(charMap.containsKey(currentChar)) {
                currentChar = charMap.get(currentChar);
            }

            //append converted char to output
            output.append(currentChar);

            if(isCancelled()) {
                return "";
            }
        }

        return output.toString();
    }

    /**
     * Returns a version of the input without accent marks or letter variants.<br><br>
     *
     * FOR UNIT TESTING ONLY!
     *
     * @param input String to remove diacritics from
     * @return copy of input without diacritics
     */
    public String removeDiacritics_Testing(String input) {
        return removeDiacritics(input);
    }



    /**
     * Returns a version of {@code text} without non-alphabetic characters.
     * The text returned is converted to lowercase.<br><br>
     *
     * WARNING! Not to be confused with removeNonAlnums! This method removes all non-letters, including numbers!
     *
     * @param text original input. Can't be null
     * @return lowercased text without non-alphabetic characters
     */
    private String removeNonAlphas(String text) {
        if(text==null) throw new AssertionError("Text can't be null");

        if(isCancelled()) {
            return "";
        }

        text=text.toLowerCase();

        StringBuilder output = new StringBuilder(text.length());
        for(int i=0; i<text.length(); i++) {
            if(isCancelled()) {
                return "";
            }

            if((int)text.charAt(i)>=97 && (int)text.charAt(i)<=122) {
                output.append(text.charAt(i));
            }
        }

        return output.toString();
    }



    /**
     * Returns a copy of {@code input}, but with spaces removed.<br><br>
     *
     * Any space between two letters is to be removed. All other spaces are to remain in the output.
     *
     * @param input text to remove spaces from. Cannot be null
     * @return copy of input without spaces
     */
    private String removeSpaces(String input) {
        if(input==null) throw new AssertionError("Input cannot be null");

        StringBuilder output = new StringBuilder(input.length());
        output.append(input.charAt(0));

        for(int i=1; i<input.length()-1; i++) {

            if(! (input.charAt(i)==' '
                    && Character.isAlphabetic(input.charAt(i-1)) && Character.isAlphabetic(input.charAt(i+1)))) {
                output.append(input.charAt(i));
            }

        }

        output.append(input.charAt( input.length()-1 ));

        return output.toString();
    }



    /**
     * Returns the key block positions for the given text length.<br><br>
     *
     * Important note: this method uses text length, not the number of blocks that are in the text.<br><br>
     *
     * Helper to the operation functions.
     *
     * @param textLength length of text. Must be at least 0
     * @return key block positions
     */
    private byte[] setKeyBlockPositions(long textLength) {
        //Check text length: must be non-negative
        if(textLength < 0) throw new AssertionError("Text length cannot be negative");

        //Set the output array, assign all empty space to 0
        byte[] result = new byte[StepperFields.BLOCK_COUNT];

        long quotient=textLength;
        double decimalPortion=0;

        //Eliminate block spill-overs.
        quotient = quotient % ((long)Math.pow(StepperFields.BLOCK_LENGTH, StepperFields.BLOCK_COUNT));

        //Divide quotient and take only the portion to the left of the decimal point
        quotient = quotient / StepperFields.BLOCK_LENGTH;


        //Much like converting a base-10 number to a base-BLOCK_LENGTH number
        //The lowest value digits end up on the right side.
        for(int i=result.length-1; i>=0; i--) {

            //Divide quotient and take only the portion to the right of the decimal point
            decimalPortion = (double)quotient / StepperFields.BLOCK_LENGTH - quotient / StepperFields.BLOCK_LENGTH;
            //Divide quotient and keep only the portion to the left of the decimal point
            quotient = quotient / (long) StepperFields.BLOCK_LENGTH;


            //Convert the decimal portion to a digit and add to the result
            result[i] = (byte)(Math.round(decimalPortion * StepperFields.BLOCK_LENGTH));

            if(quotient <= 0) {
                break;
            }

        }

        //Reverse the output
        byte[] output = new byte[result.length];
        for(int i=0; i<result.length; i++) {
            output[i] = result[result.length - i - 1];
        }

        return output;
    }

    /**
     * Returns the key block positions for the given text length.<br><br>
     *
     * Important note: this method uses text length, not the number of blocks that are in the text.<br><br>
     *
     * FOR TESTING PURPOSES ONLY!
     *
     * @param textLength length of text. Must be at least 0
     * @return key block positions
     */
    public byte[] setKeyBlockPositions_Testing(long textLength) {
        return setKeyBlockPositions(textLength);
    }

}
