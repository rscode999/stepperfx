import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import stepperfx.threading.ProcessSubtaskMain;

import java.util.Arrays;


/**
 * Class to test the method {@code initializeKeyBlockPositions} in a {@code ProcessSubtaskMain}.
 */
public class InitKeyBlockPositionsTest {


    //UTILITIES

    /**
     * Block length to test. Must be on the interval [1, 100]
     */
    final private int BLOCK_LENGTH = 25;

    /**
     * Block count to test. Must be on the interval [1, 100]
     */
    final private int BLOCK_COUNT = 6;

    /**
     * Returns the key block positions for the given text length for unenhanced (v1) operations.<br><br>
     *
     * This is a reference implementation that is much slower than the actual method.
     * Use for testing only.
     *
     * @param textLength the text length to test
     * @return key block positions for the given text length
     */
    private byte[] initializeKeyBlockPositions_Reference(long textLength, int blockCount, int blockLength) {
        if(textLength < 0) throw new AssertionError("Text length cannot be negative");
        if(blockCount <= 0) throw new AssertionError("Block count must be positive");
        if(blockLength <= 0) throw new AssertionError("Block length must be positive");

        //Set the output array, assign all empty space to 0
        byte[] result = new byte[blockCount];

        //Move through the characters. Rotate at the correct time
        for(int i=1; i<=textLength; i++) {
            if(i % blockLength == 0) {
                result[0]++;
                for (int m = 0; m < result.length - 1; m++) {
                    if (result[m] >= blockLength) {
                        for (int r = 0; r <= m; r++) {
                            result[r] = 0;
                        }
                        result[m + 1]++;
                    }
                }
                if (result[result.length - 1] >= blockLength) {
                    Arrays.fill(result, (byte)0);
                }
            }
        }
        return result;
    }

    // ////////////////////////////////////////////

    /**
     * Returns true if the two arrays are both not null, have the same length,
     * and contain equal corresponding elements.
     * Returns false otherwise.<br><br>
     *
     * Helper to {@code printAssert}.
     *
     * @param arr1 the first array to compare
     * @param arr2 the second array to compare
     * @return whether the arrays are equal
     */
     private boolean arraysEqual(byte[] arr1, byte[] arr2) {

        if(arr1==null || arr2==null || arr1.length!=arr2.length) {
            return false;
        }

        for(int i=0; i<arr1.length; i++) {
            if(arr1[i] != arr2[i]) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if {@code expected} and {@code result} are equal according to {@code arraysEqual}.
     * If not, the method prints the expected and result values as a string, then throws an AssertionFailedError.<br><br>
     *
     * Arrays are considered equal if the two arrays are both non-null, have the same length,
     * and contain equal corresponding elements.
     *
     * @param expected expected output of the test
     * @param result actual output of the test
     */
    private void printAssert(byte[] expected, byte[] result) {

        if(BLOCK_LENGTH<=0 || BLOCK_LENGTH>100) {
            throw new AssertionError("TEST INVALID- BLOCK_LENGTH value must be on the interval [1,100] (received " + BLOCK_LENGTH + ")");
        }
        if(BLOCK_COUNT<=0 || BLOCK_COUNT>100) {
            throw new AssertionError("TEST INVALID- BLOCK_COUNT value must be on the interval [1,100] (received " + BLOCK_COUNT + ")");
        }

        //Do the comparison, abort upon failure
         if(!arraysEqual(expected, result)) {
             System.err.println("Expected: " + Arrays.toString(expected));
             System.err.println("Result:   " + Arrays.toString(result));
             throw new AssertionFailedError("Test failed- expected and result not equal");
         }
    }



    // //////////////////////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////////////////////
    //TESTS


    @DisplayName("The output should be all zeros when the given text length is less than BLOCK_LENGTH")
    @Test
    void testNoRotation() {
        ProcessSubtaskMain p = new ProcessSubtaskMain();
        long input;
        byte[] expected;
        byte[] result;

        input = 0;
        expected = initializeKeyBlockPositions_Reference(input, BLOCK_COUNT, BLOCK_LENGTH);
        result = p.initializeKeyBlockPositions_Testing(input, BLOCK_COUNT, BLOCK_LENGTH);
        printAssert(expected, result);

        input = 3;
        expected = initializeKeyBlockPositions_Reference(input, BLOCK_COUNT, BLOCK_LENGTH);
        result = p.initializeKeyBlockPositions_Testing(input, BLOCK_COUNT, BLOCK_LENGTH); ;
        printAssert(expected, result);

        input = BLOCK_LENGTH - 1;
        expected = initializeKeyBlockPositions_Reference(input, BLOCK_COUNT, BLOCK_LENGTH);
        result = p.initializeKeyBlockPositions_Testing(input, BLOCK_COUNT, BLOCK_LENGTH); ;
        printAssert(expected, result);
    }


    @DisplayName("The output should be all zeros, except for the first index, which should be 1, " +
            "when the length is between BLOCK_LENGTH and 2*BLOCK_LENGTH-1")
    @Test
    void testSingleRotation() {
        ProcessSubtaskMain p = new ProcessSubtaskMain();
        long input;
        byte[] expected;
        byte[] result;

        input = BLOCK_LENGTH;
        expected = initializeKeyBlockPositions_Reference(input, BLOCK_COUNT, BLOCK_LENGTH);
        result = p.initializeKeyBlockPositions_Testing(input, BLOCK_COUNT, BLOCK_LENGTH); ;
        printAssert(expected, result);

        input = BLOCK_LENGTH+1;
        expected = initializeKeyBlockPositions_Reference(input, BLOCK_COUNT, BLOCK_LENGTH);
        result = p.initializeKeyBlockPositions_Testing(input, BLOCK_COUNT, BLOCK_LENGTH); ;
        printAssert(expected, result);

        input = BLOCK_LENGTH*2-1;
        expected = initializeKeyBlockPositions_Reference(input, BLOCK_COUNT, BLOCK_LENGTH);
        result = p.initializeKeyBlockPositions_Testing(input, BLOCK_COUNT, BLOCK_LENGTH); ;
        printAssert(expected, result);
    }


    @DisplayName("The output should be all zeros, except for the first index, when the length is between " +
            "BLOCK_LENGTH and BLOCK_LENGTH^2")
    @Test
    void testFirstIndexRotation() {
        ProcessSubtaskMain p = new ProcessSubtaskMain();
        long input;
        byte[] expected;
        byte[] result;

        input = BLOCK_LENGTH*2;
        expected = initializeKeyBlockPositions_Reference(input, BLOCK_COUNT, BLOCK_LENGTH);
        result = p.initializeKeyBlockPositions_Testing(input, BLOCK_COUNT, BLOCK_LENGTH); ;
        printAssert(expected, result);

        input = BLOCK_LENGTH*3 - 1;
        expected = initializeKeyBlockPositions_Reference(input, BLOCK_COUNT, BLOCK_LENGTH);
        result = p.initializeKeyBlockPositions_Testing(input, BLOCK_COUNT, BLOCK_LENGTH); ;
        printAssert(expected, result);

        input = BLOCK_LENGTH*3;
        expected = initializeKeyBlockPositions_Reference(input, BLOCK_COUNT, BLOCK_LENGTH);
        result = p.initializeKeyBlockPositions_Testing(input, BLOCK_COUNT, BLOCK_LENGTH); ;
        printAssert(expected, result);

        input = BLOCK_LENGTH*5 - 1;
        expected = initializeKeyBlockPositions_Reference(input, BLOCK_COUNT, BLOCK_LENGTH);
        result = p.initializeKeyBlockPositions_Testing(input, BLOCK_COUNT, BLOCK_LENGTH); ;
        printAssert(expected, result);

        input = BLOCK_LENGTH*5;
        expected = initializeKeyBlockPositions_Reference(input, BLOCK_COUNT, BLOCK_LENGTH);
        result = p.initializeKeyBlockPositions_Testing(input, BLOCK_COUNT, BLOCK_LENGTH); ;
        printAssert(expected, result);

        input = BLOCK_LENGTH * BLOCK_LENGTH - 1;
        expected = initializeKeyBlockPositions_Reference(input, BLOCK_COUNT, BLOCK_LENGTH);
        result = p.initializeKeyBlockPositions_Testing(input, BLOCK_COUNT, BLOCK_LENGTH); ;
        printAssert(expected, result);
    }


    @DisplayName("The second index should not be zero if the given length is at least BLOCK_LENGTH squared")
    @Test
    void testSecondIndexRotation() {
        ProcessSubtaskMain p = new ProcessSubtaskMain();
        long input;
        byte[] expected;
        byte[] result;

        input = BLOCK_LENGTH * BLOCK_LENGTH;
        expected = initializeKeyBlockPositions_Reference(input, BLOCK_COUNT, BLOCK_LENGTH);
        result = p.initializeKeyBlockPositions_Testing(input, BLOCK_COUNT, BLOCK_LENGTH); ;
        printAssert(expected, result);

        input = (BLOCK_LENGTH * BLOCK_LENGTH) + BLOCK_LENGTH-1;
        expected = initializeKeyBlockPositions_Reference(input, BLOCK_COUNT, BLOCK_LENGTH);
        result = p.initializeKeyBlockPositions_Testing(input, BLOCK_COUNT, BLOCK_LENGTH); ;
        printAssert(expected, result);

        input = (BLOCK_LENGTH * BLOCK_LENGTH) + BLOCK_LENGTH;
        expected = initializeKeyBlockPositions_Reference(input, BLOCK_COUNT, BLOCK_LENGTH);
        result = p.initializeKeyBlockPositions_Testing(input, BLOCK_COUNT, BLOCK_LENGTH); ;
        printAssert(expected, result);
    }


    @DisplayName("Other indices should not be zero if the given length is at least BLOCK_LENGTH squared. " +
            "WARNING: this test may take a long time to run")
    @Test
    void testOtherIndexRotations() {
        ProcessSubtaskMain p = new ProcessSubtaskMain();
        long input;
        byte[] expected;
        byte[] result;

        input = (BLOCK_LENGTH * BLOCK_LENGTH * BLOCK_LENGTH);
        expected = initializeKeyBlockPositions_Reference(input, BLOCK_COUNT, BLOCK_LENGTH);
        result = p.initializeKeyBlockPositions_Testing(input, BLOCK_COUNT, BLOCK_LENGTH); ;
        printAssert(expected, result);

        input = (BLOCK_LENGTH * BLOCK_LENGTH * BLOCK_LENGTH) + BLOCK_LENGTH*2 - 1;
        expected = initializeKeyBlockPositions_Reference(input, BLOCK_COUNT, BLOCK_LENGTH);
        result = p.initializeKeyBlockPositions_Testing(input, BLOCK_COUNT, BLOCK_LENGTH); ;
        printAssert(expected, result);

        input = (BLOCK_LENGTH * BLOCK_LENGTH * BLOCK_LENGTH) + BLOCK_LENGTH*2;
        expected = initializeKeyBlockPositions_Reference(input, BLOCK_COUNT, BLOCK_LENGTH);
        result = p.initializeKeyBlockPositions_Testing(input, BLOCK_COUNT, BLOCK_LENGTH); ;
        printAssert(expected, result);

        input = (long)Math.pow(BLOCK_LENGTH,4) + BLOCK_LENGTH*5 - 1;
        expected = initializeKeyBlockPositions_Reference(input, BLOCK_COUNT, BLOCK_LENGTH);
        result = p.initializeKeyBlockPositions_Testing(input, BLOCK_COUNT, BLOCK_LENGTH); ;
        printAssert(expected, result);

        input = (long)Math.pow(BLOCK_LENGTH,4) + BLOCK_LENGTH*5;
        expected = initializeKeyBlockPositions_Reference(input, BLOCK_COUNT, BLOCK_LENGTH);
        result = p.initializeKeyBlockPositions_Testing(input, BLOCK_COUNT, BLOCK_LENGTH); ;
        printAssert(expected, result);
    }


    @DisplayName("The output should reset to all zeros if overflowed")
    @Test
    void testOverflow() {
        ProcessSubtaskMain p = new ProcessSubtaskMain();
        long input;
        byte[] expected;
        byte[] result;

        input = (long)Math.pow(BLOCK_LENGTH, BLOCK_COUNT+1) + (BLOCK_LENGTH * BLOCK_LENGTH) + BLOCK_LENGTH*2 - 1;
        if(input < 0) {
            System.err.println("testOverflow- maximum value tested overflows long limit (max value: " + input + ")");
            return;
        }

        //Barely overflows, should be all zeros
        input = (long)Math.pow(BLOCK_LENGTH, BLOCK_COUNT+1);
        expected = initializeKeyBlockPositions_Reference(0, BLOCK_COUNT, BLOCK_LENGTH);
        result = p.initializeKeyBlockPositions_Testing(input, BLOCK_COUNT, BLOCK_LENGTH); ;
        printAssert(expected, result);

        //Overflow + 1, should be all zeros
        input = (long)Math.pow(BLOCK_LENGTH, BLOCK_COUNT+1) + 1;
        expected = initializeKeyBlockPositions_Reference(input - (long)Math.pow(BLOCK_LENGTH, BLOCK_COUNT+1), BLOCK_COUNT, BLOCK_LENGTH);
        result = p.initializeKeyBlockPositions_Testing(input, BLOCK_COUNT, BLOCK_LENGTH);
        printAssert(expected, result);

        //Overflow with some rotation, equivalent to a length of (BLOCK_LEN * BLOCK_LEN) + BLOCK_LEN*2 - 1
        input = (long)Math.pow(BLOCK_LENGTH, BLOCK_COUNT+1) + (BLOCK_LENGTH * BLOCK_LENGTH) + BLOCK_LENGTH*2 - 1;
        expected = initializeKeyBlockPositions_Reference(input - (long)Math.pow(BLOCK_LENGTH, BLOCK_COUNT+1), BLOCK_COUNT, BLOCK_LENGTH);
        result = p.initializeKeyBlockPositions_Testing(input, BLOCK_COUNT, BLOCK_LENGTH); ;
        printAssert(expected, result);
    }

}
