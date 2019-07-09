package com.dnhsolution.restokabmalang.utilities;

import android.app.Activity;
import android.content.Intent;
import com.dnhsolution.restokabmalang.R;

public class ThemeUtils {
    private static int cTheme;
    public final static int BLUE = 0;
    public final static int YELLOW = 1;
    public final static int GREEN = 2;
    public final static int RED = 3;
    public final static int PURPLE = 4;

    public static void changeToTheme(Activity activity, int theme)

    {

        cTheme = theme;

        activity.finish();
        activity.startActivity(new Intent(activity, activity.getClass()));


    }

    public static void onActivityCreateSetTheme(Activity activity)

    {

        switch (cTheme)

        {

            default:

            case BLUE:

                activity.setTheme(R.style.Theme_Blue);

                break;

            case YELLOW:

                activity.setTheme(R.style.Theme_Yellow);

                break;

            case GREEN:

                activity.setTheme(R.style.Theme_Green);

                break;

            case RED:

                activity.setTheme(R.style.Theme_Red);

                break;

            case PURPLE:

                activity.setTheme(R.style.Theme_Purple);

                break;

        }

    }
}
