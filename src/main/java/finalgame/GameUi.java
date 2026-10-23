package finalgame;

import java.awt.*;

/** Shared presentation chrome; all world, camera and input geometry remains unchanged. */
final class GameUi {
    private static final Color INK=new Color(10,14,19),TEXT=new Color(213,205,190),DIM=new Color(122,130,131),RED=new Color(164,75,91);
    private GameUi(){}
    static void exploration(Graphics graphics,String title,int chapter){
        Graphics2D g=(Graphics2D)graphics.create();
        g.setColor(INK);g.fillRect(0,0,960,60);g.fillRect(0,580,960,60);
        g.setColor(new Color(43,49,54));g.drawLine(24,59,936,59);
        PixelLetters.draw(g,"0"+chapter,25,24,2,new Color(142,111,117));
        g.setColor(new Color(65,66,72));g.drawLine(61,20,61,41);
        PixelLetters.draw(g,title,78,24,2,TEXT);
        g.setColor(new Color(29,35,42));g.fillRect(830,19,36,23);
        g.setColor(new Color(85,91,96));g.drawRect(830,19,36,23);
        g.setFont(new Font(Font.MONOSPACED,Font.BOLD,11));g.setColor(TEXT);g.drawString("ESC",838,35);
        g.setFont(new Font(Font.MONOSPACED,Font.PLAIN,12));g.setColor(DIM);g.drawString("Menu",879,35);
        g.dispose();
    }
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
