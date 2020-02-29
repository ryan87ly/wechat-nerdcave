package nerd.cave.web.wx

import org.apache.commons.codec.digest.DigestUtils

const val HASH_ALGORITHM = "MD5"

fun sign(paymentSecretKey:String, vararg params: Pair<String, Any>): String {
    val stringToBeHashed = params
        .toList()
        .sortedBy { it.first }
        .plus("key" to paymentSecretKey)
        .map { "${it.first}=${it.second}" }
        .reduce { acc, s -> "$acc&$s" }
    return stringToBeHashed.toMD5()
}

fun String.toMD5(): String  {
    return DigestUtils.md5Hex(this).toUpperCase()
}

