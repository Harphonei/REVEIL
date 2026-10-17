package finalgame;

import java.awt.Rectangle;
import java.util.ArrayList;

/** Full-arena lattice: straight / diagonal repeated three times, ending straight. */
final class GuardianGridPattern {
    static final int VOLLEYS=7;
    private static final double SPACING=88;
    private static final double[] OFFSETS={.0,.33,.5,.83,.25,.58,.75};
    private GuardianGridPattern() {}

    static void emit(Rectangle arena,int volley,ArrayList<SwordStrike> strikes) {
        double angle=(volley%2==0)?0:Math.PI/4;
        double c=Math.cos(angle),s=Math.sin(angle);
        double span=Math.hypot(arena.width,arena.height);
        int count=(int)Math.ceil(span/SPACING)+1;
        double phase=OFFSETS[volley]*SPACING;
        // Both perpendicular families extend beyond every arena edge. SwordStrike
        // clips warning and hit geometry to the same arena rectangle.
        for(int axis=0;axis<2;axis++) {
            double nx=axis==0?c:-s,ny=axis==0?s:c;
            double tx=-ny,ty=nx;
            for(int k=-count;k<=count;k++) {
                double distance=k*SPACING+phase;
                double cx=arena.getCenterX()+nx*distance;
                double cy=arena.getCenterY()+ny*distance;
                strikes.add(new SwordStrike(cx-tx*span,cy-ty*span,
                        cx+tx*span,cy+ty*span,12f,.72,.24,arena));
            }
        }
    }
}
