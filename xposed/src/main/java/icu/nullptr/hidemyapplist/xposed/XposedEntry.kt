// file: xposed/src/main/java/icu/nullptr/hidemyapplist/xposed/XposedEntry.kt

package icu.nullptr.hidemyapplist.xposed

import android.content.pm.IPackageManager
import com.github.kyuubiran.ezxhelper.init.EzXHelperInit
import com.github.kyuubiran.ezxhelper.utils.*
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import icu.nullptr.hidemyapplist.common.Constants
import icu.nullptr.hidemyapplist.xposed.hook.HookManager
import kotlin.concurrent.thread

private const val TAG = "APF-XposedEntry"

@Suppress("unused")
class XposedEntry : IXposedHookZygoteInit, IXposedHookLoadPackage {

    // HookManager sẽ được khởi tạo theo yêu cầu (lazy)
    // để tránh tạo đối tượng không cần thiết trong mọi tiến trình.
    private val hookManager: HookManager by lazy {
        // Chỉ khởi tạo khi service đã chắc chắn tồn tại
        HookManager(CentralService.instance!!)
    }

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        // Khởi tạo EzXHelper ở tầng Zygote, cần thiết cho các chức năng của thư viện
        EzXHelperInit.initZygote(startupParam)
        logI(TAG, "EzXHelper initialized in Zygote.")
    }

    /**
     * Hàm này được gọi cho mỗi ứng dụng khi nó được nạp vào bộ nhớ.
     */
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        // Khởi tạo EzXHelper cho từng tiến trình ứng dụng
        EzXHelperInit.initHandleLoadPackage(lpparam)

        // Phân loại và xử lý dựa trên tên gói của ứng dụng đang được nạp
        when (lpparam.packageName) {
            // 1. Nếu là system_server, nhiệm vụ duy nhất là khởi động CentralService
            "android" -> {
                handleLoadSystemServer()
            }

            // 2. Nếu là ứng dụng UI của chúng ta, báo cho nó biết là module đã được kích hoạt
            Constants.APP_PACKAGE_NAME -> {
                handleLoadSelf(lpparam)
            }

            // 3. Đối với tất cả các ứng dụng khác, kiểm tra xem có cần áp dụng faker không
            else -> {
                handleLoadTargetApp(lpparam)
            }
        }
    }

    /**
     * Xử lý khi nạp vào system_server.
     * Tìm và hook vào ServiceManager để lấy đối tượng IPackageManager và khởi tạo CentralService.
     */
    private fun handleLoadSystemServer() {
        logI(TAG, "Hooking system_server to start Central Service...")
        
        var serviceManagerHook: XC_MethodHook.Unhook? = null
        
        // Sử dụng EzXHelper để tìm phương thức `addService`
        serviceManagerHook = findMethodOrNull("android.os.ServiceManager") {
            name == "addService" && parameterTypes.size == 2
        }?.hookBefore { param ->
            // Chúng ta chỉ quan tâm khi "package" service (PackageManagerService) được thêm vào
            if (param.args[0] == "package") {
                // Hủy hook ngay sau khi tìm thấy để giảm overhead
                serviceManagerHook?.unhook()
                
                val pms = param.args[1] as IPackageManager
                logD(TAG, "PackageManagerService (IPackageManager) found.")

                // Khởi tạo CentralService trong một luồng riêng để không làm chậm quá trình khởi động hệ thống
                thread(name = "APFCentralServiceInit") {
                    runCatching {
                        // Khởi tạo đối tượng Singleton
                        CentralService(pms) 
                        logI(TAG, "Central Service has been successfully started.")
                    }.onFailure {
                        logE(TAG, "Failed to start Central Service!", it)
                    }
                }
            }
        }

        if (serviceManagerHook == null) {
            logE(TAG, "Failed to find ServiceManager.addService. Central Service will not start.")
        }
    }

    /**
     * Xử lý khi nạp vào chính ứng dụng UI của module.
     * Dùng để cho UI biết rằng module Xposed đã hoạt động.
     */
    private fun handleLoadSelf(lpparam: XC_LoadPackage.LoadPackageParam) {
        logD(TAG, "Hooking self: ${lpparam.packageName}")
        try {
            // Sử dụng EzXHelper để tìm và hook constructor của lớp Application
            findConstructorOrNull("icu.nullptr.hidemyapplist.MyApp") { emptyParam }
                ?.hookAfter { param ->
                    try {
                        val f = param.thisObject.javaClass.getDeclaredField("isHooked")
                        f.isAccessible = true
                        f.set(param.thisObject, true)
                        logI(TAG, "Set isHooked=true for UI application.")
                    } catch (t: Throwable) {
                        logE(TAG, "Failed to set isHooked", t)
                    }
                }
        } catch (e: Throwable) {
            logE(TAG, "Failed to hook self", e)
        }
    }

    /**
     * Xử lý khi nạp vào một ứng dụng có thể là mục tiêu.
     * Kiểm tra với CentralService và áp dụng các hook nếu cần.
     */
    private fun handleLoadTargetApp(lpparam: XC_LoadPackage.LoadPackageParam) {
        // Lấy instance của service. Nếu service chưa khởi động xong (trường hợp hiếm),
        // thì bỏ qua, không áp dụng hook cho ứng dụng này.
        val service = CentralService.instance ?: return

        // Hỏi "bộ não" xem ứng dụng này có cần được giả mạo thông tin không
        if (service.isFakerEnabledFor(lpparam.packageName)) {
            logI(TAG, "Applying Faker hooks to target: ${lpparam.packageName}")
            // Gọi HookManager để bắt đầu công việc của nó
            // `hookManager` sẽ được khởi tạo lười biếng (lazy) ở lần gọi đầu tiên
            hookManager.initializeHooks(lpparam)
        }
    }
}