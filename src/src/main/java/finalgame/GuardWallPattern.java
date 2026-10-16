package finalgame;

import java.awt.Rectangle;
import java.util.ArrayList;

// One moving gap; neighbouring waves shift it gradually, rather than teleporting it.
public class GuardWallPattern implements AttackPattern {
    private int wave = 0;

    @Override
    public void emit(Rectangle arena, Rectangle source,
            ArrayList<EnemyProjectile> projectiles) {
        double gapCentre = arena.getCenterX() + Math.sin(wave * 0.65) * 85;
        double gapLeft = gapCentre - 48;
        double gapRight = gapCentre + 48;
        int diameter = EnemyProjectile.NORMAL_DIAMETER;
        for (int x = arena.x + 3; x + diameter <= arena.x + arena.width; x += 18) {
            if (x + diameter > gapLeft && x < gapRight) {
                continue;
            }
            projectiles.add(new EnemyProjectile(x, arena.y - 20, 0.0, 165.0));
        }
        wave++;
    }

    public void reset() {
        wave = 0;
    }
}
