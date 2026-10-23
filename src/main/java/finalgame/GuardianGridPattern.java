package finalgame;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/** Full-arena lattice: straight / diagonal repeated three times, ending straight. */
final class GuardianGridPattern {
    static final int VOLLEYS=7;
    private static final double SPACING=76;
    private GuardianGridPattern() {}

    static void emit(Rectangle arena,int volley,ArrayList<SwordStrike> strikes) {
        emit(arena,volley,strikes,ThreadLocalRandom.current());
    }

    static void emit(Rectangle arena,int volley,ArrayList<SwordStrike> strikes,Random random) {
        double angle=(volley%2==0)?0:Math.PI/4;
        double c=Math.cos(angle),s=Math.sin(angle);
        double span=Math.hypot(arena.width,arena.height);
        int count=(int)Math.ceil(span/SPACING)+1;
        // Both perpendicular families extend beyond every arena edge. SwordStrike
        // clips warning and hit geometry to the same arena rectangle.
        for(int axis=0;axis<2;axis++) {
            // Choose once per warning: the damaging lines never move afterwards.
            double phase=random.nextDouble()*SPACING;
            double nx=axis==0?c:-s,ny=axis==0?s:c;
            double tx=-ny,ty=nx;
            for(int k=-count;k<=count;k++) {
                double distance=k*SPACING+phase;
                double cx=arena.getCenterX()+nx*distance;
                double cy=arena.getCenterY()+ny*distance;
                strikes.add(new SwordStrike(cx-tx*span,cy-ty*span,
                        cx+tx*span,cy+ty*span,14f,.72,.24,arena));
            }
        }
    }
}
