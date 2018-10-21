package io.eodc.planit.helper

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Formatter to format the x-axis of the week graph
 */
class DateValueFormatter : IAxisValueFormatter {
    override fun getFormattedValue(value: Float, axis: AxisBase): String {
        val sdf = SimpleDateFormat("EEEEE", Locale.ENGLISH)
        return sdf.format(Date(value.toLong()))
    }
}
