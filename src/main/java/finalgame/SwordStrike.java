package finalgame;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Line2D;

// The warning line and damaging area share exactly the same fixed geometry.
public class SwordStrike {
    private final Line2D line;
    private final Shape hitArea;
    private final Rectangle arena;
    private final double warningTime;
    private final double activeTime;
    private final float width;
    private double age = 0.0;
    private boolean hitConsumed = false;

    public SwordStrike(double x1, double y1, double x2, double y2,
            float width, double warningTime, double activeTime, Rectangle arena) {
        line = new Line2D.Double(x1, y1, x2, y2);
        this.width = width;
        this.warningTime = warningTime * CombatTuning.WARNING_TIME_MULTIPLIER;
        this.activeTime = activeTime;
        this.arena = new Rectangle(arena);
        Area area = new Area(new BasicStroke(width, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER).createStrokedShape(line));
        area.intersect(new Area(arena));
        hitArea = area;
    }

    public void update(double seconds) {
        age += seconds;
    }

    public boolean isActive() {
        return age >= warningTime && !isExpired();
    }

    public boolean isExpired() {
        return age >= warningTime + activeTime;
    }

    public boolean tryHit(Rectangle playerBounds) {
        if (!hitConsumed && isActive() && hitArea.intersects(playerBounds)) {
            hitConsumed = true;
            return true;
        }
        return false;
    }

    public void draw(Graphics g) {
        if (isExpired()) {
            return;
        }
        Graphics2D sword = (Graphics2D) g.create();
        sword.clip(arena);
        if (isActive()) {
            sword.setColor(new Color(255, 225, 130));
            sword.fill(hitArea);
            sword.setColor(Color.WHITE);
            sword.setStroke(new BasicStroke(2.0f));
            sword.draw(line);
        } else {
            // A dim full-width outline warns about the actual width, not just its centre.
            sword.setColor(new Color(85, 70, 35));
            sword.fill(hitArea);
            sword.setColor(new Color(255, 190, 60));
            sword.draw(hitArea);
            sword.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER, 10.0f, new float[]{8, 6}, 0));
            sword.draw(line);
        }
        sword.dispose();
    }
}
