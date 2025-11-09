package stepperfx.threading;

import javafx.concurrent.Task;

import java.util.Arrays;

import static stepperfx.integration.StepperFields.getKeyBlockIncrementIndex;

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
    final private int punctMode;

    /**
     * The segment number in the Boss's input string. Can't be negative
     */
    final private int startSegment;

    /**
     * True if using version 2 processes, false otherwise
     */
    final private boolean usingV2Process;

    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS


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
                              int punctMode, int startSegment) {

        if(textPiece==null) throw new AssertionError("Input text cannot be null");
        if(key==null) throw new AssertionError("Key cannot be null");
        if(punctMode<0 || punctMode>2) throw new AssertionError("Punctuation mode must be on the interval [0,2]- instead received " + punctMode);
        if(startSegment<0) throw new AssertionError("Start segment cannot be negative- instead received " + startSegment);

        this.textPiece = textPiece;

        //Make a deep copy of the key
        if(key[0]==null) throw new AssertionError("Key array at index 0 cannot be null");
        this.key = new byte[key.length][key[0].length];
        for(int a=0; a<key.length; a++) {
            if(key[a]==null) throw new AssertionError("Key array at index " + a + " cannot be null");

            for(int i=0; i<key[0].length; i++) {
                if(key[a][i]<0 || key[a][i]>25) throw new AssertionError("Key value [" + a + "][" + i + "] must be on the interval [0,25]- instead received " + key[a][i]);
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
    // CALL


    /**
     * Processes the subtask's inputs, returning an output.
     * @return the output of processing
     */
    protected String call() {
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
        byte[] keyBlockBasePositions= initializeKeyBlockPositions((long)startSegment * key[0].length + text.length(),
                key.length, key[0].length);
        StringBuilder output = new StringBuilder(text.length());

        int currentChar=0;

        byte[] keyBlockReadPositions = new byte[key.length];
        System.arraycopy(keyBlockBasePositions, 0, keyBlockReadPositions, 0, keyBlockReadPositions.length);

        for(int m = 0; m<(text.length() % key[0].length); m++) {
            for(int a=0; a<keyBlockReadPositions.length; a++) {
                keyBlockReadPositions[a]++;
                if(keyBlockReadPositions[a] >= key[0].length) {
                    keyBlockReadPositions[a]=0;
                }
            }
        }

        for(int t = text.length()-1; t>=text.length()-(text.length() % key[0].length); t--) {
            if(isCancelled()) {
                return "";
            }

            for(int d=0; d<keyBlockReadPositions.length; d++) {
                keyBlockReadPositions[d]--;
                if(keyBlockReadPositions[d] < 0) {
                    keyBlockReadPositions[d] = (byte) (key[0].length - 1);
                }
            }

            if(!(text.charAt(t)>=97 && text.charAt(t)<=122)) {
                throw new IllegalArgumentException("Text must contain all lowercase English ASCII characters");
            }

            currentChar=text.charAt(t) - 97;

            for(int k=keyBlockReadPositions.length-1; k>=0; k--) {
                currentChar = (currentChar - key[k][keyBlockReadPositions[k]]) % 26;
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


            keyBlockBasePositions[0]--;

            for(int m=0; m<keyBlockBasePositions.length-1; m++) {

                if(keyBlockBasePositions[m]<0) {
                    for(int r=0; r<=m; r++) {
                        keyBlockBasePositions[m] = (byte) (key[0].length - 1);
                    }
                    keyBlockBasePositions[m+1]--;
                }
            }

            if(keyBlockBasePositions[keyBlockBasePositions.length - 1] < 0) {
                Arrays.fill(keyBlockBasePositions, (byte)(key[0].length-1));
            }


            System.arraycopy(keyBlockBasePositions, 0, keyBlockReadPositions, 0, keyBlockReadPositions.length);
            for(int t = seg; t > seg - key[0].length; t--) {

                for(int d=0; d<keyBlockReadPositions.length; d++) {
                    keyBlockReadPositions[d]--;
                    if(keyBlockReadPositions[d] < 0) {
                        keyBlockReadPositions[d]= (byte) (key[0].length - 1);
                    }
                }

                if(!(text.charAt(t)>=97 && text.charAt(t)<=122)) {
                    throw new IllegalArgumentException("Text must contain all lowercase English ASCII characters");
                }

                currentChar=(int)text.charAt(t) - 97;
                for(int k=keyBlockReadPositions.length-1; k>=0; k--) {
                    currentChar = (currentChar - key[k][keyBlockReadPositions[k]]) % 26;
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
        byte[] keyBlockBasePositions = initializeKeyBlockPositions2(startingSegment + text.length() / key[0].length,
                key.length, key[0].length);

        StringBuilder output = new StringBuilder(text.length());

        int currentChar=0;
        int currentBlock = (startingSegment + text.length() / key[0].length);

        byte[] keyBlockReadPositions = new byte[key.length];
        System.arraycopy(keyBlockBasePositions, 0, keyBlockReadPositions, 0, keyBlockReadPositions.length);

        for(int m = 0; m < (text.length() % key[0].length); m++) {
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

        for(int t = text.length()-1; t >= text.length()-(text.length() % key[0].length); t--) {

            for(int d=0; d<keyBlockReadPositions.length; d++) {
                keyBlockReadPositions[d] -= 1;
                if(keyBlockReadPositions[d] < 0) {
                    keyBlockReadPositions[d] = (byte) (key[0].length - 1);
                }
            }

            currentChar=text.charAt(t) - 97;
            for(int k = keyBlockReadPositions.length-1; k >= 0; k--) {
                currentChar = (currentChar - key[k][keyBlockReadPositions[k]]) % 26;
                if(currentChar < 0) {
                    currentChar += 26;
                }
            }

            output.append((char)(currentChar+97));

        }

        for(int seg = text.length()-(text.length() % key[0].length)-1; seg >= 0; seg -= key[0].length) {
            if(isCancelled()) {
                return "";
            }

            currentBlock--;
            if((currentBlock+1) % key[0].length==0) {
                keyBlockBasePositions = initializeKeyBlockPositions(currentBlock, key.length, key[0].length);
            }


            for(int m=0; m<keyBlockBasePositions.length; m++) {
                keyBlockBasePositions[m] -= (byte)(getKeyBlockIncrementIndex(m) % key[0].length);

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
     * Returns a copy of {@code textNonAlphas} with its numbers decrypted using {@code key}.<br><br>
     *
     * Any non-number is unchanged in the output.
     * @param textNonAlphas input array containing non-alphabetic characters in the text
     * @param key key to decrypt with. Cannot be null. All indices must be on the interval [0,25]
     * @return copy of {@code input} with numbers decrypted
     */
    private char[] decryptNumbers(char[] textNonAlphas, byte[][] key) {
        if(textNonAlphas==null) {
            throw new AssertionError("Text non-alphas cannot be null");
        }
        if(key==null) {
            throw new AssertionError("Key cannot be null");
        }

        int dKey = 0;
        for(byte[] b : key) {
            if(b==null) {
                throw new AssertionError("All blocks in the key cannot be null");
            }

            for(byte i : b) {
                if(i<0 || i>25) {
                    throw new AssertionError("All indices in the key must be on the interval [0, 25]- received " + i);
                }
                dKey += i;
            }
        }

        dKey = dKey % 26;

        char[] output = new char[textNonAlphas.length];
        for(int i=0; i<textNonAlphas.length; i++) {
            if(!((int)textNonAlphas[i]>=48 && (int)textNonAlphas[i]<=57)) {
                output[i] = textNonAlphas[i];
            }
            else {
                int newChar = (int)textNonAlphas[i] - 48;
                newChar = (newChar - dKey) % 10;
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
            for (byte k : blocks) {
                if (k < 0 || k > 25) {
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

        byte[] keyBlockBasePositions = initializeKeyBlockPositions((long)startSegment * (long)key[0].length,
                key.length, key[0].length);
        byte[] keyBlockReadPositions = new byte[key.length];
        System.arraycopy(keyBlockBasePositions, 0, keyBlockReadPositions, 0, keyBlockReadPositions.length);

        StringBuilder output = new StringBuilder(text.length());
        int currentChar=0;

        for(int seg = 0; seg <= text.length()- key[0].length; seg += key[0].length) {
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

            keyBlockBasePositions[0]++;

            for(int m=0; m<keyBlockBasePositions.length-1; m++) {

                if(keyBlockBasePositions[m] >= key[0].length) {

                    for(int r=0; r<=m; r++) {
                        keyBlockBasePositions[r]=0;
                    }
                    keyBlockBasePositions[m+1]++;
                }
            }
            if(keyBlockBasePositions[keyBlockBasePositions.length-1] >= key[0].length) {
                Arrays.fill(keyBlockBasePositions, (byte)0);
            }

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
        
        byte[] keyBlockBasePositions = initializeKeyBlockPositions2(startingSegment, key.length, key[0].length);
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
                keyBlockBasePositions[r] = (byte) ((keyBlockBasePositions[r] + getKeyBlockIncrementIndex(r)) % key[0].length);
            }

            if((blocksEncrypted+1) % key[0].length == 0) {
                keyBlockBasePositions = initializeKeyBlockPositions(blocksEncrypted+2, key.length, key[0].length);
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
     * Returns a copy of {@code textNonAlphas}, but with numbers encrypted using {@code key}.<br><br>
     * Any non-number is unchanged in the output.
     *
     * @param textNonAlphas array of non-alphabetic characters in the text. Cannot be null
     * @param key key to encrypt with. Cannot be null. All indices must be on the interval [0, 25]
     * @return copy of input, but with numbers encrypted
     */
    private char[] encryptNumbers(char[] textNonAlphas, byte[][] key) {
        if(textNonAlphas == null) throw new AssertionError("Text non-alphas cannot be null");
        if(key==null) {
            throw new AssertionError("Key cannot be null");
        }

        int eKey = 0;
        for(byte[] b : key) {
            if(b==null) {
                throw new AssertionError("All blocks in the key cannot be null");
            }

            for(byte i : b) {
                if(i<0 || i>25) {
                    throw new AssertionError("All indices in the key must be on the interval [0, 25]- received " + i);
                }
                eKey += i;
            }
        }
        eKey = eKey % 26;

        char[] output = new char[textNonAlphas.length];
        for(int i=0; i<textNonAlphas.length; i++) {
            if(textNonAlphas[i]<48 || textNonAlphas[i]>57) {
                output[i] = textNonAlphas[i];
            }
            else {
                int newChar = (int)textNonAlphas[i] - 48;
                newChar = (newChar + eKey) % 10;
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
     * A NUL character, a (char)0, should be loaded as a (char)7 instead.<br><br>
     *
     * Example: if the text is "A1b2c3", the output, expressed as ints, is {0, 48, 0, 49, 0, 50}.
     * Since indices 0, 2, and 4 in the input are alphabetic characters, the corresponding indices in the output is 0.
     * Indices 1, 3, and 5 hold the corresponding ASCII value in the corresponding output indices<br>
     * Example: If the text is "\u0000", the output, expressed as ints, is {7}.
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

            //(char)0 special handling
            if((int)text.charAt(i) == 0){
                nonAlphas[i] = (char)7;
            }
            //Other non-letter
            else if((int)text.charAt(i)<65
                    || ((int)text.charAt(i)>90 && (int)text.charAt(i)<97)
                    || (int)text.charAt(i)>122) {

                nonAlphas[i] = text.charAt(i);
            }
            //Letter
            else {
                nonAlphas[i] = (char)0;
            }
        }

        return nonAlphas;
    }

    /**
     * FOR TESTING PURPOSES ONLY! Returns the output of {@code findNonAlphaPositions} for {@code text}.
     * @param text text to find non-alphabetic characters in. Cannot be null
     * @return char array containing locations of non-alphabetic characters
     */
    public char[] findNonAlphaPositions_Testing(String text) {
        return findNonAlphaPositions(text);
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
    private byte[] initializeKeyBlockPositions(long textLength, int blockCount, int blockLength) {
        //Check parameters
        if(textLength < 0) throw new AssertionError("Text length cannot be negative- instead received " + textLength);
        if(blockCount <= 0) throw new AssertionError("Block count must be positive- instead received " + blockCount);
        if(blockLength <= 0) throw new AssertionError("Block length must be positive- instead received " + blockLength);

        //Set the output array, assign all empty space to 0
        byte[] result = new byte[blockCount];

        long quotient = textLength;
        double decimalPortion = 0;

        //Eliminate block spill-overs.
        quotient = quotient % ((long)Math.pow(blockLength, blockCount+1));

        //Divide quotient and take only the portion to the left of the decimal point
        quotient = quotient / blockLength;


        for(int i = result.length-1; i >= 0; i--) {

            //Divide quotient and take only the portion to the right of the decimal point
            decimalPortion = (double)quotient / blockLength - (int)(quotient / blockLength);
            //Divide quotient and keep only the portion to the left of the decimal point
            quotient = quotient / (long) blockLength;

            //Convert the decimal portion to a digit and add to the result
            result[result.length - 1 - i] = (byte)(Math.round(decimalPortion * blockLength));

            if(quotient <= 0) {
                break;
            }

        }

        return result;
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
    public byte[] initializeKeyBlockPositions_Testing(long textLength, int blockCount, int blockLength) {
        return initializeKeyBlockPositions(textLength, blockCount, blockLength);
    }



    /**
     * Returns an array of bytes representing the key block positions at the end of version 2 encryption,
     * if the input had {@code segments} segments<br><br>
     *
     * {@code segments} should equal the number of segments before the starting position.<br>
     * Example: if {@code segments} equals 4, the output would be the block positions just after encrypting 4 segments.<br><br>
     *
     * Helper to the v2 operation functions.
     *
     * @param segments number of blocks encrypted. Must be non-negative
     * @param blockCount number of blocks in the key. Must be positive.
     * @param blockLength length of each block in the key. Must be positive.
     * @return key block positions after encrypting {@code segments} segments
     */
    private byte[] initializeKeyBlockPositions2(long segments, int blockCount, int blockLength) {
        if(segments<0) throw new AssertionError("Segments cannot be negative- received " + segments);
        if(blockCount<=0) throw new AssertionError("Block count must be positive- received " + blockCount);
        if(blockLength<=0) throw new AssertionError("Block length must be positive- received " + blockLength);

        byte[] output = initializeKeyBlockPositions(segments, blockCount, blockLength);

        for(int b = 0; b < segments % blockLength; b++) {
            for(int i=0; i<output.length; i++) {
                output[i] = (byte) ((output[i] + getKeyBlockIncrementIndex(i)) % blockLength);
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
     * If not, the text returned should contain only alphanumeric characters.<br>
     *
     * - Regardless of the value of {@code reinsertPunctuation}, apostrophes are never included in the output. An apostrophe is
     * any character equal to (char)39, (char)96, `, or ’.<br><br>
     *
     * Example: {@code text} is "abcdefg", {@code nonAlphas} is [0,0,0,32,0,0,0,39,48,49,50].<br>
     * {@code nonAlphas}'s positive entries are at positions where, in the original text, there were non-alphabetic
     * characters that were removed. A space (Unicode: 32) was at index 3 in the original text. Numbers '1', '2', and '3'
     * (Unicode: 48, 49, 50) were at indices 8, 9, and 10. There is a (char)39, an apostrophe, at index 7.<br>
     * Given the text, the non-alpha positions, and a {@code reinsertingPunctuation} value of true, the output would be
     * "abc defg123".<br>
     *
     * If {@code reinsertPunctuation} was false, the output would be "abcdefg123".<br>
     *
     * Note that in both cases, the apostrophe never appears in the output.<br><br>
     *
     * Undoes the separation of characters in {@code removeNonAlphas} and {@code findNonAlphaPositions}.<br>
     * IMPORTANT: Given an original string, {@code text} must be the string's alphabetic characters,
     * and {@code nonAlphas} should be the result of using {@code findNonAlphaPositions} on the string.
     *
     * @param text input text without non-alphabetic characters. Cannot be null. Must contain English lowercase ASCII letters only
     * @param nonAlphas array containing locations of non-alphabetic characters. Cannot be null
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


        StringBuilder output = new StringBuilder(text.length());
        int textIndex = 0;
        int nonAlphasIndex = 0;


        if(text.length() > nonAlphas.length) {
            System.err.println("WARNING: does 'nonAlphas' have blank spaces accounted for?");
        }


        while(nonAlphasIndex < nonAlphas.length) {
            if(isCancelled()) {
                return "";
            }

            //Letter found: add current letter to output, then move to next letter
            if(nonAlphas[nonAlphasIndex] == (char)0) {
                //assertion
                if((int)text.charAt(textIndex) < 97 || (int)text.charAt(textIndex) > 122) {
                    throw new AssertionError("Text character at index " + textIndex + " must be a English lowercase ASCII letter");
                }

                output.append(text.charAt(textIndex));

                textIndex++;
                nonAlphasIndex++;

            }
            //Apostrophe found: ignore it
            else if(nonAlphas[nonAlphasIndex]==(char)39 || nonAlphas[nonAlphasIndex]==(char)96 || nonAlphas[nonAlphasIndex]=='’' || nonAlphas[nonAlphasIndex]=='`') {
                nonAlphasIndex++;
            }
            //Other non-letter found
            else {
                //Add, if either a number or reinserting punctuation
                if(reinsertingPunctuation ||
                        ((int)nonAlphas[nonAlphasIndex] >= 48 && (int)nonAlphas[nonAlphasIndex] <= 57)) {
                    output.append(nonAlphas[nonAlphasIndex]);
                }

                nonAlphasIndex++;
            }
        }

        return output.toString();
    }

    /**
     * FOR TESTING PURPOSES ONLY! Returns the output of the {@code recombineNonAlphas} method.
     * @param text input text without non-alphabetic characters. Cannot be null
     * @param nonAlphas array containing locations of non-alphabetic characters. Cannot be null
     * @param reinsertingPunctuation whether to include punctuation in the output;
     *                            if false, the function reinserts numbers only
     * @return version of text with non-alphabetic characters in their places
     */
    public String recombineNonAlphas_Testing(String text, char[] nonAlphas, boolean reinsertingPunctuation) {
        return recombineNonAlphas(text, nonAlphas, reinsertingPunctuation);
    }



    /**
     * Returns a version of {@code text} without non-alphabetic characters.
     * The text returned is converted to lowercase.<br><br>
     *
     * An alphabetic character is an English ASCII letter (its int value is between 65 and 90, or 97 and 122).<br><br>
     *
     * WARNING! Not to be confused with {@code removeNonAlnums}!
     * {@code removeNonAlphas} removes all non-letters, including numbers!
     *
     * @param text original input. Can't be null
     * @return lowercased text without non-alphabetic characters
     */
    private String removeNonAlphas(String text) {
        if(text==null) throw new AssertionError("Text can't be null");

        if(isCancelled()) {
            return "";
        }

        StringBuilder output = new StringBuilder(text.length());
        for(int i=0; i<text.length(); i++) {
            if(isCancelled()) {
                return "";
            }

            //lowercase letter: append to output
            if((int)text.charAt(i)>=97 && (int)text.charAt(i)<=122) {
                output.append(text.charAt(i));
            }
            //uppercase letter: convert to uppercase, then append to output
            else if((int)text.charAt(i)>=65 && (int)text.charAt(i)<=90) {
                output.append((char)(text.charAt(i) + 32));
            }
        }

        return output.toString();
    }

    /**
     * FOR TESTING PURPOSES ONLY! Returns a lowercased version of {@code text} without non-alphabetic characters.<br><br>
     *
     * Note: All non-alphabetic characters, including numbers, should be removed.
     * An alphabetic character is an English ASCII letter (its int value is between 65 and 90, or 97 and 122).
     *
     * @param text original input. Can't be null
     * @return lowercased text without non-alphabetic characters
     */
    public String removeNonAlphas_Testing(String text) {
        return removeNonAlphas(text);
    }



    /**
     * Returns a copy of {@code input}, but with spaces removed.<br><br>
     *
     * Any space between two letters is removed. All other spaces are to remain in the output.
     * A letter is any character returned by the {@code Character.isAlphabetic} method.
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


}
