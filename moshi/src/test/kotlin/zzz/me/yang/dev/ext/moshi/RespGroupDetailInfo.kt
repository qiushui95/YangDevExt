package zzz.me.yang.dev.ext.moshi

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import zzz.me.yang.dev.ext.moshi.anno.JsonSingle

@JsonClass(generateAdapter = true)
internal data class RespGroupDetailInfo(
    @JsonSingle("user", isListChild = true)
    @Json(name = "members")
    val memberList: List<RespSimpleUserInfo>,
)
