package finalgame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

public class Player {
    private final SpriteAnimator animation = new SpriteAnimator();
    private double x;
    private double y;
    private final int size = 24;
    private final double speed = 180.0;
    private final int maxHp = 100;
    private int hp = maxHp;
    private double invincibleTime = 0.0;

    private static final double SKILL_E_COOLDOWN = 5.0;
    private static final double SKILL_E_CHARGE = 0.55;
    private static final double SKILL_E_RECOVERY = 0.20;
    private double skillECooldown = 0.0;
    private double skillEChargeTime = 0.0;
    private double skillRecoveryTime = 0.0;
    private boolean skillEReadyToFire = false;

    private static final double SKILL_Q_DURATION = 4.0;
    private static final double SKILL_Q_COOLDOWN = 15.0;
    private static final double SKILL_Q_CHARGE = 0.65;
    private static final double ULTIMATE_SPEED_MULTIPLIER = 1.2;
    private double ultimateTime = 0.0;
    private double skillQCooldown = 0.0;
    private double skillQChargeTime = 0.0;

    public Player(double startX, double startY) {
        animation.reset(SpriteAnimator.UP);
        x = startX;
        y = startY;
    }

    public void move(int directionX, int directionY, double seconds,
            Rectangle movementArea) {
        double beforeX=x,beforeY=y;
        double distance = Math.hypot(directionX, directionY);
        double currentSpeed = speed;
        if (isUltimateActive()) {
            currentSpeed = speed * ULTIMATE_SPEED_MULTIPLIER;
        }
        if (distance > 0) {
            x += directionX / distance * currentSpeed * seconds;
            y += directionY / distance * currentSpeed * seconds;
        }
        double minX = movementArea.x;
        double minY = movementArea.y;
        double maxX = movementArea.x + movementArea.width - size;
        double maxY = movementArea.y + movementArea.height - size;
        x = Math.max(minX, Math.min(x, maxX));
        y = Math.max(minY, Math.min(y, maxY));
        animation.update(x-beforeX,y-beforeY,seconds,false);
    }

    public void update(double seconds) {
        invincibleTime = Math.max(0.0, invincibleTime - seconds);
        skillECooldown = Math.max(0.0, skillECooldown - seconds);
        skillQCooldown = Math.max(0.0, skillQCooldown - seconds);
        skillRecoveryTime = Math.max(0.0, skillRecoveryTime - seconds);
        ultimateTime = Math.max(0.0, ultimateTime - seconds);

        if (skillEChargeTime > 0.0) {
            double before = skillEChargeTime;
            skillEChargeTime = Math.max(0.0, skillEChargeTime - seconds);
            if (skillEChargeTime <= 0.0 && isAlive()) {
                skillEReadyToFire = true;
                double overshoot = Math.max(0.0, seconds - before);
                skillRecoveryTime = Math.max(0.0, SKILL_E_RECOVERY - overshoot);
            }
        }
        if (skillQChargeTime > 0.0) {
            double before = skillQChargeTime;
            skillQChargeTime = Math.max(0.0, skillQChargeTime - seconds);
            if (skillQChargeTime <= 0.0 && isAlive()) {
                double overshoot = Math.max(0.0, seconds - before);
                ultimateTime = Math.max(0.0, SKILL_Q_DURATION - overshoot);
            }
        }
    }

    public boolean isUsingSkill() {
        return skillEChargeTime > 0.0 || skillEReadyToFire
                || skillRecoveryTime > 0.0 || skillQChargeTime > 0.0
                || isUltimateActive();
    }

    public Projectile shoot() {
        if (!isAlive() || isUsingSkill()) {
            return null;
        }
        return new Projectile(x + size / 2.0 - 3, y - 12);
    }

    public boolean useSkillE() {
        if (!isAlive() || isUsingSkill() || skillECooldown > 0.0) {
            return false;
        }
        skillECooldown = SKILL_E_COOLDOWN;
        skillEChargeTime = SKILL_E_CHARGE;
        return true;
    }

    // Consume the completed cast once, after movement, using the current position.
    public Projectile pollSkillProjectile() {
        if (!skillEReadyToFire) {
            return null;
        }
        skillEReadyToFire = false;
        return isAlive() ? new SkillProjectile(x + size / 2.0, y) : null;
    }

    public double getSkillECooldown() {
        return skillECooldown;
    }

    public double getSkillEChargeTime() {
        return skillEChargeTime;
    }

    public boolean useSkillQ() {
        if (!isAlive() || isUsingSkill() || skillQCooldown > 0.0) {
            return false;
        }
        skillQChargeTime = SKILL_Q_CHARGE;
        skillQCooldown = SKILL_Q_COOLDOWN;
        return true;
    }

    public double getSkillQChargeTime() {
        return skillQChargeTime;
    }

    public boolean isUltimateActive() {
        return ultimateTime > 0.0;
    }

    public double getUltimateTime() {
        return ultimateTime;
    }

    public double getSkillQCooldown() {
        return skillQCooldown;
    }

    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, size, size);
    }

    public Rectangle getLaserBounds() {
        int laserWidth = 12;
        int laserX = (int) (x + size / 2.0 - laserWidth / 2.0);
        int laserHeight = Math.max(0, (int) y);
        return new Rectangle(laserX, 0, laserWidth, laserHeight);
    }

    /** Visual reaction for the scripted hit; does not add hidden HP damage. */
    public void showHitReaction(){invincibleTime=Math.max(invincibleTime,.75);}

    public void takeDamage(int damage) {
        if (damage > 0 && isAlive() && invincibleTime <= 0.0) {
            hp = Math.max(0, hp - damage);
            invincibleTime = 0.75;
            if (!isAlive()) {
                skillEChargeTime = 0.0;
                skillEReadyToFire = false;
                skillRecoveryTime = 0.0;
                skillQChargeTime = 0.0;
                ultimateTime = 0.0;
            }
        }
    }

    public boolean isAlive() {
        return hp > 0;
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public void reset(double startX, double startY) {
        animation.reset(SpriteAnimator.UP);
        x = startX;
        y = startY;
        hp = maxHp;
        invincibleTime = 0.0;
        skillECooldown = 0.0;
        skillEChargeTime = 0.0;
        skillRecoveryTime = 0.0;
        skillEReadyToFire = false;
        ultimateTime = 0.0;
        skillQCooldown = 0.0;
        skillQChargeTime = 0.0;
    }

    private static final String[] HEART = {
        "001110011100", "011111111110", "111111111111",
        "111111111111", "111111111111", "011111111110",
        "001111111100", "000111111000", "000011110000",
        "000001100000", "000001100000", "000000000000"
    };

    public void draw(Graphics g) {
        if (isAlive() && skillQChargeTime > 0.0) {
            // Placeholder activation aura; replace with sprite animation later.
            double progress = 1.0 - skillQChargeTime / SKILL_Q_CHARGE;
            int padding = 7 + (int) (progress * 12);
            g.setColor(new Color(255, 190, 30));
            g.drawOval((int) x - padding, (int) y - padding,
                    size + padding * 2, size + padding * 2);
            g.setColor(Color.YELLOW);
            g.drawOval((int) x - padding - 4, (int) y - padding - 4,
                    size + padding * 2 + 8, size + padding * 2 + 8);
        }
        if (isAlive() && skillEChargeTime > 0.0) {
            double progress = 1.0 - skillEChargeTime / SKILL_E_CHARGE;
            int diameter = 6 + (int) (progress * 16);
            g.setColor(Color.MAGENTA);
            g.fillOval((int) (x + size / 2.0 - diameter / 2.0),
                    (int) y - diameter - 5, diameter, diameter);
        }
        if (isAlive() && isUltimateActive()) {
            g.setColor(Color.YELLOW);
            g.drawRect((int) x - 3, (int) y - 3, size + 6, size + 6);
        }
        g.setColor(invincibleTime>0.0 ? new Color(255,120,120) : new Color(235,35,50));
        for(int row=0;row<HEART.length;row++)
            for(int col=0;col<HEART[row].length();col++)
                if(HEART[row].charAt(col)=='1') g.fillRect((int)x+col*2,(int)y+row*2,2,2);
    }
}
