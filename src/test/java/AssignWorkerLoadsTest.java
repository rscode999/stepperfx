import org.junit.jupiter.api.*;
import stepperfx.threading.ProcessTask;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * Class to test the {@code assignWorkerLoads} method of a {@code ProcessTask}.<br><br>
 *
 * Note: As long as the thread loads are evenly distributed, the result is acceptable.
 */
public class AssignWorkerLoadsTest {


    @DisplayName("When threads is 1, setWorkerLoads should return a String array with the" +
            " entire contents of its String input in its first index, regardless of the minimum block length")
    @Test
    void testOneThread() {
        ProcessTask pt = new ProcessTask();

        String s = "abcdefabcdefabcdefabcdef";
        String[] result = pt.assignWorkerLoads_Testing(s, 1, 8);
        String[] expected = new String[]{"abcdefabcdefabcdefabcdef"};
        assertArrayEquals(expected, result);

        s = "abcdefgh";
        result = pt.assignWorkerLoads_Testing(s, 1, 2);
        expected = new String[]{"abcdefgh"};
        assertArrayEquals(expected, result);

        s = "abcde";
        result = pt.assignWorkerLoads_Testing(s, 1, 1);
        expected = new String[]{"abcde"};
        assertArrayEquals(expected, result);

        s = "";
        result = pt.assignWorkerLoads_Testing(s, 1, 999);
        expected = new String[]{""};
        assertArrayEquals(expected, result);
    }


    @DisplayName("setWorkerLoads should return an array with a single empty String if thread count is 0")
    @Test
    void testEmptyString() {
        ProcessTask pt = new ProcessTask();

        assertArrayEquals(new String[]{""}, pt.assignWorkerLoads_Testing("", 0, 1));
        assertArrayEquals(new String[]{""}, pt.assignWorkerLoads_Testing("helloworld", 0, 1));
        assertArrayEquals(new String[]{""}, pt.assignWorkerLoads_Testing("hello world", 0, 3));
        assertArrayEquals(new String[]{""}, pt.assignWorkerLoads_Testing("hello world hello world hello world hello world",
                0, 10));
    }


    @DisplayName("When setWorkerLoads is called and `threads` is the text's length divided by `blockLength`, the output should " +
            "hold text.length()/`blockLength` strings of length blockSize")
    @Test
    void testBlockSizeEvenSplit() {
        ProcessTask task = new ProcessTask();

        //3 blocks of 8
        String s = "abcdefghabcdefghabcdefgh";
        String[] result = task.assignWorkerLoads_Testing(s, 3, 8);
        String[] expected = new String[]{"abcdefgh", "abcdefgh", "abcdefgh"};
        assertArrayEquals(expected, result);

        //5 blocks of 8
        s = "abcdefghabcdefghabcdefghabcdefghabcdefgh";
        expected = new String[]{"abcdefgh", "abcdefgh", "abcdefgh", "abcdefgh", "abcdefgh"};
        result = task.assignWorkerLoads_Testing(s, 5, 8);
        assertArrayEquals(expected, result);

        //1 block of 8
        s = "abcdefgh";
        expected = new String[]{"abcdefgh"};
        result = task.assignWorkerLoads_Testing(s, 1, 8);
        assertArrayEquals(expected, result);

        //6 blocks of 4
        s = "abcdabcdabcdabcdabcdabcd";
        expected = new String[]{"abcd", "abcd", "abcd", "abcd", "abcd", "abcd"};
        result = task.assignWorkerLoads_Testing(s, 6, 4);
        assertArrayEquals(expected, result);

        //2 blocks of 6
        s = "abcdefabcdef";
        expected = new String[]{"abcdef", "abcdef"};
        result = task.assignWorkerLoads_Testing(s, 2, 6);
        assertArrayEquals(expected, result);

        //4 blocks of 3
        s = "abcabcabcabc";
        expected = new String[]{"abc", "abc", "abc", "abc"};
        result = task.assignWorkerLoads_Testing(s, 4, 3);
        assertArrayEquals(expected, result);
    }


    @DisplayName("When setWorkerLoads is called and `threads` is the text's length divided by 'm', a multiple of `blockLength`, " +
            "the output should hold text.length()/m strings of length m.")
    @Test
    void testEvenSplit() {
        ProcessTask task = new ProcessTask();

        //Block length is 8*2 (m=16)
        String s = "abcdabcdabcdabcdefghefghefghefgh";
        String[] result = task.assignWorkerLoads_Testing(s, 2, 8);
        String[] expected = new String[]{"abcdabcdabcdabcd", "efghefghefghefgh"};
        assertArrayEquals(expected, result);

        //Block length is 8*2 (m=16)
        s = "aaaaaaaaaaaaaaaabbbbbbbbbbbbbbbbccccccccccccccccdddddddddddddddd";
        expected = new String[]{"aaaaaaaaaaaaaaaa", "bbbbbbbbbbbbbbbb", "cccccccccccccccc", "dddddddddddddddd"};
        result = task.assignWorkerLoads_Testing(s, 4,8);
        assertArrayEquals(expected, result);

        //Block length is 5*3 (m=15)
        s = "abcdefghijklmnabcdefghijklmnabcdefghijklmn";
        expected = new String[]{"abcdefghijklmn", "abcdefghijklmn", "abcdefghijklmn"};
        result = task.assignWorkerLoads_Testing(s, 3, 5);

        //Block length is 6*2 (m=12)
        s = "abababababababababababababababababababababababab";
        expected = new String[]{"abababababab", "abababababab", "abababababab", "abababababab"};
        result = task.assignWorkerLoads_Testing(s, 4, 2);
    }


    @DisplayName("When `threads` is not the text length divided by `blockLength` and the text is split in half," +
            " the output's final index should take the remainder")
    @Test
    void testSingleUnevenSplit() {
        ProcessTask task = new ProcessTask();

        //First index has enough room for one string of length `blockLength`
        String s = "aaaaaaaabbbbbbbbbbbb";
        String[] result = task.assignWorkerLoads_Testing(s, 2, 8);
        String[] expected = new String[]{"aaaaaaaa", "bbbbbbbbbbbb"};
        assertArrayEquals(expected, result);

        //First index has enough room for many strings of length `blockLength`
        s = "aaaaaaaacccccccceeeeeeee";
        result = task.assignWorkerLoads_Testing(s, 2, 8);
        expected = new String[]{"aaaaaaaa", "cccccccceeeeeeee"};
        assertArrayEquals(expected, result);

        //First index has enough room for many strings of length `blockLength`, `blockLength` is not 8
        s = "aaaaaabbbbbbccccccd";
        result = task.assignWorkerLoads_Testing(s, 2, 6);
        expected = new String[]{"aaaaaabbbbbb", "ccccccd"};
        assertArrayEquals(expected, result);

        //First index has enough room for many strings of length `blockLength` and second index has room for many length-`blockLength` strings
        s = "aaabbbcccdddeee";
        result = task.assignWorkerLoads_Testing(s, 2, 3);
        expected = new String[]{"aaabbb", "cccdddeee"};
        assertArrayEquals(expected, result);
    }


    @DisplayName("When `threads` is not the text length divided by `blockLength` and `threads` is more than 2," +
            " the output's final index should take the remainder")
    @Test
    void testUnevenSplit() {
        ProcessTask task = new ProcessTask();

        //Remainder block should be longer than others
        String s = "abcdefghabcdefghabcdefghabcdefghabcdefgh";
        String[] result = task.assignWorkerLoads_Testing(s, 4, 8);
        String[] expected = new String[]{"abcdefgh", "abcdefgh", "abcdefgh", "abcdefghabcdefgh"};
        assertArrayEquals(expected, result);

        //Remainder block should be longer than others. Each block is longer than the minimum block size
        s = "abcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcde";
        result = task.assignWorkerLoads_Testing(s, 3, 8);
        expected = new String[]{"abcdefghabcdefgh", "abcdefghabcdefgh", "abcdefghabcdefghabcde"};
        assertArrayEquals(expected, result);

        //Remainder block should be shorter than others
        s = "abcdeabcdeabcdeabcdeabcdeabcdeabcdeabcdeabc";
        result = task.assignWorkerLoads_Testing(s, 3, 5);
        expected = new String[]{"abcdeabcdeabcde", "abcdeabcdeabcde", "abcdeabcdeabc"};
        assertArrayEquals(expected, result);
    }


    @DisplayName("When `threads` divided by `blockLength`, rounded down to the nearest integer, equals 1 and " +
            "more than 2 blocks should be loaded into each output index, setWorkerLoads should load the appropriate" +
            "number of blocks (not 1 block) into each output index")
    @Test
    void testBugfix() {
        ProcessTask task = new ProcessTask();

        String s = "abcdefghijklmnopabcdefghijklmnopabcdefghijklmnopabcdefghijklmnopabcdefghijkl";
        String[] result = task.assignWorkerLoads_Testing(s, 3, 16);
        String[] expected = new String[]{"abcdefghijklmnop", "abcdefghijklmnopabcdefghijklmnop", "abcdefghijklmnopabcdefghijkl"};
        assertArrayEquals(expected, result);

        s = "abcdabcdabcdabcdabcdabcdabcdabcdabc";
        result = task.assignWorkerLoads_Testing(s, 5, 4);
        expected = new String[]{"abcd", "abcdabcd", "abcdabcd", "abcdabcd", "abcdabc"};
        assertArrayEquals(expected, result);
    }


    @DisplayName("When blockSize is more than the text length divided by `blockLength`, all remaining strings should be empty (non-null) strings")
    @Test
    void testUnderflow() {
        ProcessTask task = new ProcessTask();

        String s = "abcdefghabcdefghabcdefgh";
        String[] result = task.assignWorkerLoads_Testing(s, 5, 8);
        String[] expected = new String[]{"", "", "abcdefgh", "abcdefgh", "abcdefgh"};
        assertArrayEquals(expected, result);

        s = "abcdefgha";
        result = task.assignWorkerLoads_Testing(s, 3, 8);
        expected = new String[]{"", "abcdefgh", "a"};
        assertArrayEquals(expected, result);

        s = "abcdefgh";
        result = task.assignWorkerLoads_Testing(s, 3, 8);
        expected = new String[]{"", "", "abcdefgh"};
        assertArrayEquals(expected, result);

        s = "abcdefg";
        result = task.assignWorkerLoads_Testing(s, 2, 8);
        expected = new String[]{"", "abcdefg"};
        assertArrayEquals(expected, result);

        s = "abcabcabcabcabc";
        result = task.assignWorkerLoads_Testing(s, 6, 3);
        expected = new String[]{"", "abc", "abc", "abc", "abc", "abc"};
        assertArrayEquals(expected, result);

        s = "abcdefghijabcdefghijabcdefghij";
        result = task.assignWorkerLoads_Testing(s, 5, 30);
        expected = new String[]{"", "", "", "", "abcdefghijabcdefghijabcdefghij"};
        assertArrayEquals(expected, result);
    }


    @DisplayName("Each block should contain `blockLength` English lowercase ASCII characters. Each block should end in an " +
            "English lowercase ASCII character")
    @Test
    void testIgnoreNonAlphas() {
        ProcessTask task = new ProcessTask();

        //Non-alphanumeric characters
        String s = "abcdefgh-abcdefgh-abcdefgh";
        String[] result = task.assignWorkerLoads_Testing(s, 3, 8);
        String[] expected = new String[]{"abcdefgh", "-abcdefgh", "-abcdefgh"};
        assertArrayEquals(expected, result);

        //Numeric characters and uppercase letters
        s = "aZbc0abcA1abc2abc9Z";
        result = task.assignWorkerLoads_Testing(s, 4, 3);
        expected = new String[]{"aZbc", "0abc", "A1abc", "2abc9Z"};
        assertArrayEquals(expected, result);

        //Mix
        s = "---abcd*e123f00gh[]ij===k";
        result = task.assignWorkerLoads_Testing(s, 3, 5);
        expected = new String[]{"---abcd*e", "123f00gh[]ij", "===k"};
        assertArrayEquals(expected, result);

        //Number of non-alphabetic characters between letter groups are greater than blockLength
        s = "=======================abcde-------------------fghi;;;;;;;;;;;;;;;;;jk";
        result = task.assignWorkerLoads_Testing(s, 3, 5);
        expected = new String[]{"=======================abcde", "-------------------fghi;;;;;;;;;;;;;;;;;j", "k"};
        assertArrayEquals(expected, result);

        //Same as above, but tests block spillover
        s = "===============abc-------------de000000000000000abcd;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;e" +
                "            ab          cd        e[[[[[[[[[]]]]]]abc----------dea---";
        result = task.assignWorkerLoads_Testing(s, 3, 5);
        expected = new String[]{"===============abc-------------de",
                "000000000000000abcd;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;e            ab          cd        e",
                "[[[[[[[[[]]]]]]abc----------dea---"};
        assertArrayEquals(expected, result);

        //"Test Bugfix" but with non-alpha characters included
        s = "abc-AAA-------  dabcdab   cdabcdabcdab   cdabcdabc     dabc ";
        result = task.assignWorkerLoads_Testing(s, 5, 4);
        expected = new String[]{"abc-AAA-------  d", "abcdab   cd", "abcdabcd", "ab   cdabcd", "abc     dabc "};
        assertArrayEquals(expected, result);
    }

}
