package com.hx.recyclerviewdemo.utils;

import android.animation.ObjectAnimator;
import android.view.animation.RotateAnimation;

/**
 * 动画工具类
 *
 * @Author dzl on 2016/7/14.
 */
public class AnimationUtil {

    /**
     * 旋转动画 360自转
     */
    public static RotateAnimation getRoteSelfAnimation(long duration, int repeatCount) {
        return getRoteSelfAnimation(0, 360f, 0.5f, 0.5f, duration, repeatCount);
    }

    /**
     * 对象旋转的属性动画 360自转
     */
    public static ObjectAnimator getRoteSelfObjectAnimation(Object target, long duration, int repeatCount) {
        return getRotationAnimator(target, "rotation", duration, repeatCount, 0f, 360f);
    }

    /**
     * Y轴移动
     */
    public static ObjectAnimator getTranslateYObjectAnimation(Object target, long duration, int repeatCount, float... height) {
        return getRotationAnimator(target, "translationY", duration, repeatCount, height);
    }

    /**
     * 旋转动画
     */
    public static RotateAnimation getRoteSelfAnimation(float fromDegrees, float toDegrees,
                                                       float pivotXValue,
                                                       float pivotYValue, long duration, int repeatCount) {
        RotateAnimation rotateAnimation = new RotateAnimation(fromDegrees, toDegrees,
                RotateAnimation.RELATIVE_TO_SELF, pivotXValue, RotateAnimation.RELATIVE_TO_SELF, pivotYValue);
        rotateAnimation.setRepeatCount(repeatCount);
        rotateAnimation.setDuration(duration);
        return rotateAnimation;
    }

    /**
     * 属性动画
     */
    public static ObjectAnimator getRotationAnimator(Object target, String propertyName, long duration, int repeatCount, float... values) {
        ObjectAnimator rotation = ObjectAnimator.ofFloat(target, propertyName, values);
        rotation.setDuration(duration).setRepeatCount(repeatCount);
        return rotation;
    }


}
