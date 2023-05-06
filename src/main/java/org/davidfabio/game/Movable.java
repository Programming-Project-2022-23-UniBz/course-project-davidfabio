package org.davidfabio.game;

import org.davidfabio.utils.Transform2D;

/**
 * This interface is used for all Entities that are moving in the game.
 * This mainly includes any class that extends {@link Entity}.
 */
public interface Movable {
    float getX();
    void setX(float newX);
    float getY();
    void setY(float newY);
    float getScale();
    void setScale(float newScale);
    float getMoveSpeed();
    PolygonShape getShape();

    // returns true if at least 1 vertex of the shape is inside level
    default boolean isInView(World world) {
        float[] vertices = getShape().getVertices();
        for (int i = 0; i < vertices.length; i += 2) {
            float x = vertices[i];
            float y = vertices[i + 1];

            if (Collision.pointIsInLevel(x, y, world))
                return true;
        }

        return false;
    }

    // returns true if all vertices of the shape are inside level
    default boolean isCompletelyInView(World world) {
        float[] vertices = getShape().getVertices();
        for (int i = 0; i < vertices.length; i += 2) {
            float x = vertices[i];
            float y = vertices[i + 1];

            if (!Collision.pointIsInLevel(x, y, world))
                return false;
        }

        return true;
    }

    // TODO (David): should probably be redone after new collision system
    default void restrictToLevel(Level level) {
        float x = getX();
        float y = getY();
        float halfScale = getScale() / 2;
        x = Math.max(x, halfScale); // set x minimum to halfScale
        x = Math.min(x, level.getWidth() - halfScale); // set x maximum to game width - halfScale
        y = Math.max(y, halfScale); // set y minimum to halfScale
        y = Math.min(y, level.getHeight() - halfScale); // set y maximum to game height - halfScale
        setX(x);
        setY(y);
    }

    default void moveTowards(float angle, float deltaTime) {
        float speed = getMoveSpeed() * deltaTime;
        float deltaX = (float)Math.cos(angle) * speed;
        float deltaY = (float)Math.sin(angle) * speed;

        setX(getX() + deltaX);
        setY(getY() + deltaY);
    }

    default void moveTowards(float otherX, float otherY, float deltaTime) {
        float dir = getAngleTowards(otherX, otherY);
        moveTowards(dir, deltaTime);
    }

    default float getAngleTowards(float otherX, float otherY) {
        return Transform2D.getAngleTowards(getX(), getY(), otherX, otherY);
    }

    default float getDistanceTo(float otherX, float otherY) {
        return Transform2D.getDistance(getX(),getY(),otherX,otherY);
    }
}
