package finalgame;

import java.util.ArrayList;

// Retained as an optional test boss, not one of the five story bosses.
public class TrainingBoss extends Boss {
    private boolean spreadNext = false;
    private final SpreadPattern spread = new SpreadPattern();

    public TrainingBoss(int startX, int startY) {
        super(startX, startY);
    }

    @Override
    public void attack(ArrayList<EnemyProjectile> projectiles) {
        if (spreadNext) {
            spread.emit(null, getBounds(), projectiles);
        } else {
            projectiles.add(shoot());
        }
        spreadNext = !spreadNext;
    }

    @Override
    public void reset() {
        super.reset();
        spreadNext = false;
        spread.reset();
    }
}
