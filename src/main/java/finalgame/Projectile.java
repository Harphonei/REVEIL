package finalgame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

public class Projectile {
    private double x;
    private double y;
    private final int width;
    private final int height;
    private final double speed;
    private final int damage;
    private final Color color;

    public Projectile(double startX, double startY) {
        this(startX, startY, 6, 12, 230.0, 10, Color.CYAN);
    }

    protected Projectile(double startX, double startY,
            int width, int height, double speed, int damage, Color color) {
        x = startX;
        y = startY;
        this.width = width;
        this.height = height;
        this.speed = speed;
        this.damage = damage;
        this.color = color;
    }

    public void update(double seconds) {
        y -= speed * seconds;
    }

    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, width, height);
    }

    public boolean isOutsideScreen() {
        return y + height < 0;
    }

    public int getDamage() {
        return damage;
    }

    public void draw(Graphics g) {
        g.setColor(color);
        g.fillRect((int) x, (int) y, width, height);
    }
}
