package com.sijayyx.todone.ui.composables

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

//用于选择未来某一日期的选择器
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FutureDatePicker(
    initialSelectedFutureDate: Long,
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier.Companion
) {
    val datePickerState = rememberDatePickerState(
        //不知道为啥这里就不用加1天
        initialSelectedDateMillis = initialSelectedFutureDate,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val localDate = Instant.ofEpochMilli(utcTimeMillis)
                    .atZone(ZoneId.of("UTC"))
                    .toLocalDate()

                val currentDate = LocalDate.now()

                return ChronoUnit.DAYS.between(currentDate, localDate) >= 0
            }
        }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        dismissButton = {
            IconButton(
                onClick = {
                    onDismiss()
                },
            ) {
                Icon(
                    Icons.Filled.Clear,
                    contentDescription = null
                )
            }
        },
        confirmButton = {
            IconButton(
                onClick = {
                    onDateSelected(datePickerState.selectedDateMillis)
                    onDismiss()
                },
                enabled = datePickerState.selectedDateMillis != null
            ) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null
                )
            }
        },
    ) {
        DatePicker(datePickerState)
    }
}