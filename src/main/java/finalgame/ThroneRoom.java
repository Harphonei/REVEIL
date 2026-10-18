package finalgame;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

/** The unlit inner sanctuary and the masked guardian of the throne. */
public final class ThroneRoom {
    private static final double ZOOM=1.10,ACTOR_SCALE=1.35/ZOOM,SPEED=90*ACTOR_SCALE;
    private static final int HERO_WIDTH=(int)Math.round(36*ACTOR_SCALE);
    private static final int HERO_HEIGHT=(int)Math.round(54*ACTOR_SCALE);
    private final BufferedImage background=load("throne-map.png");
    private final BufferedImage floor=load("throne-floor.png"),objects=load("throne-objects.png");
    private final int width=background.getWidth(),height=background.getHeight();
    private final BufferedImage collision=makeCollisionOverlay();
    private final int[] blockedPrefix=makePrefix();
    private final SpriteAnimator animation=new SpriteAnimator();
    private double x,y,cameraX,cameraY;
    private boolean backRequested,showCollision,battleRequested,cleared;
    private static final int BOSS_X=1560,BOSS_Y=420;

    public ThroneRoom(){reset();}
    private static BufferedImage load(String name) {
        try {
            BufferedImage im=ImageIO.read(ThroneRoom.class.getResource("/finalgame/"+name));
            if(im==null)throw new IllegalStateException("Missing "+name);
            return im;
        }catch(Exception e){throw new IllegalStateException("Cannot load "+name,e);}
    }
    private int[] makePrefix() {
        if(floor.getWidth()!=width||floor.getHeight()!=height||objects.getWidth()!=width||objects.getHeight()!=height)
            throw new IllegalStateException("Throne collision size mismatch");
        int stride=width+1;int[] sums=new int[stride*(height+1)];
        for(int py=0;py<height;py++) {
            int row=0;
            for(int px=0;px<width;px++) {
                if((floor.getRGB(px,py)&255)<128||(objects.getRGB(px,py)&255)>=128)row++;
                sums[(py+1)*stride+px+1]=sums[py*stride+px+1]+row;
            }
        }
        return sums;
    }
    private BufferedImage makeCollisionOverlay() {
        BufferedImage im=new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
        for(int py=0;py<height;py++)for(int px=0;px<width;px++) {
            if((objects.getRGB(px,py)&255)>=128)im.setRGB(px,py,new Color(240,40,40,115).getRGB());
            else if((floor.getRGB(px,py)&255)>=128)im.setRGB(px,py,new Color(25,210,65,65).getRGB());
        }
        return im;
    }
    public void reset(){cleared=false;battleRequested=false;enter();}
    public void enter(){x=100;y=392;backRequested=false;battleRequested=false;animation.reset(SpriteAnimator.RIGHT);updateCamera();}
    public void stopMovement(){animation.stop();}
    public void toggleCollisionView(){showCollision=!showCollision;}
    private boolean canStand(double px,double py) {
        int left=(int)Math.floor(px+4),right=(int)Math.ceil(px+12);
        int top=(int)Math.floor(py+10),bottom=(int)Math.ceil(py+16);
        if(left<0||top<0||right>width||bottom>height)return false;
        int stride=width+1;
        return blockedPrefix[bottom*stride+right]-blockedPrefix[top*stride+right]
                -blockedPrefix[bottom*stride+left]+blockedPrefix[top*stride+left]==0;
    }
    public void update(int dx,int dy,double seconds,boolean running) {
        double ox=x,oy=y,length=Math.hypot(dx,dy);
        if(length>0) {
            double distance=SPEED*(running?1.65:1)*Math.max(0,Math.min(seconds,.1));
            int steps=Math.max(1,(int)Math.ceil(distance/2));
            for(int i=0;i<steps;i++) {
                double nx=x+dx/length*distance/steps;if(canStand(nx,y))x=nx;
                double ny=y+dy/length*distance/steps;if(canStand(x,ny))y=ny;
            }
        }
        if(!cleared && Math.hypot(x+8-BOSS_X,y+16-BOSS_Y)<=70)battleRequested=true;
        animation.update(x-ox,y-oy,seconds,running);
        // Return only when the left edge of the feet reaches the end of the corridor.
        if(x+4<=54)backRequested=true;
        updateCamera();
    }
    private void updateCamera() {
        cameraX=Math.max(0,Math.min(x+8-960/(2*ZOOM),Math.max(0,width-960/ZOOM)));
        cameraY=Math.max(0,Math.min(y+8-520/(2*ZOOM),Math.max(0,height-520/ZOOM)));
    }
    public boolean consumeBackRequest(){boolean b=backRequested;backRequested=false;return b;}
    public boolean consumeBattleRequest(){boolean b=battleRequested;battleRequested=false;return b;}
    public void returnFromBattle(){cleared=true;battleRequested=false;stopMovement();}
    public void skipToHall(){x=1445;y=404;backRequested=false;battleRequested=false;animation.reset(SpriteAnimator.RIGHT);updateCamera();}
    private int ambientShade() {
        if(x<=590)return 0;
        if(x<1090)return (int)Math.round((x-590)/500*16);
        return Math.min(32,16+(int)Math.round(Math.max(0,x-1090)/260*16));
    }
    public void draw(Graphics graphics) {
        Graphics2D g=(Graphics2D)graphics.create();g.setClip(0,60,960,520);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.translate(0,60);g.scale(ZOOM,ZOOM);g.translate(-Math.round(cameraX),-Math.round(cameraY));
        g.drawImage(background,0,0,null);
        if(!cleared && y+16<BOSS_Y)BossArtwork.drawMaskedMap(g,BOSS_X,BOSS_Y,(int)Math.round(HERO_HEIGHT*1.30));
        Graphics2D actor=(Graphics2D)g.create();
        animation.drawInThrone(actor,(int)Math.round(x+8),(int)Math.round(y+16),HERO_WIDTH,HERO_HEIGHT,ambientShade());
        actor.dispose();
        if(!cleared && y+16>=BOSS_Y)BossArtwork.drawMaskedMap(g,BOSS_X,BOSS_Y,(int)Math.round(HERO_HEIGHT*1.30));
        if(showCollision){g.drawImage(collision,0,0,null);g.setColor(Color.CYAN);g.drawRect((int)x+4,(int)y+10,8,6);}
        g.dispose();
        graphics.setColor(new Color(10,13,18));graphics.fillRect(0,0,960,60);graphics.fillRect(0,580,960,60);
        graphics.setColor(new Color(190,194,205));graphics.setFont(new Font(Font.MONOSPACED,Font.BOLD,18));
        graphics.drawString("INNER SANCTUARY v3.58  |  The masked one",20,25);
        graphics.setFont(new Font(Font.MONOSPACED,Font.PLAIN,14));
        graphics.drawString("WASD: Walk | Shift: Run | Esc: Pause | F3: Collision | F6: Skip",20,47);
        graphics.setColor(new Color(150,156,172));
        graphics.drawString(x<1170?"Only your footsteps answer.":"Silence fills the throne hall.",20,612);
    }
}
