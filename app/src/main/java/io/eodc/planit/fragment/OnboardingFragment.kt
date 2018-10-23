package io.eodc.planit.fragment

import android.os.Bundle
import android.support.annotation.DrawableRes
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.eodc.planit.R
import kotlinx.android.synthetic.main.fragment_onboarding_image.*

/**
 * A standard onboarding fragment, with a title, image, and description.
 *
 * @author 2n
 */
open class OnboardingFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_onboarding_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = arguments

        if (args != null) {
            val title = args.getString(ARG_TITLE)
            val drawable = args.getInt(ARG_DRAWABLE)
            val description = args.getString(ARG_DESC)


            textContentTitle.text = title
            imageContent.setImageResource(drawable)
            textContentSubtitle.text = description

        }

    }

    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_DRAWABLE = "drawable"
        private const val ARG_DESC = "desc"

        /**
         * Creates a new instance of an OnboardingFragment
         *
         * @param title       The title of the slide
         * @param drawable    The resource drawable of the image for the slide
         * @param description The description of the slide
         * @return A new instance of OnboardingFragment
         */
        fun newInstance(title: String,
                        @DrawableRes drawable: Int,
                        description: String): OnboardingFragment {
            val args = Bundle()
            args.putString(ARG_TITLE, title)
            args.putInt(ARG_DRAWABLE, drawable)
            args.putString(ARG_DESC, description)

            val fragment = OnboardingFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
