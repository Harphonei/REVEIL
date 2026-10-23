package finalgame;

import java.awt.*;

/** Short, quiet credits after the final reveal. No game or save state is mutated here. */
final class CreditsScreen {
    static final double DURATION=20;
    static final Rectangle SKIP=new Rectangle(812,556,124,40);
    private static final double[] CUTS={0,5,11,15,DURATION};
    private static final Color TEXT=new Color(214,205,192),DIM=new Color(146,138,138),ACCENT=new Color(154,113,125);
    private CreditsScreen(){}
    private static void line(Graphics2D g,String text,int y,int size,int style,Color color){
        g.setFont(new Font(Font.SERIF,style,size));g.setColor(color);
        g.drawString(text,(960-g.getFontMetrics().stringWidth(text))/2,y);
    }
    static void draw(Graphics graphics,double time){
        Graphics2D g=(Graphics2D)graphics.create();g.setColor(new Color(5,7,11));g.fillRect(0,0,960,640);
        int page=0;while(page<3&&time>=CUTS[page+1])page++;
        double fade=Math.max(0,Math.min(1,Math.min((time-CUTS[page])/.7,(CUTS[page+1]-time)/.7)));
        Graphics2D words=(Graphics2D)g.create();words.setComposite(AlphaComposite.SrcOver.derive((float)fade));
        if(page==0){
            PixelLetters.centered(words,"REVEIL",480,184,8,TEXT);
            words.setColor(new Color(83,61,71));words.drawLine(416,269,544,269);
            line(words,"Created by",315,19,Font.PLAIN,DIM);
            line(words,"Artima Paivongsa",358,29,Font.PLAIN,TEXT);
        }else if(page==1){
            line(words,"Story · Game Design · Development",222,20,Font.PLAIN,DIM);
            line(words,"Artima Paivongsa",266,29,Font.PLAIN,TEXT);
            words.setColor(new Color(83,61,71));words.drawLine(446,306,514,306);
            line(words,"AI Assistance",355,19,Font.PLAIN,DIM);
            line(words,"ChatGPT — Programming & Visual Assets",397,23,Font.PLAIN,TEXT);
        }else if(page==2){
            line(words,"Special Thanks",270,22,Font.PLAIN,DIM);
            line(words,"Everyone who played and tested FINAL",322,26,Font.PLAIN,TEXT);
        }else{
            line(words,"Thank you for playing.",278,32,Font.PLAIN,TEXT);
            line(words,"…Have we met before?",352,24,Font.ITALIC,ACCENT);
        }
        words.dispose();MenuScreen.skipButton(g,SKIP);
        g.setFont(new Font(Font.MONOSPACED,Font.PLAIN,11));g.setColor(new Color(112,109,112));
        g.drawString("Enter / Space: Skip credits",28,590);g.dispose();
    }
}
