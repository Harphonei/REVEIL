package finalgame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

public class EnemyProjectile {
    private double x;
    private double y;
    private final int diameter = 10;
    private final double velocityX;
    private final double velocityY;

    public EnemyProjectile(double startX, double startY) {
        this(startX, startY, 0.0, 160.0);
    }

    public EnemyProjectile(double startX, double startY,
            double velocityX, double velocityY) {
        x = startX;
        y = startY;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
    }

    public void update(double seconds) {
        x += velocityX * seconds;
        y += velocityY * seconds;
    }

    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, diameter, diameter);
    }

    public boolean isOutsideScreen(int screenWidth, int screenHeight) {
        return x + diameter < 0 || x > screenWidth
                || y + diameter < 0 || y > screenHeight;
    }

    public void draw(Graphics g) {
        g.setColor(Color.ORANGE);
        g.fillOval((int) x, (int) y, diameter, diameter);
    }
}
