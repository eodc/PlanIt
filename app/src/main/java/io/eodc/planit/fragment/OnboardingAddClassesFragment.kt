package io.eodc.planit.fragment

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import io.eodc.planit.R
import io.eodc.planit.adapter.SubjectAdapter
import io.eodc.planit.model.SubjectListViewModel

/**
 * The last slide of the onboarding carousel, where the user initially adds their classes
 *
 * @author 2n
 */
class OnboardingAddClassesFragment : OnboardingFragment() {

    @BindView(R.id.recycle_class)
    internal var mRvClasses: RecyclerView? = null
    @BindView(R.id.image_no_class)
    internal var mImageNoClass: ImageView? = null
    @BindView(R.id.text_no_class)
    internal var mTextNoClass: TextView? = null

    @OnClick(R.id.btn_add_class)
    internal fun addClass() {
        if (fragmentManager != null) {
            ModifyClassFragment.newInstance(null)
                    .show(fragmentManager!!, null)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_onboarding_classes, container, false)
        ButterKnife.bind(this, v)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mRvClasses!!.layoutManager = LinearLayoutManager(context)
        ViewModelProviders.of(this).get<SubjectListViewModel>(SubjectListViewModel::class.java!!)
                .subjectsObservable.observe(this, { subjects ->
            if (subjects != null) {
                if (mRvClasses!!.adapter != null) {
                    (mRvClasses!!.adapter as SubjectAdapter).swapClassesList(subjects)
                } else {
                    mRvClasses!!.adapter = SubjectAdapter(subjects, context)
                }
                updateNoClassIndicators(subjects!!.size)
            }
        })
    }

    private fun updateNoClassIndicators(count: Int) {
        if (count > 0) {
            mTextNoClass!!.visibility = View.GONE
            mImageNoClass!!.visibility = View.GONE
        } else {
            mTextNoClass!!.visibility = View.VISIBLE
            mImageNoClass!!.visibility = View.VISIBLE
        }
    }
}
