// file: app/src/main/java/icu/nullptr/hidemyapplist/ui/fragment/SettingsFragment.kt

package icu.nullptr.hidemyapplist.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.net.toUri
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceDataStore
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tsng.hidemyapplist.R
import com.tsng.hidemyapplist.databinding.FragmentSettingsBinding
import icu.nullptr.hidemyapplist.common.Constants
import icu.nullptr.hidemyapplist.hmaApp
import icu.nullptr.hidemyapplist.service.ConfigManager
import icu.nullptr.hidemyapplist.service.PrefManager
import icu.nullptr.hidemyapplist.service.ServiceClient
import icu.nullptr.hidemyapplist.ui.util.makeToast
import icu.nullptr.hidemyapplist.ui.util.setupToolbar
import icu.nullptr.hidemyapplist.util.LangList
import rikka.material.app.LocaleDelegate
import rikka.preference.SimpleMenuPreference
import java.util.*

// Lớp cha không cần implement OnPreferenceStartFragmentCallback nữa vì không còn fragment con
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val binding by viewBinding<FragmentSettingsBinding>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupToolbar(binding.toolbar, getString(R.string.title_settings))

        if (childFragmentManager.findFragmentById(R.id.settings_container) == null) {
            childFragmentManager.beginTransaction()
                .replace(R.id.settings_container, SettingsPreferenceFragment())
                .commit()
        }
    }
    
    // Đã xóa onPreferenceStartFragment vì không còn sub-fragment

    /**
     * DataStore để kết nối các Preference với PrefManager và ConfigManager.
     * Đã được dọn dẹp, chỉ giữ lại các key còn tồn tại trong settings.xml
     */
    class SettingsPreferenceDataStore : PreferenceDataStore() {
        override fun getBoolean(key: String, defValue: Boolean): Boolean {
            return when (key) {
                "followSystemAccent" -> PrefManager.followSystemAccent
                "blackDarkTheme" -> PrefManager.blackDarkTheme
                "detailLog" -> ConfigManager.detailLog
                "hideIcon" -> PrefManager.hideIcon
                else -> defValue // Trả về giá trị mặc định nếu key không khớp
            }
        }

        override fun getString(key: String, defValue: String?): String? {
            return when (key) {
                "language" -> PrefManager.locale
                "themeColor" -> PrefManager.themeColor
                "darkTheme" -> PrefManager.darkTheme.toString()
                "maxLogSize" -> ConfigManager.maxLogSize.toString()
                else -> defValue
            }
        }

        override fun putBoolean(key: String, value: Boolean) {
            when (key) {
                "followSystemAccent" -> PrefManager.followSystemAccent = value
                "blackDarkTheme" -> PrefManager.blackDarkTheme = value
                "detailLog" -> ConfigManager.detailLog = value
                "hideIcon" -> PrefManager.hideIcon = value
                else -> { /* Không làm gì với các key không xác định */ }
            }
        }

        override fun putString(key: String, value: String?) {
            if (value == null) return
            when (key) {
                "language" -> PrefManager.locale = value
                "themeColor" -> PrefManager.themeColor = value
                "darkTheme" -> PrefManager.darkTheme = value.toInt()
                "maxLogSize" -> ConfigManager.maxLogSize = value.toInt()
                else -> { /* Không làm gì với các key không xác định */ }
            }
        }
    }
    
    /**
     * Fragment chính chứa các Preference.
     */
    class SettingsPreferenceFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            preferenceManager.preferenceDataStore = SettingsPreferenceDataStore()
            setPreferencesFromResource(R.xml.settings, rootKey)

            // Cấu hình cho tùy chọn Ngôn ngữ
            setupLanguagePreference()

            // Cấu hình cho các tùy chọn Giao diện
            setupThemePreferences()

            // Cấu hình cho các tùy chọn Service
            setupServicePreferences()
        }

        private fun setupLanguagePreference() {
            @Suppress("DEPRECATION")
            findPreference<SimpleMenuPreference>("language")?.let {
                val userLocale = hmaApp.getLocale(PrefManager.locale)
                val entries = buildList {
                    for (lang in LangList.LOCALES) {
                        if (lang == "SYSTEM") {
                            add(getString(rikka.core.R.string.follow_system))
                        } else {
                            val locale = Locale.forLanguageTag(lang)
                            add(HtmlCompat.fromHtml(locale.getDisplayName(locale), HtmlCompat.FROM_HTML_MODE_LEGACY))
                        }
                    }
                }
                it.entries = entries.toTypedArray()
                it.entryValues = LangList.LOCALES

                it.summary = if (it.value == "SYSTEM") {
                    getString(rikka.core.R.string.follow_system)
                } else {
                    val locale = Locale.forLanguageTag(it.value)
                    if (!TextUtils.isEmpty(locale.script)) locale.getDisplayScript(userLocale) else locale.getDisplayName(userLocale)
                }

                it.setOnPreferenceChangeListener { _, newValue ->
                    val newLocaleTag = newValue as String
                    if (PrefManager.locale != newLocaleTag) {
                        PrefManager.locale = newLocaleTag
                        activity?.recreate()
                    }
                    true
                }
            }

            findPreference<Preference>("translation")?.let {
                it.summary = getString(R.string.settings_translate_summary, getString(R.string.app_name))
                it.setOnPreferenceClickListener {
                    startActivity(Intent(Intent.ACTION_VIEW, Constants.TRANSLATE_URL.toUri()))
                    true
                }
            }
        }
        
        private fun setupThemePreferences() {
            val themeChangeListener = Preference.OnPreferenceChangeListener { _, _ ->
                activity?.recreate()
                true
            }
            findPreference<SwitchPreference>("followSystemAccent")?.onPreferenceChangeListener = themeChangeListener
            findPreference<SimpleMenuPreference>("themeColor")?.onPreferenceChangeListener = themeChangeListener
            findPreference<SimpleMenuPreference>("darkTheme")?.setOnPreferenceChangeListener { _, newValue ->
                val newMode = (newValue as String).toInt()
                if (PrefManager.darkTheme != newMode) {
                    AppCompatDelegate.setDefaultNightMode(newMode)
                    // `recreate()` sẽ được gọi bởi onPreferenceChangeListener chung
                }
                true
            }
            findPreference<SwitchPreference>("blackDarkTheme")?.onPreferenceChangeListener = themeChangeListener
        }
        
        private fun setupServicePreferences() {
            findPreference<Preference>("stopSystemService")?.setOnPreferenceClickListener {
                if (ServiceClient.serviceVersion != 0) {
                    // Hiện không có tùy chọn cleanEnv, nên mặc định là false
                    ServiceClient.stopService()
                    makeToast(R.string.settings_stop_system_service)
                } else {
                    makeToast(R.string.home_xposed_service_off)
                }
                true
            }

            findPreference<Preference>("forceCleanEnv")?.setOnPreferenceClickListener {
                MaterialAlertDialogBuilder(requireActivity())
                    .setTitle(R.string.settings_force_clean_env)
                    .setMessage(R.string.settings_is_clean_env_summary) // Giữ lại summary này vì nó giải thích rõ
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        // Cần sửa lại đường dẫn thư mục hoặc làm cho nó linh động
                        // Hiện tại, chúng ta không biết tên thư mục ngẫu nhiên từ phía UI
                        // Đây là một hạn chế cần giải quyết sau, tạm thời comment lại
                        // val result = SuUtils.execPrivileged("rm -rf /data/misc/apf_service_*")
                        // if (result) makeToast(R.string.settings_force_clean_env_toast_successful)
                        // else makeToast(R.string.settings_permission_denied)
                        makeToast(R.string.disabled) // Tạm thời vô hiệu hóa
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
                true
            }
        }
    }
}