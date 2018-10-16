package io.eodc.planit.activity;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.eodc.planit.R;
import io.eodc.planit.adapter.ClassesAdapter;
import io.eodc.planit.db.Class;
import io.eodc.planit.fragment.ModifyClassFragment;
import io.eodc.planit.model.ClassListViewModel;

/**
 * Activity for adding or modifying classes
 *
 * @author 2n
 */
public class ClassesActivity extends AppCompatActivity {

    @BindView(R.id.tb)     Toolbar         mToolbar;
    @BindView(R.id.recycle_class)  RecyclerView    mRvClasses;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classes);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ViewModelProviders.of(this).get(ClassListViewModel.class)
                .getClasses().observe(this, this::onClassListChanged);

        mRvClasses.setLayoutManager(new LinearLayoutManager(this));
    }

    private void onClassListChanged(List<Class> classes) {
        ClassesAdapter classesAdapter = (ClassesAdapter) mRvClasses.getAdapter();
        if (classesAdapter != null) {
            classesAdapter.swapClassesList(classes);
        } else {
            mRvClasses.setAdapter(new ClassesAdapter(classes, this));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.classes_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.mnu_add_class) {
            ModifyClassFragment.newInstance(null)
                    .show(getSupportFragmentManager(), null);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
