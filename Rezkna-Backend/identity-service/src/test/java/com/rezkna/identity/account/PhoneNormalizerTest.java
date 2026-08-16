package com.rezkna.identity.account;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PhoneNormalizerTest {

    @Test
    void addsTunisiaCountryCodeToLocalNumber() {
        assertThat(PhoneNormalizer.normalize("12345678")).isEqualTo("21612345678");
    }

    @Test
    void stripsNonDigitCharacters() {
        assertThat(PhoneNormalizer.normalize("12 345 678")).isEqualTo("21612345678");
    }

    @Test
    void leavesAlreadyInternationalNumberUnchanged() {
        assertThat(PhoneNormalizer.normalize("21612345678")).isEqualTo("21612345678");
    }

    @Test
    void stripsLeadingInternationalPrefix() {
        assertThat(PhoneNormalizer.normalize("0021612345678")).isEqualTo("21612345678");
    }

    @Test
    void returnsNullForNullInput() {
        assertThat(PhoneNormalizer.normalize(null)).isNull();
    }

    @Test
    void tailReturnsLast8Digits() {
        assertThat(PhoneNormalizer.tail("21612345678")).isEqualTo("12345678");
    }

    @Test
    void tailReturnsInputUnchangedWhenAlreadyShort() {
        assertThat(PhoneNormalizer.tail("12345678")).isEqualTo("12345678");
    }
}
