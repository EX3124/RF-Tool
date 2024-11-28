package com.wudaokou.nrf.tool;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

@androidx.annotation.Keep
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
                int eventID = (int) XposedHelpers.callMethod(param.thisObject, "getAction");
                if (eventID == android.view.KeyEvent.ACTION_DOWN) {
                    if (param.getResult().equals(android.view.KeyEvent.KEYCODE_MOVE_HOME) || param.getResult().equals(android.view.KeyEvent.KEYCODE_VOLUME_UP) || param.getResult().equals(android.view.KeyEvent.KEYCODE_VOLUME_DOWN)) {
                        Object LastEnterTime = XposedHelpers.getAdditionalInstanceField(param.thisObject, "LastEnterTime");
                        if (LastEnterTime == null || System.currentTimeMillis() - (Long) LastEnterTime > 500) {
                            XposedHelpers.setAdditionalInstanceField(param.thisObject, "LastEnterTime", System.currentTimeMillis());
                            Runtime.getRuntime().exec("input keyevent 66");
                        }
                    }
                }
            }
        });
    }
}