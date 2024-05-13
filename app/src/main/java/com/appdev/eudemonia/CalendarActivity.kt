package com.appdev.eudemonia
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import

class CalendarActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ExpandableCalendar(onDayClick = {
            selectedDate = selectedDate,
            currentMonth = currentMonth,
            onDateSelected = { date -> selectedDate.value = date },
            modifier = Modifier.fillMaxSize()
        })
    }
}