package com.appdev.eudemonia

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun ExpandableCalendarScreen() {
    val currentMonth = YearMonth.now()
    val selectedDate = mutableStateOf(LocalDate.now())
    ExpandableCalendar(
        selectedDate = selectedDate,
        currentMonth = currentMonth,
        onDateSelected = { date -> selectedDate.value = date },
        modifier = Modifier.fillMaxSize()
    )
}
