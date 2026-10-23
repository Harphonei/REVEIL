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
    private final Shape line;
    private final Shape hitArea;
    private final Rectangle arena;
    private final double warningTime;
    private final double activeTime;
    private final float width;
    private double age = 0.0;
    private boolean hitConsumed = false;

    public SwordStrike(double x1, double y1, double x2, double y2,
            float width, double warningTime, double activeTime, Rectangle arena) {
        this(new Line2D.Double(x1,y1,x2,y2),width,warningTime,activeTime,arena);
    }

    public SwordStrike(Shape shape,float width,double warningTime,
            double activeTime,Rectangle arena) {
        line = shape;
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
        draw(g,4);
    }
    public void draw(Graphics g,int theme) {
        if (isExpired()) {
            return;
        }
        BossEffects.strike(g,hitArea,line,arena,theme,isActive(),Math.min(1,age/warningTime));
    }
}
