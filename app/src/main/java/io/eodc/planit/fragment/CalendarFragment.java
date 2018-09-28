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
import android.widget.TextView;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.eodc.planit.R;
import io.eodc.planit.adapter.AssignmentViewHolder;
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
    @BindView(R.id.tv_all_done)         TextView                mTvAllDone;

    private AssignmentListViewModel     mAssignmentListViewModel;
    private LiveData<List<Assignment>>  mAllAssignments;
    private LiveData<List<Assignment>>  mCurrentDayAssignments;
    private List<DateTime>              mDateHasAssignmentList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewModelProviders.of(this).get(ClassListViewModel.class)
                .getClasses().observe(this, this::onClassesGet);


        mAssignmentListViewModel = ViewModelProviders.of(this)
                .get(AssignmentListViewModel.class);

        mAllAssignments = mAssignmentListViewModel
                .getAllAssignments();
        mAllAssignments.observe(this, this::onDateRangeAssignmentsChange);
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

            ItemTouchHelper.SimpleCallback touchSimpleCallback = new AssignmentTouchHelper(
                    0,
                    ItemTouchHelper.RIGHT,
                    this::onDismiss);
            ItemTouchHelper touchHelper = new ItemTouchHelper(touchSimpleCallback);
            touchHelper.attachToRecyclerView(mRvDaysAssignments);
        }
    }

    private void onDismiss(AssignmentViewHolder holder) {
        RecyclerView.Adapter adapter = mRvDaysAssignments.getAdapter();
        if (adapter != null) {
            adapter.notifyItemRemoved(holder.getAdapterPosition());
        }
        new Thread(() -> mAssignmentListViewModel.removeAssignments(holder.getAssignment())).start();
    }

    private void onSingleDayAssignmentsChange(List<Assignment> assignments) {
        if (mRvDaysAssignments.getAdapter() != null) {
            AssignmentsAdapter adapter = (AssignmentsAdapter) mRvDaysAssignments.getAdapter();
            if (assignments != null && assignments.size() > 0) {
                mTvAllDone.setVisibility(View.GONE);
                mRvDaysAssignments.setVisibility(View.VISIBLE);
                adapter.swapAssignmentsList(assignments);
            } else {
                mRvDaysAssignments.setVisibility(View.GONE);
                mTvAllDone.setVisibility(View.VISIBLE);
                adapter.swapAssignmentsList(null);
            }
        }
    }

    private void onDateRangeAssignmentsChange(List<Assignment> assignments) {
        if (assignments != null) {
            mDateHasAssignmentList = new ArrayList<DateTime>(){{
                add(assignments.get(0).getDueDate());
            }};
            int currIndex = 0;
            for (int i = 0; i < assignments.size(); ++i) {
                Assignment nextAssign = findNextDueAssign(assignments, currIndex);
                if (nextAssign != null) {
                    mDateHasAssignmentList.add(nextAssign.getDueDate());
                    currIndex = assignments.indexOf(nextAssign);
                }
            }
            mCalendar.addDecorator(this);
        }
    }

    private Assignment findNextDueAssign(List<Assignment> assignments, int currIndex) {
        for (int i = currIndex; i < assignments.size(); ++i) {
            Assignment currAssignment = assignments.get(i);
            DateTime mostRecent = mDateHasAssignmentList.get(mDateHasAssignmentList.size() - 1);
            if (mostRecent.isBefore(currAssignment.getDueDate())) return currAssignment;
        }
        return null;
    }

    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
        mCurrentDayAssignments.removeObservers(this);
        mCurrentDayAssignments = mAssignmentListViewModel.getAssignmentsDueOnDay(new DateTime(date.getDate()));
        mCurrentDayAssignments.observe(this, this::onSingleDayAssignmentsChange);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        for (DateTime date : mDateHasAssignmentList) {
            if (new DateTime(day.getDate()).equals(date)) return true;
        }
        return false;
    }

    @Override
    public void decorate(DayViewFacade view) { view.addSpan(new DotSpan(5f)); }

    @Override
    public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {

    }
}
