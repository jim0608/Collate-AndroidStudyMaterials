package com.blackbox.lerist.utils;

/**
 * Created by Lerist on 2017/04/17 0017.
 */

public class DistanceUtils {
    /**
     * 计算两点之间距离
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return
     */
    public static double getDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }
}
