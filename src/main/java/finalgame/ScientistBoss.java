package finalgame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Random;

public class ScientistBoss extends Boss {
    @Override
    public Rectangle getBounds() {
        Rectangle base=super.getBounds();
        return new Rectangle(base.x+base.width/2-82,base.y+base.height-162,164,168);
    }

    @Override
    public Rectangle getHitBounds() {
        Rectangle art=getBounds();
        // Target the body, excluding weapons, limbs and cloak; stable in both phases.
        return new Rectangle(art.x+62,art.y+40,58,108);
    }
    @Override
    public void draw(Graphics graphics) {
        if(!isAlive())return;
        Graphics g=graphics.create();BossArtwork.drawScientistBattle(g,getBounds());g.dispose();
    }
    private boolean openingRain=true;
    private final RandomAttackOrder order = new RandomAttackOrder(3);
    private enum Phase {
        RAIN, REST_AFTER_RAIN, REACTION, REST_AFTER_REACTION,
        GRID, REST_AFTER_GRID
    }

    private final AttackPattern rain = new RainPattern(new Random(),140.0,8,2);
    private final ArrayList<ExperimentZone> zones = new ArrayList<>();
    private final Random random = new Random();
    private Phase phase = Phase.RAIN;
    private double phaseTime = 5.2;
    private double attackCooldown = 0.7;
    private Rectangle arena = new Rectangle(300, 300, 360, 260);
    private int experimentWave = 0;

    public ScientistBoss(int startX, int startY) {
        super(startX, startY, 800, "THE SCIENTIST");
        selectAttack();
    }

    @Override
    public void update(double seconds, Rectangle battleArea,
            Rectangle playerBounds, ArrayList<EnemyProjectile> projectiles) {
        if (!isAlive()) {
            return;
        }
        arena = new Rectangle(battleArea);
        for (int i = zones.size() - 1; i >= 0; i--) {
            zones.get(i).update(seconds);
            if (zones.get(i).isExpired()) {
                zones.remove(i);
            }
        }
        phaseTime -= seconds;
        if (phaseTime <= 0.0) {
            nextPhase();
            return;
        }
        attackCooldown = Math.max(0.0, attackCooldown - seconds / CombatTuning.ATTACK_INTERVAL_MULTIPLIER);
        if (attackCooldown <= 0.0) {
            attack(projectiles);
            switch (phase) {
                case RAIN: attackCooldown = 0.50; break;
                case REACTION: attackCooldown = 1.8; break;
                case GRID: attackCooldown = 1.6; break;
                default: break;
            }
        }
    }

    @Override
    public void attack(ArrayList<EnemyProjectile> projectiles) {
        switch (phase) {
            case RAIN:
                rain.emit(arena, getBounds(), projectiles);
                break;
            case REACTION:
                int column = random.nextInt(3);
                double x = arena.x + (column + 0.5) * arena.width / 3.0 - 14;
                projectiles.add(new ChemicalOrb(x, arena.y - 32, arena.y + 75));
                break;
            case GRID:
                createGridWave();
                break;
            default:
                break;
        }
    }

    private void createGridWave() {
        // Two narrow vertical strips OR one horizontal strip, never both at once.
        if (experimentWave % 4 < 2) {
            int columnWidth = arena.width / 6;
            int offset = experimentWave % 2;
            for (int column = offset; column <= offset + 3; column += 3) {
                zones.add(new ExperimentZone(new Rectangle(
                        arena.x + column * columnWidth, arena.y,
                        columnWidth, arena.height)));
            }
        } else {
            int rowHeight = arena.height / 5;
            int row = experimentWave % 4 == 2 ? 1 : 3;
            zones.add(new ExperimentZone(new Rectangle(
                    arena.x, arena.y + row * rowHeight,
                    arena.width, rowHeight)));
        }
        experimentWave++;
    }

    private void selectAttack() {
        Phase[] attacks = {Phase.RAIN, Phase.REACTION, Phase.GRID};
        int selected = openingRain ? 0 : order.next();
        if(openingRain){openingRain=false;order.reset(0);}
        phase = attacks[selected];
        double[] durations = {5.6, 6.2, 6.8};
        phaseTime = durations[selected];
        attackCooldown = 0.7;
        experimentWave = 0;
    }

    private void nextPhase() {
        switch (phase) {
            case RAIN:
                phase = Phase.REST_AFTER_RAIN;
                phaseTime = 1.5;
                break;
            case REACTION:
                phase = Phase.REST_AFTER_REACTION;
                phaseTime = 2.0;
                break;
            case GRID:
                phase = Phase.REST_AFTER_GRID;
                phaseTime = 1.5;
                break;
            default:
                selectAttack();
                return;
        }
        attackCooldown = 0.45;
    }

    @Override
    public int getHazardDamage(Rectangle playerBounds) {
        int damage = 0;
        for (ExperimentZone zone : zones) {
            if (zone.tryHit(playerBounds)) {
                damage = 14;
            }
        }
        return damage;
    }

    /** Give the new ability dialogue a clean, safe return to combat. */
    public void clearStoryHazards(){zones.clear();attackCooldown=Math.max(attackCooldown,.7);}

    public void resumeAfterSkillReveal(){
        zones.clear();phase=Phase.RAIN;phaseTime=5.6;attackCooldown=0;
    }

    @Override
    public void drawHazards(Graphics g) {
        if (isAlive()) {
            for (ExperimentZone zone : zones) {
                zone.draw(g);
            }
        }
    }

    @Override
    public String getAttackName() {
        switch (phase) {
            case RAIN: return "RAIN";
            case REACTION: return "REACTION";
            case GRID: return "LAB GRID";
            default: return "RECOVERY";
        }
    }

    @Override
    public String getDifficultyLabel() {
        return "2/5";
    }

    @Override
    protected Color getBodyColor() {
        return new Color(90, 180, 220);
    }

    @Override
    public void reset() {
        super.reset();
        zones.clear();
        openingRain=true;
        order.reset();
        selectAttack();
    }
}
