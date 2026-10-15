package finalgame;

import java.awt.Rectangle;
import java.util.ArrayList;

// Sweeps the centre of a five-shot fan left, centre, right, centre.
public class SpreadPattern implements AttackPattern {
    private int wave = 0;

    @Override
    public void emit(Rectangle battleArea, Rectangle bossBounds,
            ArrayList<EnemyProjectile> projectiles) {
        int[] sweepAngles = {-12, 0, 12, 0};
        int centreAngle = sweepAngles[wave % sweepAngles.length];
        double x = bossBounds.x + bossBounds.width / 2.0 - 5;
        double y = bossBounds.y + bossBounds.height;

        for (int i = -2; i <= 2; i++) {
            double angle = Math.toRadians(centreAngle + i * 15);
            double velocityX = Math.sin(angle) * 145.0;
            double velocityY = Math.cos(angle) * 145.0;
            projectiles.add(new EnemyProjectile(x, y, velocityX, velocityY));
        }
        wave++;
    }

    public void reset() {
        wave = 0;
    }
}
