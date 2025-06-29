import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import stepperfx.threading.ProcessSubtaskDiacritics;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Class to test the method {@code removeDiacritics} of a {@code ProcessSubtaskDiacritics}.
 */
public class RemoveDiacriticsTest {

    /**
     * Starts the JavaFX runtime so the tests can properly run
     */
    @BeforeAll
    public static void initJavaFxRuntime() {
        Platform.startup(() -> {});
    }


    @DisplayName("removeDiacritics should leave strings containing only lowercase ASCII letters unchanged")
    @Test
    void testNoRemoveLetters() {
        ProcessSubtaskDiacritics psd = new ProcessSubtaskDiacritics();
        String input;
        String output;

        //One letter
        input = "a";
        output = psd.removeDiacritics_Testing(input);
        assertEquals("a", output);

        //Many letters
        input = "lkasjewrflgajfg";
        output = psd.removeDiacritics_Testing(input);
        assertEquals("lkasjewrflgajfg", output);

        //Empty string
        input = "";
        output = psd.removeDiacritics_Testing(input);
        assertEquals("", output);
    }


    @DisplayName("removeDiacritics should leave strings containing lowercase ASCII letters, ASCII numbers, " +
            "and (not necessarily ASCII) symbols unchanged")
    @Test
    void testNoRemoveSymbols() {
        ProcessSubtaskDiacritics psd = new ProcessSubtaskDiacritics();
        String input;
        String output;

        //One number
        input = "9";
        output = psd.removeDiacritics_Testing(input);
        assertEquals("9", output);

        //One ASCII symbol
        input = "{";
        output = psd.removeDiacritics_Testing(input);
        assertEquals("{", output);

        //One non-ASCII symbol
        input = "·";
        output = psd.removeDiacritics_Testing(input);
        assertEquals("·", output);

        //Combined letters and numbers, all ASCII
        input = "ljkf34ijo3409fj345023450fg8u34";
        output = psd.removeDiacritics_Testing(input);
        assertEquals("ljkf34ijo3409fj345023450fg8u34", output);

        //Combined letters, numbers, and symbols, all ASCII
        input = "azjlkwejf0*(jf0w ifj23rjf0j3r/ 23 3 \\3\\]r'\\] ]\\ f'34-f=vc we";
        output = psd.removeDiacritics_Testing(input);
        assertEquals("azjlkwejf0*(jf0w ifj23rjf0j3r/ 23 3 \\3\\]r'\\] ]\\ f'34-f=vc we", output);

        //Combined letters, numbers, and symbols, but some symbols are not ASCII
        input = "ǃeau4tb5 54fer 4 ֎ 4;'[ a43f᠁ 4gfd";
        output = psd.removeDiacritics_Testing(input);
        assertEquals("ǃeau4tb5 54fer 4 ֎ 4;'[ a43f᠁ 4gfd", output);
    }


    @DisplayName("removeDiacritics should convert letters to their lowercase equivalents")
    @Test
    void testLowercasing() {
        ProcessSubtaskDiacritics psd = new ProcessSubtaskDiacritics();
        String input;
        String output;

        //Single uppercase letter
        input = "Z";
        output = psd.removeDiacritics_Testing(input);
        assertEquals("z", output);

        //Many uppercase letters
        input = "LIEUAWRJFOGIAERWZ";
        output = psd.removeDiacritics_Testing(input);
        assertEquals("lieuawrjfogiaerwz", output);

        //Some lowercase and uppercase
        input = "akLEkfJxcleiwDjfeXfeZlAeelZ";
        output = psd.removeDiacritics_Testing(input);
        assertEquals("aklekfjxcleiwdjfexfezlaeelz", output);

        //Includes symbols
        input = "alkjAEDFlk23j4fEFj[o23jfsd j;flkj34w;flkj2 3l;ZrkfSJE}FKO}{PO#$JKf rw[3epo0fizr j[]p2j34r";
        output = psd.removeDiacritics_Testing(input);
        assertEquals("alkjaedflk23j4fefj[o23jfsd j;flkj34w;flkj2 3l;zrkfsje}fko}{po#$jkf rw[3epo0fizr j[]p2j34r", output);
    }


    @DisplayName("removeDiacritics should replace lowercase accented letters with their non-accented variations")
    @Test
    void testReplacements() {
        ProcessSubtaskDiacritics psd = new ProcessSubtaskDiacritics();
        String input;
        String output;

        //Single letter
        input = "à";
        output = psd.removeDiacritics_Testing(input);
        assertEquals("a", output);

        //Another single letter
        input = "å";
        output = psd.removeDiacritics_Testing(input);
        assertEquals("a", output);

        //Another single letter that becomes something different
        input = "æ";
        output = psd.removeDiacritics_Testing(input);
        assertEquals("e", output);

        //All letters in the string transform into the same letter
        input = "ëêêœèé";
        output = psd.removeDiacritics_Testing(input);
        assertEquals("eeeeee", output);

        //All letters in the string transform into different letters
        input = "ÿâïãçǹùńä";
        output = psd.removeDiacritics_Testing(input);
        assertEquals("yaiacnuna", output);
    }


    @DisplayName("removeDiacritics should replace lowercase accented letters with their non-accented variations, " +
        "and ignore all non-letters and non-accented letters")
    @Test
    void testReplacementIgnores() {
        ProcessSubtaskDiacritics psd = new ProcessSubtaskDiacritics();
        String input;
        String output;

        //Letters to replace, along with unaccented letters
        input = "õoôøoöòó";
        output = psd.removeDiacritics_Testing(input);
        assertEquals("oooooooo", output);

        //Letters to replace, along with unaccented letters (but with different letters)
        input = "ndõńmðñoôøoöònqóo";
        output = psd.removeDiacritics_Testing(input);
        assertEquals("ndonmdnoooooonqoo", output);

        //Letters to replace, along with unaccented letters and symbols
        input = " -ì x!]ß ií* sî[öø^ï% +u= ";
        output = psd.removeDiacritics_Testing(input);
        assertEquals(" -i x!]s ii* si[oo^i% +u= ", output);

        //Letters to replace, along with unaccented letters, numbers, and non-ASCII symbols
        input = "ùúýu e3⸓1ÿ ûǹń-᐀ {]?3 ñňü0 9 ";
        output = psd.removeDiacritics_Testing(input);
        assertEquals("uuyu e3⸓1y unn-᐀ {]?3 nnu0 9 ", output);
    }


    @DisplayName("removeDiacritics should replace uppercase accented letters with a lowercased version " +
            "of the letter without diacritics")
    @Test
    void testLowercasingReplacements() {
        ProcessSubtaskDiacritics psd = new ProcessSubtaskDiacritics();
        String input;
        String output;

        //Single uppercase accented letter
        input = "À";
        output = psd.removeDiacritics_Testing(input);
        assertEquals("a", output);

        //Many uppercase accented letters
        input = "ÁÇÆÄÌÃ";
        output = psd.removeDiacritics_Testing(input);
        assertEquals("aceaia", output);

        //Many uppercase accented letters, along with some lowercase accented letters
        input = "ÒÔËòóÐÕôõöøÕÑÉ";
        output = psd.removeDiacritics_Testing(input);
        assertEquals("ooeoodoooooone", output);

        //Many uppercase accented letters with symbols and normal letters
        input = " × ;Aãa   ZzÏßý 09ÿ 6d35erÀ oO ";
        output = psd.removeDiacritics_Testing(input);
        assertEquals(" × ;aaa   zzisy 09y 6d35era oo ", output);
    }


    @DisplayName("removeDiacritics should replace superscript numbers, subscript numbers, and dashes " +
            "with their ASCII equivalents")
    @Test
    void testReplaceNonAlphas() {
        ProcessSubtaskDiacritics psd = new ProcessSubtaskDiacritics();
        String input;
        String output;

        //Superscript numbers
        input = "⁰¹²³⁴⁵⁶⁷⁸⁹";
        output = psd.removeDiacritics_Testing(input);
        assertEquals("0123456789", output);

        //Subscript numbers
        input = "₀₁₂₃₄₅₆₇₈₉";
        output = psd.removeDiacritics_Testing(input);
        assertEquals("0123456789", output);

        //Dash
        input = "—";
        output = psd.removeDiacritics_Testing(input);
        assertEquals("-", output);

        //Combination
        input = "0a1—ð⁰ ¹234œ5⁶₇⁸";
        output = psd.removeDiacritics_Testing(input);
        assertEquals("0a1-d0 1234e5678", output);
    }
}
