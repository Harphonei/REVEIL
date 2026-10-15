package finalgame;

import java.awt.Rectangle;
import java.util.ArrayList;

public class TrainingBoss extends Boss {
    private boolean spreadNext = false;

    public TrainingBoss(int startX, int startY) {
        super(startX, startY);
    }

    @Override
    public void attack(ArrayList<EnemyProjectile> projectiles) {
        if (spreadNext) {
            shootSpread(projectiles);
        } else {
            projectiles.add(shoot());
        }

        spreadNext = !spreadNext;
    }

    private void shootSpread(ArrayList<EnemyProjectile> projectiles) {
        Rectangle bounds = getBounds();

        double startX = bounds.x + bounds.width / 2.0 - 5;
        double startY = bounds.y + bounds.height;
        double bulletSpeed = 160.0;

        for (int i = -2; i <= 2; i++) {
            double angle = Math.toRadians(i * 15);
            double velocityX = Math.sin(angle) * bulletSpeed;
            double velocityY = Math.cos(angle) * bulletSpeed;

            projectiles.add(new EnemyProjectile(
                    startX,
                    startY,
                    velocityX,
                    velocityY
            ));
        }
    }

    @Override
    public void reset() {
        super.reset();
        spreadNext = false;
    }
}