package finalgame;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.Random;

/** A dim analogue television. Noise is cached, bounded and independent of gameplay. */
public final class MenuScreen {
    private static final Color IVORY=new Color(216,207,190),DIM=new Color(125,128,127),RED=new Color(145,65,76);
    private static final Rectangle SCREEN=new Rectangle(68,48,716,528);
    private static final Shape GLASS=new RoundRectangle2D.Double(68,48,716,528,42,42);
    private static final BufferedImage[] NOISE=new BufferedImage[8];
    static{
        Random random=new Random(3191);
        for(int n=0;n<NOISE.length;n++){
            BufferedImage image=new BufferedImage(179,132,BufferedImage.TYPE_INT_ARGB);
            for(int y=0;y<132;y++)for(int x=0;x<179;x++){
                int shade=70+random.nextInt(110),alpha=3+random.nextInt(15);
                image.setRGB(x,y,(alpha<<24)|(shade<<16)|(shade<<8)|shade);
            }
            NOISE[n]=image;
        }
    }
    private MenuScreen(){}
    private static void text(Graphics g,String s,int cx,int y,int size,Color c){
        g.setFont(new Font(Font.MONOSPACED,Font.PLAIN,size));g.setColor(c);g.drawString(s,cx-g.getFontMetrics().stringWidth(s)/2,y);
    }
    private static void shell(Graphics2D g){
        g.setColor(new Color(7,8,11));g.fillRect(0,0,960,640);
        g.setPaint(new GradientPaint(0,20,new Color(58,53,53),0,620,new Color(23,22,27)));
        g.fillRoundRect(36,20,888,600,36,36);
        g.setColor(new Color(83,75,72));g.drawRoundRect(37,21,886,598,36,36);
        g.setColor(new Color(15,15,19));g.fillRoundRect(54,34,744,558,48,48);
        g.setColor(new Color(102,95,87));g.drawRoundRect(60,40,732,546,44,44);
        g.setColor(new Color(30,28,32));g.fillRoundRect(807,47,93,530,10,10);
        g.setColor(new Color(69,63,63));g.drawRoundRect(807,47,93,530,10,10);
        knob(g,854,124,28,-.55);knob(g,854,235,22,.75);
        text(g,"VHF",854,174,10,DIM);text(g,"UHF",854,276,10,DIM);
        g.setColor(new Color(13,14,18));
        for(int y=320;y<493;y+=9)g.fillRoundRect(821,y,65,4,3,3);
        PixelLetters.centered(g,"FINAL",854,528,2,new Color(143,134,123));
        g.setColor(new Color(83,37,45));g.fillOval(823,593,8,8);
        g.setColor(new Color(175,78,83));g.fillRect(825,595,3,3);
        g.setFont(new Font(Font.MONOSPACED,Font.PLAIN,10));g.setColor(new Color(130,121,111));g.drawString("POWER",841,601);
        g.setColor(new Color(112,104,97));g.drawString("FINAL / 3.91",72,603);
    }
    private static void knob(Graphics2D g,int cx,int cy,int radius,double angle){
        g.setColor(new Color(10,11,15));g.fillOval(cx-radius-3,cy-radius-1,radius*2+6,radius*2+6);
        g.setPaint(new GradientPaint(cx-radius,cy-radius,new Color(87,81,78),cx+radius,cy+radius,new Color(29,30,34)));
        g.fillOval(cx-radius,cy-radius,radius*2,radius*2);
        g.setColor(new Color(115,105,95));g.drawOval(cx-radius,cy-radius,radius*2,radius*2);
        int dx=(int)(Math.sin(angle)*(radius-5)),dy=(int)(-Math.cos(angle)*(radius-5));
        g.setStroke(new BasicStroke(3));g.setColor(IVORY);g.drawLine(cx+dx/2,cy+dy/2,cx+dx,cy+dy);g.setStroke(new BasicStroke(1));
        for(int i=0;i<9;i++){double a=-2.4+i*.6;int x=cx+(int)(Math.sin(a)*(radius+8)),y=cy+(int)(Math.cos(a)*(radius+8));g.setColor(new Color(107,96,87));g.fillRect(x,y,2,2);}
    }
    private static Graphics2D glass(Graphics2D g,double time){
        Graphics2D screen=(Graphics2D)g.create();screen.clip(GLASS);
        screen.setPaint(new RadialGradientPaint(426,294,420,new float[]{0,1},new Color[]{new Color(23,30,34),new Color(4,7,12)}));
        screen.fillRect(SCREEN.x,SCREEN.y,SCREEN.width,SCREEN.height);
        screen.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        screen.drawImage(NOISE[(int)(time*9)%NOISE.length],SCREEN.x,SCREEN.y,SCREEN.width,SCREEN.height,null);
        // Slow tracking band; low contrast, with no full-screen flash.
        int band=SCREEN.y+(int)((time*23)%(SCREEN.height+36))-36;
        screen.setColor(new Color(103,112,112,6));screen.fillRect(SCREEN.x,band,SCREEN.width,28);
        return screen;
    }
    private static void scan(Graphics2D g){
        Graphics2D screen=(Graphics2D)g.create();screen.clip(GLASS);
        screen.setColor(new Color(0,0,0,23));for(int y=SCREEN.y;y<SCREEN.y+SCREEN.height;y+=3)screen.drawLine(SCREEN.x,y,SCREEN.x+SCREEN.width,y);
        screen.setColor(new Color(140,154,151,10));screen.drawRoundRect(77,57,698,510,38,38);screen.dispose();
    }
    public static void button(Graphics g,Rectangle b,String label,boolean selected){
        g.setColor(selected?new Color(79,43,53,150):new Color(15,21,27,95));g.fillRect(b.x,b.y,b.width,b.height);
        g.setColor(selected?new Color(156,100,103):new Color(60,69,74));
        g.drawLine(b.x,b.y,b.x+b.width,b.y);g.drawLine(b.x,b.y+b.height,b.x+b.width,b.y+b.height);
        for(int x:new int[]{b.x,b.x+b.width}){g.drawLine(x,b.y,x,b.y+7);g.drawLine(x,b.y+b.height-7,x,b.y+b.height);}
        PixelLetters.centered(g,label,b.x+b.width/2,b.y+(b.height-21)/2,3,selected?IVORY:new Color(145,151,147));
        if(selected){
            g.setColor(RED);int x=b.x-22,y=b.y+b.height/2-5;
            g.fillRect(x,y,4,4);g.fillRect(x+6,y,4,4);g.fillRect(x-2,y+2,14,4);g.fillRect(x,y+6,10,2);g.fillRect(x+2,y+8,6,2);g.fillRect(x+4,y+10,2,2);
        }
    }
    public static void menu(Graphics graphics,double time,SaveStore.Profile profile,int selected,Rectangle continueButton,Rectangle newButton,Rectangle helpButton,String notice){
        Graphics2D g=(Graphics2D)graphics.create();shell(g);Graphics2D screen=glass(g,time);
        PixelLetters.centered(screen,"FINAL",426+2,112,11,new Color(72,39,49));
        PixelLetters.centered(screen,"FINAL",426,110,11,IVORY);
        text(screen,"- - -",426,216,13,new Color(102,104,100));
        boolean resume=profile.checkpoint()!=null;
        if(resume)button(screen,continueButton,"CONTINUE",selected==0);
        button(screen,newButton,"NEW GAME",selected==(resume?1:0));
        button(screen,helpButton,"HOW TO PLAY",selected==(resume?2:1));
        if(resume)text(screen,"MAP "+profile.checkpoint().map()+" / ENTRANCE",426,485,13,DIM);
        else if(profile.completed())text(screen,"THE LOOP IS COMPLETE",426,485,13,DIM);
        text(screen,"W/S OR ARROWS    ENTER TO SELECT",426,530,12,DIM);
        if(!notice.isEmpty())text(screen,notice,426,551,10,new Color(196,129,120));
        screen.dispose();scan(g);g.dispose();
    }
    public static void help(Graphics graphics,double time,Rectangle back){
        Graphics2D g=(Graphics2D)graphics.create();shell(g);Graphics2D screen=glass(g,time);
        PixelLetters.centered(screen,"HOW TO PLAY",426,91,3,IVORY);
        String[][] lines={
            {"EXPLORE","WASD to move / Shift to run"},{"DIALOGUE","Enter, Space or left click"},
            {"BATTLE","WASD to move your heart"},{"FIRE","Hold right mouse button"},
            {"E","Charge a heavy attack"},{"Q","Ultimate power / hold fire to attack"},
            {"SKILLS","Awaken as the story progresses"},{"PAUSE","Esc / time stops in menus"},
            {"RETRY","Restart the same battle"},{"CONTINUE","Restart at the map entrance"}
        };
        screen.setFont(new Font(Font.MONOSPACED,Font.PLAIN,13));
        for(int i=0;i<lines.length;i++){
            int y=155+i*27;screen.setColor(new Color(178,138,137));screen.drawString(lines[i][0],115,y);
            screen.setColor(new Color(182,186,178));screen.drawString(lines[i][1],211,y);
        }
        button(screen,back,"BACK",true);text(screen,"ESC / ENTER TO GO BACK",426,548,12,DIM);
        screen.dispose();scan(g);g.dispose();
    }
}
