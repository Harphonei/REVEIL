package finalgame;

import java.util.ArrayList;

public class TrainingBoss extends Boss {

    public TrainingBoss(int startX, int startY) {
        super(startX, startY);
    }

    @Override
    public void attack(ArrayList<EnemyProjectile> projectiles) {
        projectiles.add(shoot());
    }
}