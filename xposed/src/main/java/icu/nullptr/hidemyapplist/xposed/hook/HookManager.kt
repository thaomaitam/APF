// file: xposed/src/main/java/icu/nullptr/hidemyapplist/xposed/hook/HookManager.kt

package icu.nullptr.hidemyapplist.xposed.hook

import de.robv.android.xposed.callbacks.XC_LoadPackage
import icu.nullptr.hidemyapplist.xposed.CentralService // Đảm bảo import đúng
import icu.nullptr.hidemyapplist.xposed.hook.interfaces.IHookModule
import icu.nullptr.hidemyapplist.xposed.hook.impl.* // Import tất cả các hook
import icu.nullptr.hidemyapplist.xposed.logD
import icu.nullptr.hidemyapplist.xposed.logE
import icu.nullptr.hidemyapplist.xposed.logI

// Sửa constructor để nhận CentralService
class HookManager(private val service: CentralService) {
    
    companion object {
        private const val TAG = "APF-HookManager"
    }

    private val hookModules = mutableListOf<IHookModule>()
    private var initialized = false

    // Đăng ký tất cả các module hook của bạn ở đây
    private fun registerAllHookModules() {
        if (initialized) return
        
        logI(TAG, "Registering all hook modules...")

        // Thứ tự đăng ký có thể quan trọng nếu có sự phụ thuộc, 
        // hoặc bạn có thể sắp xếp theo getPriority()
        registerHookModule(BuildHook(service))
        registerHookModule(SystemPropertiesHook(service))
        registerHookModule(AndroidIdHook(service))
        // Thêm các hook khác ở đây khi bạn viết chúng
        // registerHookModule(TelephonyHook(service))
        // registerHookModule(NetworkHook(service))
        // ...

        // Sắp xếp các module theo độ ưu tiên (cao hơn được khởi tạo trước)
        hookModules.sortByDescending { it.getPriority() }

        initialized = true
        logI(TAG, "Registered ${hookModules.size} hook modules.")
    }
    
    fun initializeHooks(lpparam: XC_LoadPackage.LoadPackageParam) {
        // Chỉ đăng ký một lần
        if (!initialized) {
            registerAllHookModules()
        }
        
        logI(TAG, "Initializing hooks for package: ${lpparam.packageName}")
        
        hookModules.forEach { module ->
            try {
                // Kích hoạt hook trước khi khởi tạo
                module.enableHook() 
                module.initialize(lpparam)
                logD(TAG, "Initialized and enabled: ${module.getModuleName()}")
            } catch (e: Exception) {
                logE(TAG, "Failed to initialize ${module.getModuleName()}", e)
                module.onError(e) // Gọi hàm xử lý lỗi của module
            }
        }
    }
    
    private fun registerHookModule(module: IHookModule) {
        hookModules.add(module)
    }

    // Các phương thức còn lại có thể giữ nguyên hoặc điều chỉnh nếu cần
    fun disableAllHooks() {
        hookModules.forEach { module ->
            try {
                module.disableHook()
                logD(TAG, "Disabled hook: ${module.getModuleName()}")
            } catch (e: Exception) {
                logE(TAG, "Failed to disable ${module.getModuleName()}", e)
            }
        }
    }

    // ... các hàm getter khác
}