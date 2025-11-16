package com.sijayyx.todone.utils

import android.content.Context
import com.sijayyx.todone.R
import com.sijayyx.todone.ui.checklists.RepeatPeriod
import kotlin.math.abs


fun combineDateTimeToString(context: Context, date: Long, hour: Int, min: Int): String {
    val timestamp = combineDateAndTime(date, hour, min)
    return formatReminderTimestampToString(context, timestamp, fallbackString = EMPTY_STRING)
}

fun formatDeadlineTimestampToString(
    context: Context,
    timestamp: Long,
    fallbackString: String,
): String {
    if (timestamp >= 0) {
        val daysDifference = calculateDaysDifference(timestamp)

        return when (daysDifference) {
            in Long.MIN_VALUE..-1L -> context.getString(
                R.string.deadline_xxx_days_ago,
                abs(daysDifference)
            )

            0L -> context.getString(R.string.deadline_today)
            1L -> context.getString(R.string.deadline_tomorrow)
            2L -> context.getString(R.string.deadline_the_day_after_tomorrow)
            in 3L..6L -> context.getString(R.string.deadline_in_xxx_days, daysDifference)
            7L -> context.getString(R.string.deadline_next_week)
            in 8L..365L -> context.getString(
                R.string.due_on_xxx, formatTimestampToReadableString(
                    timestamp,
                    DateFormatters.shortMonthAndDay(context)
                )
            )

            in 366L..Long.MAX_VALUE -> context.getString(
                R.string.due_on_xxx, formatTimestampToReadableString(
                    timestamp,
                    DateFormatters.yearWithShortMonthAndDay(context)
                )
            )

            else -> context.getString(R.string.invalid_time)
        }
    } else return fallbackString
}

fun formatReminderTimestampToString(
    context: Context,
    timestamp: Long,
    fallbackString: String
): String {
    if (timestamp > 0) {
        val daysDifference = calculateDaysDifference(timestamp)
        val timeStr =
            formatTimestampToReadableString(timestamp, DateFormatters.hourAndMinute(context))

        return when (daysDifference) {
            in Long.MIN_VALUE..-1L -> "${
                formatTimestampToReadableString(
                    timestamp,
                    DateFormatters.yearWithShortMonthAndDay(context)
                )
            } $timeStr" // 年月日 时分

            0L -> context.getString(R.string.reminder_today_xxx, timeStr)
            1L -> context.getString(R.string.reminder_tomorrow_xxx, timeStr)
            2L -> context.getString(R.string.reminder_the_day_after_tomorrow_xxx, timeStr)
            in 3L..7L -> context.getString(
                R.string.reminder_xxx_days_later_time,
                daysDifference,
                timeStr
            )

            in 8L..365L -> "${
                formatTimestampToReadableString(
                    timestamp,
                    DateFormatters.shortMonthAndDay(context)
                )
            }, $timeStr"

            in 366L..Long.MAX_VALUE -> context.getString(
                R.string.due_on_xxx_time, formatTimestampToReadableString(
                    timestamp,
                    DateFormatters.yearWithShortMonthAndDay(context)
                ), timeStr
            )

            else -> context.getString(R.string.invalid_time)
        }

    } else return fallbackString
}

fun formatRepeatPeriodToString(
    context: Context,
    num: Int,
    period: String,
    fallbackString: String,
): String {
    val numText = if (num > 1) "$num" else "" //注意空格

    return try {
        if (period != RepeatPeriod.None.content && num != 0) {
            val periodMap = mapOf(
                RepeatPeriod.Day.content to context.getString(R.string.option_day),
                RepeatPeriod.Week.content to context.getString(R.string.option_week),
                RepeatPeriod.Month.content to context.getString(R.string.option_month),
                RepeatPeriod.Year.content to context.getString(R.string.option_year),
            )

            context.getString(R.string.repeat_every_num_period, numText, periodMap[period])
        } else fallbackString
    } catch (_: Exception) {
        fallbackString
    }
}

/**
 * 获取第一行的内容
 *
 * @return
 */
fun String.getFirstLine(): String {
    return this.lines().firstOrNull() ?: ""
}

/**
 * 移除第一行的内容，返回剩下的字符串
 *
 * @return
 */
fun String.removeFirstLine(): String {
    return this.lineSequence()
        .drop(1)
        .joinToString("\n")
}
