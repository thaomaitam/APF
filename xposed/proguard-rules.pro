# Giữ lại lớp entry point của Xposed và các lớp hook
-keep class com.KTA.APF.xposed.XposedHook { *; }
-keep class com.KTA.APF.xposed.hook.** { *; }

# Giữ lại các phương thức được đánh dấu bởi annotation của Xposed
-keepclassmembers class * {
    @de.robv.android.xposed.** *;
}

# Không cảnh báo về các lớp của Xposed Framework
-dontwarn de.robv.android.xposed.**

# --- Các quy tắc cho Kotlinx Serialization ---
# Giữ lại các lớp data được đánh dấu là @Serializable
# Điều này rất quan trọng để đọc file cấu hình từ SharedPreferences
-keep @kotlinx.serialization.Serializable class * { *; }
-keepclassmembers class **$$serializer { *; }
-keep class * implements kotlinx.serialization.KSerializer { *; }
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keepnames class kotlinx.serialization.internal.*

# --- Các quy tắc cho API ẩn của Rikka ---
-keep class dev.rikka.tools.refine.** { *; }
-keep class dev.rikka.hidden.** { *; }

# --- Các quy tắc cho các lớp hệ thống bị hook ---
-keep class android.os.Build { *; }
-keep class android.os.Build$VERSION { *; }
-keep class android.os.SystemProperties { *; }
-keep class android.provider.Settings$Secure { *; }