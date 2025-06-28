package com.KTA.APF.ui.util

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import com.google.android.material.color.DynamicColors

/**
 * Đối tượng tiện ích để quản lý chủ đề và màu sắc của ứng dụng.
 */
object ThemeUtils {

    /**
     * Kiểm tra xem thiết bị có hỗ trợ màu động (Material You) hay không.
     * @return `true` nếu có, ngược lại là `false`.
     */
    val isDynamicColorAvailable: Boolean
        get() = DynamicColors.isDynamicColorAvailable()

    /**
     * Kiểm tra xem hệ thống có đang ở chế độ ban đêm (dark mode) không.
     * @param context Context để truy cập tài nguyên.
     * @return `true` nếu là dark mode, ngược lại là `false`.
     */
    fun isNightMode(context: Context): Boolean {
        val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }

    /**
     * Lấy một giá trị màu từ thuộc tính (attribute) của chủ đề hiện tại.
     * Ví dụ: lấy màu `?colorPrimary`.
     *
     * @param context Context để truy cập theme.
     * @param themeAttrId ID của thuộc tính màu, ví dụ: `com.google.android.material.R.attr.colorPrimary`.
     * @return Giá trị màu (integer).
     */
    @ColorInt
    fun getColorFromAttr(context: Context, @AttrRes themeAttrId: Int): Int {
        val typedArray = context.obtainStyledAttributes(intArrayOf(themeAttrId))
        val color = try {
            typedArray.getColor(0, 0)
        } finally {
            typedArray.recycle()
        }
        return color
    }
}