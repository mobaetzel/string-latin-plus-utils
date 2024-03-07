package de.btzl.stringlatinplusutils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StringLatinPlusUtilsTest {
    @Test
    void transform() {
        assertEquals(
                "$$$$",
                StringLatinPlusUtils
                        .build()
                        .withInvalidChar('$')
                        .transform("1234")
        );

        assertEquals(
                "SS",
                StringLatinPlusUtils
                        .build()
                        .withAllCharClasses()
                        .transform("ß")
        );

        assertEquals(
                "AE",
                StringLatinPlusUtils
                        .build()
                        .withAllCharClasses()
                        .transform("ä")
        );

        assertEquals(
                "O",
                StringLatinPlusUtils
                        .build()
                        .withAllCharClasses()
                        .transform("Ő")
        );
    }
}