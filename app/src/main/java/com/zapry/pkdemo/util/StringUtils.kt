package com.zapry.pkdemo.util

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.icu.text.DecimalFormat
import android.os.Build
import android.text.TextUtils
import android.util.Base64
import androidx.annotation.ArrayRes
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.core.util.PatternsCompat
//import com.cyberflow.mimolite.common.app.BaseYXApp
//import com.cyberflow.mimolite.lib.resource.textstring.R
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.lang.reflect.Type
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.regex.Matcher

object StringUtils {
    private const val TAG = "StringUtils"

    fun isNumber(s: String): Boolean {
        return if (s.isEmpty()) {
            false
        } else {
            s.matches("\\d+".toRegex())
        }
    }

    /**
     * Return whether the string is null or white space.
     *
     * @param s The string.
     * @return `true`: yes<br></br> `false`: no
     */
    fun isSpace(s: String?): Boolean {
        if (s == null) return true
        var i = 0
        val len = s.length
        while (i < len) {
            if (!Character.isWhitespace(s[i])) {
                return false
            }
            ++i
        }
        return true
    }

    /**
     * Set the first letter of string upper.
     *
     * @param s The string.
     * @return the string with first letter upper.
     */
    fun upperFirstLetter(s: String?): String {
        if (s.isNullOrEmpty()) return ""
        return if (!Character.isLowerCase(s[0])) s else (s[0].code - 32).toChar()
            .toString() + s.substring(1)
    }

    /**
     * Set the first letter of string lower.
     *
     * @param s The string.
     * @return the string with first letter lower.
     */
    fun lowerFirstLetter(s: String?): String {
        if (s.isNullOrEmpty()) return ""
        return if (!Character.isUpperCase(s[0])) s else (s[0].code + 32).toChar()
            .toString() + s.substring(1)
    }

    /**
     * Reverse the string.
     *
     * @param s The string.
     * @return the reverse string.
     */
    fun reverse(s: String?): String {
        if (s == null) return ""
        val len = s.length
        if (len <= 1) return s
        val mid = len shr 1
        val chars = s.toCharArray()
        var c: Char
        for (i in 0 until mid) {
            c = chars[i]
            chars[i] = chars[len - i - 1]
            chars[len - i - 1] = c
        }
        return String(chars)
    }

    /**
     * 半角转换为全角
     *
     * @param input
     * @return
     */
    fun toDBC(input: String): String {
        val c = input.toCharArray()
        for (i in c.indices) {
            if (c[i].code == 12288) {
                c[i] = 32.toChar()
                continue
            }
            if (c[i].code in 65281..65374) c[i] = (c[i].code - 65248).toChar()
        }
        return String(c)
    }

    /**
     * Convert string to SBC.
     *
     * @param s The string.
     * @return the SBC string
     */
    fun toSBC(s: String?): String {
        if (s.isNullOrEmpty()) return ""
        val chars = s.toCharArray()
        var i = 0
        val len = chars.size
        while (i < len) {
            if (chars[i] == ' ') {
                chars[i] = 12288.toChar()
            } else if (chars[i].code in 33..126) {
                chars[i] = (chars[i].code + 65248).toChar()
            } else {
                chars[i] = chars[i]
            }
            i++
        }
        return String(chars)
    }

    /**
     * Return the string value associated with a particular resource ID.
     *
     * @param id The desired resource identifier.
     * @return the string value associated with a particular resource ID.
     */
//    fun getString(@StringRes id: Int): String {
//        return try {
//            BaseYXApp.instance.getString(id)
//        } catch (e: Resources.NotFoundException) {
//            e.printStackTrace()
//            ""
//        }
//    }

    /**
     * Return the string value associated with a particular resource ID.
     *
     * @param id         The desired resource identifier.
     * @param formatArgs The format arguments that will be used for substitution.
     * @return the string value associated with a particular resource ID.
     */
//    fun getString(@StringRes id: Int, vararg formatArgs: Any?): String {
//        return try {
//            BaseYXApp.instance.getString(id, formatArgs)
//        } catch (e: Resources.NotFoundException) {
//            e.printStackTrace()
//            ""
//        }
//    }

    /**
     * Return the string array associated with a particular resource ID.
     *
     * @param id The desired resource identifier.
     * @return The string array associated with the resource.
     */
//    fun getStringArray(@ArrayRes id: Int): Array<String?> {
//        return try {
//            BaseYXApp.instance.resources.getStringArray(id)
//        } catch (e: Resources.NotFoundException) {
//            e.printStackTrace()
//            arrayOfNulls(0)
//        }
//    }

    fun hasText(str: String): Boolean {
        return if (str.isNullOrEmpty()) {
            false
        } else {
            val strLen = str.length
            for (i in 0 until strLen) {
                if (!Character.isWhitespace(str[i])) {
                    return true
                }
            }
            false
        }
    }

    fun takeLast(text: String, len: Int): String {
        return if (TextUtils.isEmpty(text)) "" else text.substring(0, text.length - len)
    }

    /**
     * 不可见字符
     */
    const val INVISIBLE_CHAR = '\u200b'

    /**
     * 碰到空格,@和#就在其前面加一个不可见字符
     *
     * @param name
     * @return
     */
    fun addInvisibleCharBeforeWhiteSpace(name: String): String {
        if (TextUtils.isEmpty(name)) {
            return name
        }
        val stringBuilder = StringBuilder()
        for (c in name) {
            if (isSpecialChar(c)) {
                stringBuilder.append(INVISIBLE_CHAR)
            }
            stringBuilder.append(c)
        }
        return stringBuilder.toString()
    }

    private fun isSpecialChar(c: Char): Boolean {
        return c == ' ' || c == '@' || c == '#'
    }

    /**
     * 去除不可见字符
     *
     * @param name
     * @return
     */
    fun removeInvisibleCharBeforeWhiteSpace(name: String): String {
        if (TextUtils.isEmpty(name)) {
            return name
        }
        val stringBuilder = StringBuilder()
        for (c in name) {
            if (c != INVISIBLE_CHAR) {
                stringBuilder.append(c)
            }
        }
        return stringBuilder.toString()
    }

    /**
     * 字符串转换unicode
     */
    fun toUnicode(string: String): String {
        val unicode = StringBuilder()
        for (c in string) {
            // 转换为unicode
            unicode.append(String.format("\\u%04x", c.code))
        }
        return unicode.toString()
    }

    /**
     * 判断一个字符是否是中文
     *
     * @param c
     * @return
     */
    private fun isChinese(c: Char): Boolean {
        // 根据字节码判断
        return c.code in 0x4E00..0x9FA5
    }

    fun isChinese(str: String?): Boolean {
        if (str == null) {
            return false
        }
        for (c in str.toCharArray()) {
            if (isChinese(c)) {
                // 有一个中文字符就返回
                return true
            }
        }
        return false
    }

    fun hasChinese(str: String): Boolean {
        return if (TextUtils.isEmpty(str)) {
            false
        } else str.toByteArray().size != str.length
        //1个英文一个字节，1个 中文2个字节（GBK）
    }

    fun parseIntSafely(integerToParse: String): Int {
        return parseInt(integerToParse)
    }

    fun containsEmoji(source: String): Boolean {
        if (TextUtils.isEmpty(source)) {
            return false
        }
        val len = source.length
        for (i in 0 until len) {
            val codePoint = source[i]
            // 如果不能匹配,则该字符是Emoji表情
            if (!isEmojiCharacter(codePoint)) {
                return true
            }
        }
        return false
    }

    private fun isEmojiCharacter(codePoint: Char): Boolean {
        return codePoint.code == 0x0 || codePoint.code == 0x9 || codePoint.code == 0xA || codePoint.code == 0xD || codePoint.code >= 0x20 && codePoint.code <= 0xD7FF || codePoint.code >= 0xE000 && codePoint.code <= 0xFFFD || codePoint.code >= 0x10000 && codePoint.code <= 0x10FFFF
    }

    fun hasUrl(text: String?): Boolean {
        if (text.isNullOrEmpty()) {
            return false
        }
        val matcher: Matcher = PatternsCompat.WEB_URL.matcher(text)
        return matcher.find()
    }

    @JvmOverloads
    fun parseInt(value: String, defaultValue: Int = 0): Int {
        return if (TextUtils.isEmpty(value)) {
            defaultValue
        } else try {
            value.toInt()
        } catch (ignored: NumberFormatException) {
            parseDouble(value, defaultValue.toDouble()).toInt()
        }
    }

    @JvmOverloads
    fun parseLong(value: String, defaultValue: Long = 0L): Long {
        return if (TextUtils.isEmpty(value)) {
            defaultValue
        } else try {
            value.toLong()
        } catch (ignored: NumberFormatException) {
            parseDouble(value, defaultValue.toDouble()).toLong()
        }
    }

    @JvmOverloads
    fun parseFloat(value: String, defaultValue: Float = 0.0f): Float {
        return if (TextUtils.isEmpty(value)) {
            defaultValue
        } else try {
            value.toFloat()
        } catch (ignored: NumberFormatException) {
            defaultValue
        }

    }

    @JvmOverloads
    fun parseDouble(value: String, defaultValue: Double = 0.0): Double {
        return if (TextUtils.isEmpty(value)) {
            defaultValue
        } else try {
            value.toDouble()
        } catch (ignored: NumberFormatException) {
            defaultValue
        }
    }

    @ColorInt
    fun parseColor(color: String): Int {
        return parseColor(color, Color.WHITE)
    }

    @ColorInt
    fun parseColor(color: String, @ColorInt defaultColor: Int): Int {
        if (TextUtils.isEmpty(color)) {
            return defaultColor
        }

        // 颜色解析只支持RRGGBB 和 AARRGGBB
        return if (color.matches("^#[0-9a-fA-F]{8}".toRegex()) || color.matches("^#[0-9a-fA-F]{6}".toRegex())) {
            Color.parseColor(color)
        } else {
            defaultColor
        }
    }

    fun isJson(json: String, type: Type): Boolean {
        if (TypeToken.get(type).rawType.isArray || GsonUtils.isListTypeClass(TypeToken.get(type).rawType)) {
            // 注意这两个异常捕获不能合并，String 不可以同时是Json格式的实体又是Json格式的列表
            try {
                // 是Json格式的列表，返回true
                JSONArray(json)
                return true
            } catch (e: JSONException) {
                // 转换异常说明不符合格式，检测目的已达到，不需要打印异常信息。
            }
        } else {
            try {
                // 是Json格式的实体，返回true
                JSONObject(json)
                return true
            } catch (e: JSONException) {
                // 转换异常说明不符合格式，检测目的已达到，不需要打印异常信息。
            }
        }
        return false
    }

    fun showDayUnit(prizeType: Int): Boolean {
        return prizeType == 4 || prizeType == 5 || prizeType == 6 || prizeType == 10 || prizeType == 11 || prizeType == 13
    }

    fun encodeToBase64(data: ByteArray): String {
        return Base64.encodeToString(data, Base64.DEFAULT)
    }

    fun decodeFromBase64(base64String: String): ByteArray {
        return Base64.decode(base64String, Base64.DEFAULT)
    }

    fun wrapBase64(data: String): String {
        return encodeToBase64(data.toByteArray(Charsets.UTF_8))
    }

    fun unwrapBase64(base64String: String): String {
        return decodeFromBase64(base64String).toString(Charsets.UTF_8)
    }

//    private val THOUSAND = BigDecimal(1000)
//    private val MILLION = BigDecimal(1000) * THOUSAND
//    private val BILLION
//        get() = if (LanguageUtil.isChain()) {
//            BigDecimal(100) * MILLION // 中文显示的是亿的值
//        } else {
//            BigDecimal(1000) * MILLION // 英文显示的是十亿的值
//        }
//
//    // 万亿
//    private val TRILLION = BigDecimal(1000 * 1000) * MILLION

//    fun formatDigital(context: Context?, amount: BigDecimal, scale: Int = 2): String {
//        var value = amount
//        var unit = ""
//        when {
//
//            amount.abs() >= TRILLION -> {
//                value = amount / TRILLION
//                unit = context?.getString(R.string.quotation_magnitude_trillion) ?: "T"
//            }
//
//            amount.abs() >= BILLION -> {
//                value = amount / BILLION
//                unit = if (LanguageUtil.isChain()) {
//                    // 中文环境是亿不是十亿
//                    context?.getString(R.string.quotation_magnitude_hundred_million) ?: "B"
//                } else {
//                    context?.getString(R.string.quotation_magnitude_billion) ?: "B"
//                }
//            }
//
//            amount.abs() >= MILLION -> {
//                value = amount / MILLION
//                unit = context?.getString(R.string.quotation_magnitude_million) ?: "M"
//            }
//
//            amount.abs() >= THOUSAND * BigDecimal(10) && LanguageUtil.isChain() -> {
//                value = amount / (THOUSAND * BigDecimal(10))
//                unit = context?.getString(R.string.quotation_magnitude_ten_thousand) ?: "M"
//            }
//
//            amount.abs() >= THOUSAND -> {
//                if (LanguageUtil.isChain()) {
//                    value = amount
//                    unit = ""
//                } else {
//                    value = amount / THOUSAND
//                    unit = context?.getString(R.string.quotation_magnitude_thousand) ?: "K"
//                }
//            }
//
//            else -> {
//                value = amount
//                unit = ""
//            }
//        }
//
//        return formatOmitPrice(value, scale) + unit
//    }

    /**
     * 获取[BigDecimal]小数点后中连续有效的零数量长度
     */
    private fun getDecimalLength(number: BigDecimal): Int {

        // 去除尾部无效的零
        val strippedStr = number.stripTrailingZeros().toPlainString()
        var pattern = ".0"
        while (strippedStr.contains(pattern)) {
            // 查询连续小点后的零的数量
            pattern += "0"
        }

        return pattern.length - 2
    }

    fun formatOmitPrice(price: BigDecimal, scale: Int = 4): String {
        val absoluteValue = price.abs()
        if (absoluteValue >= BigDecimal(100.0)) {
            // 如果小数点前有三个数字以及三个数字以上，只保留2位小数。比如 100.01；2834283.21
            return formatPattern(price, 2)
        }

        val length = getDecimalLength(price)

        if (length >= 10) {
            // 如果代币的价格过小，可能是0.000000023 的情况 小数点后面连续出现10个0就
            // 显示：0{4} 五个0就是0{5} 然后再取0后面非0数字的第四位
            val repeat = "." + "0".repeat(length)
            return formatPattern(price, length + scale).replace(repeat, ".0{$length}")
        }
        // 如果小数点前只有2个数字以及一个数字，显示到小数点后非0数字的第四位 比如12.2323
        return formatPattern(price, scale + length)
    }

    fun formatPattern(digital: BigDecimal, scale: Int = 2): String {
        val repeat = if (scale >= 2) {
            "00" + "#".repeat(scale - 2)
        } else {
            "#".repeat(scale)
        }

        val pattern = "###,###,###,###,##0.$repeat"

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            DecimalFormat(pattern).format(digital)
        } else {
            java.text.DecimalFormat(pattern).format(digital)
        }
    }

    fun formatPrice(currency: BigDecimal, scale: Int = 4): String {
        // modified by liangyu：钱不要四舍五入
        return currency.setScale(scale, RoundingMode.DOWN).stripTrailingZeros()
            .toPlainString()
    }
}