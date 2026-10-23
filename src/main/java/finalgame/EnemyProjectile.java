package finalgame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

public class EnemyProjectile {
    protected double x;
    protected double y;
    public static final int NORMAL_DIAMETER = 12;
    private final int diameter;
    private final Color color;
    private final double velocityX;
    private final double velocityY;

    public EnemyProjectile(double startX, double startY) {
        this(startX, startY, 0.0, 160.0);
    }

    public EnemyProjectile(double startX, double startY,
            double velocityX, double velocityY) {
        this(startX, startY, velocityX, velocityY, NORMAL_DIAMETER, Color.ORANGE);
    }

    protected EnemyProjectile(double startX, double startY,
            double velocityX, double velocityY, int diameter, Color color) {
        x = startX;
        y = startY;
        this.velocityX = velocityX * CombatTuning.ENEMY_SPEED_MULTIPLIER;
        this.velocityY = velocityY * CombatTuning.ENEMY_SPEED_MULTIPLIER;
        this.diameter = diameter;
        this.color = color;
    }

    public void update(double seconds) {
        x += velocityX * seconds;
        y += velocityY * seconds;
    }

    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, diameter, diameter);
    }

    public boolean isOutsideScreen(int screenWidth, int screenHeight) {
        return x + diameter < 0 || x > screenWidth
                || y + diameter < 0 || y > screenHeight;
    }

    public void draw(Graphics g) {
        draw(g,1,false);
    }

    public void draw(Graphics g,int theme,boolean phaseTwo){
        if(isDangerous())BossEffects.projectile(g,getBounds(),theme,phaseTwo,visualKind(),reacting());
    }
    protected int visualKind(){return 0;}
    protected boolean reacting(){return false;}

    public boolean shouldSplit() {
        return false;
    }

    public boolean isDangerous() {
        return true;
    }

    public java.util.ArrayList<EnemyProjectile> split() {
        return new java.util.ArrayList<>();
    }
}
