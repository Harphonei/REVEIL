package finalgame;

import java.awt.*;

/** The same dim CRT as the title screen, with a compact encounter list and portrait. */
final class RematchScreen {
    static final Rectangle BACK=new Rectangle(112,487,180,42);
    static final Rectangle FIGHT=new Rectangle(456,487,280,42);
    private static final Color TEXT=new Color(216,207,190),DIM=new Color(140,145,143),RED=new Color(161,103,111);
    private static final String[] NAMES={"FOREST BEAST","THE SCIENTIST","MOTH QUEEN","LAST GUARDIAN","THE MASKED ONE"};
    private static final String[] PLACES={"THE FOREST","THE MINE","BUTTERFLY FOREST","TEMPLE ENTRANCE","THE EMPTY THRONE"};
    private static final Boss[] PREVIEWS={createBoss(1),createBoss(2),createBoss(3),createBoss(4),createBoss(5)};
    private RematchScreen(){}
    static Boss createBoss(int number){
        return switch(number){
            case 1->new ForestBoss(430,210);case 2->new ScientistBoss(430,210);
            case 3->new MothBoss(430,210);case 4->new KnightBoss(430,210);
            case 5->new MaskedBoss(430,210);default->throw new IllegalArgumentException("Unknown boss");
        };
    }
    static Rectangle card(int index){return new Rectangle(112,144+index*60,300,50);}
    static int choiceAt(int x,int y){
        for(int i=0;i<5;i++)if(card(i).contains(x,y))return i;
        return BACK.contains(x,y)?5:-1;
    }
    private static void text(Graphics g,String s,int cx,int y,int size,Color color){
        g.setFont(new Font(Font.MONOSPACED,Font.PLAIN,size));g.setColor(color);
        g.drawString(s,cx-g.getFontMetrics().stringWidth(s)/2,y);
    }
    static void draw(Graphics graphics,double time,int choice,boolean[] wins){
        Graphics2D g=(Graphics2D)graphics.create();MenuScreen.shell(g);Graphics2D screen=MenuScreen.glass(g,time);
        PixelLetters.centered(screen,"BOSS REMATCH",426,78,4,TEXT);
        text(screen,"RETURN TO THE FIGHT",426,125,11,DIM);
        for(int i=0;i<5;i++){
            Rectangle b=card(i);boolean selected=choice==i;
            screen.setColor(selected?new Color(63,36,47,235):new Color(19,26,33,220));screen.fillRect(b.x,b.y,b.width,b.height);
            screen.setColor(selected?RED:new Color(60,69,74));screen.drawRect(b.x,b.y,b.width,b.height);
            screen.setColor(selected?RED:new Color(88,92,95));screen.fillRect(b.x,b.y,3,b.height);
            PixelLetters.draw(screen,"0"+(i+1),b.x+14,b.y+16,2,selected?TEXT:DIM);
            screen.setFont(new Font(Font.MONOSPACED,Font.BOLD,14));screen.setColor(selected?TEXT:DIM);screen.drawString(NAMES[i],b.x+56,b.y+22);
            screen.setFont(new Font(Font.MONOSPACED,Font.PLAIN,10));screen.setColor(DIM);screen.drawString(PLACES[i],b.x+56,b.y+38);
            if(wins[i]){screen.setColor(new Color(141,157,139));screen.fillRect(b.x+b.width-18,b.y+21,5,5);}
        }
        int selected=Math.min(choice,4);
        screen.setColor(new Color(11,16,23,220));screen.fillRect(436,144,312,290);
        screen.setColor(new Color(73,76,82));screen.drawRect(436,144,312,290);
        text(screen,NAMES[selected],592,169,15,TEXT);
        Graphics2D art=(Graphics2D)screen.create();art.clipRect(444,178,296,218);
        Boss boss=PREVIEWS[selected];Rectangle bounds=boss.getBounds();
        double scale=Math.min(268.0/bounds.width,211.0/bounds.height);
        art.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        art.translate(592-bounds.getCenterX()*scale,184-bounds.y*scale);art.scale(scale,scale);boss.draw(art);art.dispose();
        text(screen,selected==4?"TWO PHASES / ONE FINAL LOOP":"A STRONGER ECHO",592,417,11,RED);
        text(screen,"E + Q READY   /   STORY SAVE UNCHANGED",426,464,11,DIM);
        MenuScreen.button(screen,BACK,"BACK",choice==5);
        MenuScreen.button(screen,FIGHT,"FIGHT",choice<5);
        text(screen,"W/S OR ARROWS: SELECT   ENTER: FIGHT   ESC: BACK",426,551,10,DIM);
        screen.dispose();MenuScreen.scan(g);g.dispose();
    }
}
