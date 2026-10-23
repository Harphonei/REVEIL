package finalgame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class SkillProjectile extends Projectile {
    private boolean aimed;
    private double aimedX,aimedY,velocityX,velocityY;
    public SkillProjectile(double centerX, double startY) {
        super(centerX - 16, startY - 32,
                32, 32, 190.0, 40, Color.MAGENTA);
    }

    /** Scripted discovery shot uses the same artwork and damage, aimed at the boss. */
    public SkillProjectile(double centerX,double startY,double targetX,double targetY){
        this(centerX,startY);
        aimed=true;aimedX=centerX-16;aimedY=startY-32;
        velocityX=(targetX-centerX)/.9;
        velocityY=(targetY-(startY-16))/.9;
    }

    @Override public void update(double seconds){
        if(aimed){effectAge+=seconds;aimedX+=velocityX*seconds;aimedY+=velocityY*seconds;}
        else super.update(seconds);
    }

    @Override public Rectangle getBounds(){
        return aimed?new Rectangle((int)aimedX,(int)aimedY,32,32):super.getBounds();
    }

    @Override
    public void draw(Graphics g) {
        Rectangle bounds = getBounds();
        Graphics2D pixels=(Graphics2D)g.create();
        if(aimed)pixels.rotate(Math.atan2(velocityX,-velocityY),bounds.getCenterX(),bounds.getCenterY());
        HeroEffects.heavy(pixels,bounds,effectAge);pixels.dispose();
    }
}
