package finalgame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Random;

public class MaskedBoss extends Boss {
    @Override
    public Rectangle getBounds() {
        Rectangle base=super.getBounds();
        return new Rectangle(base.x+base.width/2-140,48,240,240);
    }
    @Override
    public void draw(Graphics graphics) {
        if(!isAlive())return;
        Graphics g=graphics.create();BossArtwork.drawMaskedBattle(g,getBounds(),phaseTwo,transitionTime);g.dispose();
    }
    private static final int MAX_HP = 1800;
    private final RandomAttackOrder phaseOneOrder = new RandomAttackOrder(4);
    private final RandomAttackOrder phaseTwoOrder = new RandomAttackOrder(5);
    private final CrownPattern crown = new CrownPattern(155.0);
    private final ArrayList<SwordStrike> beams = new ArrayList<>();
    private Rectangle arena = new Rectangle(300, 300, 360, 260);
    private boolean phaseTwo = false;
    private boolean storyControlled;
    public void setStoryControlled(boolean value){storyControlled=value;}
    public boolean awaitingStoryPhase(){return !phaseTwo&&getHp()<=getMaxHp()/2;}
    public void clearStoryHazards(){beams.clear();crown.reset();}
    public void showStoryPhase(double progress){phaseTwo=true;transitionTime=1.1*(1-Math.min(1,progress));}
    public void finishStoryPhase(){phaseTwo=true;transitionTime=0;clearStoryHazards();phaseTwoOrder.reset(4);beginAttack(4);}
    private double transitionTime = 0.0;
    private int attackIndex;
    private boolean recovering = false;
    private double phaseTime;
    private double elapsed;
    private double attackCooldown;
    private int judgmentWave = 0;
    private int finalStep = 0;
    private final Random random=new Random();
    private final RandomAttackOrder judgmentOrder=new RandomAttackOrder(4);
    private final ArrayList<Integer> judgmentHistory=new ArrayList<>();
    private Rectangle target=new Rectangle(468,500,16,16);
    private int rainWave;
    private int orbWave;

    public MaskedBoss(int startX, int startY) {
        super(startX, startY, MAX_HP, "THE MASKED ONE");
        phaseOneOrder.reset(0);
        beginAttack(0);
    }

    @Override
    public void takeDamage(int damage) {
        if (transitionTime > 0.0 || damage <= 0) {
            return;
        }
        // Stop at half HP so a strong hit cannot skip the phase-two opening.
        if (!phaseTwo) {
            damage = Math.min(damage, Math.max(0, getHp() - getMaxHp() / 2));
        }
        super.takeDamage(damage);
    }

    private void beginAttack(int selected) {
        attackIndex = selected;
        double[] durations = {6.4, 6.0, 7.2, 10.2, 10.0};
        phaseTime = durations[selected];
        attackCooldown = 0.7;
        elapsed = 0.0;
        recovering = false;
        judgmentWave = 0;
        judgmentOrder.reset();
        judgmentHistory.clear();
        rainWave = 0;
        orbWave = 0;
        finalStep = 0;
        crown.reset();
    }

    @Override
    public void update(double seconds, Rectangle battleArea,
            Rectangle playerBounds, ArrayList<EnemyProjectile> projectiles) {
        if (!isAlive()) {
            return;
        }
        arena = new Rectangle(battleArea);
        target = new Rectangle(playerBounds);
        if (!phaseTwo && getHp() <= getMaxHp() / 2) {
            if(storyControlled)return;
            phaseTwo = true;
            transitionTime = 1.1;
            beams.clear();
            projectiles.clear();
            phaseTwoOrder.reset(4);
            return;
        }
        if (transitionTime > 0.0) {
            transitionTime = Math.max(0.0, transitionTime - seconds);
            if (transitionTime <= 0.0) {
                beginAttack(4);
            }
            return;
        }

        for (int i = beams.size() - 1; i >= 0; i--) {
            beams.get(i).update(seconds);
            if (beams.get(i).isExpired()) {
                beams.remove(i);
            }
        }
        phaseTime -= seconds;
        elapsed += seconds;
        if (phaseTime <= 0.0) {
            if (recovering) {
                beginAttack(phaseTwo ? phaseTwoOrder.next() : phaseOneOrder.next());
            } else {
                recovering = true;
                phaseTime = attackIndex == 4 ? 3.3 : 2.3;
            }
            return;
        }
        if (recovering) {
            return;
        }
        if (attackIndex == 4) {
            updateFinalSequence(seconds, projectiles);
            return;
        }
        attackCooldown = Math.max(0.0, attackCooldown - seconds / CombatTuning.ATTACK_INTERVAL_MULTIPLIER);
        if (attackCooldown <= 0.0) {
            attack(projectiles);
            double[] intervals = {phaseTwo ? 0.30 : 0.37,
                phaseTwo ? 0.45 : 0.56, phaseTwo ? 2.05 : 2.4, phaseTwo ? 1.12 : 1.28};
            attackCooldown = intervals[attackIndex];
        }
    }

    @Override
    public void attack(ArrayList<EnemyProjectile> projectiles) {
        if (recovering || transitionTime > 0.0) {
            return;
        }
        switch (attackIndex) {
            case 0:
                emitBlackRain(projectiles);
                break;
            case 1:
                crown.emit(arena, getBounds(), projectiles);
                break;
            case 2:
                int lane = new int[]{1, 0, 2}[orbWave % 3];
                double x = arena.x + (lane + 0.5) * arena.width / 3.0 - 22;
                projectiles.add(new MaskedDoomOrb(x, arena.y - 45,
                        arena.y + 65, orbWave++, phaseTwo));
                break;
            case 3:
                createJudgmentPulse();
                break;
            default:
                break;
        }
    }

    private void emitBlackRain(ArrayList<EnemyProjectile> projectiles) {
        // Original straight, scattered rainfall with more occupied columns and shorter intervals.
        int columns=12,count=phaseTwo?4:3;
        ArrayList<Integer> slots=new ArrayList<>();
        for(int i=0;i<columns;i++)slots.add(i);
        java.util.Collections.shuffle(slots,random);
        for(int i=0;i<count;i++) {
            double x=arena.x+(slots.get(i)+0.5)*arena.width/columns-6
                    +(random.nextDouble()-0.5)*8;
            projectiles.add(new EnemyProjectile(x,arena.y-40,0,phaseTwo?180:165));
        }
        rainWave++;
    }

    private void createJudgmentPulse() {
        if(judgmentWave>=8)return;
        int shape=judgmentOrder.next();
        judgmentHistory.add(shape);
        addJudgmentShape(shape,judgmentWave++);
    }

    private void addJudgmentShape(int shape,int wave) {
        double warning=phaseTwo?0.68:0.78;
        float stroke=phaseTwo?20:16;
        if(shape==0) {
            double shift=(wave%2)*41;
            for(double x=arena.x-41+shift;x<=arena.getMaxX()+41;x+=82)
                beams.add(new SwordStrike(x,arena.y,x,arena.getMaxY(),stroke,warning,.20,arena).withDamage(attackIndex==4?14:18));
        } else if(shape==1) {
            double slope=(wave%2)==0?1:-1;
            for(double x=arena.x-arena.height-110;x<=arena.getMaxX()+arena.height+110;x+=110)
                beams.add(new SwordStrike(x+wave*13,arena.y,
                        x+wave*13+slope*arena.height,arena.getMaxY(),stroke,warning,.20,arena).withDamage(attackIndex==4?14:18));
        } else if(shape==2) {
            for(int inset:new int[]{26,88})
                beams.add(new SwordStrike(new java.awt.geom.Rectangle2D.Double(
                        arena.x+inset,arena.y+inset,arena.width-2*inset,arena.height-2*inset),
                        stroke,warning,.20,arena).withDamage(attackIndex==4?14:18));
        } else {
            double cx=arena.getCenterX()+((wave%2)==0?-14:14),cy=arena.getCenterY();
            for(int radius:new int[]{48,110,172})
                beams.add(new SwordStrike(new java.awt.geom.Ellipse2D.Double(
                        cx-radius,cy-radius,radius*2,radius*2),stroke,warning,.20,arena).withDamage(attackIndex==4?14:18));
        }
    }

    private void updateFinalSequence(double seconds,
            ArrayList<EnemyProjectile> projectiles) {
        // Alternating side fans, dense rain and committed aimed beams force continuous movement.
        if(elapsed<4.8) {
            attackCooldown-=seconds;
            if(attackCooldown<=0) {
                shadowVolley((finalStep&1)==0,projectiles);
                emitBlackRain(projectiles);
                if((finalStep&1)==0) {
                    double x=Math.max(arena.x+20,Math.min(target.getCenterX(),arena.getMaxX()-20));
                    beams.add(new SwordStrike(x,arena.y,x,arena.getMaxY(),
                            26,.90,.22,arena).withDamage(14));
                }
                finalStep++;
                attackCooldown=.76;
            }
        } else if(finalStep<100) {
            projectiles.clear();
            finalStep=100;
            attackCooldown=.80;
        } else if(finalStep<103) {
            attackCooldown-=seconds;
            if(attackCooldown<=0) {
                addJudgmentShape(judgmentOrder.next(),finalStep-100);
                finalStep++;
                attackCooldown=1.34;
            }
        }
    }

    private void shadowVolley(boolean left,ArrayList<EnemyProjectile> projectiles) {
        double x=left?arena.x+12:arena.getMaxX()-12;
        double y=arena.y-18;
        double aim=Math.atan2(target.getCenterY()-y,target.getCenterX()-x);
        for(int i=-2;i<=2;i++) {
            double angle=aim+i*.14;
            projectiles.add(new EnemyProjectile(x-6,y-6,
                    Math.cos(angle)*160,Math.sin(angle)*160,14));
        }
    }

    @Override
    public int getHazardDamage(Rectangle playerBounds) {
        int damage = 0;
        for (SwordStrike beam : beams) {
            if (beam.tryHit(playerBounds)) {
                damage = Math.max(damage,beam.getDamage());
            }
        }
        return damage;
    }

    @Override
    public void drawHazards(Graphics g) {
        if (!isAlive()) {
            return;
        }
        if (!recovering && transitionTime <= 0 && attackIndex == 1) {
            for (int i = 0; i < 5; i++) {
                BossEffects.seal(g,(int)CrownPattern.emitterX(arena,i),(int)CrownPattern.emitterY(arena,i),10,5);
            }
        }
        for (SwordStrike beam : beams) {
            beam.draw(g,5);
        }
    }

    @Override
    public String getAttackName() {
        if (transitionTime > 0.0) {
            return "AWAKENING";
        }
        if (recovering) {
            return "RECOVERY";
        }
        String[] names = {"BLACK RAIN", "END CROWN", "DOOM ORB", "JUDGMENT", "FINAL LOOP"};
        return names[attackIndex];
    }

    @Override
    public String getPhaseLabel() {
        return phaseTwo ? "PHASE 2" : "PHASE 1";
    }

    @Override
    public String getDifficultyLabel() {
        return "5/5";
    }

    @Override
    protected Color getBodyColor() {
        return phaseTwo ? new Color(120, 65, 110) : new Color(85, 85, 100);
    }

    @Override
    public void reset() {
        super.reset();
        beams.clear();
        crown.reset();
        phaseOneOrder.reset();
        phaseTwoOrder.reset();
        phaseTwo = false;
        transitionTime = 0.0;
        phaseOneOrder.reset(0);
        beginAttack(0);
    }
}
