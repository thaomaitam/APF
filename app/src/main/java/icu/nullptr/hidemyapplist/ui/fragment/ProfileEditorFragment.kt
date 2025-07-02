// file: app/src/main/java/icu/nullptr/hidemyapplist/ui/fragment/ProfileEditorFragment.kt

package icu.nullptr.hidemyapplist.ui.fragment

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tsng.hidemyapplist.R
import com.tsng.hidemyapplist.databinding.FragmentProfileEditorBinding
import icu.nullptr.hidemyapplist.common.AndroidProfile
import icu.nullptr.hidemyapplist.service.ConfigManager
import icu.nullptr.hidemyapplist.ui.util.setupToolbar
import java.util.*
import kotlin.random.Random

class ProfileEditorFragment : Fragment(R.layout.fragment_profile_editor) {

    private val binding by viewBinding<FragmentProfileEditorBinding>()
    private val args: ProfileEditorFragmentArgs by navArgs()

    private var currentProfile: AndroidProfile? = null
    private var originalProfileName: String? = null
    private var isCreatingNew: Boolean = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        originalProfileName = args.profileName
        isCreatingNew = originalProfileName == null

        setupToolbar(
            toolbar = binding.toolbar,
            title = getString(if (isCreatingNew) R.string.profile_new else R.string.title_profile_editor),
            navigationIcon = R.drawable.baseline_arrow_back_24,
            navigationOnClick = { findNavController().navigateUp() }
        )

        setupMenu()
        loadProfileData()

        binding.fabSaveProfile.setOnClickListener {
            saveProfile()
        }
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_profile_editor, menu)
            }

            override fun onPrepareMenu(menu: Menu) {
                // Chỉ hiển thị nút xóa khi đang sửa profile đã có
                menu.findItem(R.id.menu_delete_profile).isVisible = !isCreatingNew
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_randomize -> {
                        randomizeAllFields()
                        true
                    }
                    R.id.menu_delete_profile -> {
                        confirmDeleteProfile()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun loadProfileData() {
        if (isCreatingNew) {
            currentProfile = AndroidProfile()
            binding.editProfileName.requestFocus()
        } else {
            currentProfile = ConfigManager.getProfile(originalProfileName!!)
            if (currentProfile == null) {
                // Trường hợp lỗi: profile bị xóa ở đâu đó nhưng vẫn điều hướng được tới đây
                Toast.makeText(context, "Profile not found", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
                return
            }
            binding.editProfileName.setText(originalProfileName)
            // Bind dữ liệu từ profile vào các EditText
            bindDataToViews()
        }
    }

    private fun bindDataToViews() {
        currentProfile?.let {
            binding.editImei1.setText(it.imei1)
            binding.editImei2.setText(it.imei2)
            binding.editAndroidId.setText(it.androidId)
            // ... bind cho tất cả các EditText khác
            binding.editBuildFingerprint.setText(it.buildFingerprint)
        }
    }

    private fun collectDataFromViews(): AndroidProfile {
        return AndroidProfile(
            imei1 = binding.editImei1.text.toString().ifEmpty { null },
            imei2 = binding.editImei2.text.toString().ifEmpty { null },
            androidId = binding.editAndroidId.text.toString().ifEmpty { null },
            // ... collect từ tất cả các EditText khác
            buildFingerprint = binding.editBuildFingerprint.text.toString().ifEmpty { null }
        )
    }

    private fun saveProfile() {
        val newProfileName = binding.editProfileName.text.toString().trim()

        if (newProfileName.isEmpty()) {
            binding.layoutProfileName.error = getString(R.string.profile_editor_name_empty)
            return
        }

        // Kiểm tra nếu đổi tên thành một profile đã tồn tại
        if (newProfileName != originalProfileName && ConfigManager.getProfile(newProfileName) != null) {
            binding.layoutProfileName.error = getString(R.string.profile_editor_name_exist)
            return
        }
        
        binding.layoutProfileName.error = null // Xóa lỗi nếu có

        val updatedProfile = collectDataFromViews()

        if (isCreatingNew) {
            ConfigManager.saveProfile(newProfileName, updatedProfile)
        } else {
            // Nếu người dùng đổi tên
            if (newProfileName != originalProfileName) {
                ConfigManager.renameProfile(originalProfileName!!, newProfileName)
            }
            ConfigManager.saveProfile(newProfileName, updatedProfile)
        }

        Toast.makeText(context, R.string.profile_editor_save_success, Toast.LENGTH_SHORT).show()
        findNavController().navigateUp()
    }

    private fun confirmDeleteProfile() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.profile_delete_title)
            .setMessage(getString(R.string.profile_delete_confirm, originalProfileName))
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ ->
                ConfigManager.deleteProfile(originalProfileName!!)
                findNavController().navigateUp()
            }
            .show()
    }
    
    private fun randomizeAllFields() {
        // Đây là nơi bạn sẽ viết logic tạo dữ liệu ngẫu nhiên
        // Ví dụ đơn giản:
        binding.editImei1.setText(generateRandomNumber(15))
        binding.editImei2.setText(generateRandomNumber(15))
        binding.editAndroidId.setText(generateRandomHex(16))
        binding.editBuildFingerprint.setText("google/flame/flame:${Random.nextInt(10, 13)}/A${Random.nextInt(1, 4)}.${Random.nextInt(100000, 999999)}.00${Random.nextInt(1, 9)}/${Random.nextInt(1000000, 9999999)}:user/release-keys")
        // ... tạo ngẫu nhiên cho các trường khác
        
        Toast.makeText(context, "Generated random values", Toast.LENGTH_SHORT).show()
    }

    private fun generateRandomNumber(length: Int) = (1..length).map { Random.nextInt(0, 10) }.joinToString("")
    private fun generateRandomHex(length: Int) = (1..length).map { "0123456789abcdef"[Random.nextInt(0, 16)] }.joinToString("")

}