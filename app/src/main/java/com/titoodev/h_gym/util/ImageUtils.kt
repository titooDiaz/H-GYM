package com.titoodev.h_gym.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object ImageUtils {
    /**
     * Compresses a given image Uri down to <= 100 KB and saves permanently in filesDir/meal_photos/
     */
    fun compressImageTo100Kb(context: Context, imageUri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri) ?: return null
            var bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close() ?: return null

            // Resize dimensions if bitmap is large
            val maxDimension = 1024
            if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
                val scale = maxDimension.toFloat() / Math.max(bitmap.width, bitmap.height)
                bitmap = Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width * scale).toInt(),
                    (bitmap.height * scale).toInt(),
                    true
                )
            }

            var quality = 90
            val outputStream = ByteArrayOutputStream()
            do {
                outputStream.reset()
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                quality -= 10
            } while (outputStream.toByteArray().size > 100 * 1024 && quality > 10)

            val photosDir = File(context.filesDir, "meal_photos").apply {
                if (!exists()) mkdirs()
            }

            val compressedFile = File(photosDir, "meal_${System.currentTimeMillis()}.jpg")
            val fileOutputStream = FileOutputStream(compressedFile)
            fileOutputStream.write(outputStream.toByteArray())
            fileOutputStream.flush()
            fileOutputStream.close()

            compressedFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Creates a temporary camera output file and Uri using FileProvider
     */
    fun createTempCameraUri(context: Context): Pair<File, Uri>? {
        return try {
            val tempFile = File.createTempFile(
                "camera_temp_${System.currentTimeMillis()}",
                ".jpg",
                context.cacheDir
            )
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempFile
            )
            Pair(tempFile, uri)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
