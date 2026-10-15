package finalgame;

import java.awt.Rectangle;
import java.util.ArrayList;

public class WingPattern implements AttackPattern {
    private boolean leftNext = true;

    @Override
    public void emit(Rectangle arena, Rectangle source,
            ArrayList<EnemyProjectile> projectiles) {
        double centreX = leftNext ? source.x - 15 : source.x + source.width + 15;
        double centreAngle = leftNext ? 22 : -22;
        for (int i = -1; i <= 1; i++) {
            double angle = Math.toRadians(centreAngle + i * 15);
            projectiles.add(new EnemyProjectile(
                    centreX - EnemyProjectile.NORMAL_DIAMETER / 2.0,
                    source.y + source.height,
                    Math.sin(angle) * 165.0,
                    Math.cos(angle) * 165.0));
        }
        leftNext = !leftNext;
    }

    public void reset() {
        leftNext = true;
    }
}
