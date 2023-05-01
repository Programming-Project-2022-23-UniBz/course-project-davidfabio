package org.davidfabio.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import org.davidfabio.input.Inputs;
import org.davidfabio.input.Mouse;
import org.davidfabio.utils.Pulsation;
import org.davidfabio.utils.Settings;
import org.davidfabio.utils.Transform2D;

import java.util.Random;

public class Player extends Entity implements Attackable, Attacker {
    private float fireRate = 0.07f;
    private float fireRateCooldown = 0.0f;
    private float bulletSpeed = 800;
    private float bulletScale = 32;
    private float bulletSpreadMax = 8;
    private int initialHealth = 3;
    private int health;
    private Bullet[] bullets = new Bullet[Settings.MAX_PLAYER_BULLETS];

    private float dashSpeed = 800;
    private float dashDuration = 0.2f;
    private float dashAngle;
    private float dashDurationCooldown;
    private boolean isDashing;
    private boolean inDashChooseDirectionState;
    private float[] dashPositionsX, dashPositionsY, dashTrailTransparencies;
    private int dashPositionsCount = 0;

    private boolean isInHitState;
    private float hitDuration = 2.5f;
    private float hitCooldown;
    private Pulsation pulsationHitTransparency;

    /**
     * The Attack Power is the damage a Player instance deals to an Enemy instance when hitting it while dashing.
     */
    private int attackPower = 20;
    /**
     * The Pickups Collected Variable stores how many pickups the Player has picked up in total (This may be useful for
     * statistics).
     */
    private int pickupsCollected = 0;
    /**
     * The Current Pickup Collection stores how many Pickups the Player has collected since last taking damage. This is
     * used to calculate a Multiplier for the Player's score.
     */
    private int currentPickupCollection = 0;
    /**
     * This is the number of {@link Player#currentPickupCollection} required in order to gain a higher multiplier.
     * For example if this is 10, the {@link Player#getMultiplier()} is calculated using {@link Player#currentPickupCollection}
     * divided by 10. Only the integer Part is used and it cannot be lower than 1. The maximum Multiplier is defined in
     * the Settings {@link Settings#MAX_MULTIPLIER}.
     */
    private static int pickupMultiplierDivisor = 10;

    public int getHealth() { return this.health; }
    public void setHealth(int newHealth) { this.health = newHealth; }
    public int getInitialHealth() { return this.initialHealth; }
    public void setInitialHealth(int newInitialHealth) { this.initialHealth = newInitialHealth; }
    public void setIsInHitState(boolean isInHitState) { this.isInHitState = isInHitState; }
    public boolean getIsInHitState() { return isInHitState; }
    public int getAttackPower() { return attackPower; }
    public int getPickupsCollected() { return pickupsCollected; }

    public void setHitCooldown(float hitCooldown) { this.hitCooldown = hitCooldown; }
    public float getHitDuration() { return hitDuration; }
    public boolean getIsDashing() { return isDashing; }

    public Bullet[] getBullets() { return bullets; }

    // indicates shooting direction (purely cosmetic)
    private PolygonShape shapeArrow;
    private float arrowScale = 16;
    private float arrowOffset = 24;

    private Random random;





    public void init(float x, float y, float scale, float moveSpeed, Color color)  {
        super.init(x, y, scale, color);
        this.setMoveSpeed(moveSpeed);
        this.initializeHealth();

        random = new Random();

        pulsationHitTransparency = new Pulsation(4, 0.25f);
        isDashing = false;
        inDashChooseDirectionState = false;

        int dashPositionMax = 32;
        dashPositionsX = new float[dashPositionMax];
        dashPositionsY = new float[dashPositionMax];
        dashTrailTransparencies = new float[dashPositionMax];

        for (int i = 0; i < Settings.MAX_PLAYER_BULLETS; i += 1)
            bullets[i] = new Bullet();
        
        setShape(PolygonShape.getPlayerShape(scale));
        shapeArrow = PolygonShape.getPlayerArrowShape(arrowScale);
    }



    public void render(PolygonSpriteBatch polygonSpriteBatch, ShapeRenderer shapeRenderer) {
        // main shape (circle)
        Color color = getColor();
        if (isInHitState)
            color.a = pulsationHitTransparency.getCounter() + 0.6f;
        getShape().render(polygonSpriteBatch, color);

        // direction arrow
        shapeArrow.render(polygonSpriteBatch, Color.LIGHT_GRAY);

        // dash "preview" line + circle outline
        if (inDashChooseDirectionState) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(getColorInitial());
            float dashLineLength = dashSpeed * dashDuration;
            float endX = Transform2D.translateX(getX(), getAngle(), dashLineLength);
            float endY = Transform2D.translateY(getY(), getAngle(), dashLineLength);
            shapeRenderer.line(getX(), getY(), endX, endY);
            shapeRenderer.circle(endX, endY, getScale() / 2);
            shapeRenderer.end();
        }

        // dash trail effect
        if (isDashing) {
            for (int i = dashPositionsCount; i > 0; i -= 1) {
                Color _color = new Color(getColor().r, getColor().g, getColor().b, dashTrailTransparencies[i]);
                getShape().resetPosition();
                getShape().translatePosition(dashPositionsX[i], dashPositionsY[i]);
                getShape().render(polygonSpriteBatch, _color);
            }
        }

        // bullets
        for (int i = 0; i < Settings.MAX_PLAYER_BULLETS; i += 1)
            bullets[i].render(polygonSpriteBatch, shapeRenderer);
    }



    public void update(float deltaTime, World world) {
        if (isInHitState) {
            hitCooldown -= deltaTime;
            pulsationHitTransparency.update(deltaTime);

            if (hitCooldown < 0) {
                setColor(getColorInitial());
                isInHitState = false;
            }
        }



        setAngle((float)Math.atan2(Mouse.getY() - getY(), Mouse.getX() - getX())); // update direction

        // ---------------- movement ----------------
        float speed = getMoveSpeed() * deltaTime;

        // normalize diagonal movement
        if ((Inputs.moveUp.getIsDown() || Inputs.moveDown.getIsDown()) && (Inputs.moveLeft.getIsDown() || Inputs.moveRight.getIsDown()))
            speed *= 0.707106f;

        float nextX = getX();
        float nextY = getY();
        if (Inputs.moveUp.getIsDown())    nextY -= speed;
        if (Inputs.moveDown.getIsDown())  nextY += speed;
        if (Inputs.moveLeft.getIsDown())  nextX -= speed;
        if (Inputs.moveRight.getIsDown()) nextX += speed;

        // moving normally
        if (!isDashing && !inDashChooseDirectionState) {
            setX(nextX);
            setY(nextY);
            restrictToLevel(world.getLevel());

            if (Inputs.dash.getWasPressed())
                inDashChooseDirectionState = true;
        }

        // start dashing
        else if (!isDashing && Inputs.dash.getWasReleased()) {
            isDashing = true;
            dashDurationCooldown = dashDuration;
            inDashChooseDirectionState = false;
            dashAngle = getAngle();
            Sounds.playDashSfx();
        }
        // while dashing
        else if (isDashing) {
            dashPositionsCount += 1;
            dashPositionsX[dashPositionsCount] = getX();
            dashPositionsY[dashPositionsCount] = getY();
            dashDurationCooldown -= deltaTime;
            dashTrailTransparencies[dashPositionsCount] = dashPositionsCount * deltaTime * 2;

            if (dashDurationCooldown < 0) {
                isDashing = false;
                dashPositionsCount = 0;
            }

            float _speed = dashSpeed * deltaTime;

            float _nextX = Transform2D.translateX(getX(), dashAngle, _speed);
            float _nextY = Transform2D.translateY(getY(), dashAngle, _speed);
            setX(_nextX);
            setY(_nextY);
            restrictToLevel(world.getLevel());
        }

        // ---------------- shooting ----------------
        if (fireRateCooldown > 0)
            fireRateCooldown -= deltaTime;

        if (!isDashing && !inDashChooseDirectionState && Inputs.shoot.getIsDown() && fireRateCooldown <= 0)
            shoot();

        for (int i = 0; i < Settings.MAX_PLAYER_BULLETS; i += 1) {
            bullets[i].update(deltaTime, world);
        }

        // ---------------- update shape vertices ----------------
        getShape().resetPosition();
        getShape().translatePosition(this);

        shapeArrow.resetPosition();
        shapeArrow.rotate(getAngle());
        float arrowX = Transform2D.translateX(getX(), getAngle(), arrowOffset);
        float arrowY = Transform2D.translateY(getY(), getAngle(), arrowOffset);
        shapeArrow.translatePosition(arrowX, arrowY);
    }


    public Bullet getBullet() {
        for (int i = 0; i < Settings.MAX_PLAYER_BULLETS; i += 1) {
            if (!bullets[i].getIsActive())
                return bullets[i];
            }

        return null;
    }


    public void shoot() {
        Bullet bullet = getBullet();
        float randomFloat = random.nextFloat() - 0.5f;
        float angleDelta = Transform2D.degreesToRadians(randomFloat * bulletSpreadMax);
        bullet.init(getX(), getY(), bulletScale, bulletSpeed, getAngle() + angleDelta, Color.GOLD, PolygonShape.getPlayerBulletShape(bulletScale));

        /*
        int bulletsToSpawn = 3; // TODO: quick and dirty test
        for (int i = 0; i < bulletsToSpawn; i += 1) {
            Bullet bullet = getBullet();
            float randomFloat = random.nextFloat() - 2.5f + (i * 2);
            float angleDelta = Transform2D.degreesToRadians(randomFloat * bulletSpreadMax);
            bullet.init(getX(), getY(), bulletScale, bulletSpeed, getAngle() + angleDelta, Color.GOLD, PolygonShape.getPlayerBulletShape(bulletScale));
        }

         */

        fireRateCooldown = fireRate;
        Sounds.playShootSfx();
    }

    /**
     * This method increases the Player's Pickup Count by 1. Both {@link Player#currentPickupCollection} and
     * {@link Player#pickupsCollected} are incremented in this method.
     */
    public void incrementPickups() {
        currentPickupCollection += 1;
        pickupsCollected += 1;
    }

    /**
     * This method resets the Player's {@link Player#currentPickupCollection} to 0. This method is used to reset the
     * Points multiplier once the player gets hit.
     */
    public void resetCurrentPickupCollection() {
        currentPickupCollection = 0;
    }

    /**
     * This method returns a multiplier by which the gained points are multiplied.
     * The multiplier is calculated using the currentPickup-collection. The pickups collected are divided by {@link Player#pickupMultiplierDivisor}
     * and only the integer part of this is used. The Multiplier cannot be lower than 1 and cannot exceed {@link Settings#MAX_MULTIPLIER}.
     * @return An integer value by which the gained points are multiplied.
     */
    public int getMultiplier() {
        int multiplier = (int)currentPickupCollection/10 + 1;
        return multiplier < Settings.MAX_MULTIPLIER ? multiplier : Settings.MAX_MULTIPLIER;
    }
}
