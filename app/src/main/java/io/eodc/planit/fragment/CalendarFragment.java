package io.eodc.planit.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import org.joda.time.DateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.eodc.planit.R;
import io.eodc.planit.adapter.AssignmentsAdapter;
import io.eodc.planit.db.PlannerContract;
import io.eodc.planit.helper.AssignmentTouchHelper;
import timber.log.Timber;

/**
 * Fragment that displays a month's assignments, as well as the information on those assignments.
 *
 * @author 2n
 */
public class CalendarFragment extends BaseFragment implements
        OnDateSelectedListener,
        OnMonthChangedListener,
        DayViewDecorator {

    private static final int LOADER_CLASSES     = 0;
    private static final int LOADER_DUE_MONTH   = 1;
    private static final int LOADER_DUE_DAY     = 2;

    private static final String ARG_DATE = "date";

    @BindView(R.id.calendar)            MaterialCalendarView    mCalendar;
    @BindView(R.id.rv_day_assignments)  RecyclerView            mRvDaysAssignments;
    @BindView(R.id.layout_nothing_due)  LinearLayout            mLayoutNothingDue;

    private AssignmentsAdapter assignmentsAdapter;
    private HashMap<DateTime, Integer> dateIntegerHashMap;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(LOADER_CLASSES, null, this);

        DateTime monthSelected = new DateTime();
        monthSelected = monthSelected.withDayOfMonth(1).withTimeAtStartOfDay();
        Bundle args = new Bundle();
        args.putSerializable(ARG_DATE, monthSelected.toDate());
        getLoaderManager().initLoader(LOADER_DUE_MONTH, args, this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mCalendar.setSelectedDate(new Date());
        mCalendar.setOnMonthChangedListener(this);
        mCalendar.setOnDateChangedListener(this);
    }

    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_DATE, date.getDate());
        getLoaderManager().restartLoader(LOADER_DUE_DAY, args, this);
    }

    @Override
    public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
        DateTime monthSelected = new DateTime(date.getDate());
        monthSelected = monthSelected.withDayOfMonth(1).withTimeAtStartOfDay();
        Bundle args = new Bundle();
        args.putSerializable(ARG_DATE, monthSelected.toDate());
        getLoaderManager().restartLoader(LOADER_DUE_MONTH, args, this);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == LOADER_CLASSES) {
            return new CursorLoader(requireContext(), PlannerContract.ClassColumns.CONTENT_URI,
                    null, null, null, null);
        }

        Date dateSelected = (Date) args.getSerializable(ARG_DATE);
        if (dateSelected != null) {
            switch (id) {
                case LOADER_DUE_DAY:
                    return new CursorLoader(requireContext(), PlannerContract.AssignmentColumns.CONTENT_URI,
                            null, PlannerContract.AssignmentColumns.DUE_DATE + "= date(" + dateSelected.getTime() / 1000 + ", 'unixepoch', 'localtime') and " +
                            PlannerContract.AssignmentColumns.COMPLETED + "=0", null,
                            null);
                case LOADER_DUE_MONTH:
                    return new CursorLoader(requireContext(), PlannerContract.AssignmentColumns.CONTENT_URI,
                            null,
                            PlannerContract.AssignmentColumns.DUE_DATE + " >= date(" + dateSelected.getTime() / 1000 + ", 'unixepoch', 'localtime') and " +
                                    PlannerContract.AssignmentColumns.DUE_DATE + " <= date(" + dateSelected.getTime() / 1000 + ", 'unixepoch', 'localtime', '+1 month', '-1 day') and " +
                                    PlannerContract.AssignmentColumns.COMPLETED + "=0",
                            null, PlannerContract.AssignmentColumns.DUE_DATE + " asc");
            }
        }
        return new CursorLoader(requireContext());
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        int id = loader.getId();
        switch (id) {
            case LOADER_CLASSES:
                assignmentsAdapter = new AssignmentsAdapter(requireContext(), data, this, false);
                Bundle args = new Bundle();
                args.putSerializable(ARG_DATE, new Date());
                getLoaderManager().initLoader(LOADER_DUE_DAY, args, this);
                mRvDaysAssignments.setAdapter(assignmentsAdapter);
                mRvDaysAssignments.setLayoutManager(new LinearLayoutManager(getActivity()));
                ItemTouchHelper.SimpleCallback touchSimpleCallback = new AssignmentTouchHelper(requireContext(), 0,
                        ItemTouchHelper.RIGHT, this);
                ItemTouchHelper touchHelper = new ItemTouchHelper(touchSimpleCallback);
                touchHelper.attachToRecyclerView(mRvDaysAssignments);
                break;
            case LOADER_DUE_DAY:
                if (data.getCount() > 0) {
                    mLayoutNothingDue.setVisibility(View.GONE);
                    mRvDaysAssignments.setVisibility(View.VISIBLE);
                    assignmentsAdapter.swapAssignmentsCursor(data);
                } else {
                    mRvDaysAssignments.setVisibility(View.GONE);
                    mLayoutNothingDue.setVisibility(View.VISIBLE);
                }
                break;
            case LOADER_DUE_MONTH:
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                    dateIntegerHashMap = new HashMap<>();
                    int checkPosition = -1;
                    while (data.moveToNext()) {
                        DateTime currentDate = new DateTime(sdf.parse(data.getString(data.getColumnIndex(PlannerContract.AssignmentColumns.DUE_DATE))));
                        DateTime nextDate = new DateTime(currentDate);
                        int count = 0;
                        while (currentDate.getDayOfYear() == nextDate.getDayOfYear() && data.moveToNext()) {
                            nextDate = new DateTime(sdf.parse(data.getString(data.getColumnIndex(PlannerContract.AssignmentColumns.DUE_DATE))));
                            count++;
                        }
                        if (data.getPosition() == data.getCount()) count++;
                        dateIntegerHashMap.put(currentDate, count);
                        checkPosition++;
                        if (data.getPosition() != data.getCount()) data.moveToPosition(checkPosition);
                    }
                    mCalendar.addDecorator(this);
                } catch (ParseException e) {
                    Timber.e(e);
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        for (Object o : dateIntegerHashMap.entrySet()) {
            Map.Entry pair = (Map.Entry) o;
            DateTime dtCalDay = new DateTime(day.getDate());
            DateTime dtKey = (DateTime) pair.getKey();
            if (dtKey.getDayOfMonth() == dtCalDay.getDayOfMonth() && (int) pair.getValue() > 0) return true;
        }
        return false;
    }

    @Override
    public void decorate(DayViewFacade view) { view.addSpan(new DotSpan(5f)); }

    @Override
    public void onAssignmentEdit() {
        Bundle dayLoaderArgs = new Bundle();
        Date selectedDate = mCalendar.getSelectedDate().getDate();
        dayLoaderArgs.putSerializable(ARG_DATE, selectedDate);

        Bundle monthLoaderArgs = new Bundle();
        DateTime selectedMonth = new DateTime(mCalendar.getSelectedDate().getDate());
        selectedMonth = selectedMonth.withDayOfMonth(1).withTimeAtStartOfDay();
        monthLoaderArgs.putSerializable(ARG_DATE, selectedMonth.toDate());

        getLoaderManager().restartLoader(LOADER_DUE_MONTH, monthLoaderArgs, this);
        getLoaderManager().restartLoader(LOADER_DUE_DAY, dayLoaderArgs, this);
    }

    @Override
    public void onAssignmentComplete(Cursor cursor) {
        super.onAssignmentComplete(cursor);
        onAssignmentEdit();
    }

    @Override
    public void onAssignmentCreation() { }
}
