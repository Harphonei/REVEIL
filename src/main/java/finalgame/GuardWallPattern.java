package finalgame;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Random;

// One moving gap; neighbouring waves shift it gradually, rather than teleporting it.
public class GuardWallPattern implements AttackPattern {
    private final Random random;
    private Double lastGapOffset;

    public GuardWallPattern(){this(new Random());}
    GuardWallPattern(Random random){this.random=random;}

    private double nextGapOffset(){
        if(lastGapOffset==null)lastGapOffset=random.nextDouble()*170-85;
        else {
            double step=24+random.nextDouble()*32;
            double direction=random.nextBoolean()?1:-1;
            if(Math.abs(lastGapOffset+direction*step)>85)direction=-direction;
            lastGapOffset+=direction*step;
        }
        return lastGapOffset;
    }

    @Override
    public void emit(Rectangle arena, Rectangle source,
            ArrayList<EnemyProjectile> projectiles) {
        double gapCentre = arena.getCenterX() + nextGapOffset();
        // At least a 44px opening leaves 20px of horizontal tolerance for the 24px heart.
        double gapLeft = gapCentre - 22;
        double gapRight = gapCentre + 22;
        int diameter = EnemyProjectile.NORMAL_DIAMETER;
        for (int x = arena.x; x + diameter <= gapLeft; x += diameter) {
            projectiles.add(new EnemyProjectile(x, arena.y - 20, 0.0, 165.0,12));
        }
        for (int x = (int)Math.ceil(gapRight); x + diameter <= arena.x + arena.width; x += diameter) {
            projectiles.add(new EnemyProjectile(x, arena.y - 20, 0.0, 165.0,12));
        }
    }

    public void reset() {
        lastGapOffset = null;
    }
}
