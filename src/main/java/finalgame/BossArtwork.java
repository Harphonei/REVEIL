package finalgame;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/** Static artwork; animation/cutscene poses will be added later. */
public final class BossArtwork {
    private static final BufferedImage MAP=SpriteAnimator.Lighting.FOREST.tint(load("forest-boss-map.png"));
    private static final BufferedImage BATTLE=load("forest-boss-battle.png");
    private static final BufferedImage FOREST_POSES=SpriteAnimator.Lighting.FOREST.tint(load("forest-boss-poses.png"));
    public static void drawForestPose(Graphics graphics,int footX,int footY,int pose) {
        Graphics2D g=(Graphics2D)graphics.create();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        int width=128,height=96,left=footX-width/2,top=footY-90;
        int sx=Math.max(0,Math.min(2,pose))*128;
        g.drawImage(FOREST_POSES,left,top,left+width,top+height,sx,0,sx+128,96,null);
        g.dispose();
    }
    private static final BufferedImage SCIENTIST_MAP=SpriteAnimator.Lighting.MINE.tint(load("scientist-map.png"));
    private static final BufferedImage SCIENTIST_WORKING=SpriteAnimator.Lighting.MINE.tint(load("scientist-working.png"));
    private static final BufferedImage SCIENTIST_BATTLE=load("scientist-battle.png");
    private static final BufferedImage MOTH_MAP=SpriteAnimator.Lighting.BUTTERFLY.tint(load("moth-map.png"));
    private static final BufferedImage MOTH_BATTLE=load("moth-battle.png");
    private static final BufferedImage KNIGHT_MAP=SpriteAnimator.Lighting.TEMPLE_STATUE.tint(load("knight-map.png"));
    private static final BufferedImage KNIGHT_BATTLE=load("knight-battle.png");
    private static final BufferedImage MASKED_MAP=SpriteAnimator.Lighting.THRONE_HALL.tint(load("masked-map.png"));
    private static final BufferedImage MASKED_ONE=load("masked-battle-1.png");
    private static final BufferedImage MASKED_TWO=load("masked-battle-2.png");
    public static void drawMaskedMap(Graphics g,int footX,int footY,int height) {
        int width=(int)Math.round(MASKED_MAP.getWidth()*height/(double)MASKED_MAP.getHeight());
        draw(g,MASKED_MAP,footX-width/2,footY-height,width,height);
    }
    public static void drawMaskedBattle(Graphics graphics,Rectangle bounds,boolean phaseTwo,double transition) {
        Graphics2D g=(Graphics2D)graphics.create();
        if(phaseTwo && transition>0) {
            float blend=(float)Math.max(0,Math.min(1,1-transition/1.1));
            g.setComposite(AlphaComposite.SrcOver.derive(1-blend));
            draw(g,MASKED_ONE,bounds.x,bounds.y,bounds.width,bounds.height);
            g.setComposite(AlphaComposite.SrcOver.derive(blend));
        }
        draw(g,phaseTwo?MASKED_TWO:MASKED_ONE,bounds.x,bounds.y,bounds.width,bounds.height);
        g.dispose();
    }
    private BossArtwork() {}
    private static BufferedImage load(String name) {
        try {
            java.net.URL url=BossArtwork.class.getResource("/finalgame/"+name);
            if(url==null) throw new IOException("Missing "+name);
            BufferedImage result=ImageIO.read(url);
            if(result==null) throw new IOException("Invalid "+name);
            return result;
        } catch(IOException e) { throw new IllegalStateException("Cannot load boss artwork",e); }
    }
    public static void drawMap(Graphics g,int footX,int footY) {
        g.setColor(new Color(0,0,0,65));
        // Anchor visible soles, not transparent canvas padding.
        int height=108;
        int width=(int)Math.round(MAP.getWidth()*height/(double)MAP.getHeight());
        int left=footX-width/2;
        g.fillOval(left+width/5-5,footY-4,16,5);
        g.fillOval(left+width*4/5-5,footY-3,17,5);
        draw(g,MAP,left,footY-height+1,width,height);
    }
    public static void drawBattle(Graphics g,Rectangle bounds) {
        draw(g,BATTLE,bounds.x,bounds.y,bounds.width,bounds.height);
    }
    public static void drawScientistMap(Graphics g,int footX,int footY) {
        int height=90;
        int width=(int)Math.round(SCIENTIST_MAP.getWidth()*height/(double)SCIENTIST_MAP.getHeight());
        draw(g,SCIENTIST_MAP,footX-width/2,footY-height,width,height);
    }
    public static void drawScientistWorking(Graphics g,int footX,int footY){
        int height=90,width=(int)Math.round(SCIENTIST_WORKING.getWidth()*height/(double)SCIENTIST_WORKING.getHeight());
        draw(g,SCIENTIST_WORKING,footX-width/2,footY-height,width,height);
    }
    public static void drawScientistBattle(Graphics g,Rectangle bounds) {
        draw(g,SCIENTIST_BATTLE,bounds.x,bounds.y,bounds.width,bounds.height);
    }
    public static void drawMothMap(Graphics g,int anchorX,int anchorY) {
        int height=120;
        int width=(int)Math.round(MOTH_MAP.getWidth()*height/(double)MOTH_MAP.getHeight());
        draw(g,MOTH_MAP,anchorX-width/2,anchorY-height,width,height);
    }
    public static void drawMothBattle(Graphics g,Rectangle bounds) {
        draw(g,MOTH_BATTLE,bounds.x,bounds.y,bounds.width,bounds.height);
    }
    public static void drawKnightMap(Graphics g,int footX,int footY) {
        drawKnightMap(g,footX,footY,1.0);
    }
    public static void drawKnightMap(Graphics g,int footX,int footY,double scale) {
        int height=(int)Math.round(116*scale);
        int width=(int)Math.round(KNIGHT_MAP.getWidth()*height/(double)KNIGHT_MAP.getHeight());
        // The approved blade ends two native rows below the boot soles.
        int swordTip=(int)Math.round(2*height/(double)KNIGHT_MAP.getHeight());
        draw(g,KNIGHT_MAP,footX-width/2,footY-height+swordTip,width,height);
    }
    public static void drawKnightBattle(Graphics g,Rectangle bounds) {
        draw(g,KNIGHT_BATTLE,bounds.x,bounds.y,bounds.width,bounds.height);
    }
    private static void draw(Graphics graphics,BufferedImage image,int x,int y,int w,int h) {
        Graphics2D g=(Graphics2D)graphics.create();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(image,x,y,w,h,null);g.dispose();
    }
}
