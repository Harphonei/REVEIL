package finalgame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Random;

// A disciplined guardian: committed telegraphs, precise strikes and recovery windows.
public class KnightBoss extends Boss {
    @Override
    public Rectangle getBounds() {
        Rectangle base=super.getBounds();
        // The sword extends left: centre the stance (68% across the art), not the canvas.
        return new Rectangle(base.x+base.width/2-91,100,134,184);
    }
    @Override
    public void draw(Graphics graphics) {
        if(!isAlive())return;
        Graphics g=graphics.create();BossArtwork.drawKnightBattle(g,getBounds());g.dispose();
    }

    private boolean openingRain=true;
    private final RandomAttackOrder order = new RandomAttackOrder(4);
    private final AttackPattern rain = new RainPattern(new Random(),165.0,12,3);
    private final GuardWallPattern wall = new GuardWallPattern();
    private final ArrayList<SwordStrike> strikes = new ArrayList<>();
    private Rectangle arena = new Rectangle(300, 300, 360, 260);
    private Rectangle target = new Rectangle(468, 500, 24, 24);
    private int attackIndex;
    private boolean recovering = false;
    private double phaseTime;
    private double attackCooldown;
    private int thrustsCreated = 0;
    private int gridVolleys = 0;

    public KnightBoss(int startX, int startY) {
        super(startX, startY, 1800, "THE LAST GUARDIAN");
        beginAttack();
    }

    private void beginAttack() {
        attackIndex = openingRain ? 0 : order.next();
        if(openingRain){openingRain=false;order.reset(0);}
        double[] durations = {6.2, 7.5, 4.0, 5.5};
        phaseTime = durations[attackIndex];
        attackCooldown = 0.7;
        recovering = false;
        thrustsCreated = 0;
        gridVolleys = 0;
        wall.reset();
    }

    @Override
    public void update(double seconds, Rectangle battleArea,
            Rectangle playerBounds, ArrayList<EnemyProjectile> projectiles) {
        if (!isAlive()) {
            return;
        }
        arena = new Rectangle(battleArea);
        target = new Rectangle(playerBounds);
        for (int i = strikes.size() - 1; i >= 0; i--) {
            strikes.get(i).update(seconds);
            if (strikes.get(i).isExpired()) {
                strikes.remove(i);
            }
        }
        phaseTime -= seconds;
        if (phaseTime <= 0.0) {
            if (recovering) {
                beginAttack();
            } else {
                recovering = true;
                // Let the last wall reach the bottom before another attack begins.
                phaseTime = attackIndex == 3 ? 2.3 : 1.6;
            }
            return;
        }
        if (recovering) {
            return;
        }
        attackCooldown = Math.max(0.0, attackCooldown - seconds / CombatTuning.ATTACK_INTERVAL_MULTIPLIER);
        if (attackCooldown <= 0.0) {
            attack(projectiles);
            double[] intervals = {0.42, 1.10, 0.95, 1.15};
            attackCooldown = intervals[attackIndex];
        }
    }

    @Override
    public void attack(ArrayList<EnemyProjectile> projectiles) {
        if (recovering) {
            return;
        }
        switch (attackIndex) {
            case 0:
                rain.emit(arena, getBounds(), projectiles);
                break;
            case 1:
                if (gridVolleys < GuardianGridPattern.VOLLEYS) {
                    GuardianGridPattern.emit(arena, gridVolleys++, strikes);
                }
                break;
            case 2:
                if (thrustsCreated < 3) {
                    double x = Math.max(arena.x + 17,
                            Math.min(target.getCenterX(), arena.x + arena.width - 17));
                    strikes.add(new SwordStrike(x, arena.y,
                            x, arena.y + arena.height, 34.0f, 0.65, 0.22, arena));
                    thrustsCreated++;
                }
                break;
            case 3:
                wall.emit(arena, getBounds(), projectiles);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean checkHazards(Rectangle playerBounds) {
        boolean hit = false;
        for (SwordStrike strike : strikes) {
            if (strike.tryHit(playerBounds)) {
                hit = true;
            }
        }
        return hit;
    }

    @Override
    public void drawHazards(Graphics g) {
        if (isAlive()) {
            for (SwordStrike strike : strikes) {
                strike.draw(g);
            }
        }
    }

    @Override
    public String getAttackName() {
        if (recovering) {
            return "RECOVERY";
        }
        String[] names = {"RAIN", "SHIFTING GRID", "SPEAR THRUST", "GUARD WALL"};
        return names[attackIndex];
    }

    @Override
    public String getDifficultyLabel() {
        return "4.5/5";
    }

    @Override
    protected Color getBodyColor() {
        return new Color(185, 195, 215);
    }

    @Override
    public void reset() {
        super.reset();
        strikes.clear();
        openingRain=true;
        order.reset();
        beginAttack();
    }
}
