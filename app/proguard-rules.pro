# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep Xposed hook classes and methods
-keep class com.KTA.devicespoof.MainHook { *; }
-keep class com.KTA.devicespoof.hook.** { *; }
-keep class com.KTA.devicespoof.config.** { *; }

# Keep interface implementations
-keep class * implements com.KTA.devicespoof.hook.interfaces.IHookModule { *; }

# Keep all methods that might be called by reflection
-keepclassmembers class * {
    @de.robv.android.xposed.** *;
}

# Keep SystemProperties related classes
-keep class android.os.SystemProperties { *; }

# Keep Settings.Secure related classes
-keep class android.provider.Settings$Secure { *; }

# Keep Build related classes
-keep class android.os.Build { *; }
-keep class android.os.Build$VERSION { *; }

# Disable warnings for Xposed API
-dontwarn de.robv.android.xposed.**

# Keep line numbers for debugging
-keepattributes SourceFile,LineNumberTable

# Preserve the special static methods that are required in all enumeration classes.
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }

# Keep refine related classes
-keep class dev.rikka.tools.refine.** { *; }