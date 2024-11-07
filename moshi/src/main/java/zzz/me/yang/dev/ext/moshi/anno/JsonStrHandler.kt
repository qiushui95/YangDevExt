package zzz.me.yang.dev.ext.moshi.anno

import com.squareup.moshi.JsonQualifier

@JsonQualifier
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD, AnnotationTarget.FUNCTION)
public annotation class JsonStrHandler(
    val nullList: Array<String> = [],
    val blank2Null: Boolean = false,
    val replaceNewLine: Boolean = false,
    val trim: Boolean = false,
    val maxLength: Int = -1,
)
