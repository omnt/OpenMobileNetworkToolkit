package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import static de.fraunhofer.fokus.OpenMobileNetworkToolkit.DebuggerService.setDebugText;


@SuppressWarnings({"unused"})
public class SRLog {

    private static final String PREFIX_TAG = "QQQ ";
    private static final String NULL_MSG = "NULL";
    private static final String WITH_ERROR = "\nWith Error: ";

    public enum LogType {
        V,
        D,
        I,
        W,
        E,
        WTF
    }


    public static void v(Object msg) {
        log(null, msg, null, LogType.V);
    }

    public static void v(String tag, Object msg) {
        log(tag, msg, null, LogType.V);
    }

    public static void v(String tag, Object msg, Throwable throwable) {
        log(tag, msg, throwable, LogType.V);
    }

    public static void d(Object msg) {
        log(null, msg, null, LogType.D);
    }

    public static void d(String tag, Object msg) {
        log(tag, msg, null, LogType.D);
    }

    public static void d(String tag, Object msg, Throwable throwable) {
        log(tag, msg, throwable, LogType.D);
    }

    public static void i(Object msg) {
        log(null, msg, null, LogType.I);
    }

    public static void i(String tag, Object msg) {
        log(tag, msg, null, LogType.I);
    }

    public static void i(String tag, Object msg, Throwable throwable) {
        log(tag, msg, throwable, LogType.I);
    }

    public static void w(Object msg) {
        log(null, msg, null, LogType.W);
    }

    public static void w(String tag, Object msg) {
        log(tag, msg, null, LogType.W);
    }

    public static void w(String tag, Object msg, Throwable throwable) {
        log(tag, msg, throwable, LogType.W);
    }

    public static void e(Object msg) {
        log(null, msg, null, LogType.E);
    }

    public static void e(String tag, Object msg) {
        log(tag, msg, null, LogType.E);
    }

    public static void e(String tag, Object msg, @Nullable Throwable t) {
        log(tag, msg, t, LogType.E);
    }

    public static void wtf(Object msg) {
        log(null, msg, null, LogType.WTF);
    }

    public static void wtf(String tag, Object msg) {
        log(tag, msg, null, LogType.WTF);
    }

    public static void wtf(String tag, Object msg, Throwable throwable) {
        log(tag, msg, throwable, LogType.WTF);
    }

    public static void println(int level, String tag, Object msg) {
        if (null != msg) {
            String a = String.valueOf(msg);
            try {
                if (!TextUtils.isEmpty(a)) {
                    setDebugText(a, R.color.debug_debug_color);
                    Log.println(level, tag, a);
                } else {
                    Log.e(tag, NULL_MSG);
                }
            } catch (Exception ignored) {
            }
        } else {
            Log.e(tag, NULL_MSG);
        }
    }


    public static void log(String tag, Object msg, @Nullable Throwable t, LogType type) {
        if (TextUtils.isEmpty(tag)) tag = PREFIX_TAG;
        String throwable = "";
        if (null != t) {
            try {
                throwable = t.toString();
            } catch (Exception e) {
                throwable = "";
            }
        }
        if (null != msg) {
            String a = String.valueOf(msg);

            if (!TextUtils.isEmpty(throwable)) {
                a = a + WITH_ERROR + throwable;
            }
            try {
                if (!TextUtils.isEmpty(a)) {
                    switch (type) {

                        case V:
                            setDebugText(a, R.color.debug_verbose_color);
                            Log.v(tag, a);
                            break;

                        case D:
                            setDebugText(a, R.color.debug_debug_color);
                            Log.d(tag, a);
                            break;

                        case I:
                            setDebugText(a, R.color.debug_info_color);
                            Log.i(tag, a);
                            break;

                        case W:
                            setDebugText(a, R.color.debug_warn_color);
                            Log.w(tag, a);
                            break;

                        case E:
                            setDebugText(a, R.color.debug_error_color);
                            Log.e(tag, a);
                            break;

                        case WTF:
                            setDebugText(a, R.color.debug_assert_color);
                            Log.wtf(tag, a);
                            break;

                        default:
                            setDebugText(a, R.color.debug_debug_color);
                            Log.d(tag, a);
                            break;
                    }
                } else {
                    Log.e(tag, NULL_MSG);
                }
            } catch (Exception ignored) {
                Log.e(tag, NULL_MSG);
            }
        } else {
            Log.e(tag, NULL_MSG);
        }
    }


}
