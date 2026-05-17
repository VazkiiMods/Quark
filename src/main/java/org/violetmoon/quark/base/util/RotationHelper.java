package org.violetmoon.quark.base.util;

// Missing rotation functions from Mth
public class RotationHelper {


    public static float wrapRadians(float angle) {
        angle %= (float) (2 * Math.PI);

        if (angle >= Math.PI) angle -= (float) (2 * Math.PI);
        else if (angle <= -Math.PI) angle += (float) (2 * Math.PI);

        return angle;
    }
}
