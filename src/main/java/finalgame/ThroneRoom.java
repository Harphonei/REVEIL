package finalgame;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

/** The unlit inner sanctuary and the masked guardian of the throne. */
public final class ThroneRoom {
    private static final double ZOOM=1.18,ACTOR_SCALE=1.35/1.10,SPEED=90*ACTOR_SCALE;
    private static final double ACTOR_ART_SCALE=1.0;
    private static final int HERO_WIDTH=(int)Math.round(36*ACTOR_SCALE*ACTOR_ART_SCALE);
    private static final int HERO_HEIGHT=(int)Math.round(54*ACTOR_SCALE*ACTOR_ART_SCALE);
    private final BufferedImage background=load("throne-map.png");
    private final BufferedImage floor=load("throne-floor.png"),objects=load("throne-objects.png");
    private final int width=background.getWidth(),height=background.getHeight();
    private final BufferedImage collision=makeCollisionOverlay();
    private final int[] blockedPrefix=makePrefix();
    private final SpriteAnimator animation=new SpriteAnimator();
    private double x,y,cameraX,cameraY;
    private boolean backRequested,showCollision,battleRequested,cleared;
    private static final int BOSS_X=1560,BOSS_Y=420;
    private boolean cinematic,injured,cloaked,newcomer,maskOnFloor;
    private int heroPose=-1,bossPose;
    private double bossX=BOSS_X,bossY=BOSS_Y,zoom=ZOOM,bodyOpacity=.28,impact,bolt=-1;
    private final SpriteAnimator echo=new SpriteAnimator();
    private double entryX=1312,entryY=404,echoX=1312,echoY=404;
    private int woundedFrame;
    public double entryFootX(){return entryX+8;}
    public double entryFootY(){return entryY+16;}
    public double focusX(){return cameraX+480/zoom;}
    public void rememberEntry(){entryX=x;entryY=y;}
    public void woundedFrame(int frame){heroPose=0;woundedFrame=frame;}
    public void woundedWalk(double nx,double ny,double time){moveHero(nx,ny,.05);woundedFrame(new int[]{1,0,2,0}[(int)(time/.22)%4]);}
    public void turnBoss(double p){bossPose=p<.23?0:p<.48?5:p<.73?1:2;}
    public double storyX(){return x;}
    public double storyY(){return y;}
    public void beginStory(){cinematic=true;stopMovement();}
    public void finishStory(){cinematic=false;impact=0;zoom=ZOOM;stopMovement();updateCamera();}
    public void resetStory(){cinematic=false;injured=false;cloaked=false;newcomer=false;maskOnFloor=false;heroPose=-1;bossPose=0;bossX=BOSS_X;bossY=BOSS_Y;zoom=ZOOM;bodyOpacity=.28;impact=0;bolt=-1;woundedFrame=0;entryX=1312;entryY=404;}
    public void focus(double cx,double cy,double z){
        cinematic=true;zoom=z;
        cameraX=Math.max(0,Math.min(cx-480/z,width-960/z));
        cameraY=Math.max(0,Math.min(cy-260/z,height-520/z));
    }
    public void moveHero(double nx,double ny,double dt){animation.update(nx-x,ny-y,dt,false);x=nx;y=ny;}
    public void faceHero(int direction){animation.reset(direction);}
    public void heroPose(int pose){heroPose=pose;stopMovement();}
    public void bossPose(int pose){bossPose=pose;}
    public void moveBoss(double nx,double ny){bossX=nx;bossY=ny;}
    public void impact(double value){impact=value;}
    public void bolt(double value){bolt=value;}
    public void injure(){injured=true;heroPose=0;woundedFrame=0;}
    public void prepareEnding(){
        cleared=true;cinematic=true;x=1480;y=404;bossX=BOSS_X;bossY=BOSS_Y;
        injured=true;heroPose=0;woundedFrame=0;maskOnFloor=true;bodyOpacity=.55;
    }
    public void takeMask(){maskOnFloor=false;}
    public void wearCloak(){cloaked=true;heroPose=-1;x=1552;y=404;bossPose=0;}
    public void revealBody(double p){bodyOpacity=.55+.45*p;}
    public void showNewcomer(){newcomer=true;echoX=entryX-16;echoY=entryY;echo.reset(SpriteAnimator.RIGHT);}
    public void approachNewcomer(double p){double nx=entryX-16+16*p;echo.update(nx-echoX,0,.05,false);echoX=nx;if(p>=1)echo.reset(SpriteAnimator.RIGHT);}
    public void turnToNewcomer(int pose){bossPose=pose;}

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
    public void reset(){cleared=false;battleRequested=false;resetStory();enter();}
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
        if(!cleared && x+8>=1320 && y+16>=300 && y+16<=520)battleRequested=true;
        animation.update(x-ox,y-oy,seconds,running);
        // Return only when the left edge of the feet reaches the end of the corridor.
        if(x+4<=54)backRequested=true;
        updateCamera();
    }
    private void updateCamera() {
        if(cinematic)return;
        cameraX=Math.max(0,Math.min(x+8-960/(2*ZOOM),Math.max(0,width-960/ZOOM)));
        cameraY=Math.max(0,Math.min(y+8-520/(2*ZOOM),Math.max(0,height-520/ZOOM)));
    }
    public boolean consumeBackRequest(){boolean b=backRequested;backRequested=false;return b;}
    public boolean consumeBattleRequest(){boolean b=battleRequested;battleRequested=false;return b;}
    public void returnFromBattle(){cleared=true;battleRequested=false;stopMovement();}
    public void skipToHall(){x=1312;y=404;backRequested=false;battleRequested=false;animation.reset(SpriteAnimator.RIGHT);updateCamera();}
    private int ambientShade() {
        if(x<=590)return 0;
        if(x<1090)return (int)Math.round((x-590)/500*16);
        return Math.min(32,16+(int)Math.round(Math.max(0,x-1090)/260*16));
    }
    public void draw(Graphics graphics) {
        Graphics2D g=(Graphics2D)graphics.create();g.setClip(0,60,960,520);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.translate(0,60);g.scale(zoom,zoom);g.translate(-Math.round(cameraX),-Math.round(cameraY));
        if(impact>0)g.translate(Math.sin(impact*67)*impact*5,Math.cos(impact*53)*impact*3);
        g.drawImage(background,0,0,null);
        Graphics2D remains=(Graphics2D)g.create();
        remains.setComposite(AlphaComposite.SrcOver.derive((float)bodyOpacity));
        FinalArtwork.hero(remains,5,1610,431,HERO_HEIGHT);remains.dispose();
        if(maskOnFloor)FinalArtwork.mask(g,1573,426);
        if(!cleared)FinalArtwork.masked(g,bossPose,bossX,bossY,HERO_HEIGHT);
        Graphics2D actor=(Graphics2D)g.create();
        if(cloaked)FinalArtwork.masked(actor,bossPose,x+8,y+16,HERO_HEIGHT);
        else if(heroPose>=0)FinalArtwork.wounded(actor,woundedFrame,x+8,y+16,HERO_HEIGHT);
        else animation.drawInThrone(actor,(int)Math.round(x+8),(int)Math.round(y+16),HERO_WIDTH,HERO_HEIGHT,ambientShade());
        actor.dispose();
        if(newcomer)echo.drawInThrone(g,(int)Math.round(echoX+8),(int)Math.round(echoY+16),HERO_WIDTH,HERO_HEIGHT,32);
        if(bolt>=0){
            double handX=bossX-20*ACTOR_ART_SCALE,handY=bossY-35*ACTOR_ART_SCALE;
            double px=handX+(x+8-handX)*bolt,py=handY+(y+16-HERO_HEIGHT*.68-handY)*bolt;
            g.setColor(new Color(92,37,52));g.fillRect((int)px-7,(int)py-3,14,6);
            g.setColor(new Color(194,129,139));g.fillRect((int)px-3,(int)py-1,6,2);
        }
        if(showCollision){g.drawImage(collision,0,0,null);g.setColor(Color.CYAN);g.drawRect((int)x+4,(int)y+10,8,6);}
        g.dispose();
        graphics.setColor(new Color(10,13,18));graphics.fillRect(0,0,960,60);graphics.fillRect(0,580,960,60);
        if(cinematic)return;
        graphics.setColor(new Color(190,194,205));graphics.setFont(new Font(Font.MONOSPACED,Font.BOLD,18));
        graphics.drawString("INNER SANCTUARY  |  The masked one",20,25);
        graphics.setFont(new Font(Font.MONOSPACED,Font.PLAIN,14));
graphics.drawString("WASD: Walk | Shift: Run | Esc: Pause | F3: Collision | F6: Skip | F7: Replay",20,47);
        graphics.setColor(new Color(150,156,172));
        graphics.drawString(x<1170?"Only your footsteps answer.":"Silence fills the throne hall.",20,612);
    }
}
