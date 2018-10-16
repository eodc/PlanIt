package io.eodc.planit.fragment;

import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.eodc.planit.R;
import io.eodc.planit.adapter.AssignmentsAdapter;
import io.eodc.planit.db.Assignment;
import io.eodc.planit.helper.AssignmentTouchHelper;
import io.eodc.planit.helper.DateValueFormatter;
import io.eodc.planit.model.AssignmentListViewModel;
import io.eodc.planit.model.ClassListViewModel;

/**
 * Fragment that shows a week's overview of assignments, the current day's assignments, and any
 * overdue assignments
 *
 * @author 2n
 */
public class HomeFragment extends BaseFragment {

    @BindView(R.id.text_done)         TextView        mLayoutAllDone;
    @BindView(R.id.card_overdue)        CardView        mCardOverdue;
    @BindView(R.id.graph_week)          LineChart       mGraphWeek;
    @BindView(R.id.recycle_today)            RecyclerView    mRvTodayAssign;
    @BindView(R.id.recycle_overdue)          RecyclerView    mRvOverdueAssign;
    @BindView(R.id.text_event_count)    TextView        mTextEventCount;

    private AssignmentsAdapter mTodayAssignmentsAdapter;
    private AssignmentsAdapter mOverdueAssignmentsAdapter;


    private AssignmentListViewModel assignmentListViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ViewModelProviders.of(this).get(ClassListViewModel.class).getClasses()
                .observe(this, classes -> {
                    mTodayAssignmentsAdapter = new AssignmentsAdapter(getContext(), classes, false);
                    mOverdueAssignmentsAdapter = new AssignmentsAdapter(getContext(), classes, false);

                    assignmentListViewModel = ViewModelProviders.of(this).get(AssignmentListViewModel.class);
                    DateTime today = new DateTime().withTimeAtStartOfDay();
                    DateTime dateToRetrieve;

                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());

                    if (preferences.getString(getString(R.string.pref_what_assign_show_key), "")
                            .equals(getString(R.string.pref_what_assign_show_curr_day_value))) {
                        dateToRetrieve = today;
                    } else {
                        dateToRetrieve = today.plusDays(1);
                    }

                    assignmentListViewModel.getAssignmentsDueOnDay(dateToRetrieve).observe(this, this::onDaysAssignmentsGet);
                    assignmentListViewModel.getAssignmentsBetweenDates(today, today.plusWeeks(1).minusDays(1)).observe(this, this::onWeekAssignmentsGet);
                    assignmentListViewModel.getOverdueAssignments(today).observe(this, this::onOverdueAssignmentsGet);
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupGraph();
    }

    private void onOverdueAssignmentsGet(List<Assignment> assignments) {
        if (assignments.size() > 0) {
            mCardOverdue.setVisibility(View.VISIBLE);
            populateRecyclerView(assignments, mOverdueAssignmentsAdapter, mRvOverdueAssign);
        } else {
            mCardOverdue.setVisibility(View.GONE);
        }
    }

    private void onWeekAssignmentsGet(List<Assignment> assignments) {
        List<Entry> entries = new ArrayList<>();
        DateTime checkDate = new DateTime();
        DateTime currentDate;
        int totalCount = 0;
        ListIterator<Assignment> iterator = assignments.listIterator();
        for (int i = 0; i < 7; ++i) {
            int count = 0;
            while (iterator.hasNext()) {
                Assignment current = iterator.next();
                currentDate = current.getDueDate();
                if (currentDate.getDayOfYear() == checkDate.getDayOfYear()) count++;
            }
            iterator = assignments.listIterator(0);
            entries.add(new Entry(checkDate.getMillis(), count));
            mGraphWeek.getAxisLeft().setAxisMaximum(Math.max(mGraphWeek.getAxisLeft().mAxisMaximum, count));
            checkDate = checkDate.plusDays(1);
            totalCount += count;
        }
        LineDataSet dataSet = new LineDataSet(entries, "");
        setupDataSet(dataSet);

        LineData lineData = new LineData(dataSet);
        DateValueFormatter formatter = new DateValueFormatter();
        XAxis xAxis = mGraphWeek.getXAxis();
        xAxis.setLabelCount(7, true);
        xAxis.setValueFormatter(formatter);

        if (totalCount > 1) mTextEventCount.setText(getString(R.string.num_events_label_plural, totalCount));
        else mTextEventCount.setText(getString(R.string.num_events_label, totalCount));
        mGraphWeek.setData(lineData);
        mGraphWeek.invalidate();
    }

    private void onDaysAssignmentsGet(List<Assignment> assignments) {
        if (assignments.size() > 0) mLayoutAllDone.setVisibility(View.GONE);
        else mLayoutAllDone.setVisibility(View.VISIBLE);
        populateRecyclerView(assignments, mTodayAssignmentsAdapter, mRvTodayAssign);
    }

    /**
     * Sets up the week graph
     */
    private void setupGraph() {
        mGraphWeek.disableScroll();
        mGraphWeek.setDragEnabled(false);
        mGraphWeek.setPinchZoom(false);
        mGraphWeek.getAxisLeft().setDrawLabels(false);
        mGraphWeek.getAxisLeft().setDrawGridLines(false);
        mGraphWeek.getAxisRight().setDrawLabels(false);
        mGraphWeek.getAxisRight().setDrawGridLines(false);
        mGraphWeek.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        mGraphWeek.getAxisLeft().setAxisMinimum(0);
        mGraphWeek.getAxisLeft().setAxisMaximum(5);
        mGraphWeek.getDescription().setText("");
        mGraphWeek.getLegend().setEnabled(false);
        mGraphWeek.setDoubleTapToZoomEnabled(false);
    }

    /**
     * Populates the specified RecyclerView with data from the specified cursor
     *
     * @param assignments       The cursor containing information from the assignments table to populate the
     *                          {@link RecyclerView} with
     * @param adapter           The adapter to bind information from the cursor to
     * @param rv                The RecyclerView to populate
     */
    private void populateRecyclerView(List<Assignment> assignments, AssignmentsAdapter adapter, RecyclerView rv) {
        if (assignments.size() > 0) adapter.swapAssignmentsList(assignments);
        else adapter.swapAssignmentsList(null);
        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        @SuppressLint("StaticFieldLeak") ItemTouchHelper.SimpleCallback touchSimpleCallback = new AssignmentTouchHelper(
                0,
                ItemTouchHelper.RIGHT,
                holder -> {
                    RecyclerView.Adapter currentAdapter = rv.getAdapter();
                    if (currentAdapter != null) {
                        currentAdapter.notifyItemRemoved(holder.getAdapterPosition());
                    }
                    new Thread(() -> assignmentListViewModel.removeAssignments(holder.getAssignment())).start();
                });
        ItemTouchHelper touchHelper = new ItemTouchHelper(touchSimpleCallback);
        touchHelper.attachToRecyclerView(rv);
    }


    /**
     * Sets up the specified data set
     *
     * @param dataSet The data set to setup
     */
    private void setupDataSet(LineDataSet dataSet) {
        dataSet.setHighlightEnabled(false);
        dataSet.setDrawValues(false);
        dataSet.setLineWidth(2f);
        dataSet.setColor(ContextCompat.getColor(requireContext(), R.color.colorAccent));
        dataSet.setCircleColor(ContextCompat.getColor(requireContext(), R.color.colorAccentDark));
        dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        dataSet.setDrawFilled(true);
    }
}
