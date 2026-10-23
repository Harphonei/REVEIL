package finalgame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;

// A large orb releases two radial waves; the second wave has a shifted gap.
public class DoomOrb extends EnemyProjectile {
    private final double burstY;
    private boolean burstDone = false;

    public DoomOrb(double x, double y, double burstY) {
        super(x, y, 0.0, 65.0, 44, new Color(190, 80, 160));
        this.burstY = burstY;
    }

    @Override
    public boolean shouldSplit() {
        return !burstDone && getBounds().y >= burstY;
    }

    @Override
    public ArrayList<EnemyProjectile> split() {
        ArrayList<EnemyProjectile> waves = new ArrayList<>();
        if (burstDone) {
            return waves;
        }
        burstDone = true;
        Rectangle b = getBounds();
        double x = b.getCenterX() - NORMAL_DIAMETER / 2.0;
        double y = b.getCenterY() - NORMAL_DIAMETER / 2.0;
        for (int ring = 0; ring < 2; ring++) {
            for (int i = 0; i < 12; i++) {
                int gapStart = ring == 0 ? 2 : 3;
                if (i >= gapStart && i <= gapStart + 2) {
                    continue;
                }
                double angle = Math.toRadians(i * 30);
                double speed = ring == 0 ? 145.0 : 165.0;
                waves.add(new DelayedProjectile(x, y,
                        Math.cos(angle) * speed, Math.sin(angle) * speed,
                        ring == 0 ? 0.0 : 0.45));
            }
        }
        return waves;
    }

    @Override
    public void draw(Graphics g) {
        draw(g,5,false);
    }
    @Override protected int visualKind(){return 2;}
    @Override protected boolean reacting(){return burstY-getBounds().y<50;}
}
