package finalgame;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/** Forest coordinates use a 2048 x 683 design grid, independent of image pixels. */
public class ExplorationRoom {
    private final double scaleX, scaleY;
    private static final int VIEW_W = 960, VIEW_H = 520, TOP = 60;
    private static final double SPEED = 90, SIZE = 16;
    private final BufferedImage background;
    private final Area floor = createFloor();
    private final Rectangle2D bossZone = new Rectangle2D.Double(1430, 335, 130, 105);
    private final Rectangle2D mineZone = new Rectangle2D.Double(1920, 322, 88, 90);
    private final Shape[] obstacles = {
        polygon(new double[][]{{1240,335},{1249,322},{1272,321},{1287,340},{1282,359},{1260,365},{1241,355}}),
        polygon(new double[][]{{74,198},{87,192},{144,213},{146,229},{135,238},{77,218}}),
        new Rectangle2D.Double(357, 335, 117, 48),
        new Rectangle2D.Double(492, 294, 98, 62),
        new Rectangle2D.Double(589, 334, 43, 29),
        new Rectangle2D.Double(637, 212, 118, 61),
        new Rectangle2D.Double(645, 162, 38, 49),
        new Rectangle2D.Double(741, 421, 64, 32),
        new Rectangle2D.Double(1294, 219, 126, 63),
        new Rectangle2D.Double(1513, 460, 127, 62),
        new Rectangle2D.Double(1544, 270, 65, 35),
        new Rectangle2D.Double(1293, 428, 65, 39),
        new Rectangle2D.Double(1470, 193, 116, 82),
        new Rectangle2D.Double(1897, 300, 41, 45)
    };
    private double x, y, cameraX, cameraY;
    private boolean cleared, battleRequested, mineReached;
    private String message;
    private boolean showCollision;
    private final SpriteAnimator animation = new SpriteAnimator();

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
        Path2D path = new Path2D.Double();
        double[][] points = {{60,345},{185,340},{265,350},{340,345},{450,355},
            {585,350},{620,348},{705,348},{750,365},{875,365},{924,383},
            {1048,383},{1125,362},{1205,340},{1280,320},{1410,300},
            {1570,310},{1660,340},{1795,340},{1870,326},{1940,315},
            {2008,315},{2008,399},{1870,415},{1795,427},{1660,439},
            {1585,470},{1420,483},{1290,453},{1205,447},{1130,447},
            {1048,439},{924,439},{875,448},{720,449},{690,450},
            {620,445},{580,450},{450,462},{360,462},{265,458},{180,444},{60,430}};
        path.moveTo(points[0][0], points[0][1]);
        for (int i=1;i<points.length;i++) path.lineTo(points[i][0],points[i][1]);
        path.closePath();
        Area area = new Area(path);
        area.add(polygon(new double[][]{{55,260},{65,230},{80,212},{150,224},{180,220},{230,260},
            {255,310},{300,342},{310,405},{190,430},{60,423}}));
        area.add(new Area(new Rectangle2D.Double(613,252,91,165)));
        area.add(polygon(new double[][]{{601,230},{620,188},{700,185},
            {754,218},{759,268},{710,287},{608,283}}));
        area.add(polygon(new double[][]{{1230,348},{1267,294},{1320,278},
            {1360,220},{1450,212},{1490,240},{1585,265},{1620,320},
            {1655,378},{1640,475},{1580,510},{1420,510},{1300,461}}));
        area.add(new Area(new Rectangle2D.Double(1940,284,62,91)));
        return area;
    }

    private static Area polygon(double[][] points) {
        Path2D p=new Path2D.Double(); p.moveTo(points[0][0],points[0][1]);
        for(int i=1;i<points.length;i++) p.lineTo(points[i][0],points[i][1]);
        p.closePath(); return new Area(p);
    }

    public void toggleCollisionView() { showCollision=!showCollision; }

    public void stopMovement() { animation.stop(); }

    public void reset() {
        animation.reset(SpriteAnimator.DOWN);
        x=135; y=312; cleared=false; battleRequested=false; mineReached=false;
        message="You wake in the forest. Follow the trail to the right.";
        updateCamera();
    }

    public void update(int dx, int dy, double seconds) {
        update(dx,dy,seconds,false);
    }

    public void update(int dx, int dy, double seconds, boolean running) {
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
        if(mineZone.intersects(x,y,SIZE,SIZE)) {
            if(cleared) {
                mineReached=true;
                message="Mine entrance reached! Next area is coming in the next update.";
            } else message="The forest guardian blocks the route. Clear the encounter first.";
        }
        updateCamera();
    }

    private boolean canStand(double px,double py) {
        Rectangle2D body=new Rectangle2D.Double(px+4,py+10,8,6);
        if(!floor.contains(body)) return false;
        if(!cleared && body.getMaxX()>1660) return false;
        for(Shape obstacle:obstacles) if(obstacle.intersects(body)) return false;
        return true;
    }

    private void updateCamera() {
        cameraX=Math.max(0,Math.min(x*scaleX+SIZE*scaleX/2-VIEW_W/2.0,background.getWidth()-VIEW_W));
        cameraY=Math.max(0,Math.min(y*scaleY+SIZE*scaleY/2-VIEW_H/2.0,background.getHeight()-VIEW_H));
    }

    public boolean consumeBattleRequest() {
        boolean pending=battleRequested;
        battleRequested=false;
        return pending;
    }

    public boolean interact(int mouseX,int mouseY) {
        double wx=(mouseX+Math.round(cameraX))/scaleX;
        double wy=(mouseY-TOP+Math.round(cameraY))/scaleY;
        if(mouseY<TOP || mouseY>TOP+VIEW_H) return false;
        if(!cleared && bossZone.contains(wx,wy) && Point2D.distance(x,y,wx,wy)<110) return true;
        if(mineZone.contains(wx,wy))
            message=cleared?"Walk into the mine entrance to finish this map.":"Defeat the forest guardian first.";
        return false;
    }

    public void returnFromBattle() {
        animation.reset(SpriteAnimator.RIGHT);
        cleared=true; battleRequested=false; x=1510; y=390;
        message="Forest cleared! The mine entrance on the right is now open.";
        updateCamera();
    }

    public void draw(Graphics graphics) {
        Graphics2D g=(Graphics2D)graphics.create();
        g.setClip(0,TOP,VIEW_W,VIEW_H);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.translate(-Math.round(cameraX),TOP-Math.round(cameraY));
        // Draw source pixels 1:1: no resize filter or palette adjustment.
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
        if(!cleared) {
            g.setColor(new Color(115,54,45));
            int bx=(int)(1660*scaleX);
            g.fillRect(bx,(int)(340*scaleY),8,(int)(100*scaleY));
        }
        int feetX=(int)Math.round((x+SIZE/2)*scaleX);
        int feetY=(int)Math.round((y+SIZE)*scaleY);
        animation.draw(g,feetX,feetY,36,54);
        g.dispose();
        graphics.setColor(new Color(15,20,19));
        graphics.fillRect(0,0,VIEW_W,TOP);
        graphics.fillRect(0,TOP+VIEW_H,VIEW_W,60);
        graphics.setFont(new Font(Font.MONOSPACED,Font.BOLD,18));
        graphics.setColor(Color.WHITE);
        graphics.drawString("FOREST v3.3  |  "+(cleared?"Route to mine: OPEN":"Follow the trail"),20,25);
        graphics.setFont(new Font(Font.MONOSPACED,Font.PLAIN,14));
        graphics.drawString(cleared ? "WASD: Walk | Shift: Run | Esc: Pause | F3: Collision view" : "WASD: Walk | Shift: Run | Esc: Pause | F3: Collision view",20,47);
        graphics.setColor(mineReached?new Color(175,210,175):Color.LIGHT_GRAY);
        graphics.drawString(message,20,612);
    }
}
