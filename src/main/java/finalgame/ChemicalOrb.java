package finalgame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;

// Large slow reagent splits once, at a fixed depth in the arena.
public class ChemicalOrb extends EnemyProjectile {
    private final double splitY;
    private boolean splitDone = false;

    public ChemicalOrb(double startX, double startY, double splitY) {
        super(startX, startY, 0.0, 85.0, 28, Color.MAGENTA);
        withDamage(16);
        this.splitY = splitY;
    }

    @Override
    public boolean shouldSplit() {
        return !splitDone && getBounds().y >= splitY;
    }

    @Override
    public ArrayList<EnemyProjectile> split() {
        ArrayList<EnemyProjectile> fragments = new ArrayList<>();
        if (splitDone) {
            return fragments;
        }
        splitDone = true;
        Rectangle bounds = getBounds();
        double x = bounds.getCenterX() - NORMAL_DIAMETER / 2.0;
        double y = bounds.getCenterY() - NORMAL_DIAMETER / 2.0;
        for (int i = 0; i < 8; i++) {
            double angle = Math.toRadians(i * 45);
            fragments.add(new EnemyProjectile(x, y,
                    Math.cos(angle) * 105.0, Math.sin(angle) * 105.0,8));
        }
        return fragments;
    }

    @Override
    public void draw(Graphics g) {
        draw(g,2,false);
    }
    @Override protected int visualKind(){return 1;}
    @Override protected boolean reacting(){return splitY-getBounds().y<=45;}
}
