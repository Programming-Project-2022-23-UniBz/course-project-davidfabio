package org.davidfabio.utils;

import org.davidfabio.game.Level;

public class Transform2D {
    public static float translateX(float x, float angle, float length) { return x + (float)Math.cos(angle) * length; }
    public static float translateY(float y, float angle, float length) {
        return y + (float)Math.sin(angle) * length;
    }

    public static float radiansToDegrees(float angleRadians) {
        return angleRadians * (float)(180 / Math.PI);
    }
    public static float degreesToRadians(float angleDegrees) {
        return angleDegrees * (float)(Math.PI / 180);
    }

    public static float getRandomX(Level level) { return (float)(Math.random() * level.getWidth()); }
    public static float getRandomY(Level level) { return (float)(Math.random() * level.getHeight()); }
}