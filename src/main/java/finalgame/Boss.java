package finalgame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;

public abstract class Boss {
    private final int x;
    private final int y;
    private final int width = 100;
    private final int height = 60;
    private final int maxHp;
    private final String name;
    private int hp;
    private double shotCooldown = 1.0;

    public Boss(int startX, int startY) {
        this(startX, startY, 100, "TRAINING BOSS");
    }

    protected Boss(int startX, int startY, int maxHp, String name) {
        x = startX;
        y = startY;
        this.maxHp = maxHp;
        this.name = name;
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

    public int getHp() {
        return hp;
    }

    public EnemyProjectile shoot() {
        return new EnemyProjectile(x + width / 2.0
                - EnemyProjectile.NORMAL_DIAMETER / 2.0, y + height);
    }

    public abstract void attack(ArrayList<EnemyProjectile> projectiles);

    public void update(double seconds, Rectangle battleArea,
            Rectangle playerBounds, ArrayList<EnemyProjectile> projectiles) {
        if (!isAlive()) {
            return;
        }
        shotCooldown = Math.max(0.0, shotCooldown - seconds);
        if (shotCooldown <= 0.0) {
            attack(projectiles);
            shotCooldown = 1.0;
        }
    }

    public boolean checkHazards(Rectangle playerBounds) {
        return false;
    }

    public void drawHazards(Graphics g) {
        // Most bosses have no floor hazards.
    }

    public String getAttackName() {
        return "STRAIGHT / SPREAD";
    }

    public String getDifficultyLabel() {
        return "1/5";
    }

    protected Color getBodyColor() {
        return Color.RED;
    }

    public void reset() {
        hp = maxHp;
        shotCooldown = 1.0;
    }

    public void draw(Graphics g) {
        if (!isAlive()) {
            return;
        }
        g.setColor(Color.WHITE);
        g.drawString(name, x - 45, y - 60);
        g.setColor(getBodyColor());
        g.fillRect(x, y, width, height);
        g.setColor(Color.DARK_GRAY);
        g.fillRect(x, y - 20, width, 8);
        int hpBarWidth = width * hp / maxHp;
        g.setColor(Color.GREEN);
        g.fillRect(x, y - 20, hpBarWidth, 8);
        g.setColor(Color.WHITE);
        g.drawString("HP: " + hp + "/" + maxHp, x - 10, y - 30);
    }
}
