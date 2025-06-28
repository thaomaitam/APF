package com.KTA.APF.ui.fragment

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.KTA.APF.R
import com.KTA.APF.ui.adapter.TargetAppAdapter
import com.KTA.APF.util.PackageHelper

class TargetAppSelectFragment : AppSelectFragment() {

    override val adapter: TargetAppAdapter = TargetAppAdapter { selectedApp ->
        onAppSelected(selectedApp)
    }

    private fun onAppSelected(appInfo: PackageHelper.AppInfo) {
        setFragmentResult("app_selection_request", bundleOf("selected_package" to appInfo.packageName))
        findNavController().navigateUp()
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Thiết lập Toolbar riêng cho Fragment này
        val activity = requireActivity() as AppCompatActivity
        activity.supportActionBar?.title = getString(R.string.app_select_title)
        
        activity.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_app_select, menu)
                val searchItem = menu.findItem(R.id.action_search)
                val searchView = searchItem.actionView as SearchView
                searchView.queryHint = getString(R.string.search_hint)
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean = false
                    override fun onQueryTextChange(newText: String?): Boolean {
                        adapter.filter.filter(newText)
                        return true
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_show_system_apps -> {
                        menuItem.isChecked = !menuItem.isChecked
                        // Gọi hàm trong lớp cha để xử lý việc lọc
                        setShowSystemApps(menuItem.isChecked)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
}