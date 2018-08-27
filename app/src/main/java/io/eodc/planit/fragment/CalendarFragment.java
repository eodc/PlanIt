package io.eodc.planit.fragment;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.eodc.planit.R;
import io.eodc.planit.adapter.AssignmentsAdapter;
import io.eodc.planit.db.Assignment;
import io.eodc.planit.db.Class;
import io.eodc.planit.helper.AssignmentTouchHelper;
import io.eodc.planit.model.AssignmentListViewModel;
import io.eodc.planit.model.ClassListViewModel;

/**
 * Fragment that displays a month's assignments, as well as the information on those assignments.
 *
 * @author 2n
 */
public class CalendarFragment extends BaseFragment implements
        OnDateSelectedListener,
        OnMonthChangedListener,
        DayViewDecorator {

    @BindView(R.id.calendar)            MaterialCalendarView    mCalendar;
    @BindView(R.id.rv_day_assignments)  RecyclerView            mRvDaysAssignments;
    @BindView(R.id.layout_nothing_due)  LinearLayout            mLayoutNothingDue;

    private AssignmentListViewModel     mAssignmentListViewModel;
    private LiveData<List<Assignment>>  mCurrentMonthAssignments;
    private LiveData<List<Assignment>>  mCurrentDayAssignments;
    private HashMap<DateTime, Integer>  mDateAssignmentCountMap;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewModelProviders.of(this).get(ClassListViewModel.class)
                .getClasses().observe(this, this::onClassesGet);

        DateTime monthBeginning = new DateTime().withDayOfMonth(1).withTimeAtStartOfDay();

        mAssignmentListViewModel = ViewModelProviders.of(this)
                .get(AssignmentListViewModel.class);

        mCurrentMonthAssignments = mAssignmentListViewModel
                .getAssignmentsBetweenDates(
                        monthBeginning,
                        monthBeginning
                                .plusMonths(1)
                                .minusDays(1));
        mCurrentMonthAssignments.observe(this, this::onDateRangeAssignmentsChange);
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

    private void onClassesGet(List<Class> classes) {
        if (getContext() != null) {
            AssignmentsAdapter adapter = new AssignmentsAdapter(getContext(), classes, false);
            mRvDaysAssignments.setAdapter(adapter);
            mRvDaysAssignments.setLayoutManager(new LinearLayoutManager(getContext()));

            mCurrentDayAssignments = mAssignmentListViewModel.getAssignmentsDueOnDay(new DateTime(mCalendar.getSelectedDate().getDate()));
            mCurrentDayAssignments.observe(this, this::onSingleDayAssignmentsChange);

            ItemTouchHelper.SimpleCallback touchSimpleCallback = new AssignmentTouchHelper(getContext(), 0,
                    ItemTouchHelper.RIGHT);
            ItemTouchHelper touchHelper = new ItemTouchHelper(touchSimpleCallback);
            touchHelper.attachToRecyclerView(mRvDaysAssignments);
        }
    }

    private void onSingleDayAssignmentsChange(List<Assignment> assignments) {
        if (mRvDaysAssignments.getAdapter() != null) {
            AssignmentsAdapter adapter = (AssignmentsAdapter) mRvDaysAssignments.getAdapter();
            if (assignments != null && assignments.size() > 0) {
                mLayoutNothingDue.setVisibility(View.GONE);
                mRvDaysAssignments.setVisibility(View.VISIBLE);
                adapter.swapAssignmentsList(assignments);
            } else {
                mRvDaysAssignments.setVisibility(View.GONE);
                mLayoutNothingDue.setVisibility(View.VISIBLE);
                adapter.swapAssignmentsList(null);
            }
        }
    }

    private void onDateRangeAssignmentsChange(List<Assignment> assignments) {
        if (assignments != null) {
            mDateAssignmentCountMap = new HashMap<>();
            int checkPosition = -1;
            ListIterator<Assignment> assignmentIterator = assignments.listIterator();
            while (assignmentIterator.hasNext()) {
                Assignment currentAssignment = assignments.iterator().next();
                DateTime currentDate = currentAssignment.getDueDate();
                DateTime nextDate = new DateTime(currentDate);
                int count = 0;
                while (currentDate.getDayOfYear() == nextDate.getDayOfYear() && assignmentIterator.hasNext()) {
                    nextDate = assignmentIterator.next().getDueDate();
                    count++;
                }
                if (!assignmentIterator.hasNext()) {
                    count++;
                }
                mDateAssignmentCountMap.put(currentDate, count);
                checkPosition++;
                if (assignmentIterator.hasNext()) {
                    assignmentIterator = assignments.listIterator(checkPosition);
                }
            }
            mCalendar.addDecorator(this);
        }
    }

    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
        mCurrentDayAssignments.removeObservers(this);
        mCurrentDayAssignments = mAssignmentListViewModel.getAssignmentsDueOnDay(new DateTime(date.getDate()));
        mCurrentDayAssignments.observe(this, this::onSingleDayAssignmentsChange);
    }

    @Override
    public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
        DateTime currentMonth = new DateTime().withDayOfMonth(1).withTimeAtStartOfDay();
        mCurrentMonthAssignments.removeObservers(this);
        mCurrentMonthAssignments = mAssignmentListViewModel.getAssignmentsBetweenDates(
                currentMonth,
                currentMonth
                        .plusMonths(1)
                        .minusDays(1));
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        for (Object o : mDateAssignmentCountMap.entrySet()) {
            Map.Entry pair = (Map.Entry) o;
            DateTime dtCalDay = new DateTime(day.getDate());
            DateTime dtKey = (DateTime) pair.getKey();
            if (dtKey.getDayOfMonth() == dtCalDay.getDayOfMonth() && (int) pair.getValue() > 0) return true;
        }
        return false;
    }

    @Override
    public void decorate(DayViewFacade view) { view.addSpan(new DotSpan(5f)); }
}
