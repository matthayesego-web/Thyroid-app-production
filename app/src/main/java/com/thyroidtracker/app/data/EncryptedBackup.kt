package com.thyroidtracker.app.data

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.security.SecureRandom
import java.util.Arrays
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

internal object EncryptedBackup {
    private val magic = byteArrayOf(0x54, 0x45, 0x42, 0x4B, 0x01) // TEBK + format v1
    private const val saltSize = 16
    private const val nonceSize = 12
    private const val keyBits = 256
    private const val iterations = 310_000

    fun encrypt(plainText: String, password: CharArray): ByteArray {
        require(password.size >= 8) { "Backup password must be at least 8 characters" }
        val random = SecureRandom()
        val salt = ByteArray(saltSize).also(random::nextBytes)
        val nonce = ByteArray(nonceSize).also(random::nextBytes)
        val key = deriveKey(password, salt, iterations)

        return try {
            val compressed = gzip(plainText.toByteArray(Charsets.UTF_8))
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, nonce))
            cipher.updateAAD(magic)
            val encrypted = cipher.doFinal(compressed)

            ByteArrayOutputStream().use { output ->
                DataOutputStream(output).use { data ->
                    data.write(magic)
                    data.writeInt(iterations)
                    data.write(salt)
                    data.write(nonce)
                    data.write(encrypted)
                }
                output.toByteArray()
            }
        } finally {
            Arrays.fill(password, '\u0000')
            key.encoded?.let { Arrays.fill(it, 0) }
        }
    }

    fun decrypt(bytes: ByteArray, password: CharArray): String {
        require(bytes.size > magic.size + 4 + saltSize + nonceSize + 16) { "Backup file is too small" }

        return try {
            DataInputStream(ByteArrayInputStream(bytes)).use { data ->
                val fileMagic = ByteArray(magic.size).also(data::readFully)
                require(fileMagic.contentEquals(magic)) { "Not a Thyroid Echo encrypted backup" }

                val fileIterations = data.readInt()
                require(fileIterations in 100_000..2_000_000) { "Unsupported backup security parameters" }
                val salt = ByteArray(saltSize).also(data::readFully)
                val nonce = ByteArray(nonceSize).also(data::readFully)
                val encrypted = data.readBytes()
                val key = deriveKey(password, salt, fileIterations)

                try {
                    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                    cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, nonce))
                    cipher.updateAAD(magic)
                    val compressed = cipher.doFinal(encrypted)
                    ungzip(compressed).toString(Charsets.UTF_8)
                } finally {
                    key.encoded?.let { Arrays.fill(it, 0) }
                }
            }
        } finally {
            Arrays.fill(password, '\u0000')
        }
    }

    private fun deriveKey(password: CharArray, salt: ByteArray, rounds: Int): SecretKeySpec {
        val spec = PBEKeySpec(password, salt, rounds, keyBits)
        return try {
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            SecretKeySpec(factory.generateSecret(spec).encoded, "AES")
        } finally {
            spec.clearPassword()
        }
    }

    private fun gzip(input: ByteArray): ByteArray = ByteArrayOutputStream().use { output ->
        GZIPOutputStream(output).use { it.write(input) }
        output.toByteArray()
    }

    private fun ungzip(input: ByteArray): ByteArray = GZIPInputStream(ByteArrayInputStream(input)).use { it.readBytes() }
}
