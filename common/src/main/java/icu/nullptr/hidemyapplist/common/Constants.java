// file: common/src/main/java/icu/nullptr/hidemyapplist/common/Constants.java

package icu.nullptr.hidemyapplist.common;

import java.util.Set;

public class Constants {

    // Tên gói của ứng dụng UI. Rất quan trọng để Xposed biết không được hook chính nó.
    public static final String APP_PACKAGE_NAME = "com.tsng.hidemyapplist";

    // Authority của ContentProvider dùng để giao tiếp ban đầu.
    public static final String PROVIDER_AUTHORITY = APP_PACKAGE_NAME + ".ServiceProvider";

    // Các hằng số này có thể vẫn hữu ích nếu bạn muốn có logic đặc biệt cho các ứng dụng Google
    public static final String GMS_PACKAGE_NAME = "com.google.android.gms";
    public static final String GSF_PACKAGE_NAME = "com.google.android.gsf";
    
    // URL cho trang dịch thuật, có thể giữ lại hoặc thay thế bằng liên kết dự án của bạn.
    public static final String TRANSLATE_URL = "https://crowdin.com/project/hide-my-applist";

    // Các hằng số cho giao tiếp Binder (giữ nguyên từ HMA gốc)
    public static final String DESCRIPTOR = "android.content.pm.IPackageManager";
    public static final int TRANSACTION = 'H' << 24 | 'M' << 16 | 'A' << 8 | 'D'; // Có thể đổi thành APFD
    public static final int ACTION_GET_BINDER = 1;

    // UID của hệ thống
    public static final int UID_SYSTEM = 1000;

    // Danh sách các gói hệ thống quan trọng mà module không bao giờ nên hook hoặc ẩn.
    // Việc giữ lại danh sách này là rất quan trọng để đảm bảo sự ổn định của hệ thống.
    public static final Set<String> packagesShouldNotHide = Set.of(
            "android",
            "android.media",
            "android.uid.system",
            "android.uid.shell",
            "android.uid.systemui",
            "com.android.permissioncontroller",
            "com.android.providers.downloads",
            "com.android.providers.downloads.ui",
            "com.android.providers.media",
            "com.android.providers.media.module",
            "com.android.providers.settings",
            "com.google.android.webview",
            "com.google.android.providers.media.module"
    );

    // CÁC HẰNG SỐ DƯỚI ĐÂY ĐÃ BỊ XÓA VÌ KHÔNG CÒN SỬ DỤNG
    // public static final String ANDROID_APP_DATA_ISOLATION_ENABLED_PROPERTY = "persist.zygote.app_data_isolation";
    // public static final String ANDROID_VOLD_APP_DATA_ISOLATION_ENABLED_PROPERTY = "persist.sys.vold_app_data_isolation_enabled";
}