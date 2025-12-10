import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.opentest4j.AssertionFailedError;
import stepperfx.threading.ProcessSubtaskMain;

import java.util.Arrays;
import java.util.stream.Stream;


/**
 * Class to test the method {@code initializeKeyBlockPositions} in a {@code ProcessSubtaskMain},
 * over several possible block counts and block lengths.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class InitKeyBlockPositionsTest {


    //UTILITIES

    /**
     * Returns the key block positions for the given text length for unenhanced (v1) operations.<br><br>
     *
     * This is a reference implementation that is much slower than the production version.
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


    /**
     * Checks if {@code result} is non-null, has the same length as {@code expected}, and matches each element of {@code expected}.
     * If not, throws {@code AssertionFailedError}.<br><br>
     *
     * Preferred over {@code printAssert} because, on assertion failure, this function prints the entire expected and result array,
     * not just the elements that are mismatched.
     *
     * @param expected what the test output should be. Cannot be null.
     * @param result output from the test
     */
    private static void printAssert(byte[] expected, byte[] result) {
        if(expected == null) throw new AssertionError("Expected cannot be null");

        //Null check
        if(result == null) throw new AssertionFailedError("Result cannot be null");

        //Length check
        if(expected.length != result.length) {
            System.err.println("Expected: " + Arrays.toString(expected));
            System.err.println("Result:   " + Arrays.toString(result));
            throw new AssertionFailedError("Expected length (" + expected.length + ") and result length (" + result.length + ") are not equal");
        }

        //Element check
        for (int i = 0; i < expected.length; i++) {
            if(expected[i] != result[i]) {
                System.err.println("Expected: " + Arrays.toString(expected));
                System.err.println("Result:   " + Arrays.toString(result));
                throw new AssertionFailedError("Expected and result do not match at index " + i);
            }
        }
    }



    // //////////////////////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////////////////////
    //TESTS

    /**
     * Returns a stream of arguments used for block counts and block lengths tested.
     * @return argument stream for variable block count/length tests
     */
    private Stream<Arguments> sharedParameterProvider() {
        return Stream.of(
                Arguments.of(6, 25),
                Arguments.of(3, 15),
                Arguments.of(15, 2),
                Arguments.of(3, 3),
                Arguments.of(3, 1),
                Arguments.of(1, 3),
                Arguments.of(1, 1)
        );
    }



    @DisplayName("The output should be all zeros when the given text length is less than blockLength")
    @ParameterizedTest
    @MethodSource("sharedParameterProvider")
    void testNoRotation(int blockCount, int blockLength) {
        ProcessSubtaskMain p = new ProcessSubtaskMain();
        long input;
        byte[] expected;
        byte[] result;

        input = 0;
        expected = initializeKeyBlockPositions_Reference(input, blockCount, blockLength);
        result = p.initializeKeyBlockPositions_Testing(input, blockCount, blockLength);
        printAssert(expected, result);

        input = 3;
        expected = initializeKeyBlockPositions_Reference(input, blockCount, blockLength);
        result = p.initializeKeyBlockPositions_Testing(input, blockCount, blockLength); ;
        printAssert(expected, result);

        input = blockLength - 1;
        expected = initializeKeyBlockPositions_Reference(input, blockCount, blockLength);
        result = p.initializeKeyBlockPositions_Testing(input, blockCount, blockLength); ;
        printAssert(expected, result);
    }


    @DisplayName("The output should be all zeros, except for the first index, which should be 1, " +
            "when the length is between blockLength and 2*blockLength-1")
    @ParameterizedTest
    @MethodSource("sharedParameterProvider")
    void testSingleRotation(int blockCount, int blockLength) {
        ProcessSubtaskMain p = new ProcessSubtaskMain();
        long input;
        byte[] expected;
        byte[] result;

        input = blockLength;
        expected = initializeKeyBlockPositions_Reference(input, blockCount, blockLength);
        result = p.initializeKeyBlockPositions_Testing(input, blockCount, blockLength); ;
        printAssert(expected, result);

        input = blockLength+1;
        expected = initializeKeyBlockPositions_Reference(input, blockCount, blockLength);
        result = p.initializeKeyBlockPositions_Testing(input, blockCount, blockLength); ;
        printAssert(expected, result);

        input = (long)blockLength*2-1;
        expected = initializeKeyBlockPositions_Reference(input, blockCount, blockLength);
        result = p.initializeKeyBlockPositions_Testing(input, blockCount, blockLength); ;
        printAssert(expected, result);
    }


    @DisplayName("The output should be all zeros, except for the first index, when the length is between " +
            "blockLength and blockLength^2")
    @ParameterizedTest
    @MethodSource("sharedParameterProvider")
    void testFirstIndexRotation(int blockCount, int blockLength) {
        ProcessSubtaskMain p = new ProcessSubtaskMain();
        long input;
        byte[] expected;
        byte[] result;

        input = (long)blockLength*2;
        expected = initializeKeyBlockPositions_Reference(input, blockCount, blockLength);
        result = p.initializeKeyBlockPositions_Testing(input, blockCount, blockLength); ;
        printAssert(expected, result);

        input = (long)blockLength*3 - 1;
        expected = initializeKeyBlockPositions_Reference(input, blockCount, blockLength);
        result = p.initializeKeyBlockPositions_Testing(input, blockCount, blockLength); ;
        printAssert(expected, result);

        input = (long)blockLength*3;
        expected = initializeKeyBlockPositions_Reference(input, blockCount, blockLength);
        result = p.initializeKeyBlockPositions_Testing(input, blockCount, blockLength); ;
        printAssert(expected, result);

        input = (long)blockLength*5 - 1;
        expected = initializeKeyBlockPositions_Reference(input, blockCount, blockLength);
        result = p.initializeKeyBlockPositions_Testing(input, blockCount, blockLength); ;
        printAssert(expected, result);

        input = (long)blockLength*5;
        expected = initializeKeyBlockPositions_Reference(input, blockCount, blockLength);
        result = p.initializeKeyBlockPositions_Testing(input, blockCount, blockLength); ;
        printAssert(expected, result);

        input = blockLength * (long)blockLength - 1;
        expected = initializeKeyBlockPositions_Reference(input, blockCount, blockLength);
        result = p.initializeKeyBlockPositions_Testing(input, blockCount, blockLength); ;
        printAssert(expected, result);
    }


    @DisplayName("The second index should not be zero if the given length is at least blockLength squared")
    @ParameterizedTest
    @MethodSource("sharedParameterProvider")
    void testSecondIndexRotation(int blockCount, int blockLength) {
        ProcessSubtaskMain p = new ProcessSubtaskMain();
        long input;
        byte[] expected;
        byte[] result;

        input = blockLength * (long)blockLength;
        expected = initializeKeyBlockPositions_Reference(input, blockCount, blockLength);
        result = p.initializeKeyBlockPositions_Testing(input, blockCount, blockLength); ;
        printAssert(expected, result);

        input = (blockLength * (long)blockLength) + blockLength-1;
        expected = initializeKeyBlockPositions_Reference(input, blockCount, blockLength);
        result = p.initializeKeyBlockPositions_Testing(input, blockCount, blockLength); ;
        printAssert(expected, result);

        input = (blockLength * (long)blockLength) + blockLength;
        expected = initializeKeyBlockPositions_Reference(input, blockCount, blockLength);
        result = p.initializeKeyBlockPositions_Testing(input, blockCount, blockLength); ;
        printAssert(expected, result);
    }


    @DisplayName("Other indices should not be zero if the given length is at least blockLength squared. " +
            "WARNING: this test may take a long time to run")
    @ParameterizedTest
    @MethodSource("sharedParameterProvider")
    void testOtherIndexRotations(int blockCount, int blockLength) {
        ProcessSubtaskMain p = new ProcessSubtaskMain();
        long input;
        byte[] expected;
        byte[] result;

        //Block length ^ 3
        input = (blockLength * (long)blockLength * (long)blockLength);
        expected = initializeKeyBlockPositions_Reference(input, blockCount, blockLength);
        result = p.initializeKeyBlockPositions_Testing(input, blockCount, blockLength); ;
        printAssert(expected, result);

        //Block length ^ 3 plus a little extra
        input = (blockLength * (long)blockLength * (long)blockLength) + (long)blockLength*2 - 1;
        expected = initializeKeyBlockPositions_Reference(input, blockCount, blockLength);
        result = p.initializeKeyBlockPositions_Testing(input, blockCount, blockLength); ;
        printAssert(expected, result);

        //Block length ^ 3 plus a little extra
        input = (blockLength * (long)blockLength * (long)blockLength) + (long)blockLength*2;
        expected = initializeKeyBlockPositions_Reference(input, blockCount, blockLength);
        result = p.initializeKeyBlockPositions_Testing(input, blockCount, blockLength); ;
        printAssert(expected, result);

        //Block length ^ 4 plus a little extra
        input = (long)Math.pow(blockLength,4) + (long)blockLength*5 - 1;
        expected = initializeKeyBlockPositions_Reference(input, blockCount, blockLength);
        result = p.initializeKeyBlockPositions_Testing(input, blockCount, blockLength); ;
        printAssert(expected, result);

        //Block length ^ 4 plus a little extra
        input = (long)Math.pow(blockLength,4) + (long)blockLength*5;
        expected = initializeKeyBlockPositions_Reference(input, blockCount, blockLength);
        result = p.initializeKeyBlockPositions_Testing(input, blockCount, blockLength); ;
        printAssert(expected, result);

        //Block length ^ block count
        input = (long)Math.pow(blockLength, blockCount);
        expected = initializeKeyBlockPositions_Reference(input, blockCount, blockLength);
        result = p.initializeKeyBlockPositions_Testing(input, blockCount, blockLength); ;
        printAssert(expected, result);

        //Block length ^ block count plus extra
        input = (long)Math.pow(blockLength, blockCount) + 1L;
        expected = initializeKeyBlockPositions_Reference(input, blockCount, blockLength);
        result = p.initializeKeyBlockPositions_Testing(input, blockCount, blockLength); ;
        printAssert(expected, result);
    }


    @DisplayName("The output should reset to all zeros if overflowed")
    @ParameterizedTest
    @MethodSource("sharedParameterProvider")
    void testOverflow(int blockCount, int blockLength) {
        ProcessSubtaskMain p = new ProcessSubtaskMain();
        long input;
        byte[] expected;
        byte[] result;

        input = (long)Math.pow(blockLength, blockCount+1) + ((long) blockLength * (long)blockLength) + blockLength*2L - 1;
        if(input < 0) {
            System.err.println("testOverflow- maximum input value tested (value: " + input + ") overflows long limit " +
                    "for block count=" + blockCount + ", block length=" + blockLength + ". Test passed by default.");
            return;
        }

        //Barely overflows, should be all zeros
        input = (long)Math.pow(blockLength, blockCount+1);
        expected = initializeKeyBlockPositions_Reference(0, blockCount, blockLength);
        result = p.initializeKeyBlockPositions_Testing(input, blockCount, blockLength);
        printAssert(expected, result);

        //Overflow + 1, should be all zeros
        input = (long)Math.pow(blockLength, blockCount+1) + 1;
        expected = initializeKeyBlockPositions_Reference(input - (long)Math.pow(blockLength, blockCount+1), blockCount, blockLength);
        result = p.initializeKeyBlockPositions_Testing(input, blockCount, blockLength);
        printAssert(expected, result);

        //Overflow with some rotation, equivalent to a length of (BLOCK_LEN * BLOCK_LEN) + BLOCK_LEN*2 - 1
        input = (long)Math.pow(blockLength, blockCount+1) + (blockLength * (long)blockLength) + (long)blockLength*2 - 1;
        expected = initializeKeyBlockPositions_Reference(input - (long)Math.pow(blockLength, blockCount+1), blockCount, blockLength);
        result = p.initializeKeyBlockPositions_Testing(input, blockCount, blockLength); ;
        printAssert(expected, result);
    }

}
