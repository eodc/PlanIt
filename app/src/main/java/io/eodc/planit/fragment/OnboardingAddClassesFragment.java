package io.eodc.planit.fragment;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.eodc.planit.R;
import io.eodc.planit.adapter.ClassesAdapter;
import io.eodc.planit.model.ClassListViewModel;

/**
 * The last slide of the onboarding carousel, where the user initially adds their classes
 *
 * @author 2n
 */
public class OnboardingAddClassesFragment extends OnboardingFragment {

    @BindView(R.id.recycle_class)      RecyclerView    mRvClasses;
    @BindView(R.id.image_no_class)      ImageView       mImageNoClass;
    @BindView(R.id.text_no_class)   TextView        mTextNoClass;

    @OnClick(R.id.btn_add_class)
    void addClass() {
        if (getFragmentManager() != null) {
            ModifyClassFragment.newInstance(null)
                    .show(getFragmentManager(), null);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_onboarding_classes, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mRvClasses.setLayoutManager(new LinearLayoutManager(getContext()));
        ViewModelProviders.of(this).get(ClassListViewModel.class)
                .getClasses().observe(this, classes -> {
                    if (classes != null) {
                        if (mRvClasses.getAdapter() != null) {
                            ((ClassesAdapter) mRvClasses.getAdapter()).swapClassesList(classes);
                        } else {
                            mRvClasses.setAdapter(new ClassesAdapter(classes, getContext()));
                        }
                        updateNoClassIndicators(classes.size());
                    }
        });
    }

    private void updateNoClassIndicators(int count) {
        if (count > 0) {
            mTextNoClass.setVisibility(View.GONE);
            mImageNoClass.setVisibility(View.GONE);
        } else {
            mTextNoClass.setVisibility(View.VISIBLE);
            mImageNoClass.setVisibility(View.VISIBLE);
        }
    }
}
