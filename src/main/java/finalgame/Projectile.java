package finalgame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

public class Projectile {
    private double x;
    private double y;
    private final int width = 6;
    private final int height = 12;
    private final double speed = 230.0;

    public Projectile(double startX, double startY) {
        x = startX;
        y = startY;
    }

    public void update(double seconds) {
        y -= speed * seconds;
    }

    public boolean isOutsideScreen() {
        return y + height < 0;
    }

    public void draw(Graphics g) {
        g.setColor(Color.CYAN);
        g.fillRect((int) x, (int) y, width, height);
    }
    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, width, height);
    }
}