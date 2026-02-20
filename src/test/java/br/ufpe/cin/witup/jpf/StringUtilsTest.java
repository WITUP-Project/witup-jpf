package br.ufpe.cin.witup.jpf;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class StringUtilsTest {

    @Test
    public void testRequireEmptyOk() {
        StringUtils.requireEmpty("");
    }

    @Test
    public void testRequireEmptyThrows() {
        assertThrows(RuntimeException.class, () -> StringUtils.requireEmpty("a"));
        assertThrows(RuntimeException.class, () -> StringUtils.requireEmpty("hello"));
    }

    @Test
    public void testRequireLengthZeroOk() {
        StringUtils.requireLengthZero(0);
    }

    @Test
    public void testRequireLengthZeroThrows() {
        assertThrows(RuntimeException.class, () -> StringUtils.requireLengthZero(1));
        assertThrows(RuntimeException.class, () -> StringUtils.requireLengthZero(5));
    }
}
