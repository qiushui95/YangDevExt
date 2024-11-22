package zzz.me.yang.dev.ext.moshi

import com.squareup.moshi.Moshi
import org.junit.jupiter.api.Test
import zzz.me.yang.dev.ext.moshi.MoshiFactories.addAllFactory

internal class JsonSingleTest {
    @Test
    fun test() {
        val json =
            "{\"group\":{\"id\":\"1\",\"createdAt\":\"1721011304\",\"gameId\":\"0\",\"name\":\"去云吧官方交流群\",\"icon\":{\"type\":\"IMAGE\",\"path\":\"chatapp/app/group/official_group.png\",\"width\":512,\"height\":512,\"cover\":\"\"},\"desc\":\"本群为官方交流群，萌新有任何问题均可在群中询问~\",\"memberCount\":\"120\",\"hot\":\"0\",\"notice\":{\"noticeId\":\"1-1721011304\",\"notice\":\"1.友好交流：请保持友好、尊重他人的交流氛围，共同维护一个健康的交流环境。\\n2.积极反馈：我们非常关心您的使用体验，无论是好的还是需要改进的地方，都请积极在群内反馈，帮助我们不断优化\\n3.禁止违规：禁止发布任何违法、违规、广告、恶意信息或骚扰行为。\\n4.关注活动：留意群内发布的相关活动通知，积极参与。\"},\"groupKey\":{\"serverId\":\"1\",\"customId\":\"1\"},\"clearTime\":\"1732185680\"},\"members\":[{\"user\":{\"id\":418217,\"userName\":\"喜悦的画板\",\"avatar\":{\"type\":\"IMAGE\",\"path\":\"//cdnimg-v2.gamekee.com/wiki2.0/avatar/default1.png\",\"width\":300,\"height\":300,\"cover\":\"\"}}},{\"user\":{\"id\":418218,\"userName\":\"友好与雪碧\",\"avatar\":{\"type\":\"IMAGE\",\"path\":\"//cdnimg-v2.gamekee.com/wiki2.0/avatar/default1.png\",\"width\":300,\"height\":300,\"cover\":\"\"}}},{\"user\":{\"id\":418219,\"userName\":\"飘逸迎花瓣\",\"avatar\":{\"type\":\"IMAGE\",\"path\":\"//cdnimg-v2.gamekee.com/wiki2.0/avatar/default1.png\",\"width\":300,\"height\":300,\"cover\":\"\"}}},{\"user\":{\"id\":418220,\"userName\":\"伶俐迎飞鸟\",\"avatar\":{\"type\":\"IMAGE\",\"path\":\"//cdnimg-v2.gamekee.com/wiki2.0/avatar/default1.png\",\"width\":300,\"height\":300,\"cover\":\"\"}}},{\"user\":{\"id\":418221,\"userName\":\"跳跃踢草丛\",\"avatar\":{\"type\":\"IMAGE\",\"path\":\"//cdnimg-v2.gamekee.com/wiki2.0/avatar/default1.png\",\"width\":300,\"height\":300,\"cover\":\"\"}}},{\"user\":{\"id\":418222,\"userName\":\"兴奋和煎饼\",\"avatar\":{\"type\":\"IMAGE\",\"path\":\"//cdnimg-v2.gamekee.com/wiki2.0/avatar/default1.png\",\"width\":300,\"height\":300,\"cover\":\"\"}}},{\"user\":{\"id\":418223,\"userName\":\"高大打大神\",\"avatar\":{\"type\":\"IMAGE\",\"path\":\"//cdnimg-v2.gamekee.com/wiki2.0/avatar/default1.png\",\"width\":300,\"height\":300,\"cover\":\"\"}}},{\"user\":{\"id\":418224,\"userName\":\"醉熏保卫犀牛\",\"avatar\":{\"type\":\"IMAGE\",\"path\":\"//cdnimg-v2.gamekee.com/wiki2.0/avatar/default1.png\",\"width\":300,\"height\":300,\"cover\":\"\"}}},{\"user\":{\"id\":418225,\"userName\":\"谦让有鱼\",\"avatar\":{\"type\":\"IMAGE\",\"path\":\"//cdnimg-v2.gamekee.com/wiki2.0/avatar/default1.png\",\"width\":300,\"height\":300,\"cover\":\"\"}}},{\"user\":{\"id\":418226,\"userName\":\"简单爱灰狼\",\"avatar\":{\"type\":\"IMAGE\",\"path\":\"//cdnimg-v2.gamekee.com/wiki2.0/avatar/default1.png\",\"width\":300,\"height\":300,\"cover\":\"\"}}}],\"permission\":{\"noticeUpdate\":true,\"notQuit\":true,\"clearMsg\":true}}"

        val moshi = Moshi.Builder()
            .addAllFactory()
            .build()

        val jsonAdapter = moshi.adapter(RespGroupDetailInfo::class.java)

        println(jsonAdapter.fromJson(json))
    }
}
