package com.masoudss.lib.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
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


fun ContentResolver.getUriExtension(uri: Uri) = MimeTypeMap.getSingleton()
    .getMimeTypeFromExtension(getType(uri))