package stepperfx.threading;

import javafx.concurrent.Task;
import java.util.HashMap;

/**
 * Worker thread that removes diacritics and non-ASCII numbers from its given input
 */
public class ProcessSubtaskDiacritics extends Task<String> {

    /**
     * The given input for this worker to process
     */
    private final String inputPiece;

    /**
     * Creates a new subtask and assigns it to remove diacritics from {@code inputPiece}.
     * @param inputPiece text to remove diacritics from. Cannot be null
     */
    public ProcessSubtaskDiacritics(String inputPiece) {
        if(inputPiece==null)  throw new AssertionError("Input piece cannot be null");
        this.inputPiece = inputPiece;
    }

    /**
     * FOR METHOD UNIT TESTING ONLY! Creates a new instance, initialized to garbage values.
     */
    public ProcessSubtaskDiacritics() {
        inputPiece = null;
     }


    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * Performs this task's processing and returns the result
     * @return result of processing
     */
    @Override
    protected String call() {
        //Constructor check
        if(inputPiece==null) throw new AssertionError("PROCESS SUBTASK DIACRITICS- TESTING CONSTRUCTOR USED FOR OPERATIONS");
        return removeDiacritics(inputPiece);
    }


    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * Returns a lowercased version of the input without accent marks or letter variants.
     *
     * @param input String to remove diacritics from. Cannot be null
     * @return copy of input without diacritics
     */
    private String removeDiacritics(String input) {
        if(input==null) throw new AssertionError("Input cannot be null");

        //These are the characters to remove. Corresponding indices in `replacementChars` are their replacements
        String[] accentedChars={"àáâãäå", "ç", "ð", "èéëêœæ", "ìíîï", "òóôõöø", "ǹńñň",
                "ß", "ùúûü", "ýÿ", "⁰₀", "¹₁", "²₂", "³₃", "⁴₄", "⁵₅", "⁶₆", "⁷₇", "⁸₈", "⁹₉", "—"};
        char[] replacementChars={'a', 'c', 'd', 'e', 'i', 'o', 'n', 's', 'u', 'y',  '0', '1',
                '2', '3', '4', '5', '6', '7', '8', '9', '-'};

        HashMap<Character, Character> charMap = new HashMap<>(accentedChars.length);

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

            updateProgress(i, input.length());
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
}
