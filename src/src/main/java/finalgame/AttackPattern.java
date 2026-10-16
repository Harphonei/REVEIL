package finalgame;

import java.awt.Rectangle;
import java.util.ArrayList;

public interface AttackPattern {
    void emit(Rectangle battleArea, Rectangle bossBounds,
            ArrayList<EnemyProjectile> projectiles);
}
