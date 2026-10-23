package finalgame;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

/** Mine world uses native artwork pixels; collision is always at the feet. */
public final class MineRoom {
    public enum InspectPoint {
        CART(449,694,449,642,80,false,
            "รถรางเก่า…ข้างในมีแต่เศษหิน", "เหมือนถูกทิ้งไว้นานแล้ว"),
        ORE(580,530,580,480,80,false,
            "แร่นี่…เรืองแสงได้ด้วย", "พอเข้าใกล้แล้วรู้สึกแปลก ๆ แฮะ"),
        DOCUMENT(618,212,618,158,86,true,
            "“ตัวอย่างยังตอบสนองต่อการกระตุ้นด้วยความเจ็บปวด”", "ตัวอย่าง…หมายถึงพวกมอนสเตอร์งั้นเหรอ?");
        final double x,y,promptX,promptY,radius;
        final boolean afterBoss;
        private final String[] lines;
        InspectPoint(double x,double y,double promptX,double promptY,double radius,boolean afterBoss,String... lines){
            this.x=x;this.y=y;this.promptX=promptX;this.promptY=promptY;this.radius=radius;this.afterBoss=afterBoss;this.lines=lines;
        }
        public String[] lines(){return lines.clone();}
    }

    private static final double ZOOM=1.45, SPEED=90;
    private final BufferedImage background;
    private final Area floor=loadMask("mine-floor.png");
    private final Shape[] obstacles={loadMask("mine-objects.png")};
    private final int[] blockedPrefix=createBlockedPrefix();
    private final Rectangle bossZone=new Rectangle(850,205,120,90);
    private final Rectangle exitDoor=new Rectangle(1494,478,32,16);
    private final Rectangle gate=new Rectangle(1005,357,119,22);
    private final SpriteAnimator animation=new SpriteAnimator(SpriteAnimator.Lighting.MINE);
    private double x,y,cameraX,cameraY;
    private boolean cleared,battleRequested,backRequested,exitReached,showCollision;
    private boolean storyCamera,scientistFacingFront;
    private double drawZoom=ZOOM,workBob,workClock;
    private static final double SCIENTIST_X=910,SCIENTIST_Y=175;

    public double storyX(){return x;}
    public double storyY(){return y;}
    public boolean isCleared(){return cleared;}
    public void faceStoryHero(int facing){animation.reset(facing);}
    public void faceScientistFront(){scientistFacingFront=true;workBob=0;}
    public void setWorkBob(double bob){workBob=bob;}
    public void focusStory(double centreX,double centreY,double zoom){
        storyCamera=true;drawZoom=zoom;
        cameraX=Math.max(0,Math.min(centreX-960/(2*zoom),Math.max(0,background.getWidth()-960/zoom)));
        cameraY=Math.max(0,Math.min(centreY-520/(2*zoom),Math.max(0,background.getHeight()-520/zoom)));
    }
    public void moveStoryHero(double px,double py,double seconds){
        double ox=x,oy=y;
        if(canStand(px,py)){x=px;y=py;}
        animation.update(x-ox,y-oy,seconds,false);
    }
    public void finishStory(){storyCamera=false;drawZoom=ZOOM;workBob=0;animation.stop();updateCamera();}

    public MineRoom() {
        try {
            background=ImageIO.read(MineRoom.class.getResource("/finalgame/mine-map.png"));
            if(background==null || background.getWidth()!=1536 || background.getHeight()!=1024)
                throw new IllegalStateException("Invalid mine map");
        } catch(Exception ex) {throw new IllegalStateException("Cannot load mine-map.png",ex);}
        reset();
    }
    private static Area loadMask(String name) {
        try {
            BufferedImage mask=ImageIO.read(MineRoom.class.getResource("/finalgame/"+name));
            if(mask==null || mask.getWidth()!=1536 || mask.getHeight()!=1024)
                throw new IllegalStateException("Invalid mine collision mask: "+name);
            Path2D path=new Path2D.Double(Path2D.WIND_NON_ZERO);
            for(int row=0;row<mask.getHeight();row++) {
                int col=0;
                while(col<mask.getWidth()) {
                    while(col<mask.getWidth() && (mask.getRGB(col,row)&255)<128)col++;
                    int first=col;
                    while(col<mask.getWidth() && (mask.getRGB(col,row)&255)>=128)col++;
                    if(col>first)path.append(new Rectangle2D.Double(first,row,col-first,1),false);
                }
            }
            return new Area(path);
        } catch(Exception ex) {throw new IllegalStateException("Cannot load "+name,ex);}
    }
    private static int[] createBlockedPrefix() {
        try {
            BufferedImage ground=ImageIO.read(MineRoom.class.getResource("/finalgame/mine-floor.png"));
            BufferedImage objects=ImageIO.read(MineRoom.class.getResource("/finalgame/mine-objects.png"));
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
        } catch(Exception ex) {throw new IllegalStateException("Cannot index mine collision masks",ex);}
    }
    public void reset() {
        cleared=false;exitReached=false;scientistFacingFront=false;workClock=0;enter();
    }
    public void enter() {
        x=40;y=847;battleRequested=false;backRequested=false;
        storyCamera=false;drawZoom=ZOOM;workBob=0;
        animation.reset(SpriteAnimator.RIGHT);updateCamera();
    }
    public InspectPoint nearestInspection(){
        InspectPoint nearest=null;double best=Double.POSITIVE_INFINITY;
        for(InspectPoint point:InspectPoint.values()){
            if(point.afterBoss&&!cleared)continue;
            double distance=Point2D.distance(x+8,y+16,point.x,point.y);
            if(distance<=point.radius&&distance<best){best=distance;nearest=point;}
        }
        return nearest;
    }
    public void faceStoryPoint(double targetX,double targetY){
        double dx=targetX-(x+8),dy=targetY-(y+16),ax=Math.abs(dx),ay=Math.abs(dy);
        int direction;
        if(Math.min(ax,ay)>Math.max(ax,ay)*0.41421356237)
            direction=dy>0?(dx>0?SpriteAnimator.DOWN_RIGHT:SpriteAnimator.DOWN_LEFT)
                          :(dx>0?SpriteAnimator.UP_RIGHT:SpriteAnimator.UP_LEFT);
        else direction=ax>=ay?(dx>=0?SpriteAnimator.RIGHT:SpriteAnimator.LEFT)
                            :(dy>=0?SpriteAnimator.DOWN:SpriteAnimator.UP);
        animation.reset(direction);
    }
    public void drawInspectionPrompt(Graphics graphics){
        InspectPoint point=nearestInspection();if(point==null)return;
        Graphics2D g=(Graphics2D)graphics.create();g.setFont(DialogueBox.font(17));
        String label="สำรวจ";int width=48+g.getFontMetrics().stringWidth(label),height=32;
        int sx=(int)Math.round((point.promptX-Math.round(cameraX))*drawZoom)-width/2;
        int sy=60+(int)Math.round((point.promptY-Math.round(cameraY))*drawZoom)-height/2;
        sx=Math.max(8,Math.min(960-width-8,sx));sy=Math.max(68,Math.min(580-height-8,sy));
        g.setColor(new Color(10,16,20,230));g.fillRoundRect(sx,sy,width,height,6,6);
        g.setColor(new Color(118,126,118));g.drawRoundRect(sx,sy,width,height,6,6);
        g.setColor(new Color(45,52,53));g.fillRect(sx+7,sy+5,22,22);
        g.setColor(new Color(158,159,143));g.drawRect(sx+7,sy+5,22,22);
        g.setFont(new Font(Font.MONOSPACED,Font.BOLD,15));g.setColor(new Color(219,214,192));g.drawString("F",sx+14,sy+22);
        g.setFont(DialogueBox.font(17));g.drawString(label,sx+37,sy+23);g.dispose();
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
        // This is the visible tunnel opening beneath the wooden lintel.
        // The original floor mask left a narrow vertical seam across it.
        if(new Rectangle2D.Double(0,835,40,48).contains(feet))return true;
        int stride=1537;
        if(blockedPrefix[bottom*stride+right]-blockedPrefix[top*stride+right]
                -blockedPrefix[bottom*stride+left]+blockedPrefix[top*stride+left]>0)return false;
        if(!cleared && gate.intersects(feet))return false;
        return true;
    }
    public void update(int dx,int dy,double seconds,boolean running) {
        workClock+=Math.max(0,Math.min(seconds,.1));
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
        if(!cleared && (bossZone.intersects(x,y,16,16) || (x>=680&&x<=1000&&y<335)))battleRequested=true;
        if(x<12)backRequested=true;
        if(cleared && exitDoor.contains(x+4,y+10,8,6))exitReached=true;
        updateCamera();
    }
    private void updateCamera() {
        if(storyCamera)return;
        cameraX=Math.max(0,Math.min(x+8-960/(2*ZOOM),background.getWidth()-960/ZOOM));
        cameraY=Math.max(0,Math.min(y+8-520/(2*ZOOM),background.getHeight()-520/ZOOM));
    }
    public boolean consumeExitRequest(){boolean b=exitReached;exitReached=false;return b;}
    public void returnFromButterfly(){x=1465;y=522;exitReached=false;battleRequested=false;animation.reset(SpriteAnimator.LEFT);updateCamera();}
    public boolean consumeBattleRequest(){boolean b=battleRequested;battleRequested=false;return b;}
    public boolean consumeBackRequest(){boolean b=backRequested;backRequested=false;return b;}
    public boolean interact(int mx,int my) {
        if(my<60||my>=580)return false;
        double wx=(mx/ZOOM+Math.round(cameraX)),wy=((my-60)/ZOOM+Math.round(cameraY));
        return !cleared && bossZone.contains(wx,wy) && Point2D.distance(x,y,wx,wy)<110;
    }
    public void returnFromBattle() {
        cleared=true;battleRequested=false;x=948;y=292;
        animation.reset(SpriteAnimator.RIGHT);updateCamera();
    }
    public void draw(Graphics graphics) {
        Graphics2D g=(Graphics2D)graphics.create();
        g.setClip(0,60,960,520);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.translate(0,60);g.scale(drawZoom,drawZoom);g.translate(-Math.round(cameraX),-Math.round(cameraY));
        g.drawImage(background,0,0,null);
        if(!cleared) {
            g.setColor(new Color(44,29,23));g.fillRect(1005,351,8,34);g.fillRect(1116,351,8,34);
            g.setColor(new Color(94,61,38));g.fillRect(1005,358,119,7);g.fillRect(1005,371,119,7);
            g.setColor(new Color(130,87,49));g.drawLine(1006,358,1122,358);g.drawLine(1006,371,1122,371);
            g.setColor(new Color(40,35,31));for(int bx:new int[]{1010,1119}){g.fillRect(bx,360,2,2);g.fillRect(bx,373,2,2);}
        }
        if(showCollision) {
            g.setColor(new Color(30,240,80,65));g.fill(floor);
            g.setColor(new Color(250,50,40,120));for(Shape o:obstacles)g.fill(o);
            g.setColor(Color.CYAN);g.draw(new Rectangle2D.Double(x+4,y+10,8,6));
            g.setColor(Color.ORANGE);if(!cleared)g.draw(bossZone);
        }
        int feetX=(int)Math.round(x+8),feetY=(int)Math.round(y+16);
        // The north-facing exit lintel hides the actor as they enter the tunnel.
        if(feetX>=1494 && feetX<1526 && feetY<521)g.clipRect(1491,476,37,100);
        if(!cleared && feetY<SCIENTIST_Y) {
            animation.draw(g,feetX,feetY,36,54);
            drawScientist(g);
        } else {
            if(!cleared)drawScientist(g);
            animation.draw(g,feetX,feetY,36,54);
        }
        g.dispose();
        graphics.setColor(new Color(15,20,19));graphics.fillRect(0,0,960,60);graphics.fillRect(0,580,960,60);
        graphics.setColor(Color.WHITE);graphics.setFont(new Font(Font.MONOSPACED,Font.BOLD,18));
        graphics.drawString("THE MINE  |  "+(cleared?"Laboratory cleared":"Follow the rails"),20,25);
        graphics.setFont(new Font(Font.MONOSPACED,Font.PLAIN,14));
graphics.drawString("WASD: Walk | Shift: Run | Esc: Pause | F3: Collision | F6: Skip | F7: Replay",20,47);
        graphics.setColor(Color.LIGHT_GRAY);
        graphics.drawString(exitReached?"Entering the butterfly forest...":
            cleared?"Follow the eastern passage, then walk UP into the dark doorway.":
            "A hidden laboratory lies ahead. Explore the room to meet its keeper.",20,612);
    }
    private void drawScientist(Graphics2D g){
        int footY=(int)Math.round(SCIENTIST_Y+(scientistFacingFront?0:workBob+(!storyCamera?Math.sin(workClock*4):0)));
        if(scientistFacingFront)BossArtwork.drawScientistMap(g,(int)SCIENTIST_X,footY);
        else BossArtwork.drawScientistWorking(g,(int)SCIENTIST_X,footY);
    }
}
