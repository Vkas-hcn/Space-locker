package com.vkas.spacelocker.enevtsl

object Constant {
    // 是否刷新加锁列表
    const val REFRESH_LOCK_LIST = "refreshLockList"
    // 跳转忘记密码
    const val SKIP_TO_FORGET_PASSWORD = "skipToForgetPassword"
    // 跳转错误密码
    const val SKIP_TO_ERROR_PASSWORD = "skipToErrorPassword"
    // 正常跳转
    const val SKIP_TO_NORMAL_PASSWORD = "skipToNormalPassword"

    //错误次数
    const val NUMBER_OF_ERRORS = "numberOfErrors"
    //锁密码
    const val LOCK_CODE_SL = "lockCodeSl"
    const val STORE_LOCKED_APPLICATIONS = "storeLockedApplications"
    //应用列表
    const val APPLICATION_LIST_SL = "application_list_sl"
    const val APP_PACKAGE_NAME = "com.vkas.spacelocker"
    // 分享地址
    const val SHARE_SL_ADDRESS="https://play.google.com/store/apps/details?id="
    // privacy_agreement
    const val PRIVACY_SL_AGREEMENT="https://www.baidu.com/"
    // email
    const val MAILBOX_SL_ADDRESS="vkas@qq.com"
    const val RETURN_SL_CURRENT_PAGE ="returnSlCurrentPage"


    //日志tag
    const val logTagSl = "logTagSl"
    //开屏关闭跳转
    const val OPEN_CLOSE_JUMP = "openCloseJump"




    const val LOCK_IS_INIT_DB = "lock_is_init_db" //是否初始化了数据库表

    const val LOCK_IS_INIT_FAVITER = "lock_is_init_faviter" //是否初始化了faviter数据表



}