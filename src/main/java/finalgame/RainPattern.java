package finalgame;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Random;

// Shared basic attack: two falling circles per wave, never a full-width wall.
public class RainPattern implements AttackPattern {
    private final Random random;
    private final double speed;

    public RainPattern() {
        this(new Random());
    }

    public RainPattern(Random random) {
        this(random, 125.0);
    }

    public RainPattern(Random random, double speed) {
        this.random = random;
        this.speed = speed;
    }

    @Override
    public void emit(Rectangle battleArea, Rectangle bossBounds,
            ArrayList<EnemyProjectile> projectiles) {
        int columns = 6;
        double columnWidth = battleArea.width / (double) columns;
        int first = random.nextInt(columns);
        int second = random.nextInt(columns - 1);
        if (second >= first) {
            second++;
        }

        int[] selected = {first, second};
        for (int column : selected) {
            double jitter = (random.nextDouble() - 0.5) * 16.0;
            double x = battleArea.x + (column + 0.5) * columnWidth + jitter
                    - EnemyProjectile.NORMAL_DIAMETER / 2.0;
            double y = battleArea.y - 40;
            projectiles.add(new EnemyProjectile(x, y, 0.0, speed));
        }
    }
}
