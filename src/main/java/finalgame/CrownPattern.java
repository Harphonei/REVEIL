package finalgame;

import java.awt.Rectangle;
import java.util.ArrayList;

public class CrownPattern implements AttackPattern {
    private int wave = 0;
    private final double speed;
    public CrownPattern(){this(175.0);}
    public CrownPattern(double speed){this.speed=speed;}

    public static double emitterX(Rectangle arena, int index) {
        return arena.getCenterX() + Math.sin(Math.toRadians(-70 + index * 35)) * 150;
    }

    public static double emitterY(Rectangle arena, int index) {
        return arena.y - 15 - Math.cos(Math.toRadians(-70 + index * 35)) * 55;
    }

    @Override
    public void emit(Rectangle arena, Rectangle source,
            ArrayList<EnemyProjectile> projectiles) {
        int[] sequence = {0, 1, 2, 3, 4, 3, 2, 1};
        int emitter = sequence[wave % sequence.length];
        double x = emitterX(arena, emitter);
        double y = emitterY(arena, emitter);
        double targetX = arena.x + 65 + (wave % 5) * (arena.width - 130) / 4.0;
        double centreAngle = Math.atan2(targetX - x, arena.y + arena.height - y);
        for (int i = -1; i <= 1; i++) {
            double angle = centreAngle + Math.toRadians(i * 12);
            projectiles.add(new EnemyProjectile(x - 6, y,
                    Math.sin(angle) * speed, Math.cos(angle) * speed,12));
        }
        wave++;
    }

    public void reset() {
        wave = 0;
    }
}
