package finalgame;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.SwingUtilities;
import java.awt.Rectangle;
import java.awt.BasicStroke;
import java.awt.Graphics2D;

public class GamePanel extends JPanel {
    private static final int VIEW_WIDTH = 960;
    private static final int VIEW_HEIGHT = 640;
    private final Player player;
    private final boolean[] pressed = new boolean[4];
    private final Timer timer;
    private long previousTime;
    private final ArrayList<Projectile> projectiles = new ArrayList<>();
    private boolean shooting = false;
    private double shotCooldown = 0.0;
    private static final double SHOT_INTERVAL = 0.3;
    private final Boss boss;
    private final Rectangle battleArea = new Rectangle(300, 300, 360, 260);
    private final ArrayList<EnemyProjectile> enemyProjectiles = new ArrayList<>();
    private double bossShotCooldown = 1.0;
    private static final double BOSS_SHOT_INTERVAL = 1.0;

    public GamePanel() {
        setPreferredSize(new Dimension(VIEW_WIDTH, VIEW_HEIGHT));
        setBackground(Color.BLACK);
        player = new Player(468, 500);
        boss = new Boss(430, 190);
        bindKey("W", 0);
        bindKey("A", 1);
        bindKey("S", 2);
        bindKey("D", 3);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("released R"),"restart-battle");
        getActionMap().put("restart-battle", new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent event) {
            if (!player.isAlive() || !boss.isAlive()) {
                restartBattle();
            }
        }
    });
        timer = new Timer(16, event -> updateGame());
        
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

    private void updateGame() {
        long now = System.nanoTime();
        double seconds = Math.min((now - previousTime) / 1_000_000_000.0, 0.05);
        previousTime = now;
        if (!player.isAlive() || !boss.isAlive()) {
            projectiles.clear();
            enemyProjectiles.clear();
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
        if (shooting && shotCooldown <= 0.0) {
            projectiles.add(player.shoot());
            shotCooldown = SHOT_INTERVAL;
        }
        for (int i = projectiles.size() - 1; i >= 0; i--) {
            Projectile projectile = projectiles.get(i);
            projectile.update(seconds);
            if (boss.isAlive()&& projectile.getBounds().intersects(boss.getBounds())) {
                boss.takeDamage(10);
                projectiles.remove(i);
            } else if (projectile.isOutsideScreen()) {
                projectiles.remove(i);
            }
        }
        if (!boss.isAlive()) {
            projectiles.clear();
            enemyProjectiles.clear();
            repaint();
            return;
        }
        bossShotCooldown = Math.max(0.0,bossShotCooldown - seconds);
        if (bossShotCooldown <= 0.0) {
            enemyProjectiles.add(boss.shoot());
            bossShotCooldown = BOSS_SHOT_INTERVAL;
        }
        for (int i = enemyProjectiles.size() - 1; i >= 0; i--) {
            EnemyProjectile projectile = enemyProjectiles.get(i);
            projectile.update(seconds);
            if (projectile.getBounds().intersects(player.getBounds())) {
               player.takeDamage(10);
               enemyProjectiles.remove(i);
            } else if (projectile.isOutsideScreen(getHeight())) {
                enemyProjectiles.remove(i);
            }
        }
        if (!player.isAlive()) {
           projectiles.clear();
           enemyProjectiles.clear();
        }
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
        previousTime = System.nanoTime();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 48));
        g.drawString("FINAL", 60, 100);
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 18));
        g.drawString("WASD: Move", 60, 145);
        g.setColor(Color.WHITE);
        Graphics2D border = (Graphics2D) g.create();
        border.setColor(Color.WHITE);
        border.setStroke(new BasicStroke(3.0f));
        border.drawRect(
            battleArea.x,
            battleArea.y,
            battleArea.width,
            battleArea.height
        );
        border.dispose();
        boss.draw(g);
        player.draw(g);
        for (Projectile projectile : projectiles) {
            projectile.draw(g);
        }
        for (EnemyProjectile projectile : enemyProjectiles) {projectile.draw(g);}
        g.setColor(Color.WHITE);
        g.drawString(
            "Player HP: " + player.getHp() + "/" + player.getMaxHp(),
            battleArea.x,
            battleArea.y + battleArea.height + 35
        );
        if (!player.isAlive()) {
            g.setColor(Color.RED);
            g.drawString("YOU DIED", battleArea.x + 120, battleArea.y + 130);
        } else if (!boss.isAlive()) {
            g.setColor(Color.GREEN);
            g.drawString("VICTORY", battleArea.x + 120, battleArea.y + 130);
        }
        if (!player.isAlive() || !boss.isAlive()) {
            g.setColor(Color.WHITE);
            g.drawString(
                "Press R to retry",
                battleArea.x + 95,
                battleArea.y + 165
            );
        }
    }
    private void restartBattle() {
        clearInput();
        projectiles.clear();
        enemyProjectiles.clear();
        player.reset(468, 500);
        boss.reset();
        shotCooldown = 0.0;
        bossShotCooldown = BOSS_SHOT_INTERVAL;
        repaint();
    }
}
