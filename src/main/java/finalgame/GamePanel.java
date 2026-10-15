package finalgame;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class GamePanel extends JPanel {
    private static final int VIEW_WIDTH = 960;
    private static final int VIEW_HEIGHT = 640;
    private final Rectangle battleArea = new Rectangle(300, 300, 360, 260);
    private final Player player;
    private Boss boss;
    private int selectedBoss = 2;
    private final LaserBeam laser = new LaserBeam();
    private final boolean[] pressed = new boolean[4];
    private final Timer timer;
    private long previousTime;

    private final ArrayList<Projectile> projectiles = new ArrayList<>();
    private final ArrayList<EnemyProjectile> enemyProjectiles = new ArrayList<>();
    private boolean shooting = false;
    private boolean eHeld = false;
    private boolean qHeld = false;
    private double shotCooldown = 0.0;
    private static final double SHOT_INTERVAL = 0.3;

    public GamePanel() {
        setPreferredSize(new Dimension(VIEW_WIDTH, VIEW_HEIGHT));
        setBackground(Color.BLACK);
        player = new Player(468, 500);
        boss = new ScientistBoss(430, 210);

        bindKey("W", 0);
        bindKey("A", 1);
        bindKey("S", 2);
        bindKey("D", 3);
        bindBossSelection("1", 1);
        bindBossSelection("2", 2);

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("released R"), "restart-battle");
        getActionMap().put("restart-battle", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (!player.isAlive() || !boss.isAlive()) {
                    restartBattle();
                }
            }
        });

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("pressed E"), "skill-e-press");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("released E"), "skill-e-release");
        getActionMap().put("skill-e-press", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (!eHeld) {
                    eHeld = true;
                    useSkillE();
                }
            }
        });
        getActionMap().put("skill-e-release", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                eHeld = false;
            }
        });

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("pressed Q"), "skill-q-press");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("released Q"), "skill-q-release");
        getActionMap().put("skill-q-press", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (!qHeld) {
                    qHeld = true;
                    if (player.isAlive() && boss.isAlive()) {
                        player.useSkillQ();
                    }
                }
            }
        });
        getActionMap().put("skill-q-release", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                qHeld = false;
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                if (SwingUtilities.isRightMouseButton(event)) {
                    shooting = true;
                }
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                if (SwingUtilities.isRightMouseButton(event)) {
                    shooting = false;
                }
            }
        });

        timer = new Timer(16, event -> updateGame());
    }

    private void bindKey(String key, int index) {
        String pressAction = "press-" + key;
        String releaseAction = "release-" + key;
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("pressed " + key), pressAction);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("released " + key), releaseAction);
        getActionMap().put(pressAction, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                pressed[index] = true;
            }
        });
        getActionMap().put(releaseAction, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                pressed[index] = false;
            }
        });
    }

    private void bindBossSelection(String key, int number) {
        String action = "select-boss-" + number;
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("released " + key), action);
        getActionMap().put(action, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                selectedBoss = number;
                boss = number == 1
                        ? new ForestBoss(430, 210) : new ScientistBoss(430, 210);
                restartBattle();
            }
        });
    }

    private void useSkillE() {
        if (!player.isAlive() || !boss.isAlive()) {
            return;
        }
        Projectile skill = player.useSkillE();
        if (skill != null) {
            projectiles.add(skill);
        }
    }

    private void updateGame() {
        long now = System.nanoTime();
        double seconds = Math.min((now - previousTime) / 1_000_000_000.0, 0.05);
        previousTime = now;

        if (!player.isAlive() || !boss.isAlive()) {
            projectiles.clear();
            enemyProjectiles.clear();
            laser.reset();
            repaint();
            return;
        }

        player.update(seconds);
        int directionX = 0;
        int directionY = 0;
        if (pressed[0]) directionY--;
        if (pressed[1]) directionX--;
        if (pressed[2]) directionY++;
        if (pressed[3]) directionX++;
        player.move(directionX, directionY, seconds, battleArea);

        shotCooldown = Math.max(0.0, shotCooldown - seconds);
        if (shooting && !player.isUltimateActive() && shotCooldown <= 0.0) {
            projectiles.add(player.shoot());
            shotCooldown = SHOT_INTERVAL;
        }

        for (int i = projectiles.size() - 1; i >= 0; i--) {
            Projectile projectile = projectiles.get(i);
            projectile.update(seconds);
            if (boss.isAlive()
                    && projectile.getBounds().intersects(boss.getBounds())) {
                boss.takeDamage(projectile.getDamage());
                projectiles.remove(i);
            } else if (projectile.isOutsideScreen()) {
                projectiles.remove(i);
            }
        }

        boolean laserEnabled =
                shooting && player.isUltimateActive() && boss.isAlive();
        int laserDamage = laser.update(
                laserEnabled, player.getLaserBounds(), boss.getBounds(), seconds);
        boss.takeDamage(laserDamage);

        if (!boss.isAlive()) {
            projectiles.clear();
            enemyProjectiles.clear();
            laser.reset();
            repaint();
            return;
        }

        boss.update(seconds, battleArea, player.getBounds(), enemyProjectiles);

        for (int i = enemyProjectiles.size() - 1; i >= 0; i--) {
            EnemyProjectile projectile = enemyProjectiles.get(i);
            projectile.update(seconds);
            if (projectile.shouldSplit()) {
                enemyProjectiles.addAll(projectile.split());
                enemyProjectiles.remove(i);
                continue;
            }
            if (projectile.getBounds().intersects(player.getBounds())) {
                player.takeDamage(10);
                enemyProjectiles.remove(i);
            } else if (projectile.isOutsideScreen(getWidth(), getHeight())) {
                enemyProjectiles.remove(i);
            }
        }

        if (boss.checkHazards(player.getBounds())) {
            player.takeDamage(10);
        }

        if (!player.isAlive()) {
            projectiles.clear();
            enemyProjectiles.clear();
            laser.reset();
        }
        repaint();
    }

    private void restartBattle() {
        clearInput();
        laser.reset();
        projectiles.clear();
        enemyProjectiles.clear();
        player.reset(468, 500);
        boss.reset();
        shotCooldown = 0.0;
        repaint();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        previousTime = System.nanoTime();
        timer.start();
    }

    @Override
    public void removeNotify() {
        timer.stop();
        super.removeNotify();
    }

    public void clearInput() {
        for (int i = 0; i < pressed.length; i++) {
            pressed[i] = false;
        }
        shooting = false;
        eHeld = false;
        qHeld = false;
        // Stop the visible beam but retain any pending gap when focus is lost.
        laser.update(false, player.getLaserBounds(), boss.getBounds(), 0.0);
        previousTime = System.nanoTime();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 48));
        g.drawString("FINAL", 60, 100);
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 18));
        g.drawString("WASD: Move | Right mouse: Fire", 60, 130);
        g.setColor(Color.LIGHT_GRAY);
        g.drawString("TEST: 1 Forest / 2 Scientist", 60, 625);
        g.setColor(Color.WHITE);

        Graphics2D border = (Graphics2D) g.create();
        border.setColor(Color.WHITE);
        border.setStroke(new BasicStroke(3.0f));
        border.drawRect(battleArea.x, battleArea.y,
                battleArea.width, battleArea.height);
        border.dispose();

        if (player.isAlive() && boss.isAlive()) {
            boss.drawHazards(g);
            laser.draw(g);
        }
        boss.draw(g);
        player.draw(g);
        for (Projectile projectile : projectiles) {
            projectile.draw(g);
        }
        Graphics2D enemyGraphics = (Graphics2D) g.create();
        enemyGraphics.clipRect(battleArea.x, battleArea.y,
                battleArea.width, battleArea.height);
        for (EnemyProjectile projectile : enemyProjectiles) {
            projectile.draw(enemyGraphics);
        }
        enemyGraphics.dispose();

        g.setColor(Color.WHITE);
        g.drawString("Player HP: " + player.getHp() + "/" + player.getMaxHp(),
                battleArea.x, battleArea.y + battleArea.height + 35);

        String skillEText;
        if (player.getSkillECooldown() <= 0.0) {
            skillEText = "E: READY";
        } else {
            int remaining = (int) Math.ceil(player.getSkillECooldown());
            skillEText = "E: " + remaining + "s";
        }
        g.setColor(Color.MAGENTA);
        g.drawString(skillEText, 700, 335);

        String skillQText;
        if (player.isUltimateActive()) {
            int remaining = (int) Math.ceil(player.getUltimateTime());
            skillQText = "Q ACTIVE: " + remaining + "s";
        } else if (player.getSkillQCooldown() > 0.0) {
            int remaining = (int) Math.ceil(player.getSkillQCooldown());
            skillQText = "Q: " + remaining + "s";
        } else {
            skillQText = "Q: READY";
        }
        g.setColor(Color.YELLOW);
        g.drawString(skillQText, 700, 365);
        g.setColor(Color.ORANGE);
        g.drawString("BOSS: " + boss.getAttackName(), 700, 405);
        g.setColor(Color.LIGHT_GRAY);
        g.drawString("Target: " + boss.getDifficultyLabel(), 700, 435);
        g.drawString("Boss " + selectedBoss + " / 2", 700, 465);

        if (!player.isAlive()) {
            g.setColor(Color.RED);
            g.drawString("YOU DIED", battleArea.x + 120, battleArea.y + 130);
        } else if (!boss.isAlive()) {
            g.setColor(Color.GREEN);
            g.drawString("VICTORY", battleArea.x + 120, battleArea.y + 130);
        }
        if (!player.isAlive() || !boss.isAlive()) {
            g.setColor(Color.WHITE);
            g.drawString("Press R to retry",
                    battleArea.x + 95, battleArea.y + 165);
        }
    }
}
