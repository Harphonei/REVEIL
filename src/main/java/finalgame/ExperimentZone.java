package finalgame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

public class ExperimentZone {
    private final Rectangle bounds;
    private static final double WARNING_TIME = 0.9;
    private static final double ACTIVE_TIME = 0.38;
    private double age = 0.0;
    private boolean hitConsumed = false;

    public ExperimentZone(Rectangle bounds) {
        this.bounds = new Rectangle(bounds);
    }

    public void update(double seconds) {
        age += seconds;
    }

    public boolean isActive() {
        return age >= WARNING_TIME && !isExpired();
    }

    public boolean isExpired() {
        return age >= WARNING_TIME + ACTIVE_TIME;
    }

    public boolean tryHit(Rectangle playerBounds) {
        if (isActive() && !hitConsumed && bounds.intersects(playerBounds)) {
            hitConsumed = true;
            return true;
        }
        return false;
    }

    public void draw(Graphics g) {
        if (isExpired()) {
            return;
        }
        if (isActive()) {
            g.setColor(new Color(70, 200, 230));
            g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            g.setColor(Color.WHITE);
            if (bounds.height > bounds.width) {
                g.drawLine((int) bounds.getCenterX(), bounds.y,
                        (int) bounds.getCenterX(), bounds.y + bounds.height);
            } else {
                g.drawLine(bounds.x, (int) bounds.getCenterY(),
                        bounds.x + bounds.width, (int) bounds.getCenterY());
            }
        } else {
            g.setColor(Color.YELLOW);
            g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
            g.drawRect(bounds.x + 2, bounds.y + 2,
                    bounds.width - 4, bounds.height - 4);
            g.drawString("!", (int) bounds.getCenterX() - 4,
                    (int) bounds.getCenterY() + 6);
        }
    }
}
