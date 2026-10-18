package finalgame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Random;

// A giant nocturnal moth drawn to the protagonist's repeating memories.
public class MothBoss extends Boss {
    @Override
    public Rectangle getBounds() {
        Rectangle base=super.getBounds();
        return new Rectangle(base.x+base.width/2-112,base.y+base.height-162,224,168);
    }

    @Override
    public void draw(Graphics graphics) {
        if(!isAlive())return;
        Graphics g=graphics.create();Rectangle art=getBounds();
        g.setFont(new Font(Font.MONOSPACED,Font.PLAIN,16));g.setColor(Color.WHITE);
        String title="MOTHER MOTH";
        g.drawString(title,art.x+art.width/2-g.getFontMetrics().stringWidth(title)/2,50);
        String hp="HP: "+getHp()+"/"+getMaxHp();
        g.drawString(hp,art.x+art.width/2-g.getFontMetrics().stringWidth(hp)/2,75);
        g.setColor(Color.DARK_GRAY);g.fillRect(art.x,85,art.width,7);
        g.setColor(Color.GREEN);g.fillRect(art.x,85,art.width*getHp()/getMaxHp(),7);
        BossArtwork.drawMothBattle(g,art);g.dispose();
    }
    private boolean openingRain=true;
    private final RandomAttackOrder order = new RandomAttackOrder(4);
    private final AttackPattern rain = new RainPattern(new Random(),155.0,10,2);
    private final WingPattern wings = new WingPattern();
    private final SpiralPattern spiral = new SpiralPattern();
    private final ArrayList<MemoryEcho> echoes = new ArrayList<>();
    private Rectangle arena = new Rectangle(300, 300, 360, 260);
    private int attackIndex;
    private boolean recovering = false;
    private double phaseTime;
    private double attackCooldown;
    private int echoesCreated = 0;

    public MothBoss(int startX, int startY) {
        super(startX, startY, 1600, "MEMORY DEVOURER");
        beginAttack();
    }

    private void beginAttack() {
        attackIndex = openingRain ? 0 : order.next();
        if(openingRain){openingRain=false;order.reset(0);}
        double[] durations = {5.9, 5.4, 4.6, 5.8};
        phaseTime = durations[attackIndex];
        attackCooldown = 0.7;
        recovering = false;
        echoesCreated = 0;
        wings.reset();
        spiral.reset();
    }

    @Override
    public void update(double seconds, Rectangle battleArea,
            Rectangle playerBounds, ArrayList<EnemyProjectile> projectiles) {
        if (!isAlive()) {
            return;
        }
        arena = new Rectangle(battleArea);
        for (int i = echoes.size() - 1; i >= 0; i--) {
            MemoryEcho echo = echoes.get(i);
            echo.update(seconds);
            if (echo.isReady()) {
                echo.release(projectiles);
                echoes.remove(i);
            }
        }

        phaseTime -= seconds;
        if (phaseTime <= 0.0) {
            if (recovering) {
                beginAttack();
            } else {
                recovering = true;
                phaseTime = 2.0;
            }
            return;
        }
        if (recovering) {
            return;
        }
        attackCooldown = Math.max(0.0, attackCooldown - seconds / CombatTuning.ATTACK_INTERVAL_MULTIPLIER);
        if (attackCooldown > 0.0) {
            return;
        }
        if (attackIndex == 2) {
            if (echoesCreated < 3) {
                echoes.add(new MemoryEcho(playerBounds, arena));
                echoesCreated++;
            }
            attackCooldown = 1.1;
        } else {
            attack(projectiles);
            double[] intervals = {0.44, 0.60, 1.1, 0.95};
            attackCooldown = intervals[attackIndex];
        }
    }

    @Override
    public void attack(ArrayList<EnemyProjectile> projectiles) {
        if (recovering) {
            return;
        }
        switch (attackIndex) {
            case 0: rain.emit(arena, getBounds(), projectiles); break;
            case 1: wings.emit(arena, getBounds(), projectiles); break;
            case 3: spiral.emit(arena, getBounds(), projectiles); break;
            default: break;
        }
    }

    @Override
    public void drawHazards(Graphics g) {
        if (isAlive()) {
            for (MemoryEcho echo : echoes) {
                echo.draw(g);
            }
        }
    }

    @Override
    public String getAttackName() {
        if (recovering) {
            return "RECOVERY";
        }
        String[] names = {"RAIN", "WING CROSS", "MEMORY ECHO", "WING VORTEX"};
        return names[attackIndex];
    }

    @Override
    public String getDifficultyLabel() {
        return "3.5/5";
    }

    @Override
    protected Color getBodyColor() {
        return new Color(160, 100, 210);
    }

    @Override
    public void reset() {
        super.reset();
        echoes.clear();
        openingRain=true;
        order.reset();
        beginAttack();
    }
}
