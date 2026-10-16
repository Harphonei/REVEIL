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
        int frame=getFrame(),sx=frame*WIDTH,sy=facing*HEIGHT;
        g.drawImage(SHEET,footX-width/2,footY-height,footX-width/2+width,footY,
                sx,sy,sx+WIDTH,sy+HEIGHT,null);
    }
}
