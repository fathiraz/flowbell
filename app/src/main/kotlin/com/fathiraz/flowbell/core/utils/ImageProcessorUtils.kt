package com.fathiraz.flowbell.core.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.IOException

/**
 * Utility class for processing notification icons and converting them to various formats
 * for webhook payloads.
 */
class ImageProcessorUtils(
    private val context: Context
) {
    
    companion object {
        private const val MAX_ICON_SIZE_KB = 50 // 50KB limit for Base64 encoding
        private const val MAX_LARGE_ICON_SIZE_KB = 200 // 200KB limit for large icons
        private const val ICON_QUALITY = 85 // JPEG quality for compression
    }
    
    /**
     * Converts a notification icon to Base64 string if it's small enough
     */
    suspend fun convertIconToBase64(icon: Icon?): String? = withContext(Dispatchers.IO) {
        if (icon == null) return@withContext null
        
        try {
            val drawable = icon.loadDrawable(context) ?: return@withContext null
            val bitmap = drawableToBitmap(drawable)
            
            if (bitmap == null) {
                Timber.w("Failed to convert icon drawable to bitmap")
                return@withContext null
            }
            
            val compressedBitmap = compressBitmap(bitmap, MAX_ICON_SIZE_KB)
            val base64 = bitmapToBase64(compressedBitmap)
            
            Timber.d("Converted icon to Base64 (${base64.length} chars)")
            base64
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to convert icon to Base64")
            null
        }
    }
    
    /**
     * Converts a large notification icon to Base64 string if it's small enough
     */
    suspend fun convertLargeIconToBase64(icon: Icon?): String? = withContext(Dispatchers.IO) {
        if (icon == null) return@withContext null
        
        try {
            val drawable = icon.loadDrawable(context) ?: return@withContext null
            val bitmap = drawableToBitmap(drawable)
            
            if (bitmap == null) {
                Timber.w("Failed to convert large icon drawable to bitmap")
                return@withContext null
            }
            
            val compressedBitmap = compressBitmap(bitmap, MAX_LARGE_ICON_SIZE_KB)
            val base64 = bitmapToBase64(compressedBitmap)
            
            Timber.d("Converted large icon to Base64 (${base64.length} chars)")
            base64
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to convert large icon to Base64")
            null
        }
    }
    
    /**
     * Converts a drawable to bitmap
     */
    private fun drawableToBitmap(drawable: Drawable): Bitmap? {
        return try {
            when (drawable) {
                is BitmapDrawable -> drawable.bitmap
                else -> {
                    val bitmap = Bitmap.createBitmap(
                        drawable.intrinsicWidth.coerceAtLeast(1),
                        drawable.intrinsicHeight.coerceAtLeast(1),
                        Bitmap.Config.ARGB_8888
                    )
                    val canvas = Canvas(bitmap)
                    drawable.setBounds(0, 0, canvas.width, canvas.height)
                    drawable.draw(canvas)
                    bitmap
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to convert drawable to bitmap")
            null
        }
    }
    
    /**
     * Compresses a bitmap to fit within the specified size limit
     */
    private fun compressBitmap(bitmap: Bitmap, maxSizeKB: Int): Bitmap {
        var quality = ICON_QUALITY
        var compressedBitmap = bitmap
        
        while (quality > 10) {
            val outputStream = ByteArrayOutputStream()
            compressedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            val sizeKB = outputStream.size() / 1024
            
            if (sizeKB <= maxSizeKB) {
                Timber.d("Bitmap compressed to ${sizeKB}KB with quality $quality")
                break
            }
            
            // Reduce quality and try again
            quality -= 10
            if (quality <= 10) {
                // If still too large, scale down the bitmap
                val scaleFactor = 0.8f
                val newWidth = (compressedBitmap.width * scaleFactor).toInt()
                val newHeight = (compressedBitmap.height * scaleFactor).toInt()
                compressedBitmap = Bitmap.createScaledBitmap(compressedBitmap, newWidth, newHeight, true)
                quality = ICON_QUALITY
            }
        }
        
        return compressedBitmap
    }
    
    /**
     * Converts a bitmap to Base64 string
     */
    private fun bitmapToBase64(bitmap: Bitmap): String {
        return try {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, ICON_QUALITY, outputStream)
            val byteArray = outputStream.toByteArray()
            val base64 = Base64.encodeToString(byteArray, Base64.NO_WRAP)
            "data:image/jpeg;base64,$base64"
        } catch (e: IOException) {
            Timber.e(e, "Failed to convert bitmap to Base64")
            throw e
        }
    }
    
    /**
     * Gets the size of a Base64 string in KB
     */
    fun getBase64SizeKB(base64: String): Int {
        return base64.length / 1024
    }
    
    /**
     * Checks if a Base64 string is within size limits
     */
    fun isWithinSizeLimit(base64: String, maxSizeKB: Int): Boolean {
        return getBase64SizeKB(base64) <= maxSizeKB
    }
}
