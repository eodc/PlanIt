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
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.eodc.planit.R;
import io.eodc.planit.adapter.AssignmentViewHolder;
import io.eodc.planit.adapter.AssignmentsAdapter;
import io.eodc.planit.helper.AssignmentTouchHelper;
import io.eodc.planit.model.AssignmentListViewModel;
import io.eodc.planit.model.ClassListViewModel;

/**
 * Fragment showing all of the user's inputted assignments
 *
 * @author 2n
 */
public class PlannerFragment extends BaseFragment {

    @BindView(R.id.recycle_assignment)         RecyclerView    mRvContent;
    @BindView(R.id.text_done)     TextView        mTvAllDone;

    private AssignmentListViewModel assignmentListViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ClassListViewModel classListViewModel = ViewModelProviders.of(this)
                .get(ClassListViewModel.class);
        assignmentListViewModel = ViewModelProviders.of(this)
                .get(AssignmentListViewModel.class);
        classListViewModel.getClasses().observe(this, classes -> {
            AssignmentsAdapter adapter = new AssignmentsAdapter(getContext(), classes);
            adapter.swapAssignmentsList(assignmentListViewModel.getAllAssignments().getValue());
            mRvContent.setAdapter(adapter);
            mRvContent.setLayoutManager(new LinearLayoutManager(getContext()));

            ItemTouchHelper.SimpleCallback callback = new AssignmentTouchHelper(
                    0,
                    ItemTouchHelper.RIGHT,
                    this::onDismiss);
            ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
            touchHelper.attachToRecyclerView(mRvContent);

            assignmentListViewModel.getAllAssignments().observe(this, assignments -> {
                if (assignments != null) {
                    if (assignments.size() == 0) {
                        mTvAllDone.setVisibility(View.VISIBLE);
                        mRvContent.setVisibility(View.GONE);
                    } else {
                        mTvAllDone.setVisibility(View.GONE);
                        mRvContent.setVisibility(View.VISIBLE);
                        adapter.swapAssignmentsList(assignments);
                    }
                }
            });
        });

    }

    private void onDismiss(AssignmentViewHolder holder) {
        RecyclerView.Adapter adapter = mRvContent.getAdapter();
        if (adapter != null) {
            adapter.notifyItemRemoved(holder.getAdapterPosition());
        }
        new Thread(() -> assignmentListViewModel.removeAssignments(holder.getAssignment())).start();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_planner, container, false);
        ButterKnife.bind(this, view);
        return view;
    }
}
