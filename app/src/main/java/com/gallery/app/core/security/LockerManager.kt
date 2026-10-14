package com.gallery.app.core.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.*
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class LockerManager(private val context: Context) {
    
    companion object {
        private const val KEY_ALIAS = "gallery_locker_key"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
        private const val IV_SIZE = 12
    }
    
    private val lockerDir: File by lazy {
        File(context.filesDir, "locker").also { it.mkdirs() }
    }
    
    private val thumbnailDir: File by lazy {
        File(context.filesDir, "locker_thumbnails").also { it.mkdirs() }
    }
    
    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        
        val existingKey = keyStore.getEntry(KEY_ALIAS, null)
        if (existingKey is KeyStore.SecretKeyEntry) {
            return existingKey.secretKey
        }
        
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE
        )
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(false)
            .build()
        
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }
    
    fun encryptFile(inputStream: InputStream, outputFileName: String): String {
        val key = getOrCreateKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        
        val outputFile = File(lockerDir, outputFileName)
        FileOutputStream(outputFile).use { fos ->
            // Write IV first
            fos.write(iv)
            
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                val encrypted = cipher.update(buffer, 0, bytesRead)
                if (encrypted != null) fos.write(encrypted)
            }
            val finalBlock = cipher.doFinal()
            if (finalBlock != null) fos.write(finalBlock)
        }
        inputStream.close()
        return outputFile.absolutePath
    }
    
    fun decryptFile(encryptedFileName: String, outputStream: OutputStream) {
        val key = getOrCreateKey()
        val inputFile = File(lockerDir, encryptedFileName)
        
        FileInputStream(inputFile).use { fis ->
            val iv = ByteArray(IV_SIZE)
            fis.read(iv)
            
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, key, spec)
            
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                val decrypted = cipher.update(buffer, 0, bytesRead)
                if (decrypted != null) outputStream.write(decrypted)
            }
            val finalBlock = cipher.doFinal()
            if (finalBlock != null) outputStream.write(finalBlock)
        }
        outputStream.flush()
        outputStream.close()
    }
    
    fun deleteEncryptedFile(encryptedFileName: String): Boolean {
        val file = File(lockerDir, encryptedFileName)
        return file.delete()
    }
    
    fun getDecryptedTempFile(encryptedFileName: String): File {
        val tempDir = File(context.cacheDir, "locker_temp").also { it.mkdirs() }
        val safeName = encryptedFileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        val tempFile = File(tempDir, "dec_$safeName")
        if (tempFile.exists() && tempFile.length() > 0) {
            return tempFile
        }
        decryptFile(encryptedFileName, FileOutputStream(tempFile))
        return tempFile
    }

    fun restoreToGallery(item: com.gallery.app.core.database.entity.LockerItemEntity): android.net.Uri? {
        val contentValues = android.content.ContentValues().apply {
            put(
                android.provider.MediaStore.MediaColumns.DISPLAY_NAME,
                item.displayName.ifEmpty { "restored_${System.currentTimeMillis()}.${if (item.mimeType.contains("video")) "mp4" else "jpg"}" }
            )
            put(android.provider.MediaStore.MediaColumns.MIME_TYPE, item.mimeType)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_PICTURES + "/Gallery_Restored")
                put(android.provider.MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }
        val tableUri = if (item.mimeType.startsWith("video/")) {
            android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        } else {
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
        val newUri = context.contentResolver.insert(tableUri, contentValues) ?: return null
        try {
            context.contentResolver.openOutputStream(newUri)?.use { os ->
                decryptFile(item.encryptedFileName, os)
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(android.provider.MediaStore.MediaColumns.IS_PENDING, 0)
                context.contentResolver.update(newUri, contentValues, null, null)
            }
            deleteEncryptedFile(item.encryptedFileName)
            item.encryptedThumbnailName?.let { deleteEncryptedFile(it) }
            return newUri
        } catch (e: Exception) {
            android.util.Log.e("LockerManager", "Error restoring file", e)
            return null
        }
    }
    
    fun cleanupTempFiles() {
        val tempDir = File(context.cacheDir, "locker_temp")
        tempDir.deleteRecursively()
        context.cacheDir.listFiles()?.filter { it.name.startsWith("locker_temp_") }?.forEach { it.delete() }
    }
}
