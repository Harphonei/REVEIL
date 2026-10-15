package finalgame;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;

/** A temporary room for testing screen transitions, before building real maps. */
public class ExplorationRoom {
    private final Rectangle walkArea = new Rectangle(100, 180, 760, 380);
    private final Rectangle bossGate = new Rectangle(430, 190, 100, 65);
    private final Player explorer = new Player(468, 480);
    private boolean cleared;
    private String message = "Walk to the gate, then left-click it.";

    public void reset() {
        explorer.reset(468, 480);
        cleared = false;
        message = "Walk to the gate, then left-click it.";
    }

    public void update(int dx, int dy, double seconds) {
        explorer.move(dx, dy, seconds, walkArea);
    }

    public boolean interact(int mouseX, int mouseY) {
        if (!bossGate.contains(mouseX, mouseY)) return false;
        Rectangle reach = explorer.getBounds();
        reach.grow(45, 45);
        if (!reach.intersects(bossGate)) {
            message = "Move closer to the gate.";
            return false;
        }
        return true;
    }

    public void returnFromBattle() {
        cleared = true;
        explorer.reset(468, 285);
        message = "Battle cleared! Click the gate nearby to replay.";
    }

    public void draw(Graphics g) {
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 32));
        g.setColor(Color.WHITE);
        g.drawString("EXPLORATION TEST ROOM", 100, 85);
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 18));
        g.drawString("WASD: Move | Left mouse: Interact | Esc: Pause", 100, 125);
        g.setColor(new Color(22, 38, 31));
        g.fillRect(walkArea.x, walkArea.y, walkArea.width, walkArea.height);
        g.setColor(new Color(100, 140, 115));
        g.drawRect(walkArea.x, walkArea.y, walkArea.width, walkArea.height);
        g.setColor(cleared ? new Color(90, 160, 110) : new Color(170, 65, 75));
        g.fillRect(bossGate.x, bossGate.y, bossGate.width, bossGate.height);
        g.setColor(Color.WHITE);
        g.drawString("BOSS", 456, 228);
        explorer.draw(g);
        g.setColor(Color.LIGHT_GRAY);
        g.drawString(message, 100, 600);
    }
}
