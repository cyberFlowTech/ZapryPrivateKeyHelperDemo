package com.zapry.pkdemo.util

import androidx.annotation.VisibleForTesting
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.IOException
import java.lang.reflect.Modifier
import java.lang.reflect.Type
import java.util.Collections
import java.util.Locale

/**
 * 静态Gson实体类
 */
object GsonUtils {
    private const val TAG = "GsonUtils"
    private val instance: Gson by lazy { gsonBuilder.create() }
    private val gsonBuilder by lazy {
        GsonBuilder()
            .disableHtmlEscaping()
            .registerTypeAdapter(String::class.java, StringAdapter())
            .registerTypeAdapter(Int::class.java, IntegerDefaultAdapter())
            .registerTypeAdapter(Double::class.java, DoubleDefaultAdapter())
            .registerTypeAdapter(Float::class.java, FloatDefaultAdapter())
            .registerTypeAdapter(Long::class.java, LongDefaultAdapter())
            .registerTypeAdapterFactory(EmptyArrayCompatReflectiveTypeAdapterFactory())
            .enableComplexMapKeySerialization()
            .excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.STATIC)
    }

    @JvmStatic
    fun getGson(): Gson {
        return instance
    }

    fun getBuilder(): GsonBuilder {
        return gsonBuilder
    }

    /**
     * json字符串转对象，解析失败，不会抛出异常，会直接返回null
     *
     * @param json json字符串
     * @param type 对象类类型
     * @param <T>  返回类型
     * @return T，返回对象有可能为空
    </T> */
    @JvmStatic
    fun <T> getObject(json: String?, type: Type?): T? {
        return fromJson<T>(json, type)
    }

    /**
     * 判断指定类是否是List的子类或者父类
     *
     * @param clz
     * @return
     */
    @JvmStatic
    fun isListTypeClass(clz: Class<*>?): Boolean {
        if (clz == null) {
            return false
        }
        return try {
            MutableList::class.java.isAssignableFrom(clz) || clz.newInstance() is Collection<*>
        } catch (ignore: java.lang.Exception) {
            // 转换异常说明不符合格式，检测目的已达到，不需要打印异常信息。
            false
        }
    }

    /**
     * json字符串转对象，解析失败，将抛出对应的[JsonSyntaxException]异常，根据异常可查找原因
     *
     * @param string json字符串
     * @param type 对象类类型
     * @param <T>  返回类型
     * @return T，返回对象不为空
    </T> */
    @JvmStatic
    @Throws(JsonSyntaxException::class)
    fun <T> fromJson(string: String?, type: Type?): T? {
//        LogUtil.d(TAG, "fromJson() called with: string = $string, type = $type")
        return try {
            instance.fromJson(string, type)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @JvmStatic
    @Throws(JsonSyntaxException::class)
    fun <T> fromJson(string: String?, classOfT: Class<T>?): T? {
//        LogUtil.d(TAG, "fromJson() called with: string = $string, classOfT = $classOfT")
        return fromJson(string, TypeToken.get(classOfT).type)
    }

    @JvmStatic
    fun <T> fromJson(msg: JsonElement?, type: Type?): T? {
//        LogUtil.d(TAG, "fromJson() called with: msg = $msg, type = $type")
        return try {
            instance.fromJson(msg, type)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @JvmStatic
    fun <T> fromJson(msg: JsonElement?, classOfT: Class<T>?): T? {
//        LogUtil.d(TAG, "fromJson() called with: msg = $msg, classOfT = $classOfT")
        return try {
            instance.fromJson(msg, classOfT)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @JvmStatic
    @Throws(JsonSyntaxException::class)
    fun <T> fromJsonArray(json: String?, clazz: Class<T>?): List<T> {
        if (json.isNullOrEmpty() || clazz == null || !StringUtils.isJson(json, List::class.java)) {
            return emptyList()
        }

        val listType = TypeToken.getParameterized(List::class.java, clazz).type
        return instance.fromJson(json, listType) ?: emptyList()
    }

    @JvmStatic
    fun toJson(any: Any?, typeOfSrc: Type? = null): String {
        return if (any == null) {
            return ""
        } else if (typeOfSrc == null) {
            instance.toJson(any)
        } else {
            instance.toJson(any, typeOfSrc)
        }
    }

    @JvmStatic
    fun toJson(any: Any?): String {
        return toJson(any, null)
    }

    /**
     * 入参json专用  不会默认给空字符
     */
    @JvmStatic
    fun toRequestJson(any: Any?): String {
        return toRequestJson(any, null)
    }

    /**
     * 入参json专用  不会默认给空字符
     */
    @JvmStatic
    fun toRequestJson(any: Any?, typeOfSrc: Type? = null): String {
        return toJson(any, typeOfSrc)
    }

    private class StringAdapter : TypeAdapter<String>() {
        override fun write(out: JsonWriter?, value: String?) {
            out?.value(value ?: "")
        }

        override fun read(reader: JsonReader?): String {
            return when (reader?.peek()) {
                JsonToken.STRING -> {
                    var string = reader.nextString()
                    if (string.uppercase(Locale.getDefault()) == "NULL") {
                        string = ""
                    }
                    string
                }

                JsonToken.NAME -> {
                    var string = reader.nextName()
                    if (string.uppercase(Locale.getDefault()) == "NULL") {
                        string = ""
                    }
                    string
                }

                JsonToken.NUMBER -> {
                    reader.nextString()
                }

                JsonToken.BOOLEAN -> {
                    reader.nextBoolean().toString()
                }

                JsonToken.NULL -> {
                    // 字段值为null,需要单独处理
                    reader.nextNull()
                    ""
                }

                else -> {
                    ""
                }
            }
        }
    }

    private class IntegerDefaultAdapter : TypeAdapter<Int>() {
        override fun write(out: JsonWriter?, value: Int?) {
            out?.value(value ?: 0)
        }

        override fun read(reader: JsonReader?): Int {
            return when (reader?.peek()) {
                JsonToken.NUMBER -> {
                    reader.nextInt()
                }

                JsonToken.BOOLEAN -> {
                    if (reader.nextBoolean()) {
                        1
                    } else {
                        0
                    }
                }

                JsonToken.STRING -> {
                    StringUtils.parseInt(reader.nextString())
                }

                JsonToken.NAME -> {
                    StringUtils.parseInt(reader.nextName())
                }

                JsonToken.NULL -> {
                    // 字段值为null,需要单独处理
                    reader.nextNull()
                    0
                }

                else -> 0
            }
        }
    }

    private class DoubleDefaultAdapter : TypeAdapter<Double>() {
        override fun write(out: JsonWriter?, value: Double?) {
            out?.value(value ?: 0.0)
        }

        override fun read(reader: JsonReader?): Double {
            return when (reader?.peek()) {
                JsonToken.NUMBER -> {
                    reader.nextDouble()
                }

                JsonToken.BOOLEAN -> {
                    if (reader.nextBoolean()) {
                        1.0
                    } else {
                        0.0
                    }
                }

                JsonToken.NAME -> {
                    StringUtils.parseDouble(reader.nextName())
                }

                JsonToken.STRING -> {
                    StringUtils.parseDouble(reader.nextString())
                }

                JsonToken.NULL -> {
                    // 字段值为null,需要单独处理
                    reader.nextNull()
                    0.0
                }

                else -> 0.0
            }
        }

    }

    private class FloatDefaultAdapter : TypeAdapter<Float>() {
        override fun write(out: JsonWriter?, value: Float?) {
            out?.value(value ?: 0f)
        }

        override fun read(reader: JsonReader?): Float {
            return when (reader?.peek()) {
                JsonToken.NUMBER -> {
                    reader.nextDouble().toFloat()
                }

                JsonToken.BOOLEAN -> {
                    if (reader.nextBoolean()) {
                        1.0f
                    } else {
                        0.0f
                    }
                }

                JsonToken.NAME -> {
                    StringUtils.parseFloat(reader.nextName())
                }

                JsonToken.STRING -> {
                    StringUtils.parseFloat(reader.nextString())
                }

                JsonToken.NULL -> {
                    // 字段值为null,需要单独处理
                    reader.nextNull()
                    0.0f
                }

                else -> 0.0f
            }
        }
    }

    private class LongDefaultAdapter : TypeAdapter<Long>() {
        override fun write(out: JsonWriter?, value: Long?) {
            out?.value(value ?: 0L)
        }

        override fun read(reader: JsonReader?): Long {
            return when (reader?.peek()) {
                JsonToken.NUMBER -> {
                    reader.nextLong()
                }

                JsonToken.BOOLEAN -> {
                    if (reader.nextBoolean()) {
                        1L
                    } else {
                        0L
                    }
                }

                JsonToken.NAME -> {
                    StringUtils.parseLong(reader.nextName())
                }

                JsonToken.STRING -> {
                    StringUtils.parseLong(reader.nextString())
                }

                JsonToken.NULL -> {
                    // 字段值为null,需要单独处理
                    reader.nextNull()
                    0L
                }

                else -> 0L
            }
        }
    }

    private class ListDefaultAdapter : TypeAdapter<Long>() {
        override fun write(out: JsonWriter?, value: Long?) {
            out?.value(value ?: 0L)
        }

        override fun read(reader: JsonReader?): Long {
            return when (reader?.peek()) {
                JsonToken.NUMBER -> {
                    reader.nextLong()
                }

                JsonToken.BOOLEAN -> {
                    if (reader.nextBoolean()) {
                        1L
                    } else {
                        0L
                    }
                }

                JsonToken.NAME -> {
                    StringUtils.parseLong(reader.nextName())
                }

                JsonToken.STRING -> {
                    StringUtils.parseLong(reader.nextString())
                }

                JsonToken.NULL -> {
                    // 字段值为null,需要单独处理
                    reader.nextNull()
                    0L
                }

                else -> 0L
            }
        }
    }

    class ListTypeAdapter : JsonDeserializer<List<*>> {
        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): List<*> {
            return try {
                if (json?.isJsonArray == true) {
                    Gson().fromJson(json, typeOfT) //如果是正确的jsonArray，能进来这里，但是返回的是空
                } else {
                    Collections.EMPTY_LIST
                }
            } catch (e: Exception) {
                Collections.EMPTY_LIST
            }
        }
    }


    @VisibleForTesting
    class EmptyArrayCompatReflectiveTypeAdapterFactory : TypeAdapterFactory {
        override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T?>? {
            val raw = type.rawType
            if (!Any::class.java.isAssignableFrom(raw)) {
                return null
            }
            if (MutableCollection::class.java.isAssignableFrom(raw)) {
                return null
            }
            val delegate = gson.getDelegateAdapter(this, type)
            return object : TypeAdapter<T?>() {
                @Throws(IOException::class)
                override fun write(out: JsonWriter, value: T?) {
                    delegate.write(out, value)
                }

                @Throws(IOException::class)
                override fun read(`in`: JsonReader): T? {
                    val peeked = `in`.peek()
                    if (peeked == JsonToken.BEGIN_ARRAY) {
                        `in`.beginArray()
                        `in`.endArray()
                        return null
                    }
                    if (raw != String::class.java) {//兼容本应是Object但是返回"" 的情况
                        if (peeked == JsonToken.STRING) {
                            `in`.nextString()
                            return null
                        }
                    }
                    return delegate.read(`in`)
                }
            }
        }
    }
}