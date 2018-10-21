package io.eodc.planit.listener

import io.eodc.planit.adapter.AssignmentViewHolder

interface OnAssignmentDismissListener {
    fun onDismiss(holder: AssignmentViewHolder)
}
