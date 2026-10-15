package finalgame;

import java.awt.Graphics;

// A second shockwave waits without drawing or damaging before it starts moving.
public class DelayedProjectile extends EnemyProjectile {
    private double delay;

    public DelayedProjectile(double x, double y, double velocityX,
            double velocityY, double delay) {
        super(x, y, velocityX, velocityY);
        this.delay = delay;
    }

    @Override
    public void update(double seconds) {
        if (delay > 0.0) {
            double waiting = Math.min(seconds, delay);
            delay -= waiting;
            seconds -= waiting;
        }
        if (seconds > 0.0) {
            super.update(seconds);
        }
    }

    @Override
    public boolean isDangerous() {
        return delay <= 0.0;
    }

    @Override
    public void draw(Graphics g) {
        if (isDangerous()) {
            super.draw(g);
        }
    }
}
