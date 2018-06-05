package io.eodc.planit.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.joda.time.DateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.eodc.planit.R;
import io.eodc.planit.activity.MainActivity;
import io.eodc.planit.adapter.AssignmentsAdapter;
import io.eodc.planit.db.PlannerContract;
import io.eodc.planit.helper.AssignmentTouchHelper;
import io.eodc.planit.helper.DateValueFormatter;
import timber.log.Timber;

/**
 * Fragment that shows a week's overview of assignments, the current day's assignments, and any
 * overdue assignments
 *
 * @author 2n
 */
public class HomeFragment extends BaseFragment {

    private static final int LOADER_DUE_THIS_WEEK = 0;
    private static final int LOADER_DUE_TODAY = 1;
    private static final int LOADER_DUE_OVERDUE = 2;
    private static final int LOADER_CLASSES = 3;

    @BindView(R.id.text_event_count)    private TextView        mTextEventCount;
    @BindView(R.id.graph_week)          private LineChart       mGraphWeek;
    @BindView(R.id.rv_today)            private RecyclerView    mRvTodayAssign;
    @BindView(R.id.rv_overdue)          private RecyclerView    mRvOverdueAssign;
    @BindView(R.id.card_overdue)        private CardView        mCardOverdue;
    @BindView(R.id.all_done_layout)     private LinearLayout    mLayoutAllDone;

    private AssignmentsAdapter mTodayAssignmentsAdapter;
    private AssignmentsAdapter mOverdueAssignmentsAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(LOADER_DUE_THIS_WEEK, null, this);
        getLoaderManager().initLoader(LOADER_CLASSES, null, this);
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
        mGraphWeek.getDescription().setText("");
        mGraphWeek.getLegend().setEnabled(false);
        mGraphWeek.setDoubleTapToZoomEnabled(false);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        switch (id) {
            case LOADER_DUE_THIS_WEEK:
                String[] projection = new String[]{PlannerContract.AssignmentColumns.DUE_DATE};
                return new CursorLoader(requireContext(), PlannerContract.AssignmentColumns.CONTENT_URI, projection,
                        PlannerContract.AssignmentColumns.DUE_DATE + " <= date('now', '+7 days', 'localtime') and " +
                                PlannerContract.AssignmentColumns.DUE_DATE + " >= date('now', 'localtime') and " +
                                PlannerContract.AssignmentColumns.COMPLETED + "=0", null,
                        PlannerContract.AssignmentColumns.DUE_DATE + " asc");
            case LOADER_DUE_TODAY:
                return new CursorLoader(requireContext(), PlannerContract.AssignmentColumns.CONTENT_URI, null,
                        PlannerContract.AssignmentColumns.DUE_DATE + " = date('now', 'localtime') and " +
                                PlannerContract.AssignmentColumns.COMPLETED + "=0", null, null);
            case LOADER_DUE_OVERDUE:
                return new CursorLoader(requireContext(), PlannerContract.AssignmentColumns.CONTENT_URI, null,
                        PlannerContract.AssignmentColumns.DUE_DATE + " < date('now', 'localtime') and " +
                                PlannerContract.AssignmentColumns.COMPLETED + "=0", null,
                        PlannerContract.AssignmentColumns.DUE_DATE + " asc");
            case LOADER_CLASSES:
                return new CursorLoader(requireContext(), PlannerContract.ClassColumns.CONTENT_URI,
                        null, null, null, null);

        }
        return new CursorLoader(requireContext());
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        int id = loader.getId();
        MainActivity activity = (MainActivity) getActivity();
        switch (id) {
            case LOADER_DUE_THIS_WEEK:
                data.moveToPosition(-1); // Fix for bug where cursor is a recycled one and thus already at the end
                List<Entry> entries = new ArrayList<>();
                DateTime checkDate = new DateTime();
                DateTime currentDate = new DateTime();
                int totalCount = 0;
                for (int i = 0; i < 7; ++i) {
                    int count = 0;
                    boolean moved = false;
                    while (checkDate.getDayOfYear() == currentDate.getDayOfYear() && data.moveToNext()) {
                        try {
                            moved = true;
                            SimpleDateFormat stdFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                            currentDate = new DateTime(stdFormat.parse(data.getString(data.getColumnIndex(PlannerContract.AssignmentColumns.DUE_DATE))));
                            if (currentDate.getDayOfYear() == checkDate.getDayOfYear()) count++;
                        } catch (ParseException e) {
                            Timber.d(e);
                        }
                    }
                    if (moved) data.moveToPrevious();
                    entries.add(new Entry(checkDate.getMillis(), count));
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

                if (totalCount > 1)
                    mTextEventCount.setText(getString(R.string.num_events_label_plural, totalCount));
                else mTextEventCount.setText(getString(R.string.num_events_label, totalCount));
                mGraphWeek.setData(lineData);
                mGraphWeek.invalidate();
                break;
            case LOADER_CLASSES:
                mTodayAssignmentsAdapter = new AssignmentsAdapter(requireContext(), data, this, false);
                mOverdueAssignmentsAdapter = new AssignmentsAdapter(requireContext(), data, this, false);
                getLoaderManager().initLoader(LOADER_DUE_TODAY, null, this);
                getLoaderManager().initLoader(LOADER_DUE_OVERDUE, null, this);
                break;
            case LOADER_DUE_OVERDUE:
                if (data.getCount() > 0) {
                    mCardOverdue.setVisibility(View.VISIBLE);
                    populateRecyclerView(data, mOverdueAssignmentsAdapter, mRvOverdueAssign);
                } else {
                    if (activity != null) activity.getBottomNav().restoreBottomNavigation(true);
                    mCardOverdue.setVisibility(View.GONE);
                }
                break;
            case LOADER_DUE_TODAY:
                if (data.getCount() > 0) mLayoutAllDone.setVisibility(View.GONE);
                else mLayoutAllDone.setVisibility(View.VISIBLE);
                populateRecyclerView(data, mTodayAssignmentsAdapter, mRvTodayAssign);
                break;
        }

    }

    /**
     * Populates the specified RecyclerView with data from the specified cursor
     *
     * @param data    The cursor containing information from the assignments table to populate the
     *                {@link RecyclerView} with
     * @param adapter The adapter to bind information from the cursor to
     * @param rv      The RecyclerView to populate
     */
    private void populateRecyclerView(Cursor data, AssignmentsAdapter adapter, RecyclerView rv) {
        if (data.getCount() > 0) adapter.swapAssignmentsCursor(data);
        else adapter.swapAssignmentsCursor(null);
        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        ItemTouchHelper.SimpleCallback touchSimpleCallback = new AssignmentTouchHelper(requireContext(), 0,
                ItemTouchHelper.RIGHT, this);
        ItemTouchHelper touchHelper = new ItemTouchHelper(touchSimpleCallback);
        touchHelper.attachToRecyclerView(rv);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
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

    @Override
    public void onAssignmentComplete(Cursor cursor) {
        super.onAssignmentComplete(cursor);
        reloadAllLoaders();
    }

    @Override
    public void onAssignmentEdit() {
        reloadAllLoaders();
    }

    /**
     * Utility method to restart all loaders
     */
    private void reloadAllLoaders() {
        getLoaderManager().restartLoader(LOADER_DUE_THIS_WEEK, null, this);
        getLoaderManager().restartLoader(LOADER_DUE_TODAY, null, this);
        getLoaderManager().restartLoader(LOADER_DUE_OVERDUE, null, this);
    }
}
