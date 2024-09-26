package com.zapry.pkdemo.event

import android.text.Editable
import androidx.annotation.IntDef

sealed class FlowEvent {

    fun getEventName(): String = javaClass.name

    object UserAvatarChange : FlowEvent()

    class TribeZoneChanged(var tribeId: String) : FlowEvent()

    /**
     * 在部落管理中部落发生改变时触发事件
     */
    class TribeChangeEvent(val tribeId: String, @TribeChangeType val type: Int) : FlowEvent() {
        companion object {
            const val TYPE_NONE = 1
            const val TYPE_DELETE = 2
            const val TYPE_UPDATE = 3
            const val TYPE_EXIT = 4
            const val TYPE_JOIN = 5
            const val TYPE_ENTER = 6
            const val TYPE_BE_REMOVED = 7

            @IntDef(
                TYPE_NONE,
                TYPE_DELETE,
                TYPE_UPDATE,
                TYPE_EXIT,
                TYPE_JOIN,
                TYPE_ENTER,
                TYPE_BE_REMOVED
            )
            @Retention(AnnotationRetention.SOURCE)
            annotation class TribeChangeType
        }
    }

    /**
     * 部落成员中的搜索框架输入的用户名称事件
     */
    class TribeMemberSearchEvent(val search: Editable?) : FlowEvent()

    object JoinTribeEvent : FlowEvent()

    class DissolutionTribeEvent(val tribeId: String) : FlowEvent()

    /**
     * 身份组增删成员
     */
    class RoleAddOrRemoveMember(
        val addOrRemove: Boolean,
        val roleId: String,
        val userId: String,
        val tribeId: String
    ) : FlowEvent()

    /**
     * 设置DAPP到无分组下
     */
//    class SetDApp2Group(
//        val dApp: DApp?,
//        val tribeId: String,
//        val tribeGroupId: String,
//        @ZoneChangeType val type: Int
//
//    ) : FlowEvent() {
//        companion object {
//            const val TYPE_NONE = 1
//            const val TYPE_UPDATE = 3
//
//            @IntDef(TYPE_NONE, TYPE_UPDATE)
//            @Retention(AnnotationRetention.SOURCE)
//            annotation class ZoneChangeType
//        }
//    }

    //动态点赞
    class TopicPraiseChanged(
        val topicId: String,
        val duid: String,
        val cid: String? = null,
        val isPraise: Boolean
    ) : FlowEvent()

    //动态收藏
    class TopicCollectChanged(val topicId: String, val duid: String, val isCollect: Boolean) :
        FlowEvent()

    //动态新评论（或回复）插入
//    class TopicCommentInsert(val commentBean: CommentBean, val topicId: String, val duid: String) :
//        FlowEvent()

    //部落主同意or拒绝我的申请，tb_mid：部落id，tribeName部落名
    class TribeRequestReply(val tribeId: String, val tribeName: String) :
        FlowEvent()

    //表情
    class EmotionDelete(val emotionId: String, val tribeId: String? = null) : FlowEvent()

    class EmotionMove(val emotionId: String, val targetId: String, val tribeId: String? = null) :
        FlowEvent()

    class EmotionInsert(
        val emotionId: String,
        val remoteUrl: String,
        val width: Int,
        val height: Int,
        val tribeId: String? = null
    ) : FlowEvent()

    //动态新评论（或回复）删除
    class TopicCommentDelete(
        val topicId: String,
        val duid: String,
        val cid: String,
        val parentCid: String? = null,
        val pId: String? = null // 父评论
    ) : FlowEvent()

    //部落的动态设置修改
    class TribeCommunitySettingModify(
        val tribeId: String,
        val name: String? = null,
        val bgImg: String? = null,
        val isPub: Int? = null
    ) : FlowEvent()

    //动态删除
    class TopicDelete(val topicId: String, val duid: String) : FlowEvent()

    //发布动态
    object TopicPublish : FlowEvent()

    //创建房间
    object CreateRoom : FlowEvent()

    //跳转TribeDetailFragment
    class Navigate2TribeDetailFragment(val type: Int, val tribeId: String? = null) : FlowEvent()

    //修改dApp
//    class ModifyDAppEvent(val dApp: DApp) : FlowEvent()

    class TribeMemberCountFetchedEvent(val count: Int) : FlowEvent()

    //加入部落申请通知
    class RequestJoinTribeNotify(
        val tribeId: String,
        val oUid: String,
        val subCode: Int,
        val msgId: String
    ) :
        FlowEvent()

    //移除群成员
    object RemoveGroupMember : FlowEvent()

    //单向好友发消息事件
    class OneWayFriendSendEvent(val cid: String) : FlowEvent()

    //好友申请事件
    object FriendRequestEvent : FlowEvent()

    class ChangePagerIndexInDiscoverTab(
        val pagerIndexStartFrom0: Int,
        val discoverPostIndex: Int? = null
    ) : FlowEvent() {
        companion object {
            const val PAGER_INDEX_DISCOVER = 0
            const val PAGER_INDEX_POST = 1
            const val PAGER_INDEX_MARKET = 2
        }
    }

    //通知H5WebActivity让他刷新页面
    class TaskDone(val action: String) : FlowEvent()

    object TribeAvatarChange : FlowEvent()

    object DeleteFriendEvent : FlowEvent()

    //轮询出来了转账的新状态
    class TransferV2StatusEvent(
        val conversationId: String,
        val messageId: String,
        val status: Int
    ) : FlowEvent()


    //聊天界面需要插入旧转账Item。（原因：发转账消息的人是老版本，他给我转账了，但是没给我发c_10015，我就不得不显示旧转账item）
    class TransferV1MsgNeedInsert(val conversationId: String, val messageId: String) : FlowEvent()

    //  隐藏/展示全部nft
    object HideAllNFtEvent : FlowEvent()


    //定时清理被设置
    class AutoClearDurationResetEvent(
        val conversationId: String,
        val duration: Int
    ) : FlowEvent()

    object LanguageChangedEvent : FlowEvent()

    object ShowLoadingEvent : FlowEvent()


    /********************* wallet start ************************/
//    class ReceivedTokenBalanceEvent(val tokenId: String, val balance: Balance) : FlowEvent()
    class ReceiveGasEvent(val success: Boolean, val result: String) : FlowEvent()
    class ApproveTxSubmitResultEvent(val submitted: Boolean, val result: String) : FlowEvent()
    class SignEip712Event(val result: String) : FlowEvent()

    /********************* wallet end ************************/
}