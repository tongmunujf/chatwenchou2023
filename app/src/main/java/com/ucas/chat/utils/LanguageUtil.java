package com.ucas.chat.utils;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;

import java.util.HashMap;
import java.util.Locale;

public class LanguageUtil {

    private static final String ENGLISH = "en";
    private static final String CHINESE = "ch";
    private static final String TRADITIONAL_CHINESE = "zh_rHK";

    private static HashMap<String, Locale> languagesList = new HashMap<String, Locale>(3) {{
        put(ENGLISH, Locale.ENGLISH);
        put(CHINESE, Locale.CHINESE);
        put(TRADITIONAL_CHINESE, Locale.TRADITIONAL_CHINESE);
    }};

    /**
     * 修改语言
     *
     * @param activity 上下文
     * @param language 例如修改为 英文传“en”，参考上文字符串常量
     * @param cls      要跳转的类（一般为入口类）
     */
    public static void changeAppLanguage(Activity activity, String language, Class<?> cls) {
        Resources resources = activity.getResources();
        Configuration configuration = resources.getConfiguration();
        // app locale 默认简体中文
        Locale locale = getLocaleByLanguage(StringUtils.isEmpty(language) ? "zh" : language);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(locale);
        } else {
            configuration.locale = locale;
        }
        DisplayMetrics dm = resources.getDisplayMetrics();
        resources.updateConfiguration(configuration, dm);

        //finish();
        // 重启app
        Intent intent = new Intent(activity, cls);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        //加载动画
        //activity.overridePendingTransition(R.anim.anim_right_in, R.anim.anim_left_out);
        //activity.overridePendingTransition(0, 0);
    }

    /**
     * 获取指定语言的locale信息，如果指定语言不存在
     * 返回本机语言，如果本机语言不是语言集合中的一种，返回英语
     */
    private static Locale getLocaleByLanguage(String language) {
        if (isContainsKeyLanguage(language)) {
            return languagesList.get(language);
        } else {
            Locale locale = Locale.getDefault();
            for (String key : languagesList.keySet()) {
                if (languagesList.get(key).getLanguage().equals(locale.getLanguage())){

                    return locale;
                }
            }
        }
        return Locale.ENGLISH;
    }

    /**
     * 如果此映射包含指定键的映射关系，则返回 true
     */
    private static boolean isContainsKeyLanguage(String language) {
        return languagesList.containsKey(language);
    }
}
