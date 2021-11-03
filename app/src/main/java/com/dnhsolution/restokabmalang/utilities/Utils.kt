package com.dnhsolution.restokabmalang.utilities;

import android.app.Activity;
import android.content.Intent;
import com.dnhsolution.restokabmalang.R;

public class Utils {
    private static int sTheme;
    public final static int THEME_FIRST = 0;
    public final static int THEME_SECOND = 1;
    public final static int THEME_THIRD = 2;
    public final static int THEME_FOURTH = 3;
    public final static int THEME_FIFTH = 4;
    public final static int THEME_SIXTH = 5;

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
                activity.setTheme(R.style.Theme_Third);
                break;
            case THEME_FOURTH:
                activity.setTheme(R.style.Theme_Fourth);
                break;
            case THEME_FIFTH:
                activity.setTheme(R.style.Theme_Fifth);
                break;
            case THEME_SIXTH:
                activity.setTheme(R.style.Theme_Sixth);
                break;
        }
    }
}
