package finalgame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

public class LaserBeam {
    private static final int DAMAGE = 10;
    private static final double BURST_DURATION = 0.2;
    private static final double BURST_GAP = 0.25;

    private Rectangle bounds = new Rectangle();
    private boolean active = false;
    private boolean hitThisBurst = false;
    private double burstTime = 0.0;
    private double gapTime = 0.0;

    public int update(boolean enabled, Rectangle beamBounds,
            Rectangle targetBounds, double seconds) {
        bounds = beamBounds;

        if (!enabled) {
            // Cancelling a burst still starts its gap: rapid clicking cannot bypass it.
            if (active) {
                gapTime = BURST_GAP;
            } else {
                gapTime = Math.max(0.0, gapTime - seconds);
            }
            active = false;
            burstTime = 0.0;
            hitThisBurst = false;
            return 0;
        }

        if (active) {
            burstTime = Math.max(0.0, burstTime - seconds);
            if (burstTime <= 0.0) {
                active = false;
                gapTime = BURST_GAP;
                return 0;
            }
        } else {
            gapTime = Math.max(0.0, gapTime - seconds);
            if (gapTime > 0.0) {
                return 0;
            }
            active = true;
            burstTime = BURST_DURATION;
            hitThisBurst = false;
        }

        if (!hitThisBurst && bounds.intersects(targetBounds)) {
            hitThisBurst = true;
            return DAMAGE;
        }
        return 0;
    }

    public void draw(Graphics g) {
        if (!active) {
            return;
        }
        g.setColor(Color.CYAN);
        g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        g.setColor(Color.WHITE);
        g.fillRect(bounds.x + 4, bounds.y, 4, bounds.height);
    }

    public void reset() {
        active = false;
        hitThisBurst = false;
        burstTime = 0.0;
        gapTime = 0.0;
    }
}
