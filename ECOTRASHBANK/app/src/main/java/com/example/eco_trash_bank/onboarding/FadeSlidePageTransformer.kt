package com.example.eco_trash_bank.onboarding

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

class FadeSlidePageTransformer: ViewPager2.PageTransformer {
    override fun transformPage(page: View, position: Float) {
        page.apply {
            // slide horizontally
            translationX = -position * width
            // fade out/in
            alpha = 1f - abs(position)
        }
    }
}
