package io.eodc.planit.fragment;

import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import butterknife.ButterKnife;
import io.eodc.planit.R;

/**
 * A standard onboarding fragment, with a title, image, and description.
 *
 * @author 2n
 */
public class OnboardingFragment extends Fragment {
    private static final String ARG_TITLE       = "title";
    private static final String ARG_DRAWABLE    = "drawable";
    private static final String ARG_DESC        = "desc";

    /**
     * Creates a new instance of an OnboardingFragment
     *
     * @param title       The title of the slide
     * @param drawable    The resource drawable of the image for the slide
     * @param description The description of the slide
     * @return A new instance of OnboardingFragment
     */
    public static OnboardingFragment newInstance(@NotNull String title,
                                                 @DrawableRes int drawable,
                                                 @NotNull String description) {
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putInt(ARG_DRAWABLE, drawable);
        args.putString(ARG_DESC, description);

        OnboardingFragment fragment = new OnboardingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_onboarding_image, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();

        if (args != null) {
            String title = args.getString(ARG_TITLE);
            int drawable = args.getInt(ARG_DRAWABLE);
            String description = args.getString(ARG_DESC);

            TextView tvTitle = view.findViewById(R.id.tv_title);
            ImageView image = view.findViewById(R.id.image);
            TextView tvDesc = view.findViewById(R.id.tv_description);

            tvTitle.setText(title);
            if (drawable != 0) image.setImageResource(drawable);
            tvDesc.setText(description);

        }

    }
}
