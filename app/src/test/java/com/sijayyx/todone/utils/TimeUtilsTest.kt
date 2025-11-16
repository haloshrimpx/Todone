package com.sijayyx.todone.utils

import com.sijayyx.todone.ui.checklists.RepeatPeriod
import org.junit.Assert.*
import org.junit.Test

class TimeUtilsTest {
    @Test
    fun `check repeat period calculate function`() {
        val initialStamp = 1735660800000L

        val oneDaysLater = calculateNextTimeToRepeat(initialStamp, 1, RepeatPeriod.Day.content)
        val fiveDayLater = calculateNextTimeToRepeat(initialStamp, 5, RepeatPeriod.Day.content)
        val twoMonthsLater = calculateNextTimeToRepeat(initialStamp, 2, RepeatPeriod.Month.content)

        assert(oneDaysLater == 1735747200000)
        assert(fiveDayLater == 1736092800000)
        assert(twoMonthsLater == 1740758400000)
    }

    @Test
    fun `check time in today func`() {
        val todayTime = System.currentTimeMillis()

        val result = checkTimeInToday(todayTime)

        assertTrue(result)
    }
}