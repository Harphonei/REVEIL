package finalgame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

// Position is locked when the warning appears; it never follows the player.
public class RootHazard {
    private static final double WARNING_TIME = 0.85 * CombatTuning.WARNING_TIME_MULTIPLIER;
    private static final double ACTIVE_TIME = 0.32;
    private final Rectangle bounds;
    private double age = 0.0;
    private boolean hitConsumed = false;

    public RootHazard(Rectangle playerBounds, Rectangle battleArea) {
        int size = 64;
        int x = playerBounds.x + playerBounds.width / 2 - size / 2;
        int y = playerBounds.y + playerBounds.height / 2 - size / 2;
        x = Math.max(battleArea.x,
                Math.min(x, battleArea.x + battleArea.width - size));
        y = Math.max(battleArea.y,
                Math.min(y, battleArea.y + battleArea.height - size));
        bounds = new Rectangle(x, y, size, size);
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
        if (!hitConsumed && isActive() && bounds.intersects(playerBounds)) {
            hitConsumed = true;
            return true;
        }
        return false;
    }

    public void draw(Graphics g) {
        if(!isExpired())BossEffects.zone(g,bounds,1,isActive(),age/WARNING_TIME);
    }
}
