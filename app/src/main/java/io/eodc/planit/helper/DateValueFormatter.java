package io.eodc.planit.helper;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Formatter to format the x-axis of the week graph
 */
public class DateValueFormatter implements IAxisValueFormatter {
    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEEE", Locale.ENGLISH);
        return sdf.format(new Date((long) value));
    }
}
