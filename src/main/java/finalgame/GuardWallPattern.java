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
        // At least a 44px opening leaves 20px of horizontal tolerance for the 24px heart.
        double gapLeft = gapCentre - 22;
        double gapRight = gapCentre + 22;
        int diameter = EnemyProjectile.NORMAL_DIAMETER;
        for (int x = arena.x; x + diameter <= gapLeft; x += diameter) {
            projectiles.add(new EnemyProjectile(x, arena.y - 20, 0.0, 165.0,12));
        }
        for (int x = (int)Math.ceil(gapRight); x + diameter <= arena.x + arena.width; x += diameter) {
            projectiles.add(new EnemyProjectile(x, arena.y - 20, 0.0, 165.0,12));
        }
        wave++;
    }

    public void reset() {
        wave = 0;
    }
}
