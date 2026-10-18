package finalgame;

import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.util.ArrayList;

// A frenzied monster: the bodies in the forest are its victims.
public class ForestBoss extends Boss {
    private boolean openingRain=true;
    private final RandomAttackOrder order = new RandomAttackOrder(3);
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
        selectAttack();
    }

    @Override
    public Rectangle getBounds() {
        Rectangle base=super.getBounds();
        return new Rectangle(base.x+base.width/2-56,base.y+base.height-162,112,168);
    }

    @Override
    public void draw(Graphics graphics) {
        if(!isAlive()) return;
        Graphics g=graphics.create();
        Rectangle art=getBounds();
        g.setFont(new Font(Font.MONOSPACED,Font.PLAIN,16));
        g.setColor(Color.WHITE);
        String title="FRENZIED FOREST BEAST";
        g.drawString(title,art.x+art.width/2-g.getFontMetrics().stringWidth(title)/2,50);
        String hp="HP: "+getHp()+"/"+getMaxHp();
        g.drawString(hp,art.x+art.width/2-g.getFontMetrics().stringWidth(hp)/2,75);
        g.setColor(Color.DARK_GRAY);g.fillRect(art.x,85,art.width,7);
        g.setColor(Color.GREEN);g.fillRect(art.x,85,art.width*getHp()/getMaxHp(),7);
        BossArtwork.drawBattle(g,art);
        g.dispose();
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
        attackCooldown = Math.max(0.0, attackCooldown - seconds / CombatTuning.ATTACK_INTERVAL_MULTIPLIER);
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

    private void selectAttack() {
        Phase[] attacks = {Phase.RAIN, Phase.CLAWS, Phase.ROOTS};
        int selected = openingRain ? 0 : order.next();
        if(openingRain){openingRain=false;order.reset(0);}
        phase = attacks[selected];
        phaseTime = selected == 2 ? 4.6 : 5.2;
        attackCooldown = 0.7;
        rootsCreated = 0;
        claws.reset();
    }

    private void nextPhase() {
        switch (phase) {
            case RAIN: phase = Phase.REST_AFTER_RAIN; break;
            case CLAWS: phase = Phase.REST_AFTER_CLAWS; break;
            case ROOTS: phase = Phase.REST_AFTER_ROOTS; break;
            default:
                selectAttack();
                return;
        }
        phaseTime = 1.4;
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
        openingRain=true;
        order.reset();
        selectAttack();
    }
}
