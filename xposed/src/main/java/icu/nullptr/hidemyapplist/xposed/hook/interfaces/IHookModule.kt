// file: xposed/src/main/java/icu/nullptr/hidemyapplist/xposed/hook/interfaces/IHookModule.kt
package icu.nullptr.hidemyapplist.xposed.hook.interfaces

import de.robv.android.xposed.callbacks.XC_LoadPackage

interface IHookModule {
    fun initialize(lpparam: XC_LoadPackage.LoadPackageParam)
    fun enableHook()
    fun disableHook()
    fun isHookActive(): Boolean
    fun getModuleName(): String
    fun getDescription(): String
    fun getPriority(): Int
    fun onError(error: Exception)
    fun cleanup()
}