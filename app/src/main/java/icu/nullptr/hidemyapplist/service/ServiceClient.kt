// file: app/src/main/java/icu/nullptr/hidemyapplist/service/ServiceClient.kt

package icu.nullptr.hidemyapplist.service

import android.os.IBinder
import android.os.IBinder.DeathRecipient
import android.os.Parcel
import android.os.RemoteException
import android.os.ServiceManager
import android.util.Log
import icu.nullptr.hidemyapplist.common.Constants
import icu.nullptr.hidemyapplist.common.IAPFService // ĐÃ THAY ĐỔI
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

/**
 * Lớp client giao tiếp với CentralService từ phía ứng dụng UI.
 * Nó chịu trách nhiệm lấy Binder của service và gọi các phương thức từ xa.
 */
object ServiceClient : IAPFService, DeathRecipient {

    private const val TAG = "APF-ServiceClient"

    // Proxy để log lại các lời gọi đến service, rất hữu ích cho việc gỡ lỗi.
    private class ServiceProxy(private val obj: IAPFService) : InvocationHandler {
        override fun invoke(proxy: Any?, method: Method, args: Array<out Any?>?): Any? {
            // Log trước khi gọi để biết hàm nào đang được thực thi
            Log.i(TAG, "Calling service method: ${method.name}")
            val result = method.invoke(obj, *args.orEmpty())
            // Log kết quả (nếu có)
            if (result != null) {
                Log.d(TAG, "Method ${method.name} returned: ${result.toString().take(50)}")
            }
            return result
        }
    }

    @Volatile
    private var service: IAPFService? = null

    /**
     * Được gọi bởi ServiceProvider khi CentralService gửi Binder của nó đến.
     */
    fun linkService(binder: IBinder) {
        service = Proxy.newProxyInstance(
            javaClass.classLoader,
            arrayOf(IAPFService::class.java), // SỬ DỤNG IAPFService
            ServiceProxy(IAPFService.Stub.asInterface(binder))
        ) as IAPFService
        binder.linkToDeath(this, 0) // Lắng nghe nếu service bị chết
        Log.i(TAG, "Successfully linked to CentralService.")
    }

    /**
     * Phương thức cũ để lấy service (dùng trong các phiên bản cũ của HMA).
     * Có thể giữ lại hoặc đơn giản hóa tùy theo nhu cầu.
     * Nó cố gắng lấy binder thông qua một transaction tùy chỉnh đến PackageManager.
     */
    private fun getServiceLegacy(): IAPFService? {
        if (service != null) return service
        
        // Đoạn code này là một cách "hack" để lấy binder từ system_server.
        // Cách hoạt động: module Xposed hook vào `PackageManagerService` và lắng nghe một
        // mã transaction đặc biệt (`TRANSACTION`). Khi UI gọi `pm.transact` với mã này,
        // Xposed hook sẽ trả về binder của CentralService.
        val pm = ServiceManager.getService("package") ?: return null
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        val remote = try {
            data.writeInterfaceToken(Constants.DESCRIPTOR)
            data.writeInt(Constants.ACTION_GET_BINDER)
            pm.transact(Constants.TRANSACTION, data, reply, 0)
            reply.readException()
            val binder = reply.readStrongBinder()
            IAPFService.Stub.asInterface(binder) // SỬ DỤNG IAPFService
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to get binder via legacy method.", e)
            null
        } finally {
            data.recycle()
            reply.recycle()
        }
        
        if (remote != null) {
            Log.i(TAG, "Binder acquired via legacy method.")
            remote.asBinder().linkToDeath(this, 0)
            service = Proxy.newProxyInstance(
                javaClass.classLoader,
                arrayOf(IAPFService::class.java), // SỬ DỤNG IAPFService
                ServiceProxy(remote)
            ) as IAPFService
        }
        return service
    }

    /**
     * Callback được gọi khi CentralService (hoặc tiến trình system_server) bị chết.
     */
    override fun binderDied() {
        Log.e(TAG, "CentralService binder has died. Connection lost.")
        service = null
    }

    // --- Triển khai các phương thức của IAPFService ---
    
    // asBinder vẫn cần thiết cho việc đăng ký DeathRecipient
    override fun asBinder(): IBinder? = service?.asBinder()

    override fun getServiceVersion(): Int {
        return getServiceLegacy()?.serviceVersion ?: 0
    }

    override fun getLogs(): String {
        return getServiceLegacy()?.logs ?: "Service not connected."
    }

    override fun clearLogs() {
        getServiceLegacy()?.clearLogs()
    }

    override fun syncConfig(json: String) {
        getServiceLegacy()?.syncConfig(json)
    }

    // Phương thức mới thay thế cho stopService(boolean)
    override fun stopService() {
        getServiceLegacy()?.stopService()
    }
}