package com.cyberfanta.stringxmlfiltertool;

import android.util.Log;

public class ProjectUtils {
    public static boolean showMessages = true;

    public static void PrintLogI (String string) {
        if (showMessages)
            Log.i(null, string);
    }

    public static void PrintLogE (String string) {
        if (showMessages)
            Log.e(null, string);
    }

    public static void setShowMessages(boolean showMessages) {
        ProjectUtils.showMessages = showMessages;
    }

    public static void switchShowMessages() {
        ProjectUtils.showMessages = !ProjectUtils.showMessages;
    }

//    ---

    /**
     * Convert pounds to kilos
     * @param pounds float
     * @return kilos - float
     * */
    public static float poundsToKilos (float pounds) {
        return pounds / 2.2046F;
    }

    /**
     * Convert kilos to pounds
     * @param kilos float
     * @return pounds - float
     * */
    public static float kilosToPounds (float kilos) {
        return kilos * 2.2046F;
    }
}
