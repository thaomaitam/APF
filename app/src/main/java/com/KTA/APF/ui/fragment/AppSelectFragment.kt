package com.KTA.APF.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.KTA.APF.R
import com.KTA.APF.ui.adapter.AppSelectAdapter
import com.KTA.APF.util.PackageHelper
import com.google.android.material.checkbox.MaterialCheckBox
import kotlinx.coroutines.launch

abstract class AppSelectFragment : Fragment(R.layout.fragment_target_app_select) {

    // Adapter cụ thể sẽ được cung cấp bởi lớp con
    protected abstract val adapter: AppSelectAdapter

    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var toolbar: Toolbar
    private var showSystemApps = false // Trạng thái của bộ lọc

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ánh xạ các view từ layout
        recyclerView = view.findViewById(R.id.recycler_view_apps)
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)
        toolbar = view.findViewById(R.id.toolbar)
        
        setupToolbar()
        setupRecyclerView()
        setupSwipeRefresh()
        observeData()
    }

    private fun setupToolbar() {
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        // TODO: Cần di chuyển logic menu từ TargetAppSelectFragment lên đây nếu muốn tái sử dụng
        // Hiện tại, để đơn giản, lớp con sẽ tự xử lý menu của nó.
    }

    private fun setupRecyclerView() {
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            PackageHelper.refreshAppList()
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Lắng nghe danh sách ứng dụng từ PackageHelper
                PackageHelper.appList.collect { allApps ->
                    // Áp dụng bộ lọc (hiện/ẩn app hệ thống) trước khi gửi cho adapter
                    val filteredList = if (showSystemApps) {
                        allApps
                    } else {
                        allApps.filter { !PackageHelper.isSystemApp(it) }
                    }
                    adapter.updateAppList(filteredList)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Lắng nghe trạng thái refreshing
                PackageHelper.isRefreshing.collect { isRefreshing ->
                    swipeRefreshLayout.isRefreshing = isRefreshing
                }
            }
        }
    }

    /**
     * Lớp con có thể gọi hàm này để thay đổi trạng thái bộ lọc
     */
    protected fun setShowSystemApps(shouldShow: Boolean) {
        if (showSystemApps != shouldShow) {
            showSystemApps = shouldShow
            // Tải lại dữ liệu đã được lọc
            val currentList = PackageHelper.appList.value
            val filteredList = if (showSystemApps) {
                currentList
            } else {
                currentList.filter { !PackageHelper.isSystemApp(it) }
            }
            adapter.updateAppList(filteredList)
        }
    }
}