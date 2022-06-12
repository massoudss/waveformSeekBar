package com.masoudss.lib.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.TypedValue
import android.webkit.MimeTypeMap
import java.io.File
import java.util.*

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

fun Context.uriToFile(uri: Uri) = with(contentResolver) {
    val data = readUriBytes(uri) ?: return@with null
    val extension = getUriExtension(uri)
    File(
        cacheDir.path,
        "${UUID.randomUUID()}.$extension"
    ).also { audio -> audio.writeBytes(data) }
}

fun ContentResolver.readUriBytes(uri: Uri) = openInputStream(uri)
    ?.buffered()?.use { it.readBytes() }

fun ContentResolver.getUriExtension(uri: Uri) = MimeTypeMap.getSingleton()
    .getMimeTypeFromExtension(getType(uri))