package com.hanif.expensetracker.utils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Utility class for handling encryption operations using AES and RSA algorithms
 * through the Android KeyStore system.
 */

/**
 * We can also store keys in the local proper for security.
 */
const val AES_KEY_ALIAS = "password_encryption_key"
 const val RSA_KEY_ALIAS = "password_master_key"
 const val SALT_SIZE = 16

class EncryptionUtils {

    companion object {
        private const val ANDROID_KEY_STORE = "AndroidKeyStore"
        private const val AES_MODE = "${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_GCM}/${KeyProperties.ENCRYPTION_PADDING_NONE}"
        private const val RSA_MODE = "${KeyProperties.KEY_ALGORITHM_RSA}/${KeyProperties.BLOCK_MODE_ECB}/${KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1}"
        private const val AES_KEY_SIZE = 256
        private const val RSA_KEY_SIZE = 2048
        private const val GCM_TAG_LENGTH = 128
    }

    /**
     * AES encryption implementation
     */
    object AES {
        /**
         * Generate or retrieve an AES key from the Android KeyStore
         */
        fun getOrCreateKey(keyAlias: String): SecretKey {
            val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
            keyStore.load(null)

            // Check if the key already exists
            if (keyStore.containsAlias(keyAlias)) {
                val entry = keyStore.getEntry(keyAlias, null) as KeyStore.SecretKeyEntry
                return entry.secretKey
            }

            // Create a new key if it doesn't exist
            val keyGenSpec = KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setKeySize(AES_KEY_SIZE)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()

            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEY_STORE
            )
            keyGenerator.init(keyGenSpec)
            return keyGenerator.generateKey()
        }

        /**
         * Encrypt data using AES-GCM
         */
        fun encrypt(plaintext: String, keyAlias: String): EncryptedData {
            val key = getOrCreateKey(keyAlias)
            val cipher = Cipher.getInstance(AES_MODE)
            cipher.init(Cipher.ENCRYPT_MODE, key)

            val encryptedBytes = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
            val iv = cipher.iv

            return EncryptedData(
                encryptedData = Base64.encodeToString(encryptedBytes, Base64.DEFAULT),
                iv = Base64.encodeToString(iv, Base64.DEFAULT)
            )
        }

        /**
         * Decrypt data using AES-GCM
         */
        fun decrypt(encryptedData: EncryptedData, keyAlias: String): String {
            val key = getOrCreateKey(keyAlias)
            val cipher = Cipher.getInstance(AES_MODE)

            val iv = Base64.decode(encryptedData.iv, Base64.DEFAULT)
            val encryptedBytes = Base64.decode(encryptedData.encryptedData, Base64.DEFAULT)

            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, spec)

            val decryptedBytes = cipher.doFinal(encryptedBytes)
            return String(decryptedBytes, Charsets.UTF_8)
        }
    }

    /**
     * RSA encryption implementation
     */
    object RSA {
        /**
         * Generate or retrieve an RSA key pair from the Android KeyStore
         */
        fun getOrCreateKeyPair(keyAlias: String): KeyPair {
            val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
            keyStore.load(null)

            // Check if the key already exists
            if (keyStore.containsAlias(keyAlias)) {
                val privateKey = keyStore.getKey(keyAlias, null) as PrivateKey
                val publicKey = keyStore.getCertificate(keyAlias).publicKey
                return KeyPair(publicKey, privateKey)
            }

            // Create a new key pair if it doesn't exist
            val keyGenSpec = KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setKeySize(RSA_KEY_SIZE)
                .setBlockModes(KeyProperties.BLOCK_MODE_ECB)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                .build()

            val keyPairGenerator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_RSA,
                ANDROID_KEY_STORE
            )
            keyPairGenerator.initialize(keyGenSpec)
            return keyPairGenerator.generateKeyPair()
        }

        /**
         * Encrypt data using RSA
         */
        fun encrypt(plaintext: String, keyAlias: String): String {
            val keyPair = getOrCreateKeyPair(keyAlias)
            val cipher = Cipher.getInstance(RSA_MODE)
            cipher.init(Cipher.ENCRYPT_MODE, keyPair.public)

            val encryptedBytes = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
            return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
        }

        /**
         * Decrypt data using RSA
         */
        fun decrypt(encryptedData: String, keyAlias: String): String {
            val keyPair = getOrCreateKeyPair(keyAlias)
            val cipher = Cipher.getInstance(RSA_MODE)
            cipher.init(Cipher.DECRYPT_MODE, keyPair.private)

            val encryptedBytes = Base64.decode(encryptedData, Base64.DEFAULT)
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            return String(decryptedBytes, Charsets.UTF_8)
        }
    }

    /**
     * Data class to hold encrypted data and its initialization vector
     */
    data class EncryptedData(
        val encryptedData: String,
        val iv: String
    )
}