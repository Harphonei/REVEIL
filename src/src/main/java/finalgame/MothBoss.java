package finalgame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Random;

// A giant nocturnal moth drawn to the protagonist's repeating memories.
public class MothBoss extends Boss {
    private final RandomAttackOrder order = new RandomAttackOrder(4);
    private final AttackPattern rain = new RainPattern(new Random(), 145.0);
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
        attackIndex = order.next();
        double[] durations = {5.2, 5.4, 4.6, 5.8};
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
            double[] intervals = {0.45, 0.60, 1.1, 0.95};
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
        order.reset();
        beginAttack();
    }
}
