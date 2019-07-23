package ru.merkulyevsasha.coreandroid.common

import android.content.res.Resources
import android.graphics.PorterDuff
import android.util.TypedValue
import android.widget.ImageView
import android.widget.TextView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import ru.merkulyevsasha.coreandroid.R

class ColorThemeResolver(
    private val typedValue: TypedValue,
    private val currentTheme: Resources.Theme
) {

    fun setArticleActivityColor(expression: Boolean, textView: TextView, imageView: ImageView) {
        if (expression) {
            val color = getThemeAttrColor(R.attr.colorPrimaryDark)
            textView.setTextColor(color)
            imageView.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        } else {
            val color = getThemeAttrColor(R.attr.colorPrimary)
            textView.setTextColor(color)
            imageView.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        }
    }

    fun getThemeAttrColor(attrColor: Int): Int {
        currentTheme.resolveAttribute(attrColor, typedValue, true)
        return typedValue.data
    }

    fun initSwipeRefreshColorScheme(swipeRefreshLayout: SwipeRefreshLayout) {
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(getThemeAttrColor(R.attr.colorAccent))
        swipeRefreshLayout.setColorSchemeColors(getThemeAttrColor(R.attr.colorControlNormal))
    }

}