package finalgame;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Random;

// Scattered straight rainfall; each boss has an explicit difficulty profile.
public class RainPattern implements AttackPattern {
    private final Random random;
    private final double speed;
    private final int columns,count;

    public RainPattern() {
        this(new Random());
    }

    public RainPattern(Random random) {
        this(random, 125.0);
    }

    public RainPattern(Random random, double speed) {
        this(random,speed,6,2);
    }

    public RainPattern(Random random,double speed,int columns,int count) {
        if(columns<2 || count<1 || count>=columns)throw new IllegalArgumentException("Rain density");
        this.random=random;this.speed=speed;this.columns=columns;this.count=count;
    }

    @Override
    public void emit(Rectangle battleArea, Rectangle bossBounds,
            ArrayList<EnemyProjectile> projectiles) {
        double columnWidth=battleArea.width/(double)columns;
        java.util.ArrayList<Integer> slots=new java.util.ArrayList<>();
        for(int i=0;i<columns;i++)slots.add(i);
        java.util.Collections.shuffle(slots,random);
        for(int i=0;i<count;i++) {
            int column=slots.get(i);
            double jitter = (random.nextDouble() - 0.5) * 16.0;
            double x = battleArea.x + (column + 0.5) * columnWidth + jitter
                    - EnemyProjectile.NORMAL_DIAMETER / 2.0;
            double y = battleArea.y - 40;
            projectiles.add(new EnemyProjectile(x, y, 0.0, speed));
        }
    }
}
