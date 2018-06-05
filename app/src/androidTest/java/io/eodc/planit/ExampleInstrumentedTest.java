package io.eodc.planit;

import android.content.ContentValues;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.eodc.planit.db.PlannerContract;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        ContentValues classValues = new ContentValues();
        classValues.put(PlannerContract.ClassColumns.NAME, "Chinese");
        classValues.put(PlannerContract.ClassColumns.COLOR, "#ecd41e");
        appContext.getContentResolver().insert(PlannerContract.ClassColumns.CONTENT_URI, classValues);

    }
}
