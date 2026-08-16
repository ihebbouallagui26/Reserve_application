package com.rezkna.identity.account;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;

import static org.assertj.core.api.Assertions.assertThat;

class OtpGeneratorTest {

    private final OtpGenerator generator = new OtpGenerator();

    @RepeatedTest(50)
    void generatesExactlySixDigits() {
        String code = generator.generate();
        assertThat(code).hasSize(6);
        assertThat(code).matches("[0-9]{6}");
    }

    @Test
    void neverStripsLeadingZeros() {
        // With a 6-digit zero-padded format, values below 100000 must keep their
        // leading zeros - run enough iterations that at least one low value appears.
        boolean sawLeadingZero = false;
        for (int i = 0; i < 2000 && !sawLeadingZero; i++) {
            if (generator.generate().startsWith("0")) {
                sawLeadingZero = true;
            }
        }
        assertThat(sawLeadingZero).isTrue();
    }
}
