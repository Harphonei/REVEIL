package finalgame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

public class Boss {
    private final int x;
    private final int y;
    private final int width = 100;
    private final int height = 60;
    private final int maxHp = 1000;
    private int hp;

    public Boss(int startX, int startY) {
        x = startX;
        y = startY;
        hp = maxHp;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public void takeDamage(int damage) {
        if (damage > 0) {
            hp = Math.max(0, hp - damage);
        }
    }

    public boolean isAlive() {
        return hp > 0;
    }

    public void draw(Graphics g) {
        if (!isAlive()) {
            return;
        }

        g.setColor(Color.RED);
        g.fillRect(x, y, width, height);

        g.setColor(Color.DARK_GRAY);
        g.fillRect(x, y - 20, width, 8);

        int hpBarWidth = width * hp / maxHp;
        g.setColor(Color.GREEN);
        g.fillRect(x, y - 20, hpBarWidth, 8);

        g.setColor(Color.WHITE);
        g.drawString("HP: " + hp + "/" + maxHp, x, y - 30);
    }
    public EnemyProjectile shoot() {
        return new EnemyProjectile(
            x + width / 2.0 - 5,
            y + height
        );
    }  
}
