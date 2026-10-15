package finalgame;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;

// A frenzied monster: the bodies in the forest are its victims.
public class ForestBoss extends Boss {
    private enum Phase {
        RAIN, REST_AFTER_RAIN, CLAWS, REST_AFTER_CLAWS, ROOTS, REST_AFTER_ROOTS
    }

    private final AttackPattern rain = new RainPattern();
    private final SpreadPattern claws = new SpreadPattern();
    private final ArrayList<RootHazard> roots = new ArrayList<>();
    private Phase phase = Phase.RAIN;
    private double phaseTime = 5.2;
    private double attackCooldown = 0.7;
    private int rootsCreated = 0;
    private Rectangle arena = new Rectangle(300, 300, 360, 260);

    public ForestBoss(int startX, int startY) {
        super(startX, startY, 1200, "FRENZIED FOREST BEAST");
    }

    @Override
    public void update(double seconds, Rectangle battleArea,
            Rectangle playerBounds, ArrayList<EnemyProjectile> projectiles) {
        if (!isAlive()) {
            return;
        }
        arena = new Rectangle(battleArea);
        for (int i = roots.size() - 1; i >= 0; i--) {
            roots.get(i).update(seconds);
            if (roots.get(i).isExpired()) {
                roots.remove(i);
            }
        }

        phaseTime -= seconds;
        if (phaseTime <= 0.0) {
            nextPhase();
            return;
        }
        attackCooldown = Math.max(0.0, attackCooldown - seconds);
        if (attackCooldown > 0.0) {
            return;
        }

        switch (phase) {
            case RAIN:
                attack(projectiles);
                attackCooldown = 0.65;
                break;
            case CLAWS:
                attack(projectiles);
                attackCooldown = 1.2;
                break;
            case ROOTS:
                if (rootsCreated < 3) {
                    roots.add(new RootHazard(playerBounds, arena));
                    rootsCreated++;
                    attackCooldown = 1.25;
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void attack(ArrayList<EnemyProjectile> projectiles) {
        if (phase == Phase.RAIN) {
            rain.emit(arena, getBounds(), projectiles);
        } else if (phase == Phase.CLAWS) {
            claws.emit(arena, getBounds(), projectiles);
        }
    }

    private void nextPhase() {
        switch (phase) {
            case RAIN:
                phase = Phase.REST_AFTER_RAIN;
                phaseTime = 1.4;
                break;
            case REST_AFTER_RAIN:
                phase = Phase.CLAWS;
                phaseTime = 5.2;
                claws.reset();
                break;
            case CLAWS:
                phase = Phase.REST_AFTER_CLAWS;
                phaseTime = 1.4;
                break;
            case REST_AFTER_CLAWS:
                phase = Phase.ROOTS;
                phaseTime = 4.6;
                rootsCreated = 0;
                break;
            case ROOTS:
                phase = Phase.REST_AFTER_ROOTS;
                phaseTime = 1.4;
                break;
            default:
                phase = Phase.RAIN;
                phaseTime = 5.2;
                break;
        }
        attackCooldown = 0.45;
    }

    @Override
    public boolean checkHazards(Rectangle playerBounds) {
        boolean hit = false;
        for (RootHazard root : roots) {
            if (root.tryHit(playerBounds)) {
                hit = true;
            }
        }
        return hit;
    }

    @Override
    public void drawHazards(Graphics g) {
        if (isAlive()) {
            for (RootHazard root : roots) {
                root.draw(g);
            }
        }
    }

    @Override
    public String getAttackName() {
        switch (phase) {
            case RAIN: return "RAIN";
            case CLAWS: return "CLAW SWEEP";
            case ROOTS: return "ROOT HUNT";
            default: return "RECOVERY";
        }
    }

    @Override
    public void reset() {
        super.reset();
        roots.clear();
        claws.reset();
        phase = Phase.RAIN;
        phaseTime = 5.2;
        attackCooldown = 0.7;
        rootsCreated = 0;
    }
}
