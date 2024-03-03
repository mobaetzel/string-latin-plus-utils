package de.btzl.stringlatinplusutils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StringLatinPlusUtilsTest {

    @Test
    void transform() {
        assertEquals(
                "HELLO WORLD",
                StringLatinPlusUtils
                        .build()
                        .withAllCharClasses()
                        .withInvalidChar('$')
                        .transform("Hello World")
        );

        assertEquals(
                "HELLO WORLD",
                StringLatinPlusUtils
                        .build()
                        .withAllCharClasses()
                        .transform("Héllo World")
        );
    }
}