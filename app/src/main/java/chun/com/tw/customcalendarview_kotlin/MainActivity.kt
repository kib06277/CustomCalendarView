package chun.com.tw.customcalendarview_kotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Random

class MainActivity : AppCompatActivity() {

    //基本宣告
    lateinit var calendarPicker : CustomCalendarView
    lateinit var btn_clean : Button
    lateinit var btn_mark : Button
    lateinit var btn_selected : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        calendarPicker = findViewById(R.id.calendarPicker)
        btn_clean = findViewById(R.id.btn_clean)
        btn_mark = findViewById(R.id.btn_mark)
        btn_selected = findViewById(R.id.btn_selected)

        calendarPicker.showDateTitle(true)
        calendarPicker.setShortWeekDays(true)
        calendarPicker.minDate = Date()
        calendarPicker.date = Date()

        btn_selected.setOnClickListener{ v -> Log.e("time", calendarPicker.selectedDate.toString()) }

        btn_mark.setOnClickListener { view ->
            val calendar = Calendar.getInstance()

            for (i in 0 until 7) {
                calendarPicker.markCircleImage1(calendar.time)
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }
//            val random = Random(System.currentTimeMillis())
//            val style = random.nextInt(2)
//            val daySelected = random.nextInt(calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
//            calendar.set(Calendar.DAY_OF_MONTH, daySelected)
//
//            when (style) {
//                0 -> calendarPicker.markCircleImage1(calendar.time)
//                1 -> calendarPicker.markCircleImage2(calendar.time)
//                else -> {
//                }
//            }
        }

        btn_clean.setOnClickListener { v -> calendarPicker.clearSelectedDay() }

        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        calendarPicker.setCustomCalendarListener(object : CustomCalendarView.CustomCalendarListener {
            override fun onDayClick(date: Date) {
                val formattedDate = sdf.format(date)
                Toast.makeText(this@MainActivity, "onDayClick: $formattedDate", Toast.LENGTH_SHORT).show()
            }

            override fun onDayLongClick(date: Date) {
                val formattedDate = sdf.format(date)
                Toast.makeText(this@MainActivity, "onDayLongClick: $formattedDate", Toast.LENGTH_SHORT).show()
            }

            override fun onLeftButtonClick() {
                Toast.makeText(this@MainActivity, "onRightButtonClick", Toast.LENGTH_SHORT).show()
            }

            override fun onRightButtonClick() {
                Toast.makeText(this@MainActivity, "onLeftButtonClick", Toast.LENGTH_SHORT).show()
            }
        })
    }
}