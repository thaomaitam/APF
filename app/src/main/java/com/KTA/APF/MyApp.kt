package com.KTA.APF

import android.app.Application
import com.KTA.APF.config.ConfigManager
import me.zhanghai.android.appiconloader.AppIconLoader

class MyApp : Application() {

    // Một biến companion object để có thể truy cập instance của MyApp từ bất cứ đâu trong module 'app'
    companion object {
        lateinit var instance: MyApp
            private set // Chỉ cho phép gán giá trị bên trong lớp này
    }

    // Khởi tạo AppIconLoader một cách lười biếng (lazy) để tối ưu hiệu suất.
    // Nó sẽ chỉ được tạo ra khi được truy cập lần đầu.
    val appIconLoader: AppIconLoader by lazy {
        // Lấy kích thước icon chuẩn từ a_main.xml (hoặc bạn có thể định nghĩa trong dimens.xml)
        // và làm tròn lên một chút để đảm bảo chất lượng hình ảnh.
        val iconSize = (resources.displayMetrics.density * 56).toInt()
        AppIconLoader(iconSize, true, this)
    }

    override fun onCreate() {
        super.onCreate()
        
        // Gán instance khi ứng dụng được tạo
        instance = this

        // Khởi tạo ConfigManager để nó đọc file cấu hình ngay khi ứng dụng khởi động.
        // Điều này đảm bảo cấu hình luôn sẵn sàng khi các Fragment cần đến.
        ConfigManager.initialize(this)
        
        // TODO: Khởi tạo các thư viện khác ở đây nếu cần (ví dụ: ThemeUtils, thư viện logging...)
    }
}