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
        rig(g,MASKED_MOTION,pose,96,x,y,height,true);
    }
    public static void wounded(Graphics g,int frame,double x,double y,int height){
        rig(g,HERO_MOTION,frame,64,x,y,height,false);
    }
    /** Match the established walking sprite's head, shoulders and soles, not canvas height alone. */
    private static void rig(Graphics graphics,BufferedImage sheet,int frame,int tile,double x,double y,int height,boolean masked){
        int cols=sheet.getWidth()/tile,ox=frame%cols*tile,oy=frame/cols*tile;
        int top=tile,bottom=-1;
        for(int py=0;py<tile;py++)for(int px=0;px<tile;px++)if((sheet.getRGB(ox+px,oy+py)>>>24)>128){top=Math.min(top,py);bottom=Math.max(bottom,py);}
        int split=masked?top+(frame==3?16:20):new int[]{32,32,32,44,49,49,38,32}[frame];
        int left=tile,right=-1;
        int crownEnd=top+(int)Math.round((split-top)*.65);
        for(int py=top;py<Math.min(crownEnd,bottom);py++)for(int px=0;px<tile;px++)if((sheet.getRGB(ox+px,oy+py)>>>24)>128){left=Math.min(left,px);right=Math.max(right,px);}
        double nativeScale=height/64.0,bodyScale=nativeScale*(masked?1.25:1.20);
        int total=(int)Math.round((bottom-top+1)*nativeScale)+1;
        int headHeight=(int)Math.round(height*18.0/36),bodyHeight=Math.max(8,total-headHeight);
        double headScale=height*20.0/36/Math.max(1,right-left+1),centre=(left+right+1)/2.0;
        double anchor=x+(centre-tile/2.0)*bodyScale;
        Graphics2D g=(Graphics2D)graphics.create();g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        int by=(int)Math.round(y)-bodyHeight,bl=(int)Math.round(x-tile/2.0*bodyScale),bw=(int)Math.round(tile*bodyScale);
        g.drawImage(sheet,bl,by,bl+bw,(int)Math.round(y),ox,oy+split,ox+tile,oy+bottom+1,null);
        int hl=(int)Math.round(anchor-centre*headScale),hw=(int)Math.round(tile*headScale);
        g.drawImage(sheet,hl,by-headHeight,hl+hw,by,ox,oy+top,ox+tile,oy+split,null);g.dispose();
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
