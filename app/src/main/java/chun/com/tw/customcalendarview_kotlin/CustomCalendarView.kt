package chun.com.tw.customcalendarview_kotlin

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.days

//客製化日曆
class CustomCalendarView : LinearLayout {

    // View 畫面
    private var root: View? = null
    private var dateTitle: TextView? = null
    private var title_label_1: TextView? = null
    private var leftButton: ImageView? = null
    private var rightButton: ImageView? = null
    private var calendarMonthLayout: ViewGroup? = null

    // Calendar 日曆
    private var minCalendar = Calendar.getInstance()
    private var currentCalendar = Calendar.getInstance()
    private var lastSelectedDayCalendar: Calendar? = null

    // Event 事件
    private var listener: CustomCalendarListener? = null

    //單點擊事件
    private val onClickListener = OnClickListener { view ->
        val calendar = getDaysInCalendar(view)
        val week = calendar.get(Calendar.DAY_OF_WEEK)
        val isGreaterThanMinDate = calendar.compareTo(minCalendar) >= 1

        // 註記天
        if (!disableWeek.contains(week) && isGreaterThanMinDate) {
            markDayAsSelectedDay(calendar.time)
        }

        // 錯誤處理
        if (listener == null) {
            throw IllegalStateException(resources.getString(R.string.Error))
        } else {
//            if (!disableWeek.contains(week) && isGreaterThanMinDate) {
//                listener?.onDayClick(calendar.time)
//            }
            listener?.onDayClick(calendar.time)
        }
    }

    //長點擊事件
    private val onLongClickListener = OnLongClickListener { view ->
        val calendar = getDaysInCalendar(view)
        val week = calendar.get(Calendar.DAY_OF_WEEK)
        val isGreaterThanMinDate = calendar.compareTo(minCalendar) >= 1

        // 註記天
        if (!disableWeek.contains(week) && isGreaterThanMinDate){
            markDayAsSelectedDay(calendar.time)
        }
        // 錯誤處理
        if (listener == null) {
            throw IllegalStateException(resources.getString(R.string.Error))
        } else {
            if (!disableWeek.contains(week) && isGreaterThanMinDate) {
                listener?.onDayClick(calendar.time)
            }
        }
        true
    }

    // Other 其他
    private var shortWeekDays = false
    private var disableWeek = arrayListOf<Int>()

    // 最低日期
    var minDate: Date?
        get() = minCalendar.time
        set(date) {
            minCalendar.time = date
            updateView()
        }

    // 現在的日期
    var date: Date
        get() = currentCalendar.time
        set(date) {
            currentCalendar.time = date
            updateView()
        }

    // 取得選定日期
    val selectedDate: Date?
        get() = lastSelectedDayCalendar?.time


    /**
     * 初始化
     */
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        if (!isInEditMode) {
            val inflate = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            root = inflate.inflate(R.layout.calendar_view, this, true)
            root?.let { findViewsById(it) }
            setUpEventListeners()

            minCalendar.set(1970, 0, 1, 0, 0, 0)
            updateView()
        }
    }

    companion object {
        private const val DAY_OF_THE_WEEK_TEXT = "dayOfTheWeekText"
        private const val DAY_OF_THE_WEEK_LAYOUT = "dayOfTheWeekLayout"
        private const val DAY_OF_THE_MONTH_LAYOUT = "dayOfTheMonthLayout"
        private const val DAY_OF_THE_MONTH_TEXT = "dayOfTheMonthText"
        private const val DAY_OF_THE_MONTH_BACKGROUND = "dayOfTheMonthBackground"
        private const val DAY_OF_THE_MONTH_IMAGE_1 = "dayOfTheMonthImage"
//        private const val DAY_OF_THE_MONTH_CIRCLE_IMAGE_1 = "dayOfTheMonthCircleImage1"
//        private const val DAY_OF_THE_MONTH_CIRCLE_IMAGE_2 = "dayOfTheMonthCircleImage2"
    }

    //實作客製化監聽
    interface CustomCalendarListener {

        fun onDayClick(date: Date)

        fun onDayLongClick(date: Date)

        fun onRightButtonClick()

        fun onLeftButtonClick()
    }

    //顯示日期標題
    fun showDateTitle(show: Boolean) {
        calendarMonthLayout?.visibility = if (show) View.VISIBLE else View.GONE
    }

    //設定短工作日
    fun setShortWeekDays(shortWeekDays: Boolean) {
        this.shortWeekDays = shortWeekDays
        setUpWeekDaysLayout() //設定 WeekDaysLayout
    }

    //設定禁用週
    fun setDisableWeek(week: Array<Int>) {
        disableWeek.clear()
        disableWeek.addAll(week)
        clearSelectedDay()
        setUpDaysInCalendar() //設定日曆中的日期
    }

    //設定自訂日曆監聽器
    fun setCustomCalendarListener(listener: CustomCalendarListener) {
        this.listener = listener
    }

//    //標記圓圈
//    fun markCircleImage1(date: Date) {
//        val calendar = Calendar.getInstance()
//        calendar.time = date
//
//        val circleImage1 = getCircleImage1(calendar)
//        circleImage1.visibility = View.VISIBLE
//
//        if (lastSelectedDayCalendar != null && isTheSameDay(calendar, lastSelectedDayCalendar!!)) {
//            setDrawbleTint(circleImage1, R.color.calendar_selected_day_font)
//        } else {
//            setDrawbleTint(circleImage1, R.color.calendar_circle_1)
//        }
//    }

    //標記圓圈
    fun markCircleImage2(date: Date) {
        val calendar = Calendar.getInstance()
        calendar.time = date

//        val circleImage2 = getCircleImage2(calendar)
//        circleImage2.visibility = View.VISIBLE
//
//        if (lastSelectedDayCalendar != null && isTheSameDay(calendar, lastSelectedDayCalendar!!)) {
//            setDrawbleTint(circleImage2, R.color.calendar_selected_day_font)
//        } else {
//            setDrawbleTint(circleImage2, R.color.calendar_circle_2)
//        }
    }

    //清除所選日期
    fun clearSelectedDay() {
        lastSelectedDayCalendar?.let {

//            // 如果是今天，請保持當天的風格
//            val now = Calendar.getInstance()
//            val bg = when {
//                now.equals(it, Calendar.YEAR) && now.equals(it, Calendar.DAY_OF_YEAR) -> R.drawable.ring
//                else -> android.R.color.transparent
//            }
//            val dayOfTheMonthBackground = getDayOfMonthBackground(it)
//            dayOfTheMonthBackground.setBackgroundResource(bg)

            val dayOfTheMonth = getDayOfMonthText(it)
            dayOfTheMonth.setTextColor(ContextCompat.getColor(context, R.color.calendar_day_of_the_month_font))

//            val circleImage1 = getCircleImage1(it)
//            val circleImage2 = getCircleImage2(it)
//
//            if (circleImage1.visibility == View.VISIBLE) {
//                setDrawbleTint(circleImage1, R.color.calendar_circle_1)
//            }
//
//            if (circleImage2.visibility == View.VISIBLE) {
//                setDrawbleTint(circleImage2, R.color.calendar_circle_2)
//            }
            lastSelectedDayCalendar = null
        }
    }

    private fun findViewsById(view: View) {
        calendarMonthLayout = view.findViewById(R.id.view_calendar)
        leftButton = view.findViewById(R.id.btn_left)
        rightButton = view.findViewById(R.id.btn_right)
        dateTitle = view.findViewById(R.id.tv_month)
        title_label_1 = view.findViewById(R.id.title_label_1)
        val inflate = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        for (i in 0..41) {
            val weekIndex = i % 7 + 1
            val dayOfTheWeekLayout = view.findViewWithTag<ViewGroup>(DAY_OF_THE_WEEK_LAYOUT + weekIndex)

            // 創建該月的某一天
            val dayOfTheMonthLayout = inflate.inflate(R.layout.calendar_day, null)
            val dayOfTheMonthText = dayOfTheMonthLayout.findViewWithTag<TextView>(DAY_OF_THE_MONTH_TEXT)
            val dayOfTheMonthBackground = dayOfTheMonthLayout.findViewWithTag<ViewGroup>(DAY_OF_THE_MONTH_BACKGROUND)
            val dayOfTheMonthImage1 = dayOfTheMonthLayout.findViewWithTag<ImageView>(DAY_OF_THE_MONTH_IMAGE_1)
//            val dayOfTheMonthCircleImage1 = dayOfTheMonthLayout.findViewWithTag<ImageView>(DAY_OF_THE_MONTH_CIRCLE_IMAGE_1)
//            val dayOfTheMonthCircleImage2 = dayOfTheMonthLayout.findViewWithTag<ImageView>(DAY_OF_THE_MONTH_CIRCLE_IMAGE_2)

            // 設定標籤來識別它們
            val viewIndex = i + 1
            dayOfTheMonthLayout.setTag(DAY_OF_THE_MONTH_LAYOUT + viewIndex)
            dayOfTheMonthText.setTag(DAY_OF_THE_MONTH_TEXT + viewIndex)
            dayOfTheMonthBackground.setTag(DAY_OF_THE_MONTH_BACKGROUND + viewIndex)
            dayOfTheMonthImage1.setTag(DAY_OF_THE_MONTH_IMAGE_1 + viewIndex)
            //dayOfTheMonthCircleImage1.setTag(DAY_OF_THE_MONTH_CIRCLE_IMAGE_1 + viewIndex)
            //dayOfTheMonthCircleImage2.setTag(DAY_OF_THE_MONTH_CIRCLE_IMAGE_2 + viewIndex)

            dayOfTheWeekLayout.addView(dayOfTheMonthLayout)
        }
    }

    //設定事件監聽器
    private fun setUpEventListeners() {
        leftButton?.setOnClickListener { view ->
            if (listener == null) {
                throw IllegalStateException(resources.getString(R.string.Error1))
            }
            // 減少月份
            currentCalendar.add(Calendar.MONTH, -1)
            lastSelectedDayCalendar = null
            updateView()
            listener?.onLeftButtonClick()
        }

        rightButton?.setOnClickListener { view ->
            if (listener == null) {
                throw IllegalStateException(resources.getString(R.string.Error1))
            }
            // 增加月份
            currentCalendar.add(Calendar.MONTH, 1)
            lastSelectedDayCalendar = null
            updateView()
            listener?.onRightButtonClick()
        }
    }

    //設定月份佈局
    private fun setUpMonthLayout() {
//        val calendar = Calendar.getInstance()
        var dateText = DateFormatSymbols(Locale.getDefault()).months[currentCalendar.get(Calendar.MONTH)]
        dateText = dateText.substring(0, 1).uppercase(Locale.ROOT) + dateText.subSequence(1, dateText.length)
//        dateTitle?.text = if (calendar.equals(currentCalendar, Calendar.YEAR)) {
//            dateText
//        } else {
//            String.format("%s %s", dateText, currentCalendar.get(Calendar.YEAR))
//        }
        dateTitle?.text = dateText
        title_label_1?.text = currentCalendar.get(Calendar.YEAR).toString()
    }

    //設定 WeekDaysLayout
    private fun setUpWeekDaysLayout() {
        var dayOfWeek: TextView
        var dayOfTheWeekString: String
        val weekDaysArray = DateFormatSymbols(Locale.getDefault()).shortWeekdays
        val length = weekDaysArray.size
        for (i in 1 until length) {
            dayOfWeek = root!!.findViewWithTag(DAY_OF_THE_WEEK_TEXT + getWeekIndex(i, currentCalendar))

            //將第一天變成禮拜一
            if(i == 7) {
                dayOfTheWeekString = weekDaysArray[1]
            } else {
                dayOfTheWeekString = weekDaysArray[i + 1]
            }

            if (shortWeekDays) {
                dayOfTheWeekString =
                    if ("TW" == Locale.getDefault().country) {
                        dayOfTheWeekString.substring(dayOfTheWeekString.length - 1, dayOfTheWeekString.length)
                    } else {
                        dayOfTheWeekString.substring(0, 1).uppercase(Locale.ROOT)
                    }
            }
            dayOfWeek.text = dayOfTheWeekString
        }
    }

    //設定月份佈局
    private fun setUpDaysOfMonthLayout() {
        var dayOfTheMonthText: TextView
        var LoveImage1: View
        //var circleImage1: View
        //var circleImage2: View
        var dayOfTheMonthContainer: ViewGroup
        var dayOfTheMonthBackground: ViewGroup

        for (i in 1..42) {
            root?.let {
                dayOfTheMonthContainer = it.findViewWithTag(DAY_OF_THE_MONTH_LAYOUT + i)
                dayOfTheMonthBackground = it.findViewWithTag(DAY_OF_THE_MONTH_BACKGROUND + i)
                dayOfTheMonthText = it.findViewWithTag(DAY_OF_THE_MONTH_TEXT + i)
                LoveImage1 = it.findViewWithTag(DAY_OF_THE_MONTH_IMAGE_1 + i)
                //circleImage1 = it.findViewWithTag(DAY_OF_THE_MONTH_CIRCLE_IMAGE_1 + i)
                //circleImage2 = it.findViewWithTag(DAY_OF_THE_MONTH_CIRCLE_IMAGE_2 + i)

                dayOfTheMonthText.visibility = View.INVISIBLE
                LoveImage1.visibility = View.GONE
                //circleImage1.visibility = View.GONE
                //circleImage2.visibility = View.GONE

                // 應用程式樣式
                //dayOfTheMonthText.setBackgroundResource(android.R.color.transparent)
                //dayOfTheMonthText.setTypeface(null, Typeface.NORMAL)
                //dayOfTheMonthText.setTextColor(ContextCompat.getColor(context, R.color.calendar_day_of_the_month_font))
                //dayOfTheMonthContainer.setBackgroundResource(R.color.FFe5d6c4)
                dayOfTheMonthContainer.setOnClickListener(null)
                dayOfTheMonthBackground.setBackgroundResource(R.color.FFe5d6c4)
            }
        }
    }

    //設定日曆中的日期
    private fun setUpDaysInCalendar() {
        val auxCalendar = Calendar.getInstance(Locale.getDefault())
        auxCalendar.time = currentCalendar.time
        auxCalendar.set(Calendar.DAY_OF_MONTH, 1)

        val firstDayOfMonth = auxCalendar.get(Calendar.DAY_OF_WEEK)
        var dayOfTheMonthText: TextView?
        var dayOfTheMonthContainer: ViewGroup

        var dayOfTheMonthIndex = getWeekIndex(firstDayOfMonth, auxCalendar)
        run {
            var missingdays_lastmonth = firstDayOfMonth - 1 //上個月缺乏天數
            //下個月缺乏天數 (總數 42 - 本月天數 - 上月天數)
            var missingdays_nextmonth = 42 - auxCalendar.getActualMaximum(Calendar.DAY_OF_MONTH) - missingdays_lastmonth  //個月缺乏天數

            // 取得上個月的年份和月份
            val currentYearMonth = YearMonth.now()
            val previousYearMonth = currentYearMonth.minusMonths(1)

            // 取得上個月的最後一天
            val lastDayOfPreviousMonth = previousYearMonth.atEndOfMonth()

            // 補齊上個月
            val lastFiveDaysOfPreviousMonth = mutableListOf<LocalDate>()
            var currentDate = lastDayOfPreviousMonth.minusDays(missingdays_lastmonth.toLong())
            while (!currentDate.isAfter(lastDayOfPreviousMonth)) {
                currentDate = currentDate.plusDays(1)
                lastFiveDaysOfPreviousMonth.add(currentDate)
            }

            //填上上個月天數
            var lastmonth_i = 1
            while (lastmonth_i <= missingdays_lastmonth) {
                dayOfTheMonthContainer = root!!.findViewWithTag(DAY_OF_THE_MONTH_LAYOUT + lastmonth_i)
                dayOfTheMonthText = root!!.findViewWithTag(DAY_OF_THE_MONTH_TEXT + lastmonth_i)

                if (dayOfTheMonthText == null) {
                    break
                }

                dayOfTheMonthContainer.setOnClickListener(onClickListener)
                dayOfTheMonthText?.visibility = View.VISIBLE
                dayOfTheMonthText?.text = lastFiveDaysOfPreviousMonth[lastmonth_i - 1].dayOfMonth.toString()

                dayOfTheMonthContainer.setOnClickListener(onClickListener)
                dayOfTheMonthContainer.setOnLongClickListener(onLongClickListener)
                dayOfTheMonthText?.setTextColor(ContextCompat.getColor(context, R.color.white))

                lastmonth_i++
            }

            //填上這個月天數
            var nowmonth_i = 1
            while (nowmonth_i <= auxCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                dayOfTheMonthContainer = root!!.findViewWithTag(DAY_OF_THE_MONTH_LAYOUT + dayOfTheMonthIndex)
                dayOfTheMonthText = root!!.findViewWithTag(DAY_OF_THE_MONTH_TEXT + dayOfTheMonthIndex)

                if (dayOfTheMonthText == null) {
                    break
                }

                dayOfTheMonthText?.visibility = View.VISIBLE
                dayOfTheMonthText?.text = nowmonth_i.toString()

                dayOfTheMonthContainer.setOnClickListener(onClickListener)
                dayOfTheMonthContainer.setOnLongClickListener(onLongClickListener)
                dayOfTheMonthText?.setTextColor(ContextCompat.getColor(context, R.color.black))

                nowmonth_i++
                dayOfTheMonthIndex++
            }

            //填上下個月天數
            var nextmonth_i = 1
            while (nextmonth_i <= missingdays_nextmonth) {
                dayOfTheMonthContainer = root!!.findViewWithTag(DAY_OF_THE_MONTH_LAYOUT + dayOfTheMonthIndex)
                dayOfTheMonthText = root!!.findViewWithTag(DAY_OF_THE_MONTH_TEXT + dayOfTheMonthIndex)

                if (dayOfTheMonthText == null) {
                    break
                }
                dayOfTheMonthText?.visibility = View.VISIBLE
                dayOfTheMonthText?.text = nextmonth_i.toString()

                dayOfTheMonthContainer.setOnClickListener(onClickListener)
                dayOfTheMonthContainer.setOnLongClickListener(onLongClickListener)
                dayOfTheMonthText?.setTextColor(ContextCompat.getColor(context, R.color.white))

                nextmonth_i++
                dayOfTheMonthIndex++
            }
        }
    }

//    //設定日曆中的日期
//    private fun setUpDaysInCalendar() {
//        val auxCalendar = Calendar.getInstance(Locale.getDefault())
//        auxCalendar.time = currentCalendar.time
//        auxCalendar.set(Calendar.DAY_OF_MONTH, 1)
//
//        val firstDayOfMonth = auxCalendar.get(Calendar.DAY_OF_WEEK)
//        var dayOfTheMonthText: TextView?
//        var dayOfTheMonthContainer: ViewGroup
////        var dayOfTheMonthLayout: ViewGroup
////        var LoveImage1: View
//
////        // 計算某月某天索引
//        var dayOfTheMonthIndex = getWeekIndex(firstDayOfMonth, auxCalendar)
//        run {
//            var i = 1
//
//            while (i <= auxCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
//                dayOfTheMonthContainer = root!!.findViewWithTag(DAY_OF_THE_MONTH_LAYOUT + dayOfTheMonthIndex)
//                dayOfTheMonthText = root!!.findViewWithTag(DAY_OF_THE_MONTH_TEXT + dayOfTheMonthIndex)
////                LoveImage1 = root!!.findViewWithTag(DAY_OF_THE_MONTH_IMAGE_1 + dayOfTheMonthIndex)
//
//                if (dayOfTheMonthText == null) {
//                    break
//                }
//
//                dayOfTheMonthContainer.setOnClickListener(onClickListener)
//                dayOfTheMonthText?.visibility = View.VISIBLE
//                dayOfTheMonthText?.text = i.toString()
//
////                val calendar = Calendar.getInstance()
////                calendar.set(Calendar.YEAR, currentCalendar.get(Calendar.YEAR))
////                calendar.set(Calendar.MONTH, currentCalendar.get(Calendar.MONTH))
////                calendar.set(Calendar.DAY_OF_MONTH, i)
//
////                val week = calendar.get(Calendar.DAY_OF_WEEK)
////                val color =
////                    if (calendar < minCalendar || disableWeek.contains(week)) {
////                        R.color.FF0000FF
////                    } else {
////                        R.color.FF00FF00
////                    }
//                dayOfTheMonthContainer.setOnClickListener(onClickListener)
//                dayOfTheMonthContainer.setOnLongClickListener(onLongClickListener)
//                dayOfTheMonthText?.setTextColor(ContextCompat.getColor(context, R.color.black))
////                LoveImage1.visibility = View.VISIBLE
//                i++
//                dayOfTheMonthIndex++
//            }
//        }
//
////        for (i in 36..42) {
////            dayOfTheMonthText = root!!.findViewWithTag(DAY_OF_THE_MONTH_TEXT + i)
////            dayOfTheMonthLayout = root!!.findViewWithTag(DAY_OF_THE_MONTH_LAYOUT + i)
////
////            dayOfTheMonthLayout.visibility =
////                if (dayOfTheMonthText?.visibility == View.INVISIBLE) {
////                    View.GONE
////                } else {
////                    View.VISIBLE
////                }
////        }
//    }

    // 從視圖中的號碼取得日曆
    private fun getDaysInCalendar(view: View): Calendar {
        // 選擇提取日期
        val dayOfTheMonthContainer = view as ViewGroup
        var tagId = dayOfTheMonthContainer.tag as String
        tagId = tagId.substring(DAY_OF_THE_MONTH_LAYOUT.length, tagId.length)
        val dayOfTheMonthText = view.findViewWithTag<TextView>(DAY_OF_THE_MONTH_TEXT + tagId)

        // 從文字中提取日期
        val calendar = Calendar.getInstance()

        var changeCalender = Calendar.getInstance() //恢復本月
        //上個月
        //下個月
        //本月
        if( (tagId.toInt() < 13) && (Integer.valueOf(dayOfTheMonthText.text.toString()) > 23) ) {
            Log.i("EE" , "上個月")
            if(changeCalender != currentCalendar) {
                changeCalender = currentCalendar
            }
            // 減少月份
            changeCalender.add(Calendar.MONTH, -1)
        } else if ( (tagId.toInt() > 13) && (Integer.valueOf(dayOfTheMonthText.text.toString()) < 13) ) {
            Log.i("EE" , "下個月")
            if(changeCalender != currentCalendar) {
                changeCalender = currentCalendar
            }
            // 增加月份
            changeCalender.add(Calendar.MONTH, 1)
        } else {
            Log.i("EE" , "本月")
            changeCalender = currentCalendar
        }
        calendar.set(Calendar.YEAR, changeCalender.get(Calendar.YEAR))
        calendar.set(Calendar.MONTH, changeCalender.get(Calendar.MONTH))
        calendar.set(Calendar.DAY_OF_MONTH, Integer.valueOf(dayOfTheMonthText.text.toString()))
        return calendar
    }

    //將日期標記為目前日期
    private fun markDayAsCurrentDay() {
        // 如果是目前月份，則標記目前日期
        val calendar = Calendar.getInstance()

        if (calendar.equals(currentCalendar, Calendar.YEAR) && calendar.equals(currentCalendar, Calendar.MONTH)) {
            val dayOfTheMonthBackground = getDayOfMonthBackground(calendar)
            dayOfTheMonthBackground.setBackgroundResource(R.drawable.ring)
        }
    }

    //將日期標記為選定日期
    private fun markDayAsSelectedDay(date: Date) {
        val calendar = Calendar.getInstance()
        calendar.time = date

        // 清除前一天標記
        clearSelectedDay()

        // 將當前值儲存為最後值
        lastSelectedDayCalendar = calendar

        // 將目前日期標記為已選擇
        val dayOfTheMonth = getDayOfMonthText(calendar)
        val dayOfTheMonthBackground = getDayOfMonthBackground(calendar)
        val selectedColor = R.color.calendar_selected_day_font
        //val circleImage1 = getCircleImage1(calendar)
        //val circleImage2 = getCircleImage2(calendar)

        dayOfTheMonth.setTextColor(ContextCompat.getColor(context, selectedColor))
//        dayOfTheMonthBackground.setBackgroundResource(R.drawable.circle)

//        if (circleImage1.visibility == View.VISIBLE) {
//            setDrawbleTint(circleImage1, selectedColor)
//        }
//
//        if (circleImage2.visibility == View.VISIBLE) {
//            setDrawbleTint(circleImage2, selectedColor)
//        }
    }

    //更新畫面
    private fun updateView() {
        setUpMonthLayout() //設定月份佈局
        setUpWeekDaysLayout() //設定 WeekDaysLayout
        setUpDaysOfMonthLayout() //設定月份佈局
        setUpDaysInCalendar() //設定日曆中的日期
//        markDayAsCurrentDay() //將日期標記為目前日期
    }

    /**
     * 取得視圖
     * 設定每日背景
     */
    private fun getDayOfMonthBackground(currentCalendar: Calendar): ViewGroup {
        return getView(DAY_OF_THE_MONTH_BACKGROUND, currentCalendar) as ViewGroup
    }

    //取得月日文本
    private fun getDayOfMonthText(currentCalendar: Calendar): TextView {
        return getView(DAY_OF_THE_MONTH_TEXT, currentCalendar) as TextView
    }

    //愛心
    private fun getLoveImage(currentCalendar: Calendar): ImageView {
        return getView(DAY_OF_THE_MONTH_IMAGE_1, currentCalendar) as ImageView
    }

//    //第一種圓形圖
//    private fun getCircleImage1(currentCalendar: Calendar): ImageView {
//        return getView(DAY_OF_THE_MONTH_CIRCLE_IMAGE_1, currentCalendar) as ImageView
//    }
//
//    //第二種圓形圖
//    private fun getCircleImage2(currentCalendar: Calendar): ImageView {
//        return getView(DAY_OF_THE_MONTH_CIRCLE_IMAGE_2, currentCalendar) as ImageView
//    }

    //賦予畫面
    private fun getView(key: String, currentCalendar: Calendar): View {
        val index = getDayIndexByDate(currentCalendar)
        return root!!.findViewWithTag(key + index)
    }

    /**
     * 取得正確時間
     */
    private fun getDayIndexByDate(currentCalendar: Calendar): Int {
        val monthOffset = getMonthOffset(currentCalendar)
        val currentDay = currentCalendar.get(Calendar.DAY_OF_MONTH)
        return currentDay + monthOffset
    }

    //取得月份偏移量
    private fun getMonthOffset(currentCalendar: Calendar): Int {
        val calendar = Calendar.getInstance()
        calendar.time = currentCalendar.time
        calendar.set(Calendar.DAY_OF_MONTH, 1)

        val firstDayWeekPosition = calendar.firstDayOfWeek
        val dayPosition = calendar.get(Calendar.DAY_OF_WEEK)

        return when (firstDayWeekPosition) {
            1 -> dayPosition - 1
            else -> if (dayPosition == 1) 6 else dayPosition - 2
        }
    }

    //取得週索引
    private fun getWeekIndex(weekIndex: Int, currentCalendar: Calendar): Int {
        return when (currentCalendar.firstDayOfWeek) {
            1 -> weekIndex
            else -> if (weekIndex == 1) 7 else weekIndex - 1
        }
    }

    /**
     * 其他工具
     */
    private fun setDrawbleTint(image: ImageView, color: Int) {
        DrawableCompat.setTint(image.drawable, ContextCompat.getColor(context, color))
    }

    private fun isTheSameDay(calendarOne: Calendar, calendarTwo: Calendar): Boolean {
        return calendarOne.equals(calendarTwo, Calendar.YEAR) && calendarOne.equals(calendarTwo, Calendar.DAY_OF_YEAR)
    }

    private fun Calendar.equals(other: Calendar, unit: Int): Boolean {
        return this.get(unit) == other.get(unit)
    }
}