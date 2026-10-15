package finalgame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

public class SkillProjectile extends Projectile {
    public SkillProjectile(double centerX, double startY) {
        super(centerX - 16, startY - 32,
                32, 32, 190.0, 30, Color.MAGENTA);
    }

    @Override
    public void draw(Graphics g) {
        Rectangle bounds = getBounds();
        g.setColor(Color.MAGENTA);
        g.fillOval(bounds.x, bounds.y, bounds.width, bounds.height);
    }
}
