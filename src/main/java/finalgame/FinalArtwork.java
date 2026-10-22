package finalgame;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

/** Cutscene-only art. All poses share the same pixel grid and foot anchor. */
public final class FinalArtwork {
    private static final BufferedImage HERO=load("final-hero.png"),MASKED=load("final-masked.png");
    private static final BufferedImage HERO_DARK=SpriteAnimator.Lighting.THRONE_HALL.tint(HERO);
    private static final BufferedImage MASKED_DARK=SpriteAnimator.Lighting.THRONE_CORRIDOR.tint(MASKED);
    private static BufferedImage load(String file){
        try{return ImageIO.read(FinalArtwork.class.getResource("/finalgame/"+file));}
        catch(Exception ex){throw new IllegalStateException("Cannot load "+file,ex);}
    }
    private static void cell(Graphics g,BufferedImage sheet,int index,int x,int y,int w,int h){
        int sx=(index%3)*64,sy=(index/3)*64;
        Graphics2D p=(Graphics2D)g.create();
        p.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        p.drawImage(sheet,x,y,x+w,y+h,sx,sy,sx+64,sy+64,null);p.dispose();
    }
    public static void hero(Graphics g,int pose,double x,double y,int height){
        cell(g,HERO_DARK,pose,(int)Math.round(x-height/2.0),(int)Math.round(y-height),height,height);
    }
    public static void masked(Graphics g,int pose,double x,double y,int height){
        cell(g,MASKED_DARK,pose,(int)Math.round(x-height/2.0),(int)Math.round(y-height),height,height);
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
