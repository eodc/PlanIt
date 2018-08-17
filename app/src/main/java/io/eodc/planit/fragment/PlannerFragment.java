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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.eodc.planit.R;
import io.eodc.planit.adapter.AssignmentsAdapter;
import io.eodc.planit.db.PlannerContract;
import io.eodc.planit.helper.AssignmentTouchHelper;
import io.eodc.planit.listener.AssignmentTypeLoadChangeListener;
import timber.log.Timber;

/**
 * Fragment showing all of the user's inputted assignments
 *
 * @author 2n
 */
public class PlannerFragment extends BaseFragment implements
        AssignmentTypeLoadChangeListener {
    private static final int CLASSES_LOADER_ID      = 0;
    private static final int ASSIGNMENTS_LOADER_ID  = 1;

    @BindView(R.id.content)         RecyclerView mRvContent;
    @BindView(R.id.all_done_layout) LinearLayout mLayoutNoAssignments;

    @OnClick(R.id.create_fab) void handleCreateFab() {
        if (getFragmentManager() != null) {
            AddAssignmentFragment mBottomSheet = AddAssignmentFragment.newInstance(this);
            mBottomSheet.show(getFragmentManager(), null);
        }
    }

    private AssignmentsAdapter mAssignmentsAdapter;

    private int mFlag = PlannerContract.FLAG_SHOW_INCOMPLETE;

    /**
     * Creates a new instance of Planner Fragment
     *
     * @param flag Whether to show completed assignments or incomplete
     * @return A new instance of PlannerFragment
     */
    public static PlannerFragment newInstance(int flag) {
        PlannerFragment fragment = new PlannerFragment();
        fragment.onTypeChanged(flag);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(CLASSES_LOADER_ID, null, this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_planner, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onAssignmentCreation() {
        getLoaderManager().restartLoader(ASSIGNMENTS_LOADER_ID, null, this);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        switch (id) {
            case CLASSES_LOADER_ID:
                return new CursorLoader(requireContext(), PlannerContract.ClassColumns.CONTENT_URI,
                        null, null, null, null);
            case ASSIGNMENTS_LOADER_ID:
                String orderBy = PlannerContract.AssignmentColumns.DUE_DATE + ", " + PlannerContract.AssignmentColumns.CLASS_ID + " ASC";
                return new CursorLoader(requireContext(), PlannerContract.AssignmentColumns.CONTENT_URI,
                        null, PlannerContract.AssignmentColumns.COMPLETED + "=?",
                        new String[]{String.valueOf(mFlag)}, orderBy);
        }
        return new CursorLoader(requireContext());
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case CLASSES_LOADER_ID:
                mAssignmentsAdapter = new AssignmentsAdapter(getActivity(), data, this);
                getLoaderManager().initLoader(ASSIGNMENTS_LOADER_ID, null, this);
                break;
            case ASSIGNMENTS_LOADER_ID:
                if (data.getCount() > 0) {
                    mLayoutNoAssignments.setVisibility(View.GONE);
                    mAssignmentsAdapter.swapAssignmentsCursor(data);
                } else {
                    mLayoutNoAssignments.setVisibility(View.VISIBLE);
                    mAssignmentsAdapter.swapAssignmentsCursor(null);
                }
                break;
        }
        mRvContent.setAdapter(mAssignmentsAdapter);
        mRvContent.setLayoutManager(new LinearLayoutManager(getActivity()));

        ItemTouchHelper.SimpleCallback touchSimpleCallback = new AssignmentTouchHelper(requireContext(), 0,
                ItemTouchHelper.RIGHT, this);
        ItemTouchHelper touchHelper = new ItemTouchHelper(touchSimpleCallback);
        touchHelper.attachToRecyclerView(mRvContent);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
    }

    @Override
    public void onAssignmentEdit() {
        getLoaderManager().restartLoader(ASSIGNMENTS_LOADER_ID, null, this);
    }

    @Override
    public void onAssignmentComplete(final Cursor cursor) {
        super.onAssignmentComplete(cursor);
        getLoaderManager().restartLoader(ASSIGNMENTS_LOADER_ID, null, this);
    }

    @Override
    public void onTypeChanged(int flag) {
        mFlag = flag;
        try {
            getLoaderManager().restartLoader(ASSIGNMENTS_LOADER_ID, null, this);
            mAssignmentsAdapter.setShowAssignmnentsCompleted(mFlag == PlannerContract.FLAG_SHOW_COMPLETE);
        } catch (Exception e) {
            Timber.i(e, "May just be inserting fragment");
        }
    }
}
