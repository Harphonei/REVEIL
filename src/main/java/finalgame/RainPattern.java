package finalgame;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Random;

// Shared basic attack: two falling circles per wave, never a full-width wall.
public class RainPattern implements AttackPattern {
    private final Random random;

    public RainPattern() {
        this(new Random());
    }

    public RainPattern(Random random) {
        this.random = random;
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
            double x = battleArea.x + (column + 0.5) * columnWidth + jitter - 5;
            double y = battleArea.y - 40;
            projectiles.add(new EnemyProjectile(x, y, 0.0, 125.0));
        }
    }
}
