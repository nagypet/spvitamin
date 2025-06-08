/*
 * Copyright 2020-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hu.perit.spvitamin.core.crypto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link CryptoUtil} class
 */
class CryptoUtilTest
{

    private CryptoUtil cryptoUtil;
    private static final String SECRET_KEY = "testSecretKey";
    private static final String PLAIN_TEXT = "This is a test message to encrypt";


    @BeforeEach
    void setUp()
    {
        cryptoUtil = new CryptoUtil();
    }


    @Test
    void encrypt_withValidInput_returnsEncryptedText()
    {
        // Act
        String encryptedText = cryptoUtil.encrypt(SECRET_KEY, PLAIN_TEXT);

        // Assert
        assertThat(encryptedText).isNotNull();
        assertThat(encryptedText).isNotEqualTo(PLAIN_TEXT);
    }


    @Test
    void encrypt_withNullSecretKey_returnsNull()
    {
        // Act
        String encryptedText = cryptoUtil.encrypt(null, PLAIN_TEXT);

        // Assert
        assertThat(encryptedText).isNull();
    }


    @Test
    void encrypt_withEmptySecretKey_returnsNull()
    {
        // Act
        String encryptedText = cryptoUtil.encrypt("", PLAIN_TEXT);

        // Assert
        assertThat(encryptedText).isNull();
    }


    @Test
    void encrypt_withBlankSecretKey_returnsNull()
    {
        // Act
        String encryptedText = cryptoUtil.encrypt("   ", PLAIN_TEXT);

        // Assert
        assertThat(encryptedText).isNull();
    }


    @Test
    void encrypt_withNullPlainText_returnsNull()
    {
        // Act
        String encryptedText = cryptoUtil.encrypt(SECRET_KEY, null);

        // Assert
        assertThat(encryptedText).isNull();
    }


    @Test
    void encrypt_withEmptyPlainText_returnsNull()
    {
        // Act
        String encryptedText = cryptoUtil.encrypt(SECRET_KEY, "");

        // Assert
        assertThat(encryptedText).isNull();
    }


    @Test
    void encrypt_withBlankPlainText_returnsNull()
    {
        // Act
        String encryptedText = cryptoUtil.encrypt(SECRET_KEY, "   ");

        // Assert
        assertThat(encryptedText).isNull();
    }


    @Test
    void decrypt_withValidInput_returnsOriginalText()
    {
        // Arrange
        String encryptedText = cryptoUtil.encrypt(SECRET_KEY, PLAIN_TEXT);

        // Act
        String decryptedText = cryptoUtil.decrypt(SECRET_KEY, encryptedText);

        // Assert
        assertThat(decryptedText).isNotNull();
        assertThat(decryptedText).isEqualTo(PLAIN_TEXT);
    }


    @Test
    void decrypt_withNullSecretKey_returnsNull()
    {
        // Arrange
        String encryptedText = cryptoUtil.encrypt(SECRET_KEY, PLAIN_TEXT);

        // Act
        String decryptedText = cryptoUtil.decrypt(null, encryptedText);

        // Assert
        assertThat(decryptedText).isNull();
    }


    @Test
    void decrypt_withEmptySecretKey_returnsNull()
    {
        // Arrange
        String encryptedText = cryptoUtil.encrypt(SECRET_KEY, PLAIN_TEXT);

        // Act
        String decryptedText = cryptoUtil.decrypt("", encryptedText);

        // Assert
        assertThat(decryptedText).isNull();
    }


    @Test
    void decrypt_withBlankSecretKey_returnsNull()
    {
        // Arrange
        String encryptedText = cryptoUtil.encrypt(SECRET_KEY, PLAIN_TEXT);

        // Act
        String decryptedText = cryptoUtil.decrypt("   ", encryptedText);

        // Assert
        assertThat(decryptedText).isNull();
    }


    @Test
    void decrypt_withNullEncryptedText_returnsNull()
    {
        // Act
        String decryptedText = cryptoUtil.decrypt(SECRET_KEY, null);

        // Assert
        assertThat(decryptedText).isNull();
    }


    @Test
    void decrypt_withEmptyEncryptedText_returnsNull()
    {
        // Act
        String decryptedText = cryptoUtil.decrypt(SECRET_KEY, "");

        // Assert
        assertThat(decryptedText).isNull();
    }


    @Test
    void decrypt_withBlankEncryptedText_returnsNull()
    {
        // Act
        String decryptedText = cryptoUtil.decrypt(SECRET_KEY, "   ");

        // Assert
        assertThat(decryptedText).isNull();
    }


    @Test
    void decrypt_withWrongSecretKey_throwsCryptoException()
    {
        // Arrange
        String encryptedText = cryptoUtil.encrypt(SECRET_KEY, PLAIN_TEXT);
        String wrongKey = "wrongKey";

        // Act & Assert
        assertThatThrownBy(() -> cryptoUtil.decrypt(wrongKey, encryptedText))
                .isInstanceOf(CryptoException.class);
    }


    @Test
    void decrypt_withInvalidEncryptedText_throwsCryptoException()
    {
        // Arrange
        String invalidEncryptedText = "ThisIsNotValidBase64EncodedEncryptedText";

        // Act & Assert
        assertThatThrownBy(() -> cryptoUtil.decrypt(SECRET_KEY, invalidEncryptedText))
                .isInstanceOf(CryptoException.class);
    }


    @Test
    void encryptAndDecrypt_withSpecialCharacters_worksCorrectly()
    {
        // Arrange
        String specialText = "Special characters: !@#$%^&*()_+{}|:<>?~`-=[]\\;',./";

        // Act
        String encryptedText = cryptoUtil.encrypt(SECRET_KEY, specialText);
        String decryptedText = cryptoUtil.decrypt(SECRET_KEY, encryptedText);

        // Assert
        assertThat(decryptedText).isEqualTo(specialText);
    }


    @Test
    void encryptAndDecrypt_withUnicodeCharacters_worksCorrectly()
    {
        // Arrange
        String unicodeText = "Unicode characters: 你好, こんにちは, 안녕하세요, Привет, مرحبا";

        // Act
        String encryptedText = cryptoUtil.encrypt(SECRET_KEY, unicodeText);
        String decryptedText = cryptoUtil.decrypt(SECRET_KEY, encryptedText);

        // Assert
        assertThat(decryptedText).isEqualTo(unicodeText);
    }
}
