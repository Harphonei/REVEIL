package finalgame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

public class EnemyProjectile {
    private double x;
    private double y;
    private final int width = 10;
    private final int height = 14;
    private final double speed = 160.0;

    public EnemyProjectile(double startX, double startY) {
        x = startX;
        y = startY;
    }

    public void update(double seconds) {
        y += speed * seconds;
    }

    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, width, height);
    }

    public boolean isOutsideScreen(int screenHeight) {
        return y > screenHeight;
    }

    public void draw(Graphics g) {
        g.setColor(Color.ORANGE);
        g.fillRect((int) x, (int) y, width, height);
    }
}