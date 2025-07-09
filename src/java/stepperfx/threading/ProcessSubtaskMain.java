package stepperfx.threading;

import javafx.concurrent.Task;
import stepperfx.StepperFields;

import java.util.Arrays;

/**
 * Performs part of the work of a ProcessTask
 */
public class ProcessSubtaskMain extends Task<String> {

    /**
     * True if this worker is encrypting its text, false otherwise
     */
    final private boolean encrypting;

    /**
     * The String to process. Can't be null
     */
    private String textPiece;

    /**
     * The key to process the input with. Can't be null
     */
    final private byte[][] key;

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
     * Creates a new {@code ProcessSubtaskMain} and loads its fields.
     * @param textPiece the substring it should process. Can't be null
     * @param key the key to process the substring with. Can't be null. No subarrays can be null.
     *            All indices must be on the interval [0,25]
     * @param encrypting true if this Worker should encrypt its text, false otherwise
     * @param usingV2Process true if using enhanced (v2) process, false otherwise
     * @param punctMode 0 if the task removes all punctuation from the input, 1 if the task removes spaces from the input,
     *            2 if the task processes the input with all punctuation
     * @param startSegment text segment to start processing the input. Cannot be negative
     */
    public ProcessSubtaskMain(String textPiece, byte[][] key, boolean encrypting, boolean usingV2Process,
                              byte punctMode, int startSegment) {

        if(textPiece==null) throw new AssertionError("Input text cannot be null");
        if(key==null) throw new AssertionError("Key cannot be null");
        if(punctMode<0 || punctMode>2) throw new AssertionError("Punctuation mode must be on the interval [0,2]");
        if(startSegment<0) throw new AssertionError("Start segment cannot be negative");

        this.textPiece = textPiece;

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
        this.punctMode = punctMode;
        this.startSegment = startSegment;
        this.usingV2Process = usingV2Process;
    }


    /**
     * FOR METHOD UNIT TESTING ONLY! Creates a new {@code ProcessSubtaskMain}. Initializes fields against operation preconditions.
     */
    public ProcessSubtaskMain() {
        this.textPiece = null;
        this.key = null;
        this.encrypting = false;
        this.punctMode = -1;
        this.startSegment = -1;
        this.usingV2Process = false;
    }



    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////



    /**
     * Processes the subtask's inputs, returning an output.
     * @return the output of processing
     */
    public String call() {
        //Constructor check
        if(textPiece==null || key==null || punctMode<0 || punctMode>2 || startSegment<0) {
            throw new AssertionError("PROCESS SUBTASK MAIN- TESTING CONSTRUCTOR USED FOR OPERATIONS");
        }

        //remove spaces (if specified)
        if(encrypting && punctMode==1) {
            textPiece = removeSpaces(textPiece);
        }

        //split non-alphas, then remove from the input
        char[] nonAlphas = findNonAlphaPositions(textPiece);
        textPiece = removeNonAlphas(textPiece);


        //do the specified process
        if(usingV2Process) {
            textPiece = encrypting ? encrypt2(textPiece, key, startSegment) : decrypt2(textPiece, key, startSegment);
        }
        else {
            textPiece = encrypting ? encrypt(textPiece, key, startSegment) : decrypt(textPiece, key, startSegment);
        }

        //do the numbers
        nonAlphas = encrypting ? encryptNumbers(nonAlphas, key) : decryptNumbers(nonAlphas, key);

        //reinsert non-alphas
        textPiece = recombineNonAlphas(textPiece, nonAlphas, (!encrypting || punctMode>=1));

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
        for (byte[] bytes : key) {
            for (byte aByte : bytes) {
                if (aByte < 0 || aByte > 25) {
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
        byte[] keyBlockPositions=setKeyBlockPositions((long)startSegment * key[0].length + text.length(),
                key.length, key[0].length);
        StringBuilder output = new StringBuilder(text.length());

        int currentChar=0;

        byte[] currentKeyBlockPositions=new byte[key.length];
        System.arraycopy(keyBlockPositions, 0, currentKeyBlockPositions, 0, currentKeyBlockPositions.length);

        for(int m = 0; m<(text.length() % key[0].length); m++) {
            for(int a=0; a<currentKeyBlockPositions.length; a++) {
                currentKeyBlockPositions[a]++;
                if(currentKeyBlockPositions[a] >= key[0].length) {
                    currentKeyBlockPositions[a]=0;
                }
            }
        }

        for(int t = text.length()-1; t>=text.length()-(text.length() % key[0].length); t--) {
            if(isCancelled()) {
                return "";
            }

            for(int d=0; d<currentKeyBlockPositions.length; d++) {
                currentKeyBlockPositions[d]--;
                if(currentKeyBlockPositions[d] < 0) {
                    currentKeyBlockPositions[d] = (byte) (key[0].length - 1);
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

        for(int seg = text.length()-(text.length() % key[0].length)-1; seg >= 0; seg -= key[0].length) {
            if(isCancelled()) {
                return "";
            }

            keyBlockPositions[0]--;

            for(int m=0; m<keyBlockPositions.length-1; m++) {

                if(keyBlockPositions[m]<0) {
                    for(int r=0; r<=m; r++) {
                        keyBlockPositions[m]= (byte) (key[0].length - 1);
                    }
                    keyBlockPositions[m+1]--;

                }
            }

            System.arraycopy(keyBlockPositions, 0, currentKeyBlockPositions, 0, currentKeyBlockPositions.length);

            for(int t = seg; t>seg - key[0].length; t--) {

                for(int d=0; d<currentKeyBlockPositions.length; d++) {
                    currentKeyBlockPositions[d]--;
                    if(currentKeyBlockPositions[d] < 0) {
                        currentKeyBlockPositions[d ]= (byte) (key[0].length - 1);
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
        byte[] keyBlockBasePositions = initializeKeyBlockPositions(startingSegment + text.length() / key[0].length, 
                key.length, key[0].length);

        StringBuilder output = new StringBuilder(text.length());

        int currentChar=0;
        int currentBlock = (startingSegment + text.length() / key[0].length);

        byte[] keyBlockReadPositions=new byte[key.length];
        System.arraycopy(keyBlockBasePositions, 0, keyBlockReadPositions, 0, keyBlockReadPositions.length);

        for(int m = 0; m<(text.length() % key[0].length); m++) {
            for(int a=0; a<keyBlockReadPositions.length; a++) {
                keyBlockReadPositions[a]++;
                if(keyBlockReadPositions[a] >= key[0].length) {
                    keyBlockReadPositions[a]=0;
                }
            }
        }

        if(isCancelled()) {
            return "";
        }

        for(int t = text.length()-1; t>=text.length()-(text.length() % key[0].length); t--) {

            for(int d=0; d<keyBlockReadPositions.length; d++) {
                keyBlockReadPositions[d] -= 1;
                if(keyBlockReadPositions[d] < 0) {
                    keyBlockReadPositions[d] = (byte) (key[0].length - 1);
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


        for(int seg = text.length()-(text.length() % key[0].length)-1; seg>=0; seg-=key[0].length) {
            if(isCancelled()) {
                return "";
            }

            currentBlock--;
            if((currentBlock+1) % key[0].length==0) {
                keyBlockBasePositions = setKeyBlockPositions(currentBlock, key.length, key[0].length);
            }


            for(int m=0; m<keyBlockBasePositions.length; m++) {
                keyBlockBasePositions[m] -= StepperFields.getKeyBlockIncrementIndex(m);

                if(keyBlockBasePositions[m]<0) {
                    keyBlockBasePositions[m] += (byte) key[0].length;
                }
            }

            System.arraycopy(keyBlockBasePositions, 0, keyBlockReadPositions, 0, keyBlockReadPositions.length);

            for(int t = seg; t > seg - key[0].length; t--) {

                for(int d=0; d<keyBlockReadPositions.length; d++) {
                    keyBlockReadPositions[d]--;
                    if(keyBlockReadPositions[d] < 0) {
                        keyBlockReadPositions[d]= (byte) (key[0].length - 1);
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
     * @param key key to decrypt with. Cannot be null. All indices must be on the interval [0,25]
     * @return copy of {@code input} with numbers decrypted
     */
    private char[] decryptNumbers(char[] input, byte[][] key) {
        if(input==null) {
            throw new AssertionError("Input cannot be null");
        }
        if(key==null) {
            throw new AssertionError("Key cannot be null");
        }

        int decrKey = 0;
        for(byte[] block : key) {
            if(block==null) {
                throw new AssertionError("All blocks in the key cannot be null");
            }

            for(byte letter : block) {
                if(letter<0 || letter>25) {
                    throw new AssertionError("All indices in the key must be on the interval [0, 25]");
                }
                decrKey += letter;
            }
        }

        decrKey = decrKey % 26;

        char[] output = new char[input.length];
        for(int i=0; i<input.length; i++) {
            if(!((int)input[i]>=48 && (int)input[i]<=57)) {
                output[i] = input[i];
            }
            else {
                int newChar = (int)input[i] - 48;
                newChar = (newChar - (byte)decrKey) % 10;
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
        for (byte[] blocks : key) {
            for (byte aByte : blocks) {
                if (aByte < 0 || aByte > 25) {
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

        byte[] keyBlockPositions = setKeyBlockPositions((long)startSegment * (long)key[0].length,
                key.length, key[0].length);
        byte[] currentKeyBlockPositions = new byte[key.length];
        System.arraycopy(keyBlockPositions, 0, currentKeyBlockPositions, 0, currentKeyBlockPositions.length);

        StringBuilder output = new StringBuilder(text.length());
        int currentChar=0;

        for(int seg = 0; seg <= text.length()- key[0].length; seg += key[0].length) {
            if(isCancelled()) {
                return "";
            }

            System.arraycopy(keyBlockPositions, 0, currentKeyBlockPositions, 0, currentKeyBlockPositions.length);

            for(int t = seg; t<(seg + key[0].length); t++) {

                currentChar=(int)text.charAt(t) - 97;

                for(int k=0; k<currentKeyBlockPositions.length; k++) {
                    currentChar = (currentChar + key[k][currentKeyBlockPositions[k]]) % 26;
                }

                output.append((char)(currentChar+97));

                for(int a=0; a<currentKeyBlockPositions.length; a++) {
                    currentKeyBlockPositions[a]++;
                    if(currentKeyBlockPositions[a] >= key[0].length) {
                        currentKeyBlockPositions[a]=0;
                    }
                }
            }

            keyBlockPositions[0]++;

            for(int m=0; m<keyBlockPositions.length-1; m++) {

                if(keyBlockPositions[m] >= key[0].length) {

                    for(int r=0; r<=m; r++) {
                        keyBlockPositions[r]=0;
                    }
                    keyBlockPositions[m+1]++;
                }

            }

            if(keyBlockPositions[keyBlockPositions.length-1] >= key[0].length) {
                Arrays.fill(keyBlockPositions, (byte)0);
            }

        }

        System.arraycopy(keyBlockPositions, 0, currentKeyBlockPositions, 0, currentKeyBlockPositions.length);
        if(isCancelled()) {
            return "";
        }

        for(int t = text.length()-(text.length() % key[0].length); t<text.length(); t++) {

            currentChar=(int)text.charAt(t) - 97;

            for(int k=0; k<currentKeyBlockPositions.length; k++) {
                currentChar = (currentChar + key[k][currentKeyBlockPositions[k]]) % 26;
            }

            output.append((char)(currentChar+97));

            for(int a=0; a<currentKeyBlockPositions.length; a++) {
                currentKeyBlockPositions[a]++;
                if(currentKeyBlockPositions[a] >= key[0].length) {
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
        
        byte[] keyBlockBasePositions = initializeKeyBlockPositions(startingSegment, key.length, key[0].length);
        byte[] keyBlockReadPositions = new byte[key.length];
        System.arraycopy(keyBlockBasePositions, 0, keyBlockReadPositions, 0, keyBlockReadPositions.length);


        StringBuilder output = new StringBuilder(text.length());
        int currentChar=0;
        int blocksEncrypted = startingSegment;

        for(int seg = 0; seg <= (text.length() - key[0].length); seg += key[0].length) {
            if(isCancelled()) {
                return "";
            }

            System.arraycopy(keyBlockBasePositions, 0, keyBlockReadPositions, 0, keyBlockReadPositions.length);

            for(int t = seg; t<(seg + key[0].length); t++) {

                currentChar=(int)text.charAt(t) - 97;

                for(int k=0; k<keyBlockReadPositions.length; k++) {
                    currentChar = (currentChar + key[k][keyBlockReadPositions[k]]) % 26;
                }

                output.append((char)(currentChar+97));

                for(int a=0; a<keyBlockReadPositions.length; a++) {
                    keyBlockReadPositions[a]++;
                    if(keyBlockReadPositions[a] >= key[0].length) {
                        keyBlockReadPositions[a]=0;
                    }
                }
            }

            for(int r=0; r<keyBlockBasePositions.length; r++) {
                keyBlockBasePositions[r] = (byte) ((keyBlockBasePositions[r] + StepperFields.getKeyBlockIncrementIndex(r)) % key[0].length);
            }

            if((blocksEncrypted+1) % key[0].length == 0) {
                keyBlockBasePositions = setKeyBlockPositions(blocksEncrypted+2, key.length, key[0].length);
            }

            blocksEncrypted++;
        }

        System.arraycopy(keyBlockBasePositions, 0, keyBlockReadPositions, 0, keyBlockReadPositions.length);

        if(isCancelled()) {
            return "";
        }

        for(int t = text.length()-(text.length() % key[0].length); t<text.length(); t++) {

            currentChar=(int)text.charAt(t) - 97;

            for(int k=0; k<keyBlockReadPositions.length; k++) {
                currentChar = (currentChar + key[k][keyBlockReadPositions[k]]) % 26;
            }

            output.append((char)(currentChar+97));

            for(int a=0; a<keyBlockReadPositions.length; a++) {
                keyBlockReadPositions[a]++;
                if(keyBlockReadPositions[a] >= key[0].length) {
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
     * @param key key to encrypt with. Cannot be null. All indices must be on the interval [0, 25]
     * @return copy of input, but with numbers encrypted
     */
    private char[] encryptNumbers(char[] input, byte[][] key) {
        if(input == null) throw new AssertionError("Input cannot be null");
        if(key==null) {
            throw new AssertionError("Key cannot be null");
        }

        int decrKey = 0;
        for(byte[] block : key) {
            if(block==null) {
                throw new AssertionError("All blocks in the key cannot be null");
            }

            for(byte letter : block) {
                if(letter<0 || letter>25) {
                    throw new AssertionError("All indices in the key must be on the interval [0, 25]");
                }
                decrKey += letter;
            }
        }
        decrKey = decrKey % 26;

        char[] output = new char[input.length];
        for(int i=0; i<input.length; i++) {
            if(input[i]<48 || input[i]>57) {
                output[i] = input[i];
            }
            else {
                int newChar = (int)input[i] - 48;
                newChar = (newChar + (int)decrKey) % 10;
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
     * @param blockCount number of blocks in the key
     * @param blockLength length of each block in the key
     * @return key block positions after encrypting {@code segments} segments
     */
    private byte[] initializeKeyBlockPositions(long segments, int blockCount, int blockLength) {
        assert segments >= 0;

        byte[] output = setKeyBlockPositions(segments, blockCount, blockLength);

        //Simulate moving through the remainder of the blocks
        for(int b = 0; b < segments % blockLength; b++) {
            //Increment each index of the output
            for(int i=0; i<output.length; i++) {
                output[i] = (byte) ((output[i] + StepperFields.getKeyBlockIncrementIndex(i)) % blockLength);
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
     * Returns the unenhanced (v1) key block positions for the given text length,
     * number of blocks, and length of each block.<br><br>
     *
     * Important note: this method uses text length, not the number of blocks that are in the text.<br><br>
     *
     * Helper to the operation functions.
     *
     * @param textLength length of text. Must be at least 0
     * @param blockCount number of key blocks used. Must be positive
     * @param blockLength number of characters in each key block. Must be positive
     * @return key block positions for the given length, block count, and block length
     */
    private byte[] setKeyBlockPositions(long textLength, int blockCount, int blockLength) {
        //Check parameters
        if(textLength < 0) throw new AssertionError("Text length cannot be negative, instead received " + textLength);
        if(blockCount < 0) throw new AssertionError("Block count must be positive, instead received " + blockCount);
        if(blockLength < 0) throw new AssertionError("Block length must be positive, instead received " + blockLength);
        
        //Set the output array, assign all empty space to 0
        byte[] result = new byte[blockCount];

        long quotient=textLength;
        double decimalPortion=0;

        //Eliminate block spill-overs.
        quotient = quotient % ((long)Math.pow(blockLength, blockCount+1));

        //Divide quotient and take only the portion to the left of the decimal point
        quotient = quotient / blockLength;


        for(int i = result.length-1; i >= 0; i--) {

            //Divide quotient and take only the portion to the right of the decimal point
            decimalPortion = (double)quotient / blockLength - quotient / blockLength;
            //Divide quotient and keep only the portion to the left of the decimal point
            quotient = quotient / (long) blockLength;


            //Convert the decimal portion to a digit and add to the result
            result[i] = (byte)(Math.round(decimalPortion * blockLength));

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
     * Returns the unenhanced (v1) key block positions for the given text length,
     * number of blocks, and length of each block.<br><br>
     *
     * Important note: this method uses text length, not the number of blocks that are in the text.<br><br>
     *
     * FOR TESTING PURPOSES ONLY!
     *
     * @param textLength length of text. Must be at least 0
     * @param blockCount number of key blocks used
     * @param blockLength number of characters in each key block
     * @return key block positions for the given length, block count, and block length
     */
    public byte[] setKeyBlockPositions_Testing(long textLength, int blockCount, int blockLength) {
        return setKeyBlockPositions(textLength, blockCount, blockLength);
    }

}
