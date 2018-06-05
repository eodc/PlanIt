package io.eodc.planit.activity;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.eodc.planit.R;
import io.eodc.planit.adapter.ClassesAdapter;
import io.eodc.planit.db.PlannerContract;
import io.eodc.planit.fragment.ModifyClassFragment;
import io.eodc.planit.listener.OnClassListChangeListener;

/**
 * Activity for adding or modifying classes
 *
 * @author 2n
 */
public class ClassesActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        OnClassListChangeListener {

    @BindView(R.id.toolbar)     private Toolbar         mToolbar;
    @BindView(R.id.rv_classes)  private RecyclerView    mRvClasses;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classes);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportLoaderManager().initLoader(0, null, this);
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
            ModifyClassFragment.newInstance(this, ModifyClassFragment.FLAG_NEW_CLASS, 0)
                    .show(getSupportFragmentManager(), null);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new CursorLoader(this, PlannerContract.ClassColumns.CONTENT_URI, null,
                null, null, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        ClassesAdapter classesAdapter = new ClassesAdapter(data, this, this);
        mRvClasses.setAdapter(classesAdapter);
        mRvClasses.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }

    @Override
    public void onClassListChange(int count) {
        getSupportLoaderManager().restartLoader(0, null, this);
    }
}
