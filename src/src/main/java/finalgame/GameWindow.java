package finalgame;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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

        addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowLostFocus(WindowEvent event) {
                gamePanel.onFocusLost();
            }
        });
    }
}
