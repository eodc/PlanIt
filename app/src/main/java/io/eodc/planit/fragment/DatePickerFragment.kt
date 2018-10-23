package io.eodc.planit.fragment

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import java.util.*

/**
 * Fragment that shows a [DatePickerDialog] and interfaces it with an
 * [android.app.DatePickerDialog.OnDateSetListener]
 *
 * @author 2n
 */
class DatePickerFragment : androidx.fragment.app.DialogFragment() {

    private var year: Int = 0
    private var month: Int = 0
    private var day: Int = 0

    private var dateSetListener: DatePickerDialog.OnDateSetListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance()
        if (year == 0 && month == 0 && day == 0) {
            year = c.get(Calendar.YEAR)
            month = c.get(Calendar.MONTH) + 1
            day = c.get(Calendar.DAY_OF_MONTH) + 1
        }

        return DatePickerDialog(requireActivity(), dateSetListener, year, month - 1, day)
    }

    /**
     * Sets the date listener
     */
    private fun setOnDateSetListener(listener: DatePickerDialog.OnDateSetListener) {
        dateSetListener = listener
    }

    /**
     * Sets the shown date on the dialog
     *
     * @param year  The year to show
     * @param month The month to show, 0-indexed (January is 0, December is 11)
     * @param day   The day to show
     */
    private fun setDate(year: Int, month: Int, day: Int) {
        this.year = year
        this.month = month
        this.day = day
    }

    companion object {

        /**
         * Creates an instance of DatePickerFragment, automatically showing the current day.
         *
         * @param listener The listener listening for date selections
         * @return A new instance of DatePickerFragment
         */
        fun newInstance(listener: DatePickerDialog.OnDateSetListener): DatePickerFragment {
            val fragment = DatePickerFragment()
            fragment.setOnDateSetListener(listener)

            return fragment
        }

        /**
         * Creates an instance of DatePickerFragment, showing the specified day
         *
         * @param listener The listener listening for date selections
         * @param year     The year to show
         * @param month    The month to show, 0-indexed (January is 0, December is 11)
         * @param day      THe day to show
         * @return A new instance of DatePickerFragment
         */
        fun newInstance(listener: DatePickerDialog.OnDateSetListener, year: Int,
                        month: Int,
                        day: Int): DatePickerFragment {
            val fragment = newInstance(listener)
            fragment.setDate(year, month, day)
            return fragment
        }
    }
}
