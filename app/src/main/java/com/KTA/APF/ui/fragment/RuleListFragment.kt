package com.KTA.APF.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.KTA.APF.R
import com.KTA.APF.databinding.FragmentRuleListBinding
import com.KTA.APF.ui.adapter.RuleAdapter
import com.KTA.APF.ui.viewmodel.RuleListViewModel
import kotlinx.coroutines.launch

class RuleListFragment : Fragment(R.layout.fragment_rule_list) {

    private val binding by viewBinding(FragmentRuleListBinding::bind)
    private val viewModel: RuleListViewModel by viewModels()
    private lateinit var ruleAdapter: RuleAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Thiết lập tiêu đề cho ActionBar
        requireActivity().title = getString(R.string.rule_list_title)
        
        setupRecyclerView()
        setupFab()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        // Tải lại danh sách quy tắc mỗi khi quay lại màn hình này để cập nhật thay đổi
        viewModel.loadRules()
    }

    private fun setupRecyclerView() {
        ruleAdapter = RuleAdapter { rule ->
            // Khi người dùng nhấn vào một quy tắc, điều hướng đến màn hình chỉnh sửa
            val action = RuleListFragmentDirections.actionRuleListFragmentToRuleEditorFragment(rule.targetPackage)
            findNavController().navigate(action)
        }

        binding.recyclerViewRules.apply {
            adapter = ruleAdapter
            layoutManager = LinearLayoutManager(context)
            // Thêm đường kẻ phân cách giữa các item
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
    }

    private fun setupFab() {
        binding.fabAddRule.setOnClickListener {
            // Điều hướng đến màn hình chỉnh sửa với packageName là null (để tạo mới)
            val action = RuleListFragmentDirections.actionRuleListFragmentToRuleEditorFragment(null)
            findNavController().navigate(action)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.rules.collect { rules ->
                    // Gửi danh sách cho adapter
                    ruleAdapter.submitList(rules)
                    // Hiển thị/ẩn văn bản empty view
                    binding.tvEmptyList.visibility = if (rules.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }
}