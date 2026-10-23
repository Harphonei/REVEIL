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
    static void shell(Graphics2D g){
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
        g.setColor(new Color(112,104,97));g.drawString("FINAL / 3.100",72,603);
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
    static Graphics2D glass(Graphics2D g,double time){
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
    static void scan(Graphics2D g){
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
    public static void menu(Graphics graphics,double time,SaveStore.Profile profile,int selected,Rectangle continueButton,Rectangle newButton,Rectangle helpButton,Rectangle rematchButton,String notice){
        Graphics2D g=(Graphics2D)graphics.create();shell(g);Graphics2D screen=glass(g,time);
        PixelLetters.centered(screen,"FINAL",426+2,112,11,new Color(72,39,49));
        PixelLetters.centered(screen,"FINAL",426,110,11,IVORY);
        text(screen,"- - -",426,216,13,new Color(102,104,100));
        layoutMenu(profile,continueButton,newButton,helpButton,rematchButton);
        boolean resume=profile.checkpoint()!=null;int index=0;
        if(resume)button(screen,continueButton,"CONTINUE",selected==index++);
        button(screen,newButton,"NEW GAME",selected==index++);
        button(screen,helpButton,"HOW TO PLAY",selected==index++);
        if(profile.completed())button(screen,rematchButton,"BOSS REMATCH",selected==index);
        if(resume)text(screen,"MAP "+profile.checkpoint().map()+" / ENTRANCE",426,485,13,DIM);
        else if(profile.completed())text(screen,"THE LOOP IS COMPLETE",426,485,13,DIM);
        text(screen,"W/S OR ARROWS    ENTER TO SELECT",426,530,12,DIM);
        if(!notice.isEmpty())text(screen,notice,426,551,10,new Color(196,129,120));
        screen.dispose();scan(g);g.dispose();
    }
    public static void layoutMenu(SaveStore.Profile profile,Rectangle resume,Rectangle fresh,Rectangle help,Rectangle rematch){
        int count=2+(profile.checkpoint()!=null?1:0)+(profile.completed()?1:0);
        int y=count==4?230:count==3?266:310,step=count==4?60:72;
        if(profile.checkpoint()!=null){resume.setBounds(285,y,280,48);y+=step;}else resume.setBounds(0,0,0,0);
        fresh.setBounds(285,y,280,48);y+=step;help.setBounds(285,y,280,48);y+=step;
        if(profile.completed())rematch.setBounds(285,y,280,48);else rematch.setBounds(0,0,0,0);
    }
    private static void keycap(Graphics g,String label,int x,int y,int w){
        g.setColor(new Color(10,14,20));g.fillRoundRect(x,y+4,w,31,6,6);
        g.setColor(new Color(67,73,78));g.fillRoundRect(x,y,w,30,6,6);
        g.setColor(new Color(116,119,116));g.drawRoundRect(x,y,w,30,6,6);
        g.setFont(new Font(Font.MONOSPACED,Font.BOLD,label.length()>4?11:14));g.setColor(IVORY);
        g.drawString(label,x+(w-g.getFontMetrics().stringWidth(label))/2,y+20);
    }
    private static void mouse(Graphics g,int x,int y){
        g.setColor(new Color(62,69,75));g.fillRoundRect(x,y,36,53,15,15);
        g.setColor(new Color(173,111,116));g.fillArc(x+18,y+2,16,29,0,90);
        g.setColor(IVORY);g.drawRoundRect(x,y,36,53,15,15);g.drawLine(x+18,y,x+18,y+24);g.drawLine(x,y+24,x+35,y+24);
        g.setFont(new Font(Font.MONOSPACED,Font.BOLD,12));g.drawString("R",x+23,y+18);
    }
    private static void controls(Graphics g,String icon,int x,int y){
        if(icon.equals("EXPLORE")){keycap(g,"SHIFT",x+6,y-3,68);keycap(g,"F",x+26,y+32,28);}
        else if(icon.equals("WASD")){
            keycap(g,"W",x+29,y-8,26);keycap(g,"A",x,y+25,26);
            keycap(g,"S",x+29,y+25,26);keycap(g,"D",x+58,y+25,26);
        }else if(icon.equals("MOUSE"))mouse(g,x+23,y);
        else if(icon.equals("TALK")){keycap(g,"ENTER",x,y-3,80);keycap(g,"SPACE",x,y+32,80);}
        else keycap(g,icon,x+6,y+12,68);
    }
    private static void controlCard(Graphics g,int x,int y,String icon,String title,String detail){
        g.setColor(new Color(25,31,37,220));g.fillRoundRect(x,y,302,76,8,8);
        g.setColor(new Color(54,62,67));g.drawRoundRect(x,y,302,76,8,8);
        controls(g,icon,x+10,y+11);
        g.setFont(new Font(Font.MONOSPACED,Font.BOLD,13));g.setColor(IVORY);g.drawString(title,x+111,y+30);
        g.setFont(new Font(Font.MONOSPACED,Font.PLAIN,11));g.setColor(DIM);g.drawString(detail,x+111,y+51);
    }
    public static void skipButton(Graphics graphics,Rectangle b){
        Graphics2D g=(Graphics2D)graphics.create();
        g.setColor(new Color(0,0,0,75));g.fillRect(b.x+2,b.y+3,b.width,b.height);
        g.setColor(new Color(20,24,30,235));g.fillRect(b.x,b.y,b.width,b.height);
        g.setColor(new Color(105,99,101));g.drawRect(b.x,b.y,b.width-1,b.height-1);
        g.setColor(new Color(52,56,62));g.drawRect(b.x+3,b.y+3,b.width-7,b.height-7);
        g.setColor(new Color(151,117,116));
        for(int x:new int[]{b.x,b.x+b.width-1}){
            int direction=x==b.x?1:-1;
            g.drawLine(x,b.y,x+direction*9,b.y);
            g.drawLine(x,b.y+b.height-1,x+direction*9,b.y+b.height-1);
        }
        g.setFont(new Font(Font.MONOSPACED,Font.BOLD,17));g.setColor(IVORY);
        String label="Skip >";
        g.drawString(label,b.x+(b.width-g.getFontMetrics().stringWidth(label))/2,b.y+25);
        g.dispose();
    }
    public static void battleTutorial(Graphics graphics){
        Graphics2D g=(Graphics2D)graphics.create();
        g.setColor(new Color(15,20,27,250));g.fillRoundRect(206,344,548,233,12,12);
        g.setColor(new Color(120,106,107));g.drawRoundRect(206,344,548,233,12,12);
        PixelLetters.centered(g,"SURVIVE",480,363,3,IVORY);
        controlCard(g,224,406,"MOUSE","HOLD TO FIRE","Shots go upward");
        controls(g,"WASD",550,418);text(g,"DODGE",592,505,13,IVORY);
        text(g,"ENTER / SPACE / CLICK TO BEGIN",480,551,13,DIM);g.dispose();
    }
    public static void help(Graphics graphics,double time,Rectangle back){
        Graphics2D g=(Graphics2D)graphics.create();shell(g);Graphics2D screen=glass(g,time);
        PixelLetters.centered(screen,"HOW TO PLAY",426,85,3,IVORY);
        String[][] items={
            {"WASD","MOVE / DODGE","World and red heart"},{"EXPLORE","RUN / INSPECT","Near forest objects"},
            {"MOUSE","HOLD TO FIRE","Right mouse button"},{"TALK","READ / CONFIRM","Advance dialogue"},
            {"E","HEAVY ATTACK","Charge, then fire"},{"Q","ULTIMATE","Boost fire for 5s"},
            {"TAB","SKIP SCENE","Or click Skip >"},{"ESC","PAUSE / HELP","Time stops in menus"}
        };
        for(int i=0;i<items.length;i++)controlCard(screen,112+(i%2)*322,126+(i/2)*84,items[i][0],items[i][1],items[i][2]);
        button(screen,back,"BACK",true);text(screen,"ESC / ENTER TO GO BACK",426,548,12,DIM);
        screen.dispose();scan(g);g.dispose();
    }
}
