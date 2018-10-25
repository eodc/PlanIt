package io.eodc.planit.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import io.eodc.planit.R
import io.eodc.planit.adapter.SubjectAdapter
import io.eodc.planit.model.SubjectListViewModel
import kotlinx.android.synthetic.main.fragment_onboarding_classes.*

/**
 * The last slide of the onboarding carousel, where the user initially adds their subjects
 *
 * @author 2n
 */
class OnboardingAddClassesFragment : OnboardingFragment() {

    private fun addClass() {
        if (fragmentManager != null) {
            ModifyClassFragment.newInstance(null)
                    .show(fragmentManager!!, null)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_onboarding_classes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recycleSubject.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        ViewModelProviders.of(this).get<SubjectListViewModel>(SubjectListViewModel::class.java)
                .subjectsObservable.observe(this,
                Observer {
                    if (it != null) {
                        if (recycleSubject.adapter != null) {
                            (recycleSubject.adapter as SubjectAdapter).swapClassesList(it)
                        } else {
                            recycleSubject.adapter = SubjectAdapter(it, context!!)
                        }
                        updateNoClassIndicators(it.size)
                    }
                })
        btnAddClass.setOnClickListener { addClass() }
    }

    private fun updateNoClassIndicators(count: Int) {
        if (count > 0) {
            textOnboardingNoClass.visibility = View.GONE
            imageNoClass.visibility = View.GONE
        } else {
            textOnboardingNoClass.visibility = View.VISIBLE
            imageNoClass.visibility = View.VISIBLE
        }
    }
}
