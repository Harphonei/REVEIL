package finalgame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Random;

public class MaskedBoss extends Boss {
    private static final int MAX_HP = 2200;
    private final RandomAttackOrder phaseOneOrder = new RandomAttackOrder(4);
    private final RandomAttackOrder phaseTwoOrder = new RandomAttackOrder(5);
    private final AttackPattern rain = new RainPattern(new Random(), 175.0);
    private final CrownPattern crown = new CrownPattern();
    private final ArrayList<SwordStrike> beams = new ArrayList<>();
    private final Random random = new Random();
    private Rectangle arena = new Rectangle(300, 300, 360, 260);
    private boolean phaseTwo = false;
    private double transitionTime = 0.0;
    private int attackIndex;
    private boolean recovering = false;
    private double phaseTime;
    private double elapsed;
    private double attackCooldown;
    private int judgmentWave = 0;
    private int finalStep = 0;

    public MaskedBoss(int startX, int startY) {
        super(startX, startY, MAX_HP, "THE MASKED ONE");
        beginAttack(phaseOneOrder.next());
    }

    @Override
    public void takeDamage(int damage) {
        if (transitionTime > 0.0 || damage <= 0) {
            return;
        }
        // Stop at half HP so a strong hit cannot skip the phase-two opening.
        if (!phaseTwo) {
            damage = Math.min(damage, Math.max(0, getHp() - MAX_HP / 2));
        }
        super.takeDamage(damage);
    }

    private void beginAttack(int selected) {
        attackIndex = selected;
        double[] durations = {5.2, 6.0, 6.0, 5.6, 8.6};
        phaseTime = durations[selected];
        attackCooldown = 0.7;
        elapsed = 0.0;
        recovering = false;
        judgmentWave = 0;
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
        if (!phaseTwo && getHp() <= MAX_HP / 2) {
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
                phaseTime = attackIndex == 4 ? 3.0 : 2.0;
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
        attackCooldown = Math.max(0.0, attackCooldown - seconds);
        if (attackCooldown <= 0.0) {
            attack(projectiles);
            double[] intervals = {phaseTwo ? 0.32 : 0.40,
                phaseTwo ? 0.38 : 0.48, 2.5, phaseTwo ? 1.0 : 1.2};
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
                rain.emit(arena, getBounds(), projectiles);
                break;
            case 1:
                crown.emit(arena, getBounds(), projectiles);
                break;
            case 2:
                int lane = random.nextInt(3);
                double x = arena.x + (lane + 0.5) * arena.width / 3.0 - 22;
                projectiles.add(new DoomOrb(x, arena.y - 45, arena.y + 65));
                break;
            case 3:
                createJudgmentPulse();
                break;
            default:
                break;
        }
    }

    private void createJudgmentPulse() {
        int[] lanes = {0, 2, 1};
        int lane = lanes[judgmentWave % lanes.length];
        double x = arena.x + (lane + 0.5) * arena.width / 3.0;
        double y = arena.y + 50 + (judgmentWave % 3) * 70;
        beams.add(new SwordStrike(x, arena.y, x, arena.y + arena.height,
                60.0f, 0.75, 0.22, arena));
        beams.add(new SwordStrike(arena.x, y, arena.x + arena.width, y,
                12.0f, 0.75, 0.22, arena));
        judgmentWave++;
    }

    private void updateFinalSequence(double seconds,
            ArrayList<EnemyProjectile> projectiles) {
        if (finalStep == 0 && elapsed >= 0.7) {
            shadowVolley(true, projectiles);
            finalStep = 1;
        } else if (finalStep == 1 && elapsed >= 1.6) {
            shadowVolley(false, projectiles);
            finalStep = 2;
            attackCooldown = 0.5;
        } else if (finalStep == 2 && elapsed >= 4.6) {
            // A deliberate breath before the closing beams; no random overlap here.
            projectiles.clear();
            finalStep = 3;
        } else if (finalStep == 3 && elapsed >= 5.0) {
            double left = arena.x + 60;
            double right = arena.x + arena.width - 60;
            beams.add(new SwordStrike(left, arena.y, left, arena.y + arena.height,
                    80.0f, 1.1, 0.35, arena));
            beams.add(new SwordStrike(right, arena.y, right, arena.y + arena.height,
                    80.0f, 1.1, 0.35, arena));
            finalStep = 4;
        }
        if (elapsed >= 2.6 && elapsed < 4.2) {
            attackCooldown -= seconds;
            if (attackCooldown <= 0.0) {
                rain.emit(arena, getBounds(), projectiles);
                attackCooldown = 0.65;
            }
        }
    }

    private void shadowVolley(boolean left, ArrayList<EnemyProjectile> projectiles) {
        double x = left ? arena.x + 35 : arena.x + arena.width - 35;
        for (int i = -2; i <= 2; i++) {
            double angle = Math.toRadians((left ? 12 : -12) + i * 12);
            projectiles.add(new EnemyProjectile(x - 6, arena.y - 20,
                    Math.sin(angle) * 160, Math.cos(angle) * 160));
        }
    }

    @Override
    public boolean checkHazards(Rectangle playerBounds) {
        boolean hit = false;
        for (SwordStrike beam : beams) {
            if (beam.tryHit(playerBounds)) {
                hit = true;
            }
        }
        return hit;
    }

    @Override
    public void drawHazards(Graphics g) {
        if (!isAlive()) {
            return;
        }
        if (!recovering && transitionTime <= 0 && (attackIndex == 1 || attackIndex == 4)) {
            g.setColor(new Color(180, 100, 180));
            for (int i = 0; i < 5; i++) {
                g.drawOval((int) CrownPattern.emitterX(arena, i) - 9,
                        (int) CrownPattern.emitterY(arena, i) - 9, 18, 18);
            }
            if (attackIndex == 4) {
                g.setColor(Color.LIGHT_GRAY);
                g.drawOval(arena.x + 23, arena.y - 39, 24, 30);
                g.drawOval(arena.x + arena.width - 47, arena.y - 39, 24, 30);
            }
        }
        for (SwordStrike beam : beams) {
            beam.draw(g);
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
        beginAttack(phaseOneOrder.next());
    }
}
