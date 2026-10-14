package com.gallery.app.core.security

import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec

class CryptoCipherTest {

    @Test
    fun testAesGcmEncryptionDecryptionIntegrity() {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256)
        val secretKey = keyGen.generateKey()

        val plaintext = "Gallery Confidential Photo Data - 2026".toByteArray(Charsets.UTF_8)

        // Encrypt with AES/GCM/NoPadding
        val cipherEncrypt = Cipher.getInstance("AES/GCM/NoPadding")
        cipherEncrypt.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipherEncrypt.iv
        assertEquals(12, iv.size) // 12-byte standard GCM IV

        val ciphertext = cipherEncrypt.doFinal(plaintext)
        assertFalse(plaintext.contentEquals(ciphertext))

        // Decrypt
        val cipherDecrypt = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, iv)
        cipherDecrypt.init(Cipher.DECRYPT_MODE, secretKey, spec)
        val decrypted = cipherDecrypt.doFinal(ciphertext)

        assertArrayEquals(plaintext, decrypted)
        assertEquals("Gallery Confidential Photo Data - 2026", String(decrypted, Charsets.UTF_8))
    }

    @Test
    fun testAesGcmTamperDetection() {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256)
        val secretKey = keyGen.generateKey()

        val plaintext = "Tamper check payload".toByteArray(Charsets.UTF_8)
        val cipherEncrypt = Cipher.getInstance("AES/GCM/NoPadding")
        cipherEncrypt.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipherEncrypt.iv
        val ciphertext = cipherEncrypt.doFinal(plaintext)

        // Tamper with one byte of ciphertext
        ciphertext[0] = (ciphertext[0].toInt() xor 0xFF).toByte()

        val cipherDecrypt = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, iv)
        cipherDecrypt.init(Cipher.DECRYPT_MODE, secretKey, spec)

        // GCM authentication tag verification must fail on tampered data
        try {
            cipherDecrypt.doFinal(ciphertext)
            fail("Decryption of tampered ciphertext should have thrown an AEADBadTagException")
        } catch (e: Exception) {
            assertTrue(e is javax.crypto.AEADBadTagException || e.cause is javax.crypto.AEADBadTagException)
        }
    }
}
