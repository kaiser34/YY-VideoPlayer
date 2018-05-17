package com.ybj366533.yy_videoplayer.utils.floatUtil;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * https://github.com/yhaolpz
 */

public class MoveType {
    static final int fixed = 0;
    public static final int free = 1;
    public static final int active = 2;
    public static final int slide = 3;
    public static final int back = 4;

    @IntDef({fixed, free, active, slide, back})
    @Retention(RetentionPolicy.SOURCE)
    @interface MOVE_TYPE {
    }
}