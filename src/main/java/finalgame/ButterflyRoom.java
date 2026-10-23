package finalgame;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

/** Butterfly forest world uses native artwork pixels; collision is always at the feet. */
public final class ButterflyRoom {
    public enum InspectPoint {
        TREE(276,125,276,83,68,false,
            "ต้นไม้พวกนี้…รูปร่างแปลกชะมัด", "ทำไมป่านี้ถึงเงียบขนาดนี้นะ"),
        COCOON(463,681,463,637,74,false,
            "รังไหม…ใหญ่เกินไปหรือเปล่า", "เมื่อกี้…ข้างในขยับงั้นเหรอ?"),
        TORN_COCOON(935,862,935,815,86,true,
            "ข้างในว่างเปล่า…", "หวังว่าเจ้าของมันจะไม่อยู่แถวนี้แล้วนะ");
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
    private final BufferedImage rootArtwork;
    // Only the pointed foreground shrub shown on the upper corridor is passable.
    private static final Rectangle PATH_RELIEF=new Rectangle(550,274,94,34);
    private final Area floor=loadMask("butterfly-floor.png");
    private final Shape[] obstacles={loadMask("butterfly-objects.png")};
    private final int[] blockedPrefix=createBlockedPrefix();
    private final Rectangle bossZone=new Rectangle(1140,710,130,110);
    private final Rectangle gate=new Rectangle(1096,180,50,132);
    private final SpriteAnimator animation=new SpriteAnimator(SpriteAnimator.Lighting.BUTTERFLY);
    private double x,y,cameraX,cameraY;
    private boolean cleared,battleRequested,backRequested,exitReached,showCollision;
    private boolean storyCamera,bossVisible;
    private double mothX=1200,mothY=800;
    private boolean mothFlying,mothFacingLeft=true;
    private double mothOpacity=1;
    private int mothFrame;
    public void setStoryFlight(double px,double py,double clock,double opacity){
        setStoryMoth(px,py);mothFlying=true;mothOpacity=opacity;
        int[] cycle={0,1,2,1};mothFrame=cycle[(int)(clock/.16)%4];
    }
    private void drawStoryMoth(Graphics g){
        if(mothFlying)BossArtwork.drawMothFlight(g,(int)mothX,(int)mothY,mothFrame,mothOpacity,mothFacingLeft);
        else BossArtwork.drawMothMap(g,(int)mothX,(int)mothY);
    }
    public double storyX(){return x;}
    public double storyY(){return y;}
    public boolean isCleared(){return cleared;}
    public boolean nearRoots(){return !cleared&&x>995&&x<1100&&y>165&&y<320;}
    public void faceStoryHero(){mothFacingLeft=x<1200;animation.reset(mothFacingLeft?SpriteAnimator.RIGHT:SpriteAnimator.LEFT);}
    public void setStoryMoth(double px,double py){bossVisible=true;mothX=px;mothY=py;}
    public void focusStory(double cx,double cy){
        storyCamera=true;
        cameraX=Math.max(0,Math.min(cx-960/(2*ZOOM),1536-960/ZOOM));
        cameraY=Math.max(0,Math.min(cy-520/(2*ZOOM),1024-520/ZOOM));
    }
    public void finishStory(){storyCamera=false;mothFlying=false;mothOpacity=1;animation.stop();updateCamera();}

    public ButterflyRoom() {
        try {
            background=ImageIO.read(ButterflyRoom.class.getResource("/finalgame/butterfly-map.png"));
            rootArtwork=ImageIO.read(ButterflyRoom.class.getResource("/finalgame/butterfly-roots.png"));
            if(rootArtwork==null)throw new IllegalStateException("Missing root artwork");
            if(background==null || background.getWidth()!=1536 || background.getHeight()!=1024)
                throw new IllegalStateException("Invalid butterfly map");
        } catch(Exception ex) {throw new IllegalStateException("Cannot load butterfly-map.png",ex);}
        floor.add(new Area(PATH_RELIEF));
        ((Area)obstacles[0]).subtract(new Area(PATH_RELIEF));
        reset();
    }
    private static Area loadMask(String name) {
        try {
            BufferedImage mask=ImageIO.read(ButterflyRoom.class.getResource("/finalgame/"+name));
            if(mask==null || mask.getWidth()!=1536 || mask.getHeight()!=1024)
                throw new IllegalStateException("Invalid butterfly collision mask: "+name);
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
            BufferedImage ground=ImageIO.read(ButterflyRoom.class.getResource("/finalgame/butterfly-floor.png"));
            BufferedImage objects=ImageIO.read(ButterflyRoom.class.getResource("/finalgame/butterfly-objects.png"));
            int stride=1537;
            int[] sums=new int[stride*1025];
            for(int y=0;y<1024;y++) {
                int row=0;
                for(int x=0;x<1536;x++) {
                    if(!PATH_RELIEF.contains(x+.5,y+.5) && ((ground.getRGB(x,y)&255)<128 || (objects.getRGB(x,y)&255)>=128))row++;
                    sums[(y+1)*stride+x+1]=sums[y*stride+x+1]+row;
                }
            }
            return sums;
        } catch(Exception ex) {throw new IllegalStateException("Cannot index butterfly collision masks",ex);}
    }
    public void reset() {
        cleared=false;exitReached=false;bossVisible=false;storyCamera=false;mothFlying=false;mothOpacity=1;mothX=1200;mothY=800;enter();
    }
    public void enter() {
        x=40;y=148;battleRequested=false;backRequested=false;
        animation.reset(SpriteAnimator.RIGHT);updateCamera();
    }
    public void returnFromMine() { enter(); }
    public void skipToExit() {
        cleared=true; battleRequested=false; x=1450; y=261;
        animation.reset(SpriteAnimator.RIGHT);updateCamera();
    }
    private void drawRootGate(Graphics2D g) {
        g.drawImage(rootArtwork,1076,175,88,142,null);
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
        if(!cleared && gate.intersects(feet))return false;
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
        if(!cleared && bossZone.intersects(x,y,16,16))battleRequested=true;
        if(x<12)backRequested=true;
        if(cleared && x>1508 && y<330)exitReached=true;
        updateCamera();
    }
    private void updateCamera() {
        if(storyCamera)return;
        cameraX=Math.max(0,Math.min(x+8-960/(2*ZOOM),background.getWidth()-960/ZOOM));
        cameraY=Math.max(0,Math.min(y+8-520/(2*ZOOM),background.getHeight()-520/ZOOM));
    }
    public boolean consumeExitRequest(){boolean b=exitReached;exitReached=false;return b;}
    public void returnFromTemple(){x=1450;y=261;exitReached=false;battleRequested=false;animation.reset(SpriteAnimator.LEFT);updateCamera();}
    public boolean consumeBattleRequest(){boolean b=battleRequested;battleRequested=false;return b;}
    public boolean consumeBackRequest(){boolean b=backRequested;backRequested=false;return b;}
    public boolean interact(int mx,int my) {
        if(my<60||my>=580)return false;
        double wx=(mx/ZOOM+Math.round(cameraX)),wy=((my-60)/ZOOM+Math.round(cameraY));
        return !cleared && bossZone.contains(wx,wy) && Point2D.distance(x,y,wx,wy)<110;
    }
    public void returnFromBattle() {
        cleared=true;battleRequested=false;x=1090;y=815;
        animation.reset(SpriteAnimator.RIGHT);updateCamera();
    }
    public void draw(Graphics graphics) {
        Graphics2D g=(Graphics2D)graphics.create();
        g.setClip(0,60,960,520);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.translate(0,60);g.scale(ZOOM,ZOOM);g.translate(-Math.round(cameraX),-Math.round(cameraY));
        g.drawImage(background,0,0,null);
        if(cleared){
            // A small pixel slit opens the existing lower-edge cocoon after the fight.
            // Decoration only; the original floor and object collision are unchanged.
            g.setColor(new Color(31,23,42));
            g.fillPolygon(new int[]{932,937,935,938,934,936,931},
                          new int[]{852,857,862,869,874,878,868},7);
            g.setColor(new Color(109,89,139));g.fillRect(930,861,2,6);g.fillRect(936,870,2,4);
        }
        if(!cleared) drawRootGate(g);
        if(showCollision) {
            g.setColor(new Color(30,240,80,65));g.fill(floor);
            g.setColor(new Color(250,50,40,120));for(Shape o:obstacles)g.fill(o);
            if(!cleared){g.setColor(new Color(250,50,40,100));g.fill(gate);}
            g.setColor(Color.CYAN);g.draw(new Rectangle2D.Double(x+4,y+10,8,6));
            g.setColor(Color.ORANGE);if(!cleared)g.draw(bossZone);
        }
        int feetX=(int)Math.round(x+8),feetY=(int)Math.round(y+16);
        if(!cleared && bossVisible && feetY<mothY) {
            animation.draw(g,feetX,feetY,36,54);
            drawStoryMoth(g);
        } else {
            if(!cleared&&bossVisible)drawStoryMoth(g);
            animation.draw(g,feetX,feetY,36,54);
        }
        g.dispose();
        graphics.setColor(new Color(15,20,19));graphics.fillRect(0,0,960,60);graphics.fillRect(0,580,960,60);
        graphics.setColor(Color.WHITE);graphics.setFont(new Font(Font.MONOSPACED,Font.BOLD,18));
        graphics.drawString("BUTTERFLY FOREST v3.48  |  "+(cleared?"Roots released":"Follow the lower trail"),20,25);
        graphics.setFont(new Font(Font.MONOSPACED,Font.PLAIN,14));
graphics.drawString("WASD: Walk | Shift: Run | Esc: Pause | F3: Collision | F6: Skip | F7: Replay",20,47);
        graphics.setColor(Color.LIGHT_GRAY);
        graphics.drawString(exitReached?"Entering the ruined temple...":
            cleared?"The roots have withdrawn. Return to the upper path and head east.":
            "The upper path is sealed by roots. Follow the cocoons below.",20,612);
    }
}
