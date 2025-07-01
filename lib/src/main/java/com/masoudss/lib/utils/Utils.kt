package com.masoudss.lib.utils

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.util.TypedValue
import android.webkit.MimeTypeMap
import java.io.File
import java.util.UUID

object Utils {

    @JvmStatic
    fun dp(context: Context?, dp: Int): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            context!!.resources.displayMetrics
        )
    }

}

fun Context.uriToFile(uri: Uri): File? {
    val extension = contentResolver.getUriExtension(uri) ?: return null
    val tempFile = File(cacheDir, "${UUID.randomUUID()}.$extension")

    return try {
        contentResolver.openInputStream(uri)?.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
            tempFile
        }
    } catch (e: Exception) {
        null
    }
}


fun ContentResolver.getUriExtension(uri: Uri): String? {
    getType(uri)?.let { type ->
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(type)
    }

    var cursor: Cursor? = null
    return try {
        cursor = query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        cursor?.takeIf { it.moveToFirst() }?.getString(0)
            ?.substringAfterLast('.', missingDelimiterValue = "")
            ?.takeIf { it.isNotEmpty() }
    } catch (_: Exception) {
        null
    } finally {
        cursor?.close()
    }
}