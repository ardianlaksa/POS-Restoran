package com.dnhsolution.restokabmalang;

import android.app.Activity;
import android.content.Intent;

public class Utils {
    private static int sTheme;
    public final static int THEME_FIRST = 0;
    public final static int THEME_SECOND = 1;

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
        }

    }

}
