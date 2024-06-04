package ru.sitronics.velobike.tools

import android.content.Context
import android.content.Intent
import android.net.Uri
import ru.sitronics.velobike.R

fun callToSupport(context: Context) {
    try {
        val intent = Intent(
            Intent.ACTION_DIAL,
            Uri.fromParts("tel", context.getString(R.string.support_phone), null)
        )
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK + Intent.FLAG_ACTIVITY_NO_USER_ACTION
        context.startActivity(intent)
    } catch (e: Exception) {
        Logg.e(e)
    }
}
