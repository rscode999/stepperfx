import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import stepperfx.threading.ProcessSubtaskMain;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Class to test the method {@code removeNonAlphas} of a {@code ProcessSubtaskMain}.
 */
public class RemoveNonAlphasTest {

    @DisplayName("Strings of all lowercase alphabetic characters should not be changed")
    @Test
    void testPreserveAlphas() {
        ProcessSubtaskMain psm = new ProcessSubtaskMain();

        //String of multiple letters
        String input = "abc";
        String output = psm.removeNonAlphas_Testing(input);
        assertEquals(input, output);

        //String of one letter
        input = "z";
        output = psm.removeNonAlphas_Testing(input);
        assertEquals(input, output);
    }


    @DisplayName("All uppercase (ASCII) letters should be converted to lowercase")
    @Test
    void testLowercase() {
        ProcessSubtaskMain psm = new ProcessSubtaskMain();

        //Single uppercase letter
        String input = "A";
        String output = psm.removeNonAlphas_Testing(input);
        assertEquals("a", output);

        //Many uppercase letters
        input = "PZQCE";
        output = psm.removeNonAlphas_Testing(input);
        assertEquals("pzqce", output);

        //Mix of uppercase and lowercase letters (lowercase letters should not be changed)
        input = "AsDfG";
        output = psm.removeNonAlphas_Testing(input);
        assertEquals("asdfg", output);

        input = "aSdFg";
        output = psm.removeNonAlphas_Testing(input);
        assertEquals("asdfg", output);
    }


    @DisplayName("The empty string should be returned as itself")
    @Test
    void testEmptyString() {
        ProcessSubtaskMain psm = new ProcessSubtaskMain();

        String input = "";
        String output = psm.removeNonAlphas_Testing(input);
        assertEquals("", output);
    }


    @DisplayName("All symbols should be removed, leaving only lowercase letters")
    @Test
    void testRemoveSymbols() {
        ProcessSubtaskMain psm = new ProcessSubtaskMain();

        //Lowercase letters and symbols (the symbols should be removed)
        String input = "qwe-rty/uio[p]";
        String output = psm.removeNonAlphas_Testing(input);
        assertEquals("qwertyuiop", output);

        input = "=+:,./q@#r@";
        output = psm.removeNonAlphas_Testing(input);
        assertEquals("qr", output);

        input = "%^&!()*f_+\"'\\";
        output = psm.removeNonAlphas_Testing(input);
        assertEquals("f", output);

        //Uppercase letters and symbols (convert uppercase to lowercase, remove symbols)
        input = "<ZxcV>/~";
        output = psm.removeNonAlphas_Testing(input);
        assertEquals("zxcv", output);

        input = "j;Kl`&%@a";
        output = psm.removeNonAlphas_Testing(input);
        assertEquals("jkla", output);

        //Symbols only (should result in the empty string)
        input = "(*&%))#$@~;:<>/,./\\=+\"";
        output = psm.removeNonAlphas_Testing(input);
        assertEquals("", output);

        //Spaces
        input = "Hello, you're looking fine!";
        output = psm.removeNonAlphas_Testing(input);
        assertEquals("helloyourelookingfine", output);

        //Non-ASCII letters, because they count as symbols
        input = "èéüǹçQëêœæ"; //There is one 'Q' in this string
        output = psm.removeNonAlphas_Testing(input);
        assertEquals("q", output);
    }


    @DisplayName("removeNonAlphas should remove all numbers")
    @Test
    void testRemoveNumbers() {
        ProcessSubtaskMain psm = new ProcessSubtaskMain();

        //Some numbers with lowercase letters
        String input = "the67memeisnotfunny";
        String output = psm.removeNonAlphas_Testing(input);
        assertEquals("thememeisnotfunny", output);

        //Numbers with uppercase, lowercase, and symbols
        input = "1, 2, 3, 4, 5, 6, 7, 8, 9... The 10 Duel Commandments!";
        output = psm.removeNonAlphas_Testing(input);
        assertEquals("theduelcommandments", output);

        input = "H3llo";
        output = psm.removeNonAlphas_Testing(input);
        assertEquals("hllo", output);
    }


    @DisplayName("removeNonAlphas should remove non-printing characters, such as tabs and newlines")
    @Test
    void testNonPrintingCharacters() {
        ProcessSubtaskMain psm = new ProcessSubtaskMain();

        //Tabs
        String input = "\"Yeet  the 6\\7  meme\"";
        String output = psm.removeNonAlphas_Testing(input);
        assertEquals("yeetthememe", output);

        //Newlines + tabs
        input = "Hello\ndarkness\nmy 0ld\tfriend"; //Note: The O in "old" is a zero
        output = psm.removeNonAlphas_Testing(input);
        assertEquals("hellodarknessmyldfriend", output);

        //Exotic non-printing characters
        input = "\u0000\u0007Hallo\u001f\u007f";
        output = psm.removeNonAlphas_Testing(input);
        assertEquals("hallo", output);
    }
}
