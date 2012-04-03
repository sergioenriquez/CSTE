package org.fern.rest.android;

public class NumberUtils {
    public static int clamp(int what, int low, int high) {
        if (what < low) return low;
        if (what > high) return high;
        return what;
    }
    
    public static double clamp(double what, double low, double high) {
        if (what < low) return low;
        if (what > high) return high;
        return what;
    }
}
