package finalgame;

import java.awt.Rectangle;
import java.util.ArrayList;

public class SpiralPattern implements AttackPattern {
    private int wave = 0;

    @Override
    public void emit(Rectangle arena, Rectangle source,
            ArrayList<EnemyProjectile> projectiles) {
        for (int arm = 0; arm < 3; arm++) {
            double angle = Math.toRadians(arm * 120 + wave * 25);
            projectiles.add(new SpiralProjectile(
                    arena.getCenterX(), arena.y - 22, angle));
        }
        wave++;
    }

    public void reset() {
        wave = 0;
    }
}
