import com.squareup.moshi.JsonQualifier

@JsonQualifier
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
public annotation class ReqSingleParam(val jsonName: String)
