package finalgame;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

/** Temple world uses native artwork pixels; collision is always at the feet. */
public final class TempleRoom {
    public enum InspectPoint {
        CORPSE(165,450,165,400,85,false,
            "เกราะแตกจนแทบไม่เหลือสภาพ…", "เกิดการต่อสู้อะไรขึ้นที่นี่กัน"),
        STATUE(1004,270,1004,195,94,false,
            "รูปปั้นอัศวิน…พังไปครึ่งตัวแล้ว", "เมื่อก่อนคงดูน่าเกรงขามกว่านี้"),
        SWORD(1348,361,1348,308,82,true,
            "ดาบหนักชะมัด…", "จนถึงที่สุด แกก็ยังไม่ยอมบอกอะไรฉันเลยสินะ");
        final double x,y,promptX,promptY,radius;
        final boolean afterBoss;
        private final String[] lines;
        InspectPoint(double x,double y,double promptX,double promptY,double radius,boolean afterBoss,String... lines){
            this.x=x;this.y=y;this.promptX=promptX;this.promptY=promptY;this.radius=radius;this.afterBoss=afterBoss;this.lines=lines;
        }
        public String[] lines(){return lines.clone();}
    }

    private static final double ZOOM=1.18;
    // Keep actors and movement the same screen size/speed as the other rooms.
    private static final double ACTOR_SCALE=1.35/1.10, SPEED=90*ACTOR_SCALE;
    private static final int HERO_WIDTH=(int)Math.round(36*ACTOR_SCALE);
    private static final int HERO_HEIGHT=(int)Math.round(54*ACTOR_SCALE);
    private final BufferedImage background;
    private final BufferedImage foreground;
    private final BufferedImage floorDebug=loadDebugMask("temple-floor.png",new Color(30,240,80,65));
    private final BufferedImage objectsDebug=loadDebugMask("temple-objects.png",new Color(250,50,40,120));
    private final int[] blockedPrefix=createBlockedPrefix();
    private final Rectangle exitDoor=new Rectangle(1142,166,92,14);
    private final SpriteAnimator animation=new SpriteAnimator(SpriteAnimator.Lighting.TEMPLE);
    private double x,y,cameraX,cameraY;
    private boolean backRequested,exitReached,showCollision,cleared,battleRequested;
    private boolean storyCamera;
    private double knightTurn;
    public double storyX(){return x;}
    public double storyY(){return y;}
    public boolean isCleared(){return cleared;}
    public void faceStoryKnight(){animation.reset(SpriteAnimator.RIGHT);}
    public void setKnightTurn(double p){knightTurn=p;}
    public void focusStory(double cx,double cy){
        storyCamera=true;
        cameraX=Math.max(0,Math.min(cx-960/(2*ZOOM),1536-960/ZOOM));
        cameraY=Math.max(0,Math.min(cy-520/(2*ZOOM),1024-520/ZOOM));
    }
    public void finishStory(){storyCamera=false;animation.stop();updateCamera();}
    private final Rectangle encounterStep=new Rectangle(1110,272,152,12);
    private final Rectangle upperStairs=new Rectangle(1106,150,164,122);

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
        cleared=false;battleRequested=false;exitReached=false;storyCamera=false;knightTurn=0;enter();
    }
    public void enter() {
        x=40;y=508;exitReached=false;backRequested=false;battleRequested=false;
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
        int sx=(int)Math.round((point.promptX-Math.round(cameraX))*ZOOM)-width/2;
        int sy=60+(int)Math.round((point.promptY-Math.round(cameraY))*ZOOM)-height/2;
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
        if(!cleared && encounterStep.contains(x+8,y+13))battleRequested=true;
        if(x<12)backRequested=true;
        if(exitDoor.contains(x+4,y+10,8,6))exitReached=true;
        updateCamera();
    }
    private void updateCamera() {
        if(storyCamera)return;
        cameraX=Math.max(0,Math.min(x+8-960/(2*ZOOM),background.getWidth()-960/ZOOM));
        cameraY=Math.max(0,Math.min(y+8-520/(2*ZOOM),background.getHeight()-520/ZOOM));
    }
    public boolean consumeExitRequest(){boolean exit=exitReached;exitReached=false;return exit;}
    public void returnFromThrone(){x=1180;y=200;exitReached=false;backRequested=false;battleRequested=false;animation.reset(SpriteAnimator.DOWN);updateCamera();}
    public boolean consumeBattleRequest(){boolean b=battleRequested;battleRequested=false;return b;}
    public void returnFromBattle(){cleared=true;battleRequested=false;x=1180;y=286;animation.reset(SpriteAnimator.UP);updateCamera();}
    public boolean consumeBackRequest(){boolean b=backRequested;backRequested=false;return b;}
    public void skipToExit(){cleared=true;battleRequested=false;x=1180;y=155;exitReached=true;animation.reset(SpriteAnimator.UP);updateCamera();}
    private void drawDroppedSword(Graphics2D graphics){
        Graphics2D g=(Graphics2D)graphics.create();g.translate(1332,377);
        // Muted pixel steel, lying below the guardian's pedestal; decoration only.
        g.setColor(new Color(7,12,20,120));g.fillPolygon(new int[]{-3,2,35,32,5,1},new int[]{4,6,-27,-29,-2,-5},6);
        g.setColor(new Color(58,72,89));g.fillPolygon(new int[]{4,8,30,34,29,5},new int[]{-9,-5,-28,-35,-33,-11},6);
        g.setColor(new Color(119,132,145));g.fillPolygon(new int[]{7,9,30,33,28},new int[]{-9,-7,-29,-34,-31},5);
        g.setColor(new Color(165,171,176));g.drawLine(9,-10,30,-31);
        g.setColor(new Color(53,49,60));g.fillPolygon(new int[]{-4,-1,8,5},new int[]{1,4,-5,-8},4);
        g.setColor(new Color(101,112,125));g.fillPolygon(new int[]{1,3,12,10},new int[]{-13,-15,-6,-4},4);
        g.setColor(new Color(95,103,115));g.fillRect(-5,1,4,4);g.dispose();
    }

    public void draw(Graphics graphics) {
        Graphics2D g=(Graphics2D)graphics.create();
        g.setClip(0,60,960,520);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.translate(0,60);g.scale(ZOOM,ZOOM);g.translate(-Math.round(cameraX),-Math.round(cameraY));
        g.drawImage(background,0,0,null);
        if(!cleared)BossArtwork.drawKnightTurn(g,1377,273,ACTOR_SCALE,knightTurn);
        else drawDroppedSword(g);
        Graphics2D actor=(Graphics2D)g.create();
        if(x+8>=1140 && x+8<1240 && y+16<200)actor.clipRect(1136,154,108,100);
        animation.draw(actor,(int)Math.round(x+8),(int)Math.round(y+16),HERO_WIDTH,HERO_HEIGHT);actor.dispose();
        g.drawImage(foreground,0,0,null);
        if(showCollision) {
            g.drawImage(floorDebug,0,0,null);
            g.drawImage(objectsDebug,0,0,null);
            g.setColor(Color.CYAN);g.draw(new Rectangle2D.Double(x+4,y+10,8,6));
            g.setColor(Color.ORANGE);g.draw(exitDoor);
            if(!cleared){g.setColor(Color.MAGENTA);g.draw(encounterStep);}
        }
        g.dispose();
        graphics.setColor(new Color(15,20,19));graphics.fillRect(0,0,960,60);graphics.fillRect(0,580,960,60);
        graphics.setColor(Color.WHITE);graphics.setFont(new Font(Font.MONOSPACED,Font.BOLD,18));
        graphics.drawString("RUINED TEMPLE v3.43  |  The silent forecourt",20,25);
        graphics.setFont(new Font(Font.MONOSPACED,Font.PLAIN,14));
graphics.drawString("WASD: Walk | Shift: Run | Esc: Pause | F3: Collision | F6: Skip | F7: Replay",20,47);
        graphics.setColor(Color.LIGHT_GRAY);
        graphics.drawString(exitReached?"The inner sanctuary lies beyond.":
            "Follow the stone path to the temple entrance.",20,612);
    }
}
