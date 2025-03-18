package com.developerobaida.boipath

import android.animation.ValueAnimator
import android.view.View
import android.widget.TextView


class Common {
    /*holder.binding.body.setOnClickListener(view -> {
        if (isTextViewExpanded) {
            collapseTextView(holder.binding.body, 3);
        } else {
            expandTextView(holder.binding.body);
        }
        isTextViewExpanded = !isTextViewExpanded;
    });*/


    private fun expandTextView(textView: TextView) {
        val startHeight = textView.height
        textView.maxLines = Int.MAX_VALUE
        textView.measure(
            View.MeasureSpec.makeMeasureSpec(textView.width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        val targetHeight = textView.measuredHeight

        val animator = ValueAnimator.ofInt(startHeight, targetHeight)
        animator.addUpdateListener { animation: ValueAnimator ->
            textView.layoutParams.height = animation.animatedValue as Int
            textView.requestLayout()
        }

        animator.setDuration(300)
        animator.start()
    }

    private fun collapseTextView(textView: TextView, maxLines: Int) {
        val startHeight = textView.height
        textView.maxLines = maxLines
        textView.measure(
            View.MeasureSpec.makeMeasureSpec(textView.width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        val targetHeight = textView.measuredHeight

        val animator = ValueAnimator.ofInt(startHeight, targetHeight)
        animator.addUpdateListener { animation: ValueAnimator ->
            textView.layoutParams.height = animation.animatedValue as Int
            textView.requestLayout()
        }

        animator.setDuration(300)
        animator.start()
    }

}