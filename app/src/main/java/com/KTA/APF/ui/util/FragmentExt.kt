package com.KTA.APF.ui.util

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.annotation.MenuRes
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle

/**
 * Extension function để đơn giản hóa việc thiết lập Toolbar trong một Fragment.
 *
 * @param toolbar Đối tượng Toolbar cần thiết lập.
 * @param title Tiêu đề sẽ hiển thị trên Toolbar.
 * @param menuRes (Tùy chọn) ID của file menu XML để inflate.
 * @param onMenuItemSelected (Tùy chọn) Lambda để xử lý sự kiện khi một item trên menu được chọn.
 */
fun Fragment.setupToolbar(
    toolbar: Toolbar,
    title: String,
    @MenuRes menuRes: Int? = null,
    onMenuItemSelected: ((MenuItem) -> Unit)? = null
) {
    toolbar.title = title
    // Xóa các menu cũ để tránh bị trùng lặp khi Fragment được tạo lại
    toolbar.menu.clear()

    // Chỉ thêm menu nếu được cung cấp
    if (menuRes != null) {
        toolbar.inflateMenu(menuRes)
        toolbar.setOnMenuItemClickListener { menuItem ->
            onMenuItemSelected?.invoke(menuItem)
            true
        }
    }

    // Một cách khác sử dụng MenuProvider (hiện đại hơn và được khuyến nghị)
    // Tuy nhiên, cách trên đơn giản hơn cho các trường hợp cơ bản.
    // Nếu bạn cần menu thay đổi động, hãy dùng MenuProvider.
    /*
    requireActivity().addMenuProvider(object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            if (menuRes != null) {
                menuInflater.inflate(menuRes, menu)
            }
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            onMenuItemSelected?.invoke(menuItem)
            return true
        }
    }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    */
}