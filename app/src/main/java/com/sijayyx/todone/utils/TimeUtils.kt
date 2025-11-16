package com.sijayyx.todone.utils

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.util.Log
import com.sijayyx.todone.R
import com.sijayyx.todone.ui.checklists.RepeatPeriod
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

object DateFormatters {
    // 自定义格式化器
    private fun getAppLanguageLocale(context: Context?): Locale {
        if (context != null) {
            val resources: Resources = context.resources
            val configuration: Configuration = resources.configuration

            return configuration.locales[0]
        } else
            return Locale.US
    }

    fun fullDate(context: Context? = null): DateTimeFormatter =
        DateTimeFormatter.ofPattern(
            context?.getString(R.string.date_formatter_full_date) ?: "yyyy MMM. dd EEEE, HH:mm",
            getAppLanguageLocale(context)
        )

    fun shortMonthAndDay(context: Context? = null): DateTimeFormatter =
        DateTimeFormatter.ofPattern(
            context?.getString(R.string.date_formatter_short_month_and_day) ?: "MMM. dd",
            getAppLanguageLocale(context)
        )

    fun yearWithShortMonthAndDay(context: Context? = null): DateTimeFormatter =
        DateTimeFormatter.ofPattern(
            context?.getString(R.string.date_formatter_year_short_month_day) ?: "MMM. dd, yyyy",
            getAppLanguageLocale(context)
        )

    fun simpleYearMonthDay(context: Context? = null): DateTimeFormatter =
        DateTimeFormatter.ofPattern(
            context?.getString(R.string.date_formatter_simple_year_month_day) ?: "yyyy/MMM/dd",
            getAppLanguageLocale(context)
        )

    fun simpleYearMonthDayTime(context: Context? = null): DateTimeFormatter =
        DateTimeFormatter.ofPattern(
            context?.getString(R.string.date_formatter_simple_year_month_day_time)
                ?: "yyyy/MMM/dd HH:mm",
            getAppLanguageLocale(context)
        )

    fun monthDayAndWeekday(context: Context? = null): DateTimeFormatter =
        DateTimeFormatter.ofPattern(
            context?.getString(R.string.date_formatter_month_day_weekday) ?: "MMM. dd, EEE",
            getAppLanguageLocale(context)
        )

    fun hourAndMinute(context: Context? = null): DateTimeFormatter =
        DateTimeFormatter.ofPattern(
            context?.getString(R.string.date_formatter_hour_and_minute) ?: "HH:mm",
            getAppLanguageLocale(context)
        )
}

// 计算与现在相差的天数（不考虑时间部分）
fun calculateDaysDifference(timestampLocal: Long): Long {
    val targetDate = Instant.ofEpochMilli(timestampLocal)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()

    val currentDate = LocalDate.now()

    return ChronoUnit.DAYS.between(currentDate, targetDate)
}

fun calculateHoursDifferenceFromNow(timestampLocal: Long): Long {
    val targetDate = Instant.ofEpochMilli(timestampLocal)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()

    val currentDate = LocalDateTime.now()

    return ChronoUnit.HOURS.between(currentDate, targetDate)
}

fun calculateMinutesDifferenceFromNow(timestampLocal: Long): Long {
    val targetDate = Instant.ofEpochMilli(timestampLocal)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()

    val currentDate = LocalDateTime.now()

    return ChronoUnit.MINUTES.between(currentDate, targetDate)
}

fun getCurrentTimestamp(): Long {
    return System.currentTimeMillis()
}

/**
 * 是否为未来时间？
 *
 * @param timestamp
 * @return
 */
fun checkIsFutureTimestamp(timestamp: Long): Boolean {
    return timestamp > System.currentTimeMillis()
}

fun getTodayStartTimestamp(): Long {
    return LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

fun getTodayEndTimestamp(): Long {
    return LocalDate.now().atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant()
        .toEpochMilli()
}

fun getHourAndMinuteFromTimestamp(timestamp: Long): Pair<Int, Int> {
    val localDateTime = Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()

    return Pair(localDateTime.hour, localDateTime.minute)
}

fun localTimestampConvertToUtc(timestampLocal: Long): Long {

    return LocalDateTime.ofInstant(
        Instant.ofEpochMilli(timestampLocal),
        ZoneId.systemDefault()
    ).atZone(ZoneId.systemDefault())
        .withZoneSameInstant(ZoneId.of("UTC"))
        .toInstant()
        .toEpochMilli()
}

fun utcTimeConvertToLocal(utcTimestamp: Long): Long {
    return Instant.ofEpochMilli(utcTimestamp)
        .atZone(ZoneId.of("UTC"))
        .withZoneSameInstant(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
}

fun checkTimeInToday(ts: Long): Boolean {
    return try {
        val instant = Instant.ofEpochMilli(ts)
        val date = instant.atZone(ZoneId.systemDefault()).toLocalDate()
        val today = LocalDate.now(ZoneId.systemDefault())
        date == today
    } catch (_: Exception) {
        false
    }
}


//输出utc时间
fun combineDateAndTime(date: Long, hour: Int, min: Int): Long {
    // 将日期时间戳转换为 LocalDate
    val localDate = Instant.ofEpochMilli(date)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()

    // 创建 LocalDateTime 并合并时间
    val localDateTime = LocalDateTime.of(localDate, LocalTime.of(hour, min))

    // 转换回时间戳
    return localDateTime
        .atZone(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
}

fun getTimesLaterTimestampFromNow(
    days: Long = 0,
    hours: Long = 0,
    minutes: Long = 0,
    seconds: Long = 0
): Long {
    return Instant.now()
        .atZone(ZoneId.systemDefault())
        .plusDays(days)
        .plusHours(hours)
        .plusMinutes(minutes)
        .plusSeconds(seconds)
        .toInstant().toEpochMilli()
}

fun timeAfterOfLocalDate(
    dateLocal: Long,
    days: Long = 0,
    hours: Long = 0,
    minutes: Long = 0,
    years: Long = 0,
    weeks: Long = 0,
    months: Long = 0,
): Long {
    return Instant.ofEpochMilli(dateLocal)
        .atZone(ZoneId.systemDefault())
        .plusDays(days)
        .plusHours(hours)
        .plusMinutes(minutes)
        .plusWeeks(weeks)
        .plusMonths(months)
        .plusYears(years)
        .toInstant().toEpochMilli()
}

fun timeBeforeOfLocalDate(
    dateLocal: Long,
    days: Long = 0,
    hours: Long = 0,
    minutes: Long = 0
): Long {
    return Instant.ofEpochMilli(dateLocal)
        .atZone(ZoneId.systemDefault())
        .minusDays(days)
        .minusHours(hours)
        .minusMinutes(minutes)
        .toInstant().toEpochMilli()
}

fun getDaysEndTimestampAfterNow(days: Int): Long {
    return LocalDate.now()
        .plusDays(days.toLong())
        .atTime(LocalTime.MAX)
        .atZone(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
}

fun getDaysStartTimestampAfterNow(days: Int): Long {
    return LocalDate.now()
        .plusDays(days.toLong())
        .atStartOfDay()
        .atZone(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
}

fun formatTimestampToReadableString(timestamp: Long, formatter: DateTimeFormatter): String {
    return Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .format(formatter)
}

/**
 * 根据周期计算下一次的时间戳
 *
 * @param timestamp 当前的时间戳
 * @param repeatNum 重复数量
 * @param repeatPeriod 重复周期单位
 * @return 下一次的时间戳
 */
fun calculateNextTimeToRepeat(timestamp: Long, repeatNum: Int, repeatPeriod: String): Long {
    if (repeatNum <= 0) {
        Log.e(TAG, "TimeUtils: Repeat num cant be ZERO!!!")
        return timestamp
    }

    val nextTime: Long = when (repeatPeriod) {
        RepeatPeriod.Day.content -> timeAfterOfLocalDate(
            timestamp,
            days = repeatNum.toLong()
        )

        RepeatPeriod.Week.content -> timeAfterOfLocalDate(
            timestamp,
            weeks = repeatNum.toLong()
        )

        RepeatPeriod.Month.content -> timeAfterOfLocalDate(
            timestamp,
            months = repeatNum.toLong()
        )

        RepeatPeriod.Year.content -> timeAfterOfLocalDate(
            timestamp,
            years = repeatNum.toLong()
        )

        else -> {
            Log.e(
                TAG,
                "TimeUtils:no valid RepeatPeriod $repeatPeriod"
            )
            throw Exception("TimeUtils: Invalid repeat period!!!!")
        }
    }

    return nextTime
}
