package com.dnhsolution.restokabmalang.utilities;

import android.app.Activity;
import android.content.Intent;
import com.dnhsolution.restokabmalang.R;

public class Utils {
    private static int sTheme;
    public final static int THEME_FIRST = 0;
    public final static int THEME_SECOND = 1;
    public final static int THEME_THIRD = 2;

    public static void changeToTheme(Activity activity, int theme) {

        sTheme = theme;
        activity.finish();
        activity.startActivity(new Intent(activity, activity.getClass()));
        activity.overridePendingTransition(android.R.anim.fade_in,
                android.R.anim.fade_out);
    }

    public static void onActivityCreateSetTheme(Activity activity) {
        switch (sTheme) {
            default:
            case THEME_FIRST:
                activity.setTheme(R.style.Theme_First);
                break;
            case THEME_SECOND:
                activity.setTheme(R.style.Theme_Second);
                break;
            case THEME_THIRD:
                activity.setTheme(R.style.Theme_Second);
                break;
        }
    }
}
