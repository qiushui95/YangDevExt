package zzz.me.yang.dev.ext.moshi.anno

import com.squareup.moshi.JsonQualifier

@JsonQualifier
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
public annotation class JsonSingle(val value: String)
