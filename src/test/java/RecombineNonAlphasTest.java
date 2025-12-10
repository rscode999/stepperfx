import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import stepperfx.threading.ProcessSubtaskMain;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@code recombineNonAlphas} method of a {@code ProcessSubtaskMain}.<br><br>
 *
 * NOTE: Requires the {@code findNonAlphaPositions} and {@code removeNonAlphas} methods to work.
 * The `text` parameter of {@code recombineNonAlphas} must contain entirely English lowercase ASCII letters.
 */
public class RecombineNonAlphasTest {
    /*
    Method specification:

    Returns text, with all characters from nonAlphas reinserted in their places.

    - text represents an output without non-alphabetic characters.

    - nonAlphas contains the Unicode values of characters that were removed from text at every index containing a positive number

    - If reinsertPunctuation is true, the text returned should contain all characters from nonAlphas reinserted in their original places.
    If not, the text returned should contain only alphanumeric characters.

     - Regardless of the value of {@code reinsertPunctuation}, apostrophes are never included in the output. An apostrophe is
     any character equal to (char)39, (char)96, '`', or '’'.

    Example: text is "abcdefg", nonAlphas is [0,0,0,32,0,0,0,48,49,50].
    nonAlphas's positive entries are at positions where, in the original text, there were non-alphabetic characters that were removed. A space (Unicode: 32) was at index 3 in the original text. Numbers '1', '2', and '3' (Unicode: 48, 49, 50) were at indices 7, 8, and 9.
    Given the text, the non-alpha positions, and a reinsertingPunctuation value of true, the output would be "abc defg123".

    If reinsertPunctuation was false, the output would be "abcdefg123".

    Note that in both cases, the apostrophe never appears in the output.

    Undoes the separation of characters in removeNonAlphas and findNonAlphaPositions.

    @param text input text without non-alphabetic characters. Cannot be null. Must contain English lowercase ASCII letters only
    @param nonAlphas array containing locations of non-alphabetic characters. Cannot be null
    @param reinsertingPunctuation whether to include punctuation in the output;
                               if false, the function reinserts numbers only
    @return version of text with non-alphabetic characters in their places
    */

    // ////////////////////////////////////////////////////////////////////////////////
    //UTILITIES

    /**
     * Runs the test procedure for each input/expected pair in {@code inputs} and {@code expectedOutputs}.<br>
     * If a test fails, a {@code AssertionFailedError} is thrown, indicating which index in {@code inputs} failed the test.<br><br>
     *
     * The testing procedure separates non-alphabetic characters using {@code findNonAlphaPositions}, removes
     * non-letters with {@code removeNonAlphas}, then recombines the letters and non-letters with {@code recombineNonAlphas}.<br><br>
     *
     * In the case of a test failure, characters are printed as their ASCII values. Refer to an ASCII or Unicode table
     * for character/value mapping.<br><br>
     *
     * The input at index {@code i} in {@code inputs} corresponds to the expected output at {@code expectedOutputs[i]}.
     *
     * @param expectedOutputs array of Strings containing expected outputs. Cannot be null. No indices can be null
     * @param inputs array of Strings containing inputs to {@code recombineNonAlphas}. Cannot be null. No indices can be null.
     *               Must be the same length as {@code expectedOutputs}
     * @param reinsertingPunctuation whether to test reinserting punctuation
     */
    void runTests(String[] expectedOutputs, String[] inputs, boolean reinsertingPunctuation) {
        if(expectedOutputs==null) throw new AssertionError("Expected outputs cannot be null");
        if(inputs==null) throw new AssertionError("Inputs cannot be null");
        if(expectedOutputs.length != inputs.length) throw new AssertionError("Expected outputs length (" + expectedOutputs.length +
                ") must match inputs length (" + inputs.length + ")");

        ProcessSubtaskMain tester = new ProcessSubtaskMain();

        for (int i = 0; i < inputs.length; i++) {
            if(expectedOutputs[i]==null) throw new AssertionError("Expected output array index " + i + " cannot be null");
            if(inputs[i]==null) throw new AssertionFailedError("Input array index " + i + " cannot be null");

            //take input and expected output
            String input = inputs[i];
            String expected = expectedOutputs[i];

            //extract non-alphas
            char[] nonAlphaPositions = tester.findNonAlphaPositions_Testing(input);

            //remove non-alphas, then reinsert them
            String result = tester.recombineNonAlphas_Testing(
                    tester.removeNonAlphas_Testing(input),
                    nonAlphaPositions,
                    reinsertingPunctuation
            );

            //Null check
            if(result == null) throw new AssertionFailedError("Result from input index " + i + " cannot be null");

            //Length check
            if(result.length() != expected.length()) {
                System.err.println("Expected: " + expected);
                System.err.println("Result:   " + result);
                System.err.println("(Non-printing characters will not display properly)");
                throw new AssertionFailedError("Expected length (" + expected.length() + ") does not equal result length (" +
                        result.length() + ") at test index " + i);
            }

            //Element-wise equality check
            for (int comp = 0; comp < expectedOutputs[i].length(); comp++) {
                if(result.charAt(comp) != expected.charAt(comp)) {

                    //Print expected, as ASCII sequence, marking mismatched value
                    System.err.print("Expected: ");
                    for (int c = 0; c < expected.length(); c++) {
                        System.err.print((c == comp) ? (">" + (int)expected.charAt(c) + "<") : (int)expected.charAt(c));
                        System.err.print(" ");
                    }
                    System.err.println();

                    //Print result, as ASCII sequence, marking mismatched value
                    System.err.print("Result:   ");
                    for (int c = 0; c < result.length(); c++) {
                        System.err.print((c == comp) ? (">" + (int)result.charAt(c) + "<") : (int)result.charAt(c));
                        System.err.print(" ");
                    }
                    System.err.println();
                    System.err.println("First mismatched element marked inside `><`");
                    System.err.println("(Characters are printed as a sequence of ASCII values)");

                    throw new AssertionFailedError("Expected and result do not match at character " + comp + " at test index " + i);

                }
            }
        }
    }



    // ////////////////////////////////////////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////////////
    //TESTS



    @DisplayName("The order of letters should not be affected, and should always be reinserted. Letters should be converted to lowercase.")
    @Test
    void testLetters() {
        String[] inputs = new String[] {"abcdefgz", "ZxcVbN", "QWERTY"};
        String[] expectedOutputs = new String[] {"abcdefgz", "zxcvbn", "qwerty"};

        runTests(expectedOutputs, inputs,true);
        runTests(expectedOutputs, inputs,false);
    }


    @DisplayName("Non-letters should be replaced if reinserting punctuation. Otherwise, non-letters should not be reinserted")
    @Test
    void testNonLetters() {
        String[] inputs = new String[] {
                "~!@#$%^&*()_+",
                "[]\\ {}|:;\"",
                "<>,.?/",
                "èéüǹçëêœæ",
                "*",
                "\n",
                "\n\n\t\t\n",
                "\u0000"
        };

        String[] expectedOutputsReinsertingPunct = new String[] {
                "~!@#$%^&*()_+",
                "[]\\ {}|:;\"",
                "<>,.?/",
                "èéüǹçëêœæ",
                "*",
                "\n",
                "\n\n\t\t\n",
                "\u0007" //findNonAlphaPositions turns (char)0 into (char)7
        };

        String[] expectedOutputsNotReinsertingPunct = new String[] {
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                ""
        };

        runTests(expectedOutputsReinsertingPunct, inputs, true);
        runTests(expectedOutputsNotReinsertingPunct, inputs, false);
    }


    @DisplayName("Numbers should always be reinserted, regardless of whether punctuation is reinserted")
    @Test
    void testNumbers() {
        String[] inputs = new String[] {
                "1234567890",
                "1-2-3-4-5-9...",
                "-----0-----",
                "+1, #2, $300",
        };

        String[] expectedOutputsReinsertingPunct = new String[] {
                "1234567890",
                "1-2-3-4-5-9...",
                "-----0-----",
                "+1, #2, $300",
        };

        String[] expectedOutputsNotReinsertingPunct = new String[] {
                "1234567890",
                "123459",
                "0",
                "12300",
        };

        runTests(expectedOutputsReinsertingPunct, inputs, true);
        runTests(expectedOutputsNotReinsertingPunct, inputs, false);
    }


    @DisplayName("Letters and numbers should always be reinserted. Symbols are reinserted if `reinsertingPunctuation` is true")
    @Test
    void testCombined() {
        String[] inputs = new String[] {
                "Hi how are you",
                "I have 1 apple",
                "Trailing symbols...",
                "...Leading Symb0ls",
        };

        String[] expectedOutputsReinsertingPunct = new String[] {
                "hi how are you",
                "i have 1 apple",
                "trailing symbols...",
                "...leading symb0ls",
        };

        String[] expectedOutputsNotReinsertingPunct = new String[] {
                "hihowareyou",
                "ihave1apple",
                "trailingsymbols",
                "leadingsymb0ls",
        };

        runTests(expectedOutputsReinsertingPunct, inputs, true);
        runTests(expectedOutputsNotReinsertingPunct, inputs, false);
    }


    @DisplayName("Apostrophes should never be added, even when reinserting punctuation")
    @Test
    void testApostrophes() {
        ProcessSubtaskMain tester = new ProcessSubtaskMain();

        char[] nonAlphas = new char[] {'\'', '`', '’', '’'};
        String noAlphaText = "";
        assertEquals("", tester.recombineNonAlphas_Testing(noAlphaText, nonAlphas, true));
        assertEquals("", tester.recombineNonAlphas_Testing(noAlphaText, nonAlphas, false));

        //Corresponds to the string "don't do it"
        nonAlphas = new char[] {0, 0, 0, '\'', 0, ' ', 0, 0, ' ', 0, 0};
        noAlphaText = "dontdoit";
        assertEquals("dont do it", tester.recombineNonAlphas_Testing(noAlphaText, nonAlphas, true));
        assertEquals("dontdoit", tester.recombineNonAlphas_Testing(noAlphaText, nonAlphas, false));
        //NOTE: If an integer is appended to a StringBuilder, the String representation of the integer (not the character) is appended
    }


    @DisplayName("Even when with letters and numbers, apostrophes should not be inserted")
    @Test
    void testCombinedWithApostrophes() {
        String[] inputs = new String[] {
                "I'm a Cupcake!",
                "'Leading Apostrophe",
                "Trailing apostrophe..`",
                ":')",
                "':^(`",
                " E'E'3'E",
                "’’’python’’’"
        };

        String[] expectedOutputsReinsertingPunct = new String[] {
                "im a cupcake!",
                "leading apostrophe",
                "trailing apostrophe..",
                ":)",
                ":^(",
                " ee3e",
                "python"
        };

        String[] expectedOutputsNotReinsertingPunct = new String[] {
                "imacupcake",
                "leadingapostrophe",
                "trailingapostrophe",
                "",
                "",
                "ee3e",
                "python"
        };

        runTests(expectedOutputsReinsertingPunct, inputs, true);
        runTests(expectedOutputsNotReinsertingPunct, inputs, false);
    }
}
