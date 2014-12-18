package cz.d1x.dxcrypto.hash;

import cz.d1x.dxcrypto.common.Encoding;
import cz.d1x.dxcrypto.common.HexRepresentation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Generic test for all {@link HashingAlgorithm} implementations using default encoding.
 * With these implementations, {@link RepeatingDecorator} is tested too.
 *
 * @author Zdenek Obst, zdenek.obst-at-gmail.com
 */
public abstract class HashingAlgorithmTest {

    protected static final int REPEATS_COUNT = 3;
    protected static final String[] INPUTS = new String[]{
            "Toto-jePrvni.vstupPro_h@sh",
            "0111111111101101111111111111111111111",
            ""
    };

    protected HashingAlgorithm algorithm;

    /**
     * Gets a hashing algorithm that will be tested.
     *
     * @return algorithm to be tested
     */
    protected abstract HashingAlgorithm getAlgorithm();

    /**
     * Gets expected results for simple hashing.
     * You should take have results from any other source (e.g. online tool).
     *
     * @return expected results for simple
     */
    protected abstract String[] getExpectedSimpleOutputs();

    /**
     * Gets expected results for repeated hashing.
     * You should take have results from any other source (e.g. online tool).
     *
     * @return expected results for repeated hashing
     */
    protected abstract String[] getRepeatedOutputs();

    @Before
    public void setUp() {
        this.algorithm = getAlgorithm();
    }

    /**
     * Tests that null for hashing throws exception.
     */
    @Test(expected = HashingException.class)
    public void nullHashing() {
        algorithm.hash((String) null);
    }

    /**
     * Tests simple string hashing.
     */
    @Test
    public void simpleHashing() {
        String[] expected = getExpectedSimpleOutputs();
        Assert.assertEquals("Expected outputs must have same length as inputs", INPUTS.length, expected.length);
        int i = 0;
        for (String input : INPUTS) {
            testHash(expected[i], algorithm.hash(input), i);
            i++;
        }
    }

    /**
     * Tests repeating decorator.
     */
    @Test
    public void repeatingHashing() {
        // in "normal" world it is not recommended to use this builder but rather HashingAlgorithms factory for these builders
        HashingAlgorithm repeatedAlgorithm = new RepeatingDecoratorBuilder(algorithm, new HexRepresentation(), Encoding.DEFAULT)
                .repeats(REPEATS_COUNT)
                .build();
        String[] expected = getRepeatedOutputs();
        Assert.assertEquals("Expected outputs must have same length as inputs", INPUTS.length, expected.length);
        int i = 0;
        for (String input : INPUTS) {
            testHash(expected[i], repeatedAlgorithm.hash(input), i);
            i++;
        }
    }

    /**
     * Tests usage of salt returns different hash for same input but different hash.
     */
    @Test
    public void saltingWithDefaultCombineStrategy() {
        // in "normal" world it is not recommended to use this builder but rather HashingAlgorithms factory for these builders
        SaltingAdapter adapter = new SaltingAdapterBuilder(algorithm, new HexRepresentation(), Encoding.DEFAULT)
                .build();
        String input = INPUTS[0];
        String hash1 = adapter.hash(input, "s@Lt1");
        String hash2 = adapter.hash(input, "s@Lt2");
        assertNotEquals("Same inputs with different salt must have different hash", hash1, hash2);
    }

    protected void testHash(String expectedHash, String actualHash, int idx) {
        assertNotNull("Expecting non-null hash", actualHash);
        assertEquals("Expecting same hashes idx=" + idx + " algorithm=" + algorithm.getClass().getSimpleName(), expectedHash, actualHash);
    }
}
