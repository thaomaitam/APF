# Giữ lại các lớp data được đánh dấu là @Serializable
# Quan trọng để lưu và đọc cấu hình JSON.
-keep @kotlinx.serialization.Serializable class * { *; }
-keepclassmembers class **$$serializer { *; }
-keep class * implements kotlinx.serialization.KSerializer { *; }

# Giữ lại các lớp enum để Proguard không xóa các phương thức values() và valueOf()
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keepnames class kotlinx.serialization.internal.*

# Giữ lại các lớp của các thư viện bên thứ ba nếu cần
# Thư viện của Rikka
-keep class dev.rikka.** { *; }
-keep interface dev.rikka.** { *; }

# Các quy tắc chung cho AndroidX
-keep class androidx.preference.Preference* { *; }
-keep,allowoptimization class * extends androidx.preference.PreferenceFragmentCompat

# Giữ lại các lớp ViewBinding được tạo tự động
-keepclassmembers class **.databinding.* {
    <init>(...);
}