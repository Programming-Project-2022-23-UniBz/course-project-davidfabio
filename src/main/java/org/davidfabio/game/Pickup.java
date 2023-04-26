package org.davidfabio.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;


public class Pickup extends Entity implements Movable {


    private float stopMovingDistance = 80;
    private float lifespanCounter;
    private float lifespan = 3.0f;

    private float startBlinkingAfter = 2.0f;
    private boolean transparencyWhileBlinkingIncreasing;
    private float transparencyWhileBlinking;




    public void init(float x, float y, float scale, Color color) {
        super.init(x, y, scale, color);
        shape = new PolygonShape(4, scale);
        lifespanCounter = 0f;
    }


    public void render(PolygonSpriteBatch polygonSpriteBatch) {
        if (!getIsActive())
            return;

        if (lifespanCounter > startBlinkingAfter) {
            Color _color = getColorInitial();
            _color.a = transparencyWhileBlinking;
            shape.render(polygonSpriteBatch, _color);
        }
        else
            super.render(polygonSpriteBatch);
    }



    public void update(float deltaTime, World world) {
        if (!getIsActive())
            return;

        if (lifespanCounter > startBlinkingAfter) {
            if (transparencyWhileBlinkingIncreasing)
                transparencyWhileBlinking += deltaTime * 4;
            else
                transparencyWhileBlinking -= deltaTime * 4;

            if (transparencyWhileBlinking > 0.6f)
                transparencyWhileBlinkingIncreasing = false;
            else if (transparencyWhileBlinking < 0.1f)
                transparencyWhileBlinkingIncreasing = true;
        }

        lifespanCounter += deltaTime;
        if (lifespanCounter > lifespan)
            setIsActive(false);

        float playerX = world.getPlayer().getX();
        float playerY = world.getPlayer().getY();
        float distanceToPlayer = getDistanceTo(playerX, playerY);

        if (distanceToPlayer < stopMovingDistance) {
            float speed = (stopMovingDistance - distanceToPlayer + world.getPlayer().getMoveSpeed());

            setMoveSpeed(speed);
            moveTowards(playerX, playerY, deltaTime);
        }

        shape.resetPosition();
        shape.translatePosition(this);
    }
    
}
