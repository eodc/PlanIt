package io.eodc.planit.fragment;

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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.eodc.planit.R;
import io.eodc.planit.adapter.AssignmentsAdapter;
import io.eodc.planit.helper.AssignmentTouchHelper;
import io.eodc.planit.listener.AssignmentTypeLoadChangeListener;
import io.eodc.planit.model.AssignmentListViewModel;
import io.eodc.planit.model.ClassListViewModel;
import timber.log.Timber;

/**
 * Fragment showing all of the user's inputted assignments
 *
 * @author 2n
 */
public class PlannerFragment extends BaseFragment implements
        AssignmentTypeLoadChangeListener {

    @BindView(R.id.content)         RecyclerView mRvContent;
    @BindView(R.id.all_done_layout) LinearLayout mLayoutNoAssignments;

    @OnClick(R.id.create_fab) void handleCreateFab() {
        if (getFragmentManager() != null) {
            new AddAssignmentFragment().show(getFragmentManager(), null);
        }
    }

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_planner, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ClassListViewModel classListViewModel = ViewModelProviders.of(this)
                .get(ClassListViewModel.class);
        AssignmentListViewModel assignmentListViewModel = ViewModelProviders.of(this)
                .get(AssignmentListViewModel.class);
        classListViewModel.getClasses().observe(this, classes -> {
            AssignmentsAdapter adapter = new AssignmentsAdapter(getContext(), classes);
            adapter.swapAssignmentsList(assignmentListViewModel.getAllAssignments().getValue());
            mRvContent.setAdapter(adapter);
            mRvContent.setLayoutManager(new LinearLayoutManager(getContext()));

            ItemTouchHelper.SimpleCallback callback = new AssignmentTouchHelper(getContext(), 0,
                    ItemTouchHelper.RIGHT);
            ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
            touchHelper.attachToRecyclerView(mRvContent);
        });
        assignmentListViewModel.getAllAssignments().observe(this, assignments -> {
            AssignmentsAdapter adapter = (AssignmentsAdapter) mRvContent.getAdapter();
            if (adapter != null) {
                adapter.swapAssignmentsList(assignments);
            }
        });
    }

    @Override
    public void onTypeChanged(int flag) {
        try {
            if (mRvContent.getAdapter() != null) {
                ((AssignmentsAdapter) mRvContent.getAdapter())
                        .setShowAssignmnentsCompleted(flag == BaseFragment.FLAG_SHOW_COMPLETE);
            }
        } catch (Exception e) {
            Timber.i(e, "May just be inserting fragment");
        }
    }
}
