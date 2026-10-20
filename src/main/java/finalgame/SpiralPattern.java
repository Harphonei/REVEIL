package finalgame;

import java.awt.Rectangle;
import java.util.ArrayList;

public class SpiralPattern implements AttackPattern {
    private int wave = 0;

    @Override
    public void emit(Rectangle arena, Rectangle source,
            ArrayList<EnemyProjectile> projectiles) {
        // Two visible wing origins, five interleaved spiralling strands from each.
        for(int wing=0;wing<2;wing++) {
            double x=source.x+source.width*(wing==0?0.22:0.78);
            double y=source.y+source.height*0.50;
            for(int arm=0;arm<5;arm++) {
                double angle=Math.toRadians(arm*72+wave*27+(wing==0?0:36));
                projectiles.add(new SpiralProjectile(x,y,angle,wing==0?1:-1));
            }
        }
        wave++;
    }

    public void reset() {
        wave = 0;
    }
}
