package finalgame;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;

public class GameWindow extends JFrame {
    private final GamePanel gamePanel;

    public GameWindow() {
        super("REVEIL v3.109");
        gamePanel = new GamePanel();
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setResizable(false);
        setContentPane(gamePanel);
        pack();
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter(){
            @Override public void windowClosing(WindowEvent event){
                if(gamePanel.saveBeforeExit())dispose();
                else javax.swing.JOptionPane.showMessageDialog(GameWindow.this,
                        "Progress could not be saved. The game is still open.\nPlease check disk space and folder permissions.",
                        "Autosave failed",javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        });

        addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowLostFocus(WindowEvent event) {
                gamePanel.onFocusLost();
            }
        });
    }
}
