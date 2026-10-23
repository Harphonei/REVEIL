package finalgame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

public class ExperimentZone {
    private final Rectangle bounds;
    private static final double WARNING_TIME = 0.9 * CombatTuning.WARNING_TIME_MULTIPLIER;
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
        if(!isExpired())BossEffects.zone(g,bounds,2,isActive(),age/WARNING_TIME);
    }
}
