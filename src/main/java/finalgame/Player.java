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

    public Player(double startX, double startY) {
        x = startX;
        y = startY;
    }
    public Projectile shoot() {
        return new Projectile(x + size / 2.0 - 3, y - 12);
    }
    public void move(int directionX, int directionY, double seconds,Rectangle movementArea) {
        double distance = Math.hypot(directionX, directionY);

        if (distance > 0) {
            x += directionX / distance * speed * seconds;
            y += directionY / distance * speed * seconds;
        }

        double minX = movementArea.x;
        double minY = movementArea.y;
        double maxX = movementArea.x + movementArea.width - size;
        double maxY = movementArea.y + movementArea.height - size;

        x = Math.max(minX, Math.min(x, maxX));
        y = Math.max(minY, Math.min(y, maxY));
    }
    public void draw(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect((int) x, (int) y, size, size);
    }
    public void update(double seconds) {
        invincibleTime = Math.max(0.0, invincibleTime - seconds);
    }
    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, size, size);
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
    }
}
