package com.blackbox.lerist.utils;

import android.content.Context;
import android.util.TypedValue;

/**
 * Created by Lerist on 2017/09/09 0009.
 */

public class ResourceUtils {
    private static TypedValue mTmpValue = new TypedValue();

    private ResourceUtils() {
    }

    /**
     * 从xml获取原始值 15dp -> 15, 15sp -> 15
     * @param context
     * @param id
     * @return
     */
    public static int getXmlDef(Context context, int id) {
        synchronized (mTmpValue) {
            TypedValue value = mTmpValue;
            context.getResources().getValue(id, value, true);
            return (int) TypedValue.complexToFloat(value.data);
        }
    }
}
