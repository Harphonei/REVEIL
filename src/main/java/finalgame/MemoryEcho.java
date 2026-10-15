package finalgame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;

public class MemoryEcho {
    private final Rectangle bounds;
    private double age = 0.0;
    private boolean released = false;

    public MemoryEcho(Rectangle playerBounds, Rectangle arena) {
        int size = 36;
        int x = playerBounds.x + playerBounds.width / 2 - size / 2;
        int y = playerBounds.y + playerBounds.height / 2 - size / 2;
        x = Math.max(arena.x, Math.min(x, arena.x + arena.width - size));
        y = Math.max(arena.y, Math.min(y, arena.y + arena.height - size));
        bounds = new Rectangle(x, y, size, size);
    }

    public void update(double seconds) {
        age += seconds;
    }

    public boolean isReady() {
        return age >= 1.05 && !released;
    }

    public void release(ArrayList<EnemyProjectile> projectiles) {
        if (!isReady()) {
            return;
        }
        released = true;
        double x = bounds.getCenterX() - EnemyProjectile.NORMAL_DIAMETER / 2.0;
        double y = bounds.getCenterY() - EnemyProjectile.NORMAL_DIAMETER / 2.0;
        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(i * 60);
            projectiles.add(new EnemyProjectile(x, y,
                    Math.cos(angle) * 115.0, Math.sin(angle) * 115.0));
        }
    }

    public void draw(Graphics g) {
        g.setColor(new Color(210, 140, 255));
        g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
        g.drawRect(bounds.x + 3, bounds.y + 3, bounds.width - 6, bounds.height - 6);
        g.drawString("!", bounds.x + 14, bounds.y + 24);
    }
}
