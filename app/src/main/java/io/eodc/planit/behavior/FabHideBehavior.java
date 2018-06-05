package io.eodc.planit.behavior;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;

/**
 * Behavior of the Floating Action Button. Not actually instantiated programmatically, but rather
 * used in the XML declaration of the FAB.
 *
 * @author 2n
 */

public class FabHideBehavior extends CoordinatorLayout.Behavior<FloatingActionButton> {

    private long mLastSnackbarUpdate = 0;

    public FabHideBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
        if (dependency != null && dependency instanceof Snackbar.SnackbarLayout) return true;
        else if (dependency != null && dependency instanceof AHBottomNavigation) return true;

        return super.layoutDependsOn(parent, child, dependency);
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
        updateFloatingActionButton(parent, child, dependency);
        return super.onDependentViewChanged(parent, child, dependency);
    }

    /**
     * Update floating action button bottom margin
     */
    private void updateFloatingActionButton(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
        if (child != null && dependency != null && dependency instanceof Snackbar.SnackbarLayout) {
            mLastSnackbarUpdate = System.currentTimeMillis();
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) child.getLayoutParams();
            int fabDefaultBottomMargin = p.bottomMargin;
            child.setY(dependency.getY() - fabDefaultBottomMargin);
        } else if (child != null && dependency != null && dependency instanceof AHBottomNavigation) {
            // Hack to avoid moving the FAB when the AHBottomNavigation is moving (showing or hiding animation)
            if (System.currentTimeMillis() - mLastSnackbarUpdate < 30) return;
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) child.getLayoutParams();
            int fabDefaultBottomMargin = p.bottomMargin;
            float translateDist = dependency.getY() - fabDefaultBottomMargin;
            float windowBottom = parent.getBottom() - fabDefaultBottomMargin;
            child.setY(translateDist);
            float childPos = child.getY();
            if (childPos == windowBottom) child.hide();
            else {
                if (((AHBottomNavigation) dependency).getCurrentItem() != 1 &&
                        child.getVisibility() != View.INVISIBLE) {
                    child.hide();
                    return;
                }
                if (child.getVisibility() != View.VISIBLE) child.show();
            }
        }
    }
}
