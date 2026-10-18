package finalgame;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/** One image shared by all actors, independent animation state per actor. */
public final class SpriteAnimator {
    public static final int DOWN=0, LEFT=1, UP=2, RIGHT=3;
    public static final int DOWN_RIGHT=4, DOWN_LEFT=5, UP_RIGHT=6, UP_LEFT=7;
    private static final int WIDTH=24, HEIGHT=36;
    private static final BufferedImage SHEET=loadSheet();
    public enum Lighting {
        NEUTRAL(1,1,1,0), FOREST(.82,.85,.80,.12),
        MINE(.63,.69,.76,.20), BUTTERFLY(.57,.63,.70,.24),
        TEMPLE(.64,.66,.76,.30), TEMPLE_STONE(.78,.80,.90,.65),
        TEMPLE_STATUE(.66,.68,.75,.72),
        THRONE_CORRIDOR(.53,.55,.65,.38), THRONE_ANTECHAMBER(.44,.46,.57,.42),
        THRONE_HALL(.30,.33,.44,.50);
        private final BufferedImage sheet;
        private final double red,green,blue,desaturation;
        Lighting(double red,double green,double blue,double desaturation) {
            this.red=red;this.green=green;this.blue=blue;this.desaturation=desaturation;
            sheet=tint(SHEET);
        }
        BufferedImage tint(BufferedImage source) {
            BufferedImage result=new BufferedImage(source.getWidth(),source.getHeight(),BufferedImage.TYPE_INT_ARGB);
            for(int y=0;y<source.getHeight();y++)for(int x=0;x<source.getWidth();x++) {
                int argb=source.getRGB(x,y),r=(argb>>16)&255,g=(argb>>8)&255,b=argb&255;
                double gray=.2126*r+.7152*g+.0722*b;
                int rr=(int)Math.round((r*(1-desaturation)+gray*desaturation)*red);
                int gg=(int)Math.round((g*(1-desaturation)+gray*desaturation)*green);
                int bb=(int)Math.round((b*(1-desaturation)+gray*desaturation)*blue);
                result.setRGB(x,y,(argb&0xff000000)|(rr<<16)|(gg<<8)|bb);
            }
            return result;
        }
    }
    private final Lighting lighting;
    public SpriteAnimator(){this(Lighting.NEUTRAL);}
    public SpriteAnimator(Lighting lighting){this.lighting=java.util.Objects.requireNonNull(lighting);}
    private int facing=DOWN;
    private double clock;
    private boolean moving, running;

    private static BufferedImage loadSheet() {
        try {
            java.net.URL resource=SpriteAnimator.class.getResource("/finalgame/hero.png");
            if(resource==null) throw new IOException("Missing hero.png");
            BufferedImage image=ImageIO.read(resource);
            if(image==null || image.getWidth()!=72 || image.getHeight()!=288)
                throw new IOException("Invalid hero sheet: expected 72 x 288");
            return image;
        } catch(IOException e) { throw new IllegalStateException("Cannot load hero sprite",e); }
    }

    public void reset(int direction) { facing=direction; stop(); }
    public void stop() { moving=false; running=false; clock=0; }

    public void update(double dx,double dy,double seconds,boolean sprint) {
        boolean wasMoving=moving;
        moving=Math.hypot(dx,dy)>0.00001;
        if(!moving) { stop(); return; }
        double ax=Math.abs(dx),ay=Math.abs(dy);
        // Use actual displacement: sliding along a wall faces along the wall.
        if(Math.min(ax,ay)>Math.max(ax,ay)*0.41421356237) {
            facing=dy>0?(dx>0?DOWN_RIGHT:DOWN_LEFT):(dx>0?UP_RIGHT:UP_LEFT);
        } else if(ax>ay) facing=dx<0?LEFT:RIGHT;
        else facing=dy<0?UP:DOWN;
        if(!wasMoving || running!=sprint) clock=0;
        running=sprint;
        clock=(clock+Math.max(0,seconds))%1000;
    }

    public int getFacing() { return facing; }
    public int getFrame() {
        if(!moving) return 0;
        int step=(int)(clock/(running?0.085:0.14))%4;
        return step==0?1:step==2?2:0;
    }

    /** Feet anchor stays fixed; artwork may extend above the collision box. */
    public void draw(Graphics g,int footX,int footY,int width,int height) {
        drawSheet(g,lighting.sheet,footX,footY,width,height);
    }
    public void drawInThrone(Graphics g,int footX,int footY,int width,int height,int shade) {
        drawSheet(g,ThroneLighting.SHEETS[Math.max(0,Math.min(32,shade))],footX,footY,width,height);
    }
    private void drawSheet(Graphics g,BufferedImage sheet,int footX,int footY,int width,int height) {
        int frame=getFrame(),sx=frame*WIDTH,sy=facing*HEIGHT;
        g.drawImage(sheet,footX-width/2,footY-height,footX-width/2+width,footY,
                sx,sy,sx+WIDTH,sy+HEIGHT,null);
    }
    /** Cache 33 lighting levels once; movement/animation frames never restart on zone changes. */
    private static final class ThroneLighting {
        static final BufferedImage[] SHEETS=build();
        private static BufferedImage[] build() {
            BufferedImage[] result=new BufferedImage[33];
            BufferedImage[] anchors={Lighting.THRONE_CORRIDOR.sheet,Lighting.THRONE_ANTECHAMBER.sheet,Lighting.THRONE_HALL.sheet};
            for(int level=0;level<=32;level++) {
                int segment=Math.min(1,level/16);double t=(level-segment*16)/16.0;
                BufferedImage image=new BufferedImage(SHEET.getWidth(),SHEET.getHeight(),BufferedImage.TYPE_INT_ARGB);
                for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++) {
                    int a=anchors[segment].getRGB(x,y),b=anchors[segment+1].getRGB(x,y),pixel=a&0xff000000;
                    for(int shift:new int[]{16,8,0}) {
                        int channel=(int)Math.round(((a>>shift)&255)*(1-t)+((b>>shift)&255)*t);
                        pixel|=channel<<shift;
                    }
                    image.setRGB(x,y,pixel);
                }
                result[level]=image;
            }
            return result;
        }
    }

}
