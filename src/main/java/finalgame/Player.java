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
    private double effectTime,shotFlash,eReleaseFlash,qBurstTime,qGuardTime;

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
    private double storyQAura=-1;
    public void setStoryQAura(double progress){storyQAura=progress;}
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
        effectTime+=seconds;
        shotFlash=Math.max(0,shotFlash-seconds);
        eReleaseFlash=Math.max(0,eReleaseFlash-seconds);
        qBurstTime=Math.max(0,qBurstTime-seconds);
        qGuardTime=Math.max(0,qGuardTime-seconds);
        invincibleTime = Math.max(0.0, invincibleTime - seconds);
        skillECooldown = Math.max(0.0, skillECooldown - seconds);
        skillQCooldown = Math.max(0.0, skillQCooldown - seconds);
        skillRecoveryTime = Math.max(0.0, skillRecoveryTime - seconds);
        double ultimateBefore=ultimateTime;
        ultimateTime = Math.max(0.0, ultimateTime - seconds);
        if(ultimateBefore>0&&ultimateTime<=0)
            skillQCooldown=Math.max(0,SKILL_Q_COOLDOWN-Math.max(0,seconds-ultimateBefore));

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
                if(ultimateTime<=0)skillQCooldown=Math.max(0,SKILL_Q_COOLDOWN-(overshoot-SKILL_Q_DURATION));
                qBurstTime=Math.max(0,.42-overshoot);
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
        shotFlash=.12;
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
        if(isAlive())eReleaseFlash=.24;
        return isAlive() ? new SkillProjectile(x + size / 2.0, y) : null;
    }

    /** The demonstration cast must not consume the player's first manual cast. */
    public void resetSkillEAfterTutorial(){
        skillECooldown=0;skillEChargeTime=0;skillRecoveryTime=0;skillEReadyToFire=false;
    }

    public double getSkillECooldown() {
        return skillECooldown;
    }

    public double getSkillEChargeTime() {
        return skillEChargeTime;
    }

    /** The discovery scene already performed the charge; begin the full effect now. */
    public void activateStoryUltimate(){
        storyQAura=-1;
        if(!isAlive())return;
        skillEChargeTime=0;skillEReadyToFire=false;skillRecoveryTime=0;
        skillQChargeTime=0;ultimateTime=SKILL_Q_DURATION;skillQCooldown=0;
        invincibleTime=Math.max(invincibleTime,1.5);
        qGuardTime=1.5;qBurstTime=.42;
    }

    public boolean useSkillQ() {
        if (!isAlive() || isUsingSkill() || skillQCooldown > 0.0) {
            return false;
        }
        skillQChargeTime = SKILL_Q_CHARGE;
        skillQCooldown = 0;
        invincibleTime=Math.max(invincibleTime,1.5);
        qGuardTime=1.5;
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

    public double getUltimateProgress(){return ultimateTime/SKILL_Q_DURATION;}
    public double getSkillEChargeProgress(){return skillEChargeTime>0?1-skillEChargeTime/SKILL_E_CHARGE:0;}
    public double getSkillQChargeProgress(){return skillQChargeTime>0?1-skillQChargeTime/SKILL_Q_CHARGE:0;}

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
        storyQAura=-1;
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
        effectTime=0;shotFlash=0;eReleaseFlash=0;qBurstTime=0;qGuardTime=0;
    }

    private static final String[] HEART = {
        "001110011100", "011111111110", "111111111111",
        "111111111111", "111111111111", "011111111110",
        "001111111100", "000111111000", "000011110000",
        "000001100000", "000001100000", "000000000000"
    };

    public void draw(Graphics g) {
        int cx=(int)x+size/2,cy=(int)y+size/2;
        if (isAlive() && (skillQChargeTime > 0.0||storyQAura>=0)) {
            double progress = storyQAura>=0?storyQAura:1.0 - skillQChargeTime / SKILL_Q_CHARGE;
            HeroEffects.chargeQ(g,cx,cy,progress);
        }
        if (isAlive() && skillEChargeTime > 0.0) {
            double progress = 1.0 - skillEChargeTime / SKILL_E_CHARGE;
            HeroEffects.chargeE(g,cx,(int)y-8,progress);
        }
        if (isAlive() && isUltimateActive()) {
            HeroEffects.aura(g,cx,cy,effectTime,ultimateTime,qGuardTime>0);
        }
        if(isAlive()&&eReleaseFlash>0)HeroEffects.burst(g,cx,(int)y-8,1-eReleaseFlash/.24,true);
        if(isAlive()&&qBurstTime>0)HeroEffects.burst(g,cx,cy,1-qBurstTime/.42,false);
        if(isAlive()&&shotFlash>0)HeroEffects.muzzle(g,cx,(int)y-7,1-shotFlash/.12);
        g.setColor(qGuardTime>0?new Color(244,105,111):invincibleTime>0.0 ? new Color(255,120,120) : new Color(235,35,50));
        for(int row=0;row<HEART.length;row++)
            for(int col=0;col<HEART[row].length();col++)
                if(HEART[row].charAt(col)=='1') g.fillRect((int)x+col*2,(int)y+row*2,2,2);
    }
}
