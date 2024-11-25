package com.wudaokou.nrf.tool;

import androidx.annotation.Keep;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

@Keep
public class hook implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (lpparam.packageName.equals("com.wudaokou.nrf"))
            mainhook(lpparam);
    }

    public void mainhook(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedHelpers.findAndHookMethod("android.widget.TextView", lpparam.classLoader, "setInputType", int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                if (param.args[0].equals(android.text.InputType.TYPE_NULL))
                    param.args[0] = android.text.InputType.TYPE_CLASS_TEXT;
            }
        });
        XposedHelpers.findAndHookMethod("android.view.inputmethod.InputMethodManager", lpparam.classLoader, "hideSoftInputFromWindow", android.os.IBinder.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                if (param.args[1].equals(android.view.inputmethod.InputMethodManager.RESULT_UNCHANGED_SHOWN))
                    param.args[1] = android.view.inputmethod.InputMethodManager.RESULT_UNCHANGED_HIDDEN;
            }
        });
        XposedHelpers.findAndHookMethod("android.view.KeyEvent", lpparam.classLoader, "getKeyCode", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                if (param.getResult().equals(122) || param.getResult().equals(24) || param.getResult().equals(25))
                    Runtime.getRuntime().exec("input keyevent 66");
            }
        });
    }
}