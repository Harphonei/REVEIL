package finalgame;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

/** Cutscene-only art. All poses share the same pixel grid and foot anchor. */
public final class FinalArtwork {
    private static final BufferedImage HERO=load("final-hero.png"),MASKED=load("final-masked.png");
    private static final BufferedImage HERO_MOTION=SpriteAnimator.Lighting.THRONE_HALL.tint(load("final-hero-motion.png"));
    private static final BufferedImage MASKED_MOTION=SpriteAnimator.Lighting.THRONE_CORRIDOR.tint(load("final-masked-motion.png"));
    private static final BufferedImage MASKED_REACTIONS=load("final-mask-reactions.png");
    private static final BufferedImage HERO_DARK=SpriteAnimator.Lighting.THRONE_HALL.tint(HERO);
    private static final BufferedImage MASKED_DARK=SpriteAnimator.Lighting.THRONE_CORRIDOR.tint(MASKED);
    private static BufferedImage load(String file){
        try{return ImageIO.read(FinalArtwork.class.getResource("/finalgame/"+file));}
        catch(Exception ex){throw new IllegalStateException("Cannot load "+file,ex);}
    }
    private static void cell(Graphics g,BufferedImage sheet,int index,int x,int y,int w,int h){
        cell(g,sheet,index,x,y,w,h,64);
    }
    private static void cell(Graphics g,BufferedImage sheet,int index,int x,int y,int w,int h,int tile){
        int cols=sheet.getWidth()/tile,sx=(index%cols)*tile,sy=(index/cols)*tile;
        Graphics2D p=(Graphics2D)g.create();
        p.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        p.drawImage(sheet,x,y,x+w,y+h,sx,sy,sx+tile,sy+tile,null);p.dispose();
    }
    public static void hero(Graphics g,int pose,double x,double y,int height){
        cell(g,HERO_DARK,pose,(int)Math.round(x-height/2.0),(int)Math.round(y-height),height,height);
    }
    public static void masked(Graphics g,int pose,double x,double y,int height){
        int canvas=(int)Math.round(height*1.5*1.25);
        cell(g,MASKED_MOTION,pose,(int)Math.round(x-canvas/2.0),(int)Math.round(y-canvas+1),canvas,canvas,96);
    }
    public static void wounded(Graphics g,int frame,double x,double y,int height){
        cell(g,HERO_MOTION,frame,(int)Math.round(x-height/2.0),(int)Math.round(y-height+1),height,height);
    }
    public static void reaction(Graphics g,boolean angry,int x,int y,int size){cell(g,MASKED_REACTIONS,angry?1:0,x,y,size,size);
    }
    public static void portrait(Graphics g,boolean masked,int pose,int x,int y,int size){
        cell(g,masked?MASKED:HERO,pose,x,y,size,size);
    }
    public static void mask(Graphics g,double x,double y){
        int px=(int)x,py=(int)y;
        g.setColor(new Color(94,96,108));
        g.fillPolygon(new int[]{px-4,px+3,px+5,px+3,px-2,px-5},new int[]{py-3,py-3,py,py+4,py+5,py+1},6);
        g.setColor(new Color(18,20,26));g.fillRect(px-3,py,2,2);
    }
}
