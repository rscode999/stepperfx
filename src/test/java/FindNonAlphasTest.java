import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import stepperfx.threading.ProcessSubtaskMain;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * Class to test the method {@code findNonAlphaPositions} of a {@code ProcessSubtaskMain}.
 */
public class FindNonAlphasTest {

    /*
    Specification of findNonAlphaPositions:

    Returns an array containing the positions of all non-alphabetic characters in text.
    If there's an alphabetic character, puts a 0 in the output index.

    Alphabetic characters are ASCII characters that belong to the English alphabet.
    Uppercase and lowercase letters are both treated as letters.

    A NUL character, a (char)0, should be loaded as a (char)7 instead.

    Example: if the text is "A1b2c3", the output, expressed as ints, should be {0, 48, 0, 49, 0, 50}.
    Since indices 0, 2, and 4 in the input are alphabetic characters, the corresponding indices in the output is 0.
    Indices 1, 3, and 5 hold the corresponding ASCII value in the corresponding output indices.

    Example: If the text is "\u0000", the output, expressed as ints, is {7}.
    */

    @DisplayName("An input containing only letters should result in an array of (char)0's, of the same length as the input")
    @Test
    void testAlphasOnly() {
        ProcessSubtaskMain psm = new ProcessSubtaskMain();

        //Multiple letters
        String input = "abcdefgz";
        char[] expected = new char[input.length()];
        Arrays.fill(expected, (char)0);
        char[] result = psm.findNonAlphaPositions_Testing(input);
        assertArrayEquals(expected, result);

        //Multiple uppercase letters
        input = "ABCDEFGZ";
        expected = new char[input.length()];
        Arrays.fill(expected, (char)0);
        result = psm.findNonAlphaPositions_Testing(input);
        assertArrayEquals(expected, result);

        //Single letter
        input = "a";
        expected = new char[input.length()];
        Arrays.fill(expected, (char)0);
        result = psm.findNonAlphaPositions_Testing(input);
        assertArrayEquals(expected, result);
    }


    @DisplayName("The empty string should produce an empty array")
    @Test
    void testEmpty() {
        ProcessSubtaskMain psm = new ProcessSubtaskMain();

        //Multiple letters
        String input = "";
        char[] expected = new char[0];
        char[] result = psm.findNonAlphaPositions_Testing(input);
        assertArrayEquals(expected, result);
    }


    @DisplayName("Inputs with symbols should have the symbols saved in the output's position")
    @Test
    void testPreserveSymbols() {
        ProcessSubtaskMain psm = new ProcessSubtaskMain();

        //All symbols: entire array should be nonzero characters
        String input = "\t:; \"'<>,.!@#$%^&*()_-=+[]{}\\|~`/?+\n";
        char[] expected = new char[]  {
                '\t', ':', ';', ' ', '"', '\'', '<', '>', ',', '.', '!', '@', '#', '$', '%', '^', '&',
                '*', '(', ')', '_', '-', '=', '+', '[', ']', '{', '}', '\\', '|', '~', '`', '/', '?', '+', '\n'
        };

        char[] result = psm.findNonAlphaPositions_Testing(input);
        assertArrayEquals(expected, result);

        //One symbol
        input = "&";
        expected = new char[] {'&'};
        result = psm.findNonAlphaPositions_Testing(input);
        assertArrayEquals(expected, result);

        //Non-ASCII symbols
        input = "€ΩЖ§";
        expected = new char[] {'€', 'Ω', 'Ж', '§'};
        result = psm.findNonAlphaPositions_Testing(input);
        assertArrayEquals(expected, result);

        //Mix of letters and symbols
        input = "Hello. This is a test!";
        expected = new char[] {0, 0, 0, 0, 0, '.', ' ', 0, 0, 0, 0, ' ', 0, 0, ' ', 0, ' ', 0, 0, 0, 0, '!'};
        result = psm.findNonAlphaPositions_Testing(input);
        assertArrayEquals(expected, result);
    }


    @DisplayName("Numbers should be treated as symbols")
    @Test
    void testPreserveNumbers() {
        ProcessSubtaskMain psm = new ProcessSubtaskMain();

        //Numbers only
        String input = "1234567890";
        char[] expected = new char[] {'1', '2', '3', '4', '5', '6', '7', '8', '9', '0'};
        char[] result = psm.findNonAlphaPositions_Testing(input);
        assertArrayEquals(expected, result);

        //Letters, numbers, and symbols
        input = "The 6-7 Meme Sucks.";
        expected = new char[] {0, 0, 0, ' ', '6', '-', '7', ' ', 0, 0, 0, 0, ' ', 0, 0, 0, 0, 0, '.'};
        result = psm.findNonAlphaPositions_Testing(input);
        assertArrayEquals(expected, result);
    }


    @DisplayName("A (char)0 should be loaded as a (char)7 instead")
    @Test
    void testCharZero() {
        ProcessSubtaskMain psm = new ProcessSubtaskMain();

        //One load of a (char)0
        String input = "\u0000";
        char[] expected = new char[] {7};
        char[] result = psm.findNonAlphaPositions_Testing(input);
        assertArrayEquals(expected, result);

        //Several other non-printing characters
        input = "\u0000\u0001\u0007\u0000";
        expected = new char[] {7, 1, 7, 7};
        result = psm.findNonAlphaPositions_Testing(input);
        assertArrayEquals(expected, result);

        //Mixed with normal characters
        input = "hello\u0000world";
        expected = new char[] {0, 0, 0, 0, 0, 7, 0, 0, 0, 0, 0};
        result = psm.findNonAlphaPositions_Testing(input);
        assertArrayEquals(expected, result);
    }
}
