package finalgame;

import java.awt.Color;

// Three phase-offset trajectories wind around a shared centre while falling.
public class SpiralProjectile extends EnemyProjectile {
    private final double centreX;
    private final double startY;
    private final double startAngle;
    private double age = 0.0;

    public SpiralProjectile(double centreX, double startY, double startAngle) {
        super(centreX + Math.cos(startAngle) * 18
                - NORMAL_DIAMETER / 2.0, startY,
                0.0, 0.0, NORMAL_DIAMETER, new Color(200, 130, 255));
        this.centreX = centreX;
        this.startY = startY;
        this.startAngle = startAngle;
    }

    @Override
    public void update(double seconds) {
        age += seconds * CombatTuning.ENEMY_SPEED_MULTIPLIER;
        double radius = 18 + Math.min(age * 22, 90);
        double angle = startAngle + age * 2.8;
        x = centreX + Math.cos(angle) * radius - NORMAL_DIAMETER / 2.0;
        y = startY + age * 110.0;
    }
}
