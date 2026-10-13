package finalgame;

import javax.swing.JFrame;

public class GameWindow extends JFrame {
    private final GamePanel gamePanel;

    public GameWindow() {
        super("FINAL");
        gamePanel = new GamePanel();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setContentPane(gamePanel);
        pack();
        setLocationRelativeTo(null);
    }
}
