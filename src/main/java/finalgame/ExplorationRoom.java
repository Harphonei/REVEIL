package finalgame;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/** Forest coordinates use a 2048 x 683 design grid, independent of image pixels. */
public class ExplorationRoom {
    public enum InspectPoint {
        WAGON(516,334,516,282,76,
            "เกวียนพัง…ของข้างในกระจัดกระจายไปหมด"),
        CORPSE(390,365,390,312,76,
            "รอยแผลลึกมาก…", "อะไรทำให้มันเป็นแบบนี้กัน"),
        BRIDGE(913,406,913,356,76,
            "ไม้ตรงนี้เป็นรอยข่วน…", "เหมือนมีอะไรหนัก ๆ ถูกลากผ่านไป");
        final double x,y,promptX,promptY,radius;
        private final String[] lines;
        InspectPoint(double x,double y,double promptX,double promptY,double radius,String... lines){
            this.x=x;this.y=y;this.promptX=promptX;this.promptY=promptY;this.radius=radius;this.lines=lines;
        }
        public String[] lines(){return lines.clone();}
    }

    private final double scaleX, scaleY;
    private static final int VIEW_W = 960, VIEW_H = 520, TOP = 60;
    private static final double SPEED = 90, SIZE = 16;
    private static final double ZOOM = 1.45;
    private final BufferedImage background;
    private final Area floor = createFloor();
    private final Rectangle2D bossZone = new Rectangle2D.Double(1435, 390, 60, 45);
    private final Rectangle2D mineZone = new Rectangle2D.Double(1960, 310, 38, 12);
    private final Shape[] obstacles = {
        polygon(new double[][]{{73,208},{90,202},{147,220},{145,240},{128,246},{76,225}}),
        polygon(new double[][]{{339,358},{359,341},{399,338},{432,351},{454,377},{439,389},{349,386}}),
        polygon(new double[][]{{482,310},{536,295},{552,312},{549,348},{517,363},{480,348}}),
        new Rectangle2D.Double(445,302,36,37),
        polygon(new double[][]{{553,351},{582,339},{596,350},{587,362},{553,369}}),
        polygon(new double[][]{{603,247},{623,227},{661,216},{690,224},{711,247},{726,264},{697,277},{612,270}}),
        new Rectangle2D.Double(608,168,40,53),
        new Rectangle2D.Double(711,425,57,27),
        polygon(new double[][]{{1221,346},{1230,331},{1252,329},{1267,346},{1266,365},{1244,373},{1222,366}}),
        polygon(new double[][]{{1290,243},{1321,222},{1365,225},{1395,245},{1416,273},{1371,286},{1290,276}}),
        new Rectangle2D.Double(1544,283,74,26),
        polygon(new double[][]{{1523,479},{1545,463},{1588,467},{1614,487},{1640,500},{1630,515},{1530,513}}),
        new Rectangle2D.Double(1291,439,59,27),
        polygon(new double[][]{{1481,249},{1512,232},{1518,187},{1533,185},{1550,228},{1593,266},{1545,272}}),
        new Rectangle2D.Double(1227,299,55,23),
        new Rectangle2D.Double(1240,435,59,17),
        new Rectangle2D.Double(1364,472,36,56),
        new Rectangle2D.Double(1389,521,43,30),
        new Rectangle2D.Double(1910,305,38,47)
    };
    private double x, y, cameraX, cameraY;
    private boolean cleared, battleRequested, mineReached;
    private String message;
    private boolean showCollision;
    private final SpriteAnimator animation = new SpriteAnimator(SpriteAnimator.Lighting.FOREST);
    private int storyHeroPose;
    private double storyStrike;
    private int storyBossPose=1;
    private double feedingClock;
    private double storyBossX=1465,storyBossY=425,storyBossBob;
    private double storyZoom=ZOOM;
    private boolean storyCamera;

    public double storyX(){return x;}
    public double storyY(){return y;}
    public boolean isCleared(){return cleared;}
    public static double explorationZoom(){return ZOOM;}
    public void focusStoryHero(double zoom){focusStory(x+SIZE/2,y+SIZE/2,zoom);}
    public void setStoryHeroPose(int pose){storyHeroPose=pose;}
    public void setStoryStrike(double progress){storyStrike=progress;}
    public void faceStoryHero(int facing){animation.reset(facing);}
    public void faceStoryPoint(double targetX,double targetY) {
        double dx=targetX-(x+SIZE/2),dy=targetY-(y+SIZE);
        double ax=Math.abs(dx),ay=Math.abs(dy);
        int direction;
        if(Math.min(ax,ay)>Math.max(ax,ay)*0.41421356237)
            direction=dy>0?(dx>0?SpriteAnimator.DOWN_RIGHT:SpriteAnimator.DOWN_LEFT)
                          :(dx>0?SpriteAnimator.UP_RIGHT:SpriteAnimator.UP_LEFT);
        else direction=ax>=ay?(dx>=0?SpriteAnimator.RIGHT:SpriteAnimator.LEFT)
                            :(dy>=0?SpriteAnimator.DOWN:SpriteAnimator.UP);
        animation.reset(direction);
    }
    public void setStoryBoss(int pose,double feetX,double feetY,double bob) {
        storyBossPose=pose;storyBossX=feetX;storyBossY=feetY;storyBossBob=bob;
    }
    public void focusStory(double centreX,double centreY,double zoom) {
        storyCamera=true;storyZoom=zoom;
        cameraX=Math.max(0,Math.min(centreX*scaleX-VIEW_W/(2*zoom),Math.max(0,background.getWidth()-VIEW_W/zoom)));
        cameraY=Math.max(0,Math.min(centreY*scaleY-VIEW_H/(2*zoom),Math.max(0,background.getHeight()-VIEW_H/zoom)));
    }
    public void finishStory() {
        storyHeroPose=0;storyStrike=0;storyBossPose=1;storyBossX=1465;storyBossY=425;storyBossBob=0;
        storyCamera=false;storyZoom=ZOOM;updateCamera();
    }

    public ExplorationRoom() {
        try {
            java.net.URL resource = getClass().getResource("/finalgame/forest-map.png");
            if (resource == null) throw new IOException("Missing /finalgame/forest-map.png");
            background = ImageIO.read(resource);
            if (background == null) throw new IOException("Invalid forest PNG");
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot load forest map. Copy all of src including main/resources.", ex);
        }
        scaleX=background.getWidth()/2048.0;
        scaleY=background.getHeight()/683.0;
        reset();
    }

    private static Area createFloor() {
        try {
            java.net.URL url=ExplorationRoom.class.getResource("/finalgame/forest-floor.png");
            if(url==null) throw new IOException("Missing forest-floor.png");
            BufferedImage mask=ImageIO.read(url);
            if(mask==null || mask.getWidth()!=2048 || mask.getHeight()!=683)
                throw new IOException("Invalid forest-floor.png");
            // Horizontal runs retain every bend in the checked-in walkability mask.
            Path2D path=new Path2D.Double(Path2D.WIND_NON_ZERO);
            for(int row=0;row<mask.getHeight();row++) {
                int col=0;
                while(col<mask.getWidth()) {
                    while(col<mask.getWidth() && (mask.getRGB(col,row)&255)<128) col++;
                    int first=col;
                    while(col<mask.getWidth() && (mask.getRGB(col,row)&255)>=128) col++;
                    if(col>first) path.append(new Rectangle2D.Double(first,row,col-first,1),false);
                }
            }
            return new Area(path);
        } catch(IOException ex) { throw new IllegalStateException("Cannot load forest walkability mask",ex); }
    }

    private static Area polygon(double[][] points) {
        Path2D p=new Path2D.Double(); p.moveTo(points[0][0],points[0][1]);
        for(int i=1;i<points.length;i++) p.lineTo(points[i][0],points[i][1]);
        p.closePath(); return new Area(p);
    }

    /** One nearest point only, so overlapping ranges never show two prompts. */
    public InspectPoint nearestInspection(){
        InspectPoint nearest=null;double best=Double.POSITIVE_INFINITY;
        for(InspectPoint point:InspectPoint.values()){
            double distance=Point2D.distance(x+SIZE/2,y+SIZE,point.x,point.y);
            if(distance<=point.radius&&distance<best){best=distance;nearest=point;}
        }
        return nearest;
    }

    public void drawInspectionPrompt(Graphics graphics){
        InspectPoint point=nearestInspection();if(point==null)return;
        Graphics2D g=(Graphics2D)graphics.create();g.setFont(DialogueBox.font(17));
        String label="สำรวจ";int width=48+g.getFontMetrics().stringWidth(label),height=32;
        int sx=(int)Math.round((point.promptX*scaleX-Math.round(cameraX))*storyZoom)-width/2;
        int sy=TOP+(int)Math.round((point.promptY*scaleY-Math.round(cameraY))*storyZoom)-height/2;
        sx=Math.max(8,Math.min(VIEW_W-width-8,sx));sy=Math.max(TOP+8,Math.min(TOP+VIEW_H-height-8,sy));
        g.setColor(new Color(10,16,20,230));g.fillRoundRect(sx,sy,width,height,6,6);
        g.setColor(new Color(118,126,118));g.drawRoundRect(sx,sy,width,height,6,6);
        g.setColor(new Color(45,52,53));g.fillRect(sx+7,sy+5,22,22);
        g.setColor(new Color(158,159,143));g.drawRect(sx+7,sy+5,22,22);
        g.setFont(new Font(Font.MONOSPACED,Font.BOLD,15));g.setColor(new Color(219,214,192));g.drawString("F",sx+14,sy+22);
        g.setFont(DialogueBox.font(17));g.drawString(label,sx+37,sy+23);g.dispose();
    }

    public void setCollisionView(boolean value){showCollision=value;}
    public void toggleCollisionView() { showCollision=!showCollision; }

    public void stopMovement() { animation.stop(); }

    public void reset() {
        animation.reset(SpriteAnimator.DOWN);
        x=135; y=312; cleared=false; battleRequested=false; mineReached=false;
        storyHeroPose=0;storyStrike=0;storyBossPose=1;storyBossX=1465;storyBossY=425;storyBossBob=0;feedingClock=0;
        storyCamera=false;storyZoom=ZOOM;
        message="You wake in the forest. Follow the trail to the right.";
        updateCamera();
    }

    public void update(int dx, int dy, double seconds) {
        update(dx,dy,seconds,false);
    }

    public void update(int dx, int dy, double seconds, boolean running) {
        feedingClock+=Math.max(0,Math.min(seconds,.1));
        double oldX=x,oldY=y;
        // Small axis-separated steps prevent tunnelling and allow sliding along walls.
        double length=Math.hypot(dx,dy);
        if (length>0) {
            double distance=SPEED*(running?1.65:1.0)*Math.max(0,Math.min(seconds,0.1));
            int steps=Math.max(1,(int)Math.ceil(distance/2));
            for(int i=0;i<steps;i++) {
                double nx=x+dx/length*distance/steps;
                if(canStand(nx,y)) x=nx;
                double ny=y+dy/length*distance/steps;
                if(canStand(x,ny)) y=ny;
            }
        }
        animation.update(x-oldX,y-oldY,seconds,running);
        if(!cleared && !battleRequested && bossZone.intersects(x,y,SIZE,SIZE))
            battleRequested=true;
        if(mineZone.contains(x+4,y+10,8,6)) {
            if(cleared) {
                mineReached=true;
                message="Entering the mine...";
            } else message="The forest guardian blocks the route. Clear the encounter first.";
        }
        updateCamera();
    }

    private boolean canStand(double px,double py) {
        Rectangle2D body=new Rectangle2D.Double(px+4,py+10,8,6);
        if(!floor.contains(body)) return false;
        if(!cleared && body.getMaxX()>1658) return false;
        for(Shape obstacle:obstacles) if(obstacle.intersects(body)) return false;
        return true;
    }

    private void updateCamera() {
        if(storyCamera)return;
        cameraX=Math.max(0,Math.min(x*scaleX+SIZE*scaleX/2-VIEW_W/(2.0*ZOOM),background.getWidth()-VIEW_W/ZOOM));
        cameraY=Math.max(0,Math.min(y*scaleY+SIZE*scaleY/2-VIEW_H/(2.0*ZOOM),background.getHeight()-VIEW_H/ZOOM));
    }

    public boolean consumeMineRequest() {
        boolean pending=mineReached; mineReached=false; return pending;
    }

    public void returnFromMine() {
        x=1908; y=380; mineReached=false; animation.reset(SpriteAnimator.LEFT); updateCamera();
    }

    public boolean consumeBattleRequest() {
        boolean pending=battleRequested;
        battleRequested=false;
        return pending;
    }

    public boolean interact(int mouseX,int mouseY) {
        double wx=(mouseX/ZOOM+Math.round(cameraX))/scaleX;
        double wy=((mouseY-TOP)/ZOOM+Math.round(cameraY))/scaleY;
        if(mouseY<TOP || mouseY>TOP+VIEW_H) return false;
        if(!cleared && bossZone.contains(wx,wy) && Point2D.distance(x,y,wx,wy)<110) return true;
        if(mineZone.contains(wx,wy))
            message=cleared?"Walk into the mine entrance to enter the next map.":"Defeat the forest guardian first.";
        return false;
    }

    public void returnFromBattle() {
        animation.reset(SpriteAnimator.RIGHT);
        cleared=true; battleRequested=false; x=1510; y=390;
        message="Forest cleared! The mine entrance on the right is now open.";
        updateCamera();
    }

    private void drawPlayer(Graphics2D graphics,int feetX,int feetY) {
        Graphics2D actor=(Graphics2D)graphics.create();
        if(x+SIZE/2>=1960 && x+SIZE/2<1998 && y+SIZE<350) {
            actor.clip(new Rectangle2D.Double(1955*scaleX,290*scaleY,45*scaleX,90*scaleY));
        }
        if(storyHeroPose==1) {
            actor.translate(feetX,feetY-12);actor.rotate(-Math.PI/2);
            animation.draw(actor,0,22,36,54);
        } else animation.draw(actor,feetX,feetY,36,54);
        actor.dispose();
    }

    private void drawStoryBoss(Graphics2D g,int feetX,int feetY) {
        double idleBob=!storyCamera&&storyBossPose==1?Math.sin(feedingClock*5)*1.2:0;
        BossArtwork.drawForestPose(g,feetX,feetY+(int)Math.round(storyBossBob+idleBob),Math.max(0,storyBossPose-1));
        if(storyStrike>0 && storyStrike<1) {
            Graphics2D impact=(Graphics2D)g.create();
            impact.setStroke(new BasicStroke(2));impact.setColor(new Color(110,36,34));
            int offset=(int)Math.round(storyStrike*22);
            impact.drawLine(feetX-35+offset,feetY-25,feetX-12+offset,feetY-7);
            impact.drawLine(feetX-29+offset,feetY-26,feetX-6+offset,feetY-8);
            impact.dispose();
        }
    }

    public void draw(Graphics graphics) {
        Graphics2D g=(Graphics2D)graphics.create();
        g.setClip(0,TOP,VIEW_W,VIEW_H);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.translate(0,TOP);
        g.scale(storyZoom,storyZoom);
        g.translate(-Math.round(cameraX),-Math.round(cameraY));
        // Zoom the complete world together with nearest-neighbor pixels.
        g.drawImage(background,0,0,null);
        if(showCollision) {
            Graphics2D debug=(Graphics2D)g.create();
            debug.scale(scaleX,scaleY);
            debug.setColor(new Color(30,240,80,65)); debug.fill(floor);
            debug.setColor(new Color(250,50,40,120));
            for(Shape o:obstacles) debug.fill(o);
            debug.setColor(Color.CYAN);
            debug.draw(new Rectangle2D.Double(x+4,y+10,8,6));
            debug.dispose();
        }
        int feetX=(int)Math.round((x+SIZE/2)*scaleX);
        int feetY=(int)Math.round((y+SIZE)*scaleY);
        int bossFeetX=(int)Math.round(storyBossX*scaleX);
        int bossFeetY=(int)Math.round(storyBossY*scaleY);
        if(!cleared && feetY<bossFeetY) {
            drawPlayer(g,feetX,feetY);
            drawStoryBoss(g,bossFeetX,bossFeetY);
        } else {
            if(!cleared) drawStoryBoss(g,bossFeetX,bossFeetY);
            drawPlayer(g,feetX,feetY);
        }
        g.dispose();
        GameUi.exploration(graphics,"THE FOREST",1);
    }
}
