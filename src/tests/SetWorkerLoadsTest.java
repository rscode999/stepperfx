import org.junit.jupiter.api.*;
import stepperfx.threading.ProcessTask;

import java.util.Arrays;

/**
 * Class to test the {@code setWorkerLoads} method of a {@code ProcessTask}.<br><br>
 *
 * Note: As long as the thread loads are evenly distributed, the result is acceptable.
 */
public class SetWorkerLoadsTest {

    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //UTILITY METHODS

    /**
     * Returns true if `a1` has the same length as `a2` and all corresponding elements of the two arrays are equal.
     * i.e. a1[0].equals(a2[0]), a1[1].equals(a2[1]) ... a1[l].equals(a2[l]), l=length of a1 assuming a1.length==a2.length
     * Returns false otherwise. <br>
     * Necessary because arrays have no overridden equals method.
     *
     * @param a1 first array to compare
     * @param a2 second array to compare
     * @return true if arrays equal, false otherwise
     */
    private boolean arrayEquals(String[] a1, String[] a2) {
        if (a1.length != a2.length) {
            return false;
        }

        for (int i = 0; i < a1.length; i++) {
            if (!a1[i].equals(a2[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Compares `expected` and `result`. If the two inputs are not equal, as determined by arrayEquals, prints the
     * expected and result to System.err and throws an AssertionError.
     *
     * @param expected expected result
     * @param result output from test
     */
    private void printAssert(String[] expected, String[] result) {
        if (!arrayEquals(expected, result)) {
            System.err.println("Expected: " + Arrays.toString(expected));
            System.err.println("Result: " + Arrays.toString(result));
            throw new AssertionError("Test failed- Expected and result are not equal");
        }
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //TESTS

    @DisplayName("When blockCount is 1, setWorkerLoads should return a String array with the" +
            " entire contents of its String input in its first index, regardless of the minimum block size")
    @Test
    void testBlockCount1() {
        ProcessTask pt = new ProcessTask();

        String s = "abcdefabcdefabcdefabcdef";
        String[] result = pt.setWorkerLoads_Testing(s, 1, 8);
        String[] expected = new String[]{"abcdefabcdefabcdefabcdef"};
        printAssert(expected, result);

        s = "abcdefgh";
        result = pt.setWorkerLoads_Testing(s, 1, 2);
        expected = new String[]{"abcdefgh"};
        printAssert(expected, result);

        s = "abcde";
        result = pt.setWorkerLoads_Testing(s, 1, 1);
        expected = new String[]{"abcde"};
        printAssert(expected, result);

        s = "";
        result = pt.setWorkerLoads_Testing(s, 1, 999);
        expected = new String[]{""};
        printAssert(expected, result);
    }


    @DisplayName("When setWorkerLoads is called and blockCount is the text's length divided by minBlockSize, the output should " +
            "hold text.length()/minBlockSize strings of length blockSize")
    @Test
    void testBlockSizeEvenSplit() {
        ProcessTask task = new ProcessTask();

        //3 blocks of 8
        String s = "abcdefghabcdefghabcdefgh";
        String[] result = task.setWorkerLoads_Testing(s, 3, 8);
        String[] expected = new String[]{"abcdefgh", "abcdefgh", "abcdefgh"};
        printAssert(expected, result);

        //5 blocks of 8
        s = "abcdefghabcdefghabcdefghabcdefghabcdefgh";
        expected = new String[]{"abcdefgh", "abcdefgh", "abcdefgh", "abcdefgh", "abcdefgh"};
        result = task.setWorkerLoads_Testing(s, 5, 8);
        printAssert(expected, result);

        //1 block of 8
        s = "abcdefgh";
        expected = new String[]{"abcdefgh"};
        result = task.setWorkerLoads_Testing(s, 1, 8);
        printAssert(expected, result);

        //6 blocks of 4
        s = "abcdabcdabcdabcdabcdabcd";
        expected = new String[]{"abcd", "abcd", "abcd", "abcd", "abcd", "abcd"};
        result = task.setWorkerLoads_Testing(s, 6, 4);
        printAssert(expected, result);

        //2 blocks of 6
        s = "abcdefabcdef";
        expected = new String[]{"abcdef", "abcdef"};
        result = task.setWorkerLoads_Testing(s, 2, 6);
        printAssert(expected, result);

        //4 blocks of 3
        s = "abcabcabcabc";
        expected = new String[]{"abc", "abc", "abc", "abc"};
        result = task.setWorkerLoads_Testing(s, 4, 3);
        printAssert(expected, result);
    }


    @DisplayName("When setWorkerLoads is called and blockCount is the text's length divided by 'm', a multiple of minBlockSize, " +
            "the output should hold text.length()/m strings of length m.")
    @Test
    void testEvenSplit() {
        ProcessTask task = new ProcessTask();

        //Block length is 8*2 (m=16)
        String s = "abcdabcdabcdabcdefghefghefghefgh";
        String[] result = task.setWorkerLoads_Testing(s, 2, 8);
        String[] expected = new String[]{"abcdabcdabcdabcd", "efghefghefghefgh"};
        printAssert(expected, result);

        //Block length is 8*2 (m=16)
        s = "aaaaaaaaaaaaaaaabbbbbbbbbbbbbbbbccccccccccccccccdddddddddddddddd";
        expected = new String[]{"aaaaaaaaaaaaaaaa", "bbbbbbbbbbbbbbbb", "cccccccccccccccc", "dddddddddddddddd"};
        result = task.setWorkerLoads_Testing(s, 4,8);
        printAssert(expected, result);

        //Block length is 5*3 (m=15)
        s = "abcdefghijklmnabcdefghijklmnabcdefghijklmn";
        expected = new String[]{"abcdefghijklmn", "abcdefghijklmn", "abcdefghijklmn"};
        result = task.setWorkerLoads_Testing(s, 3, 5);

        //Block length is 6*2 (m=12)
        s = "abababababababababababababababababababababababab";
        expected = new String[]{"abababababab", "abababababab", "abababababab", "abababababab"};
        result = task.setWorkerLoads_Testing(s, 4, 2);
    }


    @DisplayName("When blockCount is not the text length divided by minBlockSize and the text is split in half," +
            " the output's final index should take the remainder")
    @Test
    void testSingleUnevenSplit() {
        ProcessTask task = new ProcessTask();

        //First index has enough room for one string of length minBlockSize
        String s = "aaaaaaaabbbbbbbbbbbb";
        String[] result = task.setWorkerLoads_Testing(s, 2, 8);
        String[] expected = new String[]{"aaaaaaaa", "bbbbbbbbbbbb"};
        printAssert(expected, result);

        //First index has enough room for many strings of length minBlockSize
        s = "aaaaaaaacccccccceeeeeeee";
        result = task.setWorkerLoads_Testing(s, 2, 8);
        expected = new String[]{"aaaaaaaa", "cccccccceeeeeeee"};
        printAssert(expected, result);

        //First index has enough room for many strings of length minBlockSize, minBlockSize is not 8
        s = "aaaaaabbbbbbccccccd";
        result = task.setWorkerLoads_Testing(s, 2, 6);
        expected = new String[]{"aaaaaabbbbbb", "ccccccd"};
        printAssert(expected, result);

        //First index has enough room for many strings of length minBlockSize and second index has room for many length-minBlockSize strings
        s = "aaabbbcccdddeee";
        result = task.setWorkerLoads_Testing(s, 2, 3);
        expected = new String[]{"aaabbb", "cccdddeee"};
        printAssert(expected, result);
    }


    @DisplayName("When blockCount is not the text length divided by minBlockSize and blockCount is more than 2," +
            " the output's final index should take the remainder")
    @Test
    void testUnevenSplit() {
        ProcessTask task = new ProcessTask();

        //Remainder block should be longer than others
        String s = "abcdefghabcdefghabcdefghabcdefghabcdefgh";
        String[] result = task.setWorkerLoads_Testing(s, 4, 8);
        String[] expected = new String[]{"abcdefgh", "abcdefgh", "abcdefgh", "abcdefghabcdefgh"};
        printAssert(expected, result);

        //Remainder block should be longer than others. Each block is longer than the minimum block size
        s = "abcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcde";
        result = task.setWorkerLoads_Testing(s, 3, 8);
        expected = new String[]{"abcdefghabcdefgh", "abcdefghabcdefgh", "abcdefghabcdefghabcde"};
        printAssert(expected, result);

        //Remainder block should be shorter than others
        s = "abcdeabcdeabcdeabcdeabcdeabcdeabcdeabcdeabc";
        result = task.setWorkerLoads_Testing(s, 3, 5);
        expected = new String[]{"abcdeabcdeabcde", "abcdeabcdeabcde", "abcdeabcdeabc"};
        printAssert(expected, result);
    }


    @DisplayName("When blockCount divided by minBlockSize, rounded down to the nearest integer, equals 1 and " +
            "more than 2 blocks should be loaded into each output index, setWorkerLoads should load the appropriate" +
            "number of blocks (not 1 block) into each output index")
    @Test
    void testBugfix() {
        ProcessTask task = new ProcessTask();

        String s = "abcdefghijklmnopabcdefghijklmnopabcdefghijklmnopabcdefghijklmnopabcdefghijkl";
        String[] result = task.setWorkerLoads_Testing(s, 3, 16);
        String[] expected = new String[]{"abcdefghijklmnop", "abcdefghijklmnopabcdefghijklmnop", "abcdefghijklmnopabcdefghijkl"};
        printAssert(expected, result);

        s = "abcdabcdabcdabcdabcdabcdabcdabcdabc";
        result = task.setWorkerLoads_Testing(s, 5, 4);
        expected = new String[]{"abcd", "abcdabcd", "abcdabcd", "abcdabcd", "abcdabc"};
        printAssert(expected, result);
    }


    @DisplayName("When blockSize is more than the text length divided by minBlockSize, all remaining strings should be empty (non-null) strings")
    @Test
    void testUnderflow() {
        ProcessTask task = new ProcessTask();

        String s = "abcdefghabcdefghabcdefgh";
        String[] result = task.setWorkerLoads_Testing(s, 5, 8);
        String[] expected = new String[]{"", "", "abcdefgh", "abcdefgh", "abcdefgh"};
        printAssert(expected, result);

        s = "abcdefgha";
        result = task.setWorkerLoads_Testing(s, 3, 8);
        expected = new String[]{"", "abcdefgh", "a"};
        printAssert(expected, result);

        s = "abcdefgh";
        result = task.setWorkerLoads_Testing(s, 3, 8);
        expected = new String[]{"", "", "abcdefgh"};
        printAssert(expected, result);

        s = "abcdefg";
        result = task.setWorkerLoads_Testing(s, 2, 8);
        expected = new String[]{"", "abcdefg"};
        printAssert(expected, result);

        s = "abcabcabcabcabc";
        result = task.setWorkerLoads_Testing(s, 6, 3);
        expected = new String[]{"", "abc", "abc", "abc", "abc", "abc"};
        printAssert(expected, result);

        s = "abcdefghijabcdefghijabcdefghij";
        result = task.setWorkerLoads_Testing(s, 5, 30);
        expected = new String[]{"", "", "", "", "abcdefghijabcdefghijabcdefghij"};
        printAssert(expected, result);
    }


    @DisplayName("Each block should contain `blockLength` English lowercase ASCII characters. Each block should end in an " +
            "English lowercase ASCII character")
    @Test
    void testIgnoreNonAlphas() {
        ProcessTask task = new ProcessTask();

        //Non-alphanumeric characters
        String s = "abcdefgh-abcdefgh-abcdefgh";
        String[] result = task.setWorkerLoads_Testing(s, 3, 8);
        String[] expected = new String[]{"abcdefgh", "-abcdefgh", "-abcdefgh"};
        printAssert(expected, result);

        //Numeric characters and uppercase letters
        s = "aZbc0abcA1abc2abc9Z";
        result = task.setWorkerLoads_Testing(s, 4, 3);
        expected = new String[]{"aZbc", "0abc", "A1abc", "2abc9Z"};
        printAssert(expected, result);

        //Mix
        s = "---abcd*e123f00gh[]ij===k";
        result = task.setWorkerLoads_Testing(s, 3, 5);
        expected = new String[]{"---abcd*e", "123f00gh[]ij", "===k"};
        printAssert(expected, result);

        //Number of non-alphabetic characters between letter groups are greater than blockLength
        s = "=======================abcde-------------------fghi;;;;;;;;;;;;;;;;;jk";
        result = task.setWorkerLoads_Testing(s, 3, 5);
        expected = new String[]{"=======================abcde", "-------------------fghi;;;;;;;;;;;;;;;;;j", "k"};
        printAssert(expected, result);

        //Same as above, but tests block spillover
        s = "===============abc-------------de000000000000000abcd;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;e" +
                "            ab          cd        e[[[[[[[[[]]]]]]abc----------dea---";
        result = task.setWorkerLoads_Testing(s, 3, 5);
        expected = new String[]{"===============abc-------------de",
                "000000000000000abcd;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;e            ab          cd        e",
                "[[[[[[[[[]]]]]]abc----------dea---"};
        printAssert(expected, result);

        //"Test Bugfix" but with non-alpha characters included
        s = "abc-AAA-------  dabcdab   cdabcdabcdab   cdabcdabc     dabc ";
        result = task.setWorkerLoads_Testing(s, 5, 4);
        expected = new String[]{"abc-AAA-------  d", "abcdab   cd", "abcdabcd", "ab   cdabcd", "abc     dabc "};
        printAssert(expected, result);
    }

}
