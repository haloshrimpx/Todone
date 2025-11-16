package com.sijayyx.todone.background.alarm

//id为TodoItem或者Checklist的id，作为取消或更新Alarm的唯一标识符
//message在创建时生成，alarmTimestamp在创建时计算。
//在AlarmMessage创建完后，交给AlarmScheduler进行提醒的安排。
data class AlarmMessage(
    val hashcode: Int,
    val itemId : Long,
    val alarmType : String,
    val message: String,
    val alarmTimestamp: Long
)
