package finalgame;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

/** Temple world uses native artwork pixels; collision is always at the feet. */
public final class TempleRoom {
    private static final double ZOOM=1.35, SPEED=90;
    private final BufferedImage background;
    private final BufferedImage foreground;
    private final BufferedImage floorDebug=loadDebugMask("temple-floor.png",new Color(30,240,80,65));
    private final BufferedImage objectsDebug=loadDebugMask("temple-objects.png",new Color(250,50,40,120));
    private final int[] blockedPrefix=createBlockedPrefix();
    private final Rectangle exitDoor=new Rectangle(1110,120,84,20);
    private final SpriteAnimator animation=new SpriteAnimator(SpriteAnimator.Lighting.TEMPLE);
    private double x,y,cameraX,cameraY;
    private boolean backRequested,exitReached,showCollision,cleared,battleRequested;
    private final Rectangle firstStep=new Rectangle(1077,346,152,12);
    private final Rectangle upperStairs=new Rectangle(1072,110,160,236);

    public TempleRoom() {
        try {
            background=ImageIO.read(TempleRoom.class.getResource("/finalgame/temple-map.png"));
            foreground=ImageIO.read(TempleRoom.class.getResource("/finalgame/temple-foreground.png"));
            if(foreground==null)throw new IllegalStateException("Missing temple foreground");
            if(background==null || background.getWidth()!=1536 || background.getHeight()!=1024)
                throw new IllegalStateException("Invalid temple map");
        } catch(Exception ex) {throw new IllegalStateException("Cannot load temple-map.png",ex);}
        reset();
    }
    private static BufferedImage loadDebugMask(String name,Color tint) {
        try {
            BufferedImage mask=ImageIO.read(TempleRoom.class.getResource("/finalgame/"+name));
            if(mask==null || mask.getWidth()!=1536 || mask.getHeight()!=1024)
                throw new IllegalStateException("Invalid temple collision mask: "+name);
            BufferedImage overlay=new BufferedImage(1536,1024,BufferedImage.TYPE_INT_ARGB);
            for(int y=0;y<1024;y++)for(int x=0;x<1536;x++)
                if((mask.getRGB(x,y)&255)>=128)overlay.setRGB(x,y,tint.getRGB());
            return overlay;
        } catch(Exception ex) {throw new IllegalStateException("Cannot load "+name,ex);}
    }
    private static int[] createBlockedPrefix() {
        try {
            BufferedImage ground=ImageIO.read(TempleRoom.class.getResource("/finalgame/temple-floor.png"));
            BufferedImage objects=ImageIO.read(TempleRoom.class.getResource("/finalgame/temple-objects.png"));
            int stride=1537;
            int[] sums=new int[stride*1025];
            for(int y=0;y<1024;y++) {
                int row=0;
                for(int x=0;x<1536;x++) {
                    if((ground.getRGB(x,y)&255)<128 || (objects.getRGB(x,y)&255)>=128)row++;
                    sums[(y+1)*stride+x+1]=sums[y*stride+x+1]+row;
                }
            }
            return sums;
        } catch(Exception ex) {throw new IllegalStateException("Cannot index temple collision masks",ex);}
    }
    public void reset() {
        cleared=false;battleRequested=false;exitReached=false;enter();
    }
    public void enter() {
        x=40;y=508;exitReached=false;backRequested=false;battleRequested=false;
        animation.reset(SpriteAnimator.RIGHT);updateCamera();
    }
    public void stopMovement(){animation.stop();}
    public void toggleCollisionView(){showCollision=!showCollision;}
    private boolean canStand(double px,double py) {
        Rectangle2D feet=new Rectangle2D.Double(px+4,py+10,8,6);
        // Test every mask pixel touched by the feet in constant time. Artwork
        // contours stay exact without scanning a complex Area on every step.
        int left=(int)Math.floor(feet.getMinX()),top=(int)Math.floor(feet.getMinY());
        int right=(int)Math.ceil(feet.getMaxX()),bottom=(int)Math.ceil(feet.getMaxY());
        if(left<0||top<0||right>1536||bottom>1024)return false;
        int stride=1537;
        if(blockedPrefix[bottom*stride+right]-blockedPrefix[top*stride+right]
                -blockedPrefix[bottom*stride+left]+blockedPrefix[top*stride+left]>0)return false;
        if(!cleared && upperStairs.intersects(feet))return false;
        return true;
    }
    public void update(int dx,int dy,double seconds,boolean running) {
        double ox=x,oy=y,length=Math.hypot(dx,dy);
        if(length>0) {
            double distance=SPEED*(running?1.65:1)*Math.max(0,Math.min(seconds,.1));
            int steps=Math.max(1,(int)Math.ceil(distance/2));
            for(int i=0;i<steps;i++) {
                double nx=x+dx/length*distance/steps;
                if(canStand(nx,y))x=nx;
                double ny=y+dy/length*distance/steps;
                if(canStand(x,ny))y=ny;
            }
        }
        animation.update(x-ox,y-oy,seconds,running);
        if(!cleared && firstStep.contains(x+8,y+13))battleRequested=true;
        if(x<12)backRequested=true;
        if(exitDoor.contains(x+4,y+10,8,6))exitReached=true;
        updateCamera();
    }
    private void updateCamera() {
        cameraX=Math.max(0,Math.min(x+8-960/(2*ZOOM),background.getWidth()-960/ZOOM));
        cameraY=Math.max(0,Math.min(y+8-520/(2*ZOOM),background.getHeight()-520/ZOOM));
    }
    public boolean consumeBattleRequest(){boolean b=battleRequested;battleRequested=false;return b;}
    public void returnFromBattle(){cleared=true;battleRequested=false;x=1140;y=367;animation.reset(SpriteAnimator.UP);updateCamera();}
    public boolean consumeBackRequest(){boolean b=backRequested;backRequested=false;return b;}
    public void skipToExit(){cleared=true;battleRequested=false;x=1140;y=112;exitReached=true;animation.reset(SpriteAnimator.UP);updateCamera();}
    public void draw(Graphics graphics) {
        Graphics2D g=(Graphics2D)graphics.create();
        g.setClip(0,60,960,520);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.translate(0,60);g.scale(ZOOM,ZOOM);g.translate(-Math.round(cameraX),-Math.round(cameraY));
        g.drawImage(background,0,0,null);
        if(!cleared)BossArtwork.drawKnightMap(g,1308,258);
        Graphics2D actor=(Graphics2D)g.create();
        if(x+8>=1110 && x+8<1194 && y+16<200)actor.clipRect(1105,116,96,120);
        animation.draw(actor,(int)Math.round(x+8),(int)Math.round(y+16),36,54);actor.dispose();
        g.drawImage(foreground,0,0,null);
        if(showCollision) {
            g.drawImage(floorDebug,0,0,null);
            g.drawImage(objectsDebug,0,0,null);
            g.setColor(Color.CYAN);g.draw(new Rectangle2D.Double(x+4,y+10,8,6));
            g.setColor(Color.ORANGE);g.draw(exitDoor);
            if(!cleared){g.setColor(Color.MAGENTA);g.draw(firstStep);}
        }
        g.dispose();
        graphics.setColor(new Color(15,20,19));graphics.fillRect(0,0,960,60);graphics.fillRect(0,580,960,60);
        graphics.setColor(Color.WHITE);graphics.setFont(new Font(Font.MONOSPACED,Font.BOLD,18));
        graphics.drawString("RUINED TEMPLE v3.33  |  The silent forecourt",20,25);
        graphics.setFont(new Font(Font.MONOSPACED,Font.PLAIN,14));
        graphics.drawString("WASD: Walk | Shift: Run | Esc: Pause | F3: Collision | F6: Skip map",20,47);
        graphics.setColor(Color.LIGHT_GRAY);
        graphics.drawString(exitReached?"Inner temple reached. The next area is coming soon.":
            "Follow the stone path to the temple entrance.",20,612);
    }
}
