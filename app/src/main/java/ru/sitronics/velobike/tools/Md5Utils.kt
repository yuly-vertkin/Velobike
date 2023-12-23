package ru.sitronics.velobike.tools

import java.math.BigInteger
import java.security.MessageDigest

object Md5Utils {

    fun md5(stringData: String): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(stringData.toByteArray())).toString(16).padStart(32, '0')
    }
}