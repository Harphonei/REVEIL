package finalgame;

import java.awt.*;

/** Shared presentation chrome; all world, camera and input geometry remains unchanged. */
final class GameUi {
    private static final Color INK=new Color(10,14,19),TEXT=new Color(213,205,190),DIM=new Color(122,130,131),RED=new Color(164,75,91);
    private GameUi(){}
    private static void text(Graphics2D g,String label,int y,int size,Color color){
        g.setFont(new Font(Font.MONOSPACED,Font.PLAIN,size));g.setColor(color);
        g.drawString(label,(960-g.getFontMetrics().stringWidth(label))/2,y);
    }
    static void overlay(Graphics graphics,boolean dead,int selected,boolean rematch,Rectangle resume,Rectangle help,Rectangle menu){
        Graphics2D g=(Graphics2D)graphics.create();
        g.setColor(new Color(5,8,13,dead?247:221));g.fillRect(0,0,960,640);
        g.setColor(new Color(17,21,29,245));g.fillRoundRect(292,144,376,374,10,10);
        g.setColor(new Color(74,64,75));g.drawRoundRect(292,144,376,374,10,10);
        g.setColor(new Color(47,47,57));g.drawLine(318,238,642,238);
        PixelLetters.centered(g,dead?"YOU DIED":"PAUSED",480,184,4,dead?RED:TEXT);
        MenuScreen.button(g,resume,dead?"RETRY":"RESUME",selected==0);
        MenuScreen.button(g,help,"HOW TO PLAY",selected==1);
        MenuScreen.button(g,menu,rematch?"BOSS SELECT":"MAIN MENU",selected==2);
        String note=rematch?"Your story save is unchanged.":dead?"Retry restarts this battle.":"Continue starts at the map entrance.";
        text(g,note,550,12,DIM);
        text(g,dead?"W/S or arrows: Select   Enter: Confirm":"W/S or arrows: Select   Enter: Confirm   Esc: Resume",590,11,DIM);
        g.dispose();
    }
}
