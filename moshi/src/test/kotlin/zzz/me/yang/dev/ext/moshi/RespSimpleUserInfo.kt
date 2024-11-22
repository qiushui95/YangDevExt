package zzz.me.yang.dev.ext.moshi

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class RespSimpleUserInfo(
    @Json(name = "id")
    val userId: String,
    @Json(name = "userName")
    val nickName: String,
)
