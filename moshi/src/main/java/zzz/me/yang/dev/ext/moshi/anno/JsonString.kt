package zzz.me.yang.dev.ext.moshi.anno

import com.squareup.moshi.JsonQualifier

@JsonQualifier
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD, AnnotationTarget.FUNCTION)
public annotation class JsonString
