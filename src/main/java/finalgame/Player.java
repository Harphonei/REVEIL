package finalgame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

public class Player {
    private double x;
    private double y;
    private final int size = 24;
    private final double speed = 180.0;
    private final int maxHp = 100;
    private int hp = maxHp;
    private double invincibleTime = 0.0;

    private static final double SKILL_E_COOLDOWN = 5.0;
    private double skillECooldown = 0.0;

    private static final double SKILL_Q_DURATION = 4.0;
    private static final double SKILL_Q_COOLDOWN = 15.0;
    private static final double ULTIMATE_SPEED_MULTIPLIER = 1.2;
    private double ultimateTime = 0.0;
    private double skillQCooldown = 0.0;

    public Player(double startX, double startY) {
        x = startX;
        y = startY;
    }

    public void move(int directionX, int directionY, double seconds,
            Rectangle movementArea) {
        double distance = Math.hypot(directionX, directionY);
        double currentSpeed = speed;
        if (isUltimateActive()) {
            currentSpeed = speed * ULTIMATE_SPEED_MULTIPLIER;
        }
        if (distance > 0) {
            x += directionX / distance * currentSpeed * seconds;
            y += directionY / distance * currentSpeed * seconds;
        }
        double minX = movementArea.x;
        double minY = movementArea.y;
        double maxX = movementArea.x + movementArea.width - size;
        double maxY = movementArea.y + movementArea.height - size;
        x = Math.max(minX, Math.min(x, maxX));
        y = Math.max(minY, Math.min(y, maxY));
    }

    public void update(double seconds) {
        invincibleTime = Math.max(0.0, invincibleTime - seconds);
        skillECooldown = Math.max(0.0, skillECooldown - seconds);
        ultimateTime = Math.max(0.0, ultimateTime - seconds);
        skillQCooldown = Math.max(0.0, skillQCooldown - seconds);
    }

    public Projectile shoot() {
        return new Projectile(x + size / 2.0 - 3, y - 12);
    }

    public Projectile useSkillE() {
        if (!isAlive() || skillECooldown > 0.0) {
            return null;
        }
        skillECooldown = SKILL_E_COOLDOWN;
        return new SkillProjectile(x + size / 2.0, y);
    }

    public double getSkillECooldown() {
        return skillECooldown;
    }

    public boolean useSkillQ() {
        if (!isAlive() || skillQCooldown > 0.0) {
            return false;
        }
        ultimateTime = SKILL_Q_DURATION;
        skillQCooldown = SKILL_Q_COOLDOWN;
        return true;
    }

    public boolean isUltimateActive() {
        return ultimateTime > 0.0;
    }

    public double getUltimateTime() {
        return ultimateTime;
    }

    public double getSkillQCooldown() {
        return skillQCooldown;
    }

    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, size, size);
    }

    public Rectangle getLaserBounds() {
        int laserWidth = 12;
        int laserX = (int) (x + size / 2.0 - laserWidth / 2.0);
        int laserHeight = Math.max(0, (int) y);
        return new Rectangle(laserX, 0, laserWidth, laserHeight);
    }

    public void takeDamage(int damage) {
        if (damage > 0 && isAlive() && invincibleTime <= 0.0) {
            hp = Math.max(0, hp - damage);
            invincibleTime = 0.75;
        }
    }

    public boolean isAlive() {
        return hp > 0;
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public void reset(double startX, double startY) {
        x = startX;
        y = startY;
        hp = maxHp;
        invincibleTime = 0.0;
        skillECooldown = 0.0;
        ultimateTime = 0.0;
        skillQCooldown = 0.0;
    }

    public void draw(Graphics g) {
        g.setColor(invincibleTime > 0.0 ? Color.YELLOW : Color.WHITE);
        g.fillRect((int) x, (int) y, size, size);
    }
}
