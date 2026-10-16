package finalgame;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/** Forest coordinates use a 2048 x 683 design grid, independent of image pixels. */
public class ExplorationRoom {
    private static final double SCALE = 1.5;
    private static final int VIEW_W = 960, VIEW_H = 520, TOP = 60;
    private static final double SPEED = 90, SIZE = 16;
    private final BufferedImage background;
    private final Area floor = createFloor();
    private final Rectangle2D bossZone = new Rectangle2D.Double(1430, 335, 130, 105);
    private final Rectangle2D mineZone = new Rectangle2D.Double(1920, 322, 88, 90);
    private final Rectangle2D[] obstacles = {
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

    public ExplorationRoom() {
        try {
            java.net.URL resource = getClass().getResource("/finalgame/forest-map.png");
            if (resource == null) throw new IOException("Missing /finalgame/forest-map.png");
            background = ImageIO.read(resource);
            if (background == null) throw new IOException("Invalid forest PNG");
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot load forest map. Copy all of src including main/resources.", ex);
        }
        reset();
    }

    private static Area createFloor() {
        Path2D path = new Path2D.Double();
        double[][] points = {{70,365},{180,365},{265,377},{360,378},{450,375},
            {580,375},{620,370},{690,370},{720,380},{870,380},{924,386},
            {1048,386},{1130,382},{1205,368},{1290,343},{1410,320},
            {1570,325},{1660,355},{1795,354},{1870,336},{1935,326},
            {1995,326},{1995,391},{1870,405},{1795,410},{1660,419},
            {1585,443},{1420,460},{1290,435},{1205,431},{1130,438},
            {1048,438},{924,438},{870,438},{720,438},{690,435},
            {620,434},{580,438},{450,446},{360,449},{265,446},{180,442},{70,425}};
        path.moveTo(points[0][0], points[0][1]);
        for (int i=1;i<points.length;i++) path.lineTo(points[i][0],points[i][1]);
        path.closePath();
        Area area = new Area(path);
        area.add(new Area(new Rectangle2D.Double(65,248,165,177)));
        area.add(new Area(new Rectangle2D.Double(105,226,91,70)));
        area.add(new Area(new Rectangle2D.Double(621,254,77,160)));
        area.add(new Area(new Rectangle2D.Double(609,207,140,72)));
        area.add(new Area(new Rectangle2D.Double(1320,288,265,183)));
        area.add(new Area(new Rectangle2D.Double(1390,265,103,210)));
        area.add(new Area(new Rectangle2D.Double(1535,336,100,141)));
        return area;
    }

    public void reset() {
        x=135; y=312; cleared=false; battleRequested=false; mineReached=false;
        message="You wake in the forest. Follow the trail to the right.";
        updateCamera();
    }

    public void update(int dx, int dy, double seconds) {
        // Small axis-separated steps prevent tunnelling and allow sliding along walls.
        double length=Math.hypot(dx,dy);
        if (length>0) {
            double distance=SPEED*Math.max(0,Math.min(seconds,0.1));
            int steps=Math.max(1,(int)Math.ceil(distance/2));
            for(int i=0;i<steps;i++) {
                double nx=x+dx/length*distance/steps;
                if(canStand(nx,y)) x=nx;
                double ny=y+dy/length*distance/steps;
                if(canStand(x,ny)) y=ny;
            }
        }
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
        Rectangle2D body=new Rectangle2D.Double(px,py,SIZE,SIZE);
        if(!floor.contains(body)) return false;
        if(!cleared && px+SIZE>1660) return false;
        for(Rectangle2D obstacle:obstacles) if(obstacle.intersects(body)) return false;
        return true;
    }

    private void updateCamera() {
        cameraX=Math.max(0,Math.min(x*SCALE+SIZE*SCALE/2-VIEW_W/2.0,2048*SCALE-VIEW_W));
        cameraY=Math.max(0,Math.min(y*SCALE+SIZE*SCALE/2-VIEW_H/2.0,683*SCALE-VIEW_H));
    }

    public boolean consumeBattleRequest() {
        boolean pending=battleRequested;
        battleRequested=false;
        return pending;
    }

    public boolean interact(int mouseX,int mouseY) {
        double wx=(mouseX+cameraX)/SCALE;
        double wy=(mouseY-TOP+cameraY)/SCALE;
        if(mouseY<TOP || mouseY>TOP+VIEW_H) return false;
        if(!cleared && bossZone.contains(wx,wy) && Point2D.distance(x,y,wx,wy)<110) return true;
        if(mineZone.contains(wx,wy))
            message=cleared?"Walk into the mine entrance to finish this map.":"Defeat the forest guardian first.";
        return false;
    }

    public void returnFromBattle() {
        cleared=true; battleRequested=false; x=1510; y=390;
        message="Forest cleared! The mine entrance on the right is now open.";
        updateCamera();
    }

    public void draw(Graphics graphics) {
        Graphics2D g=(Graphics2D)graphics.create();
        g.setClip(0,TOP,VIEW_W,VIEW_H);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.translate(-Math.round(cameraX),TOP-Math.round(cameraY));
        g.drawImage(background,0,0,(int)(2048*SCALE),(int)(683*SCALE),null);
        if(!cleared) {
            g.setColor(new Color(115,54,45));
            int bx=(int)(1660*SCALE);
            g.fillRect(bx,(int)(353*SCALE),8,(int)(67*SCALE));
        }
        // Temporary actor is separate from the approved environment PNG.
        g.setColor(new Color(0,0,0,100));
        g.fillOval((int)(x*SCALE)-2,(int)((y+SIZE-3)*SCALE),28,10);
        g.setColor(new Color(30,30,35));
        g.fillRect((int)(x*SCALE)-2,(int)(y*SCALE)-2,28,28);
        g.setColor(new Color(225,225,212));
        g.fillRect((int)(x*SCALE),(int)(y*SCALE),24,24);
        g.dispose();
        graphics.setColor(new Color(15,20,19));
        graphics.fillRect(0,0,VIEW_W,TOP);
        graphics.fillRect(0,TOP+VIEW_H,VIEW_W,60);
        graphics.setFont(new Font(Font.MONOSPACED,Font.BOLD,18));
        graphics.setColor(Color.WHITE);
        graphics.drawString("FOREST  |  "+(cleared?"Route to mine: OPEN":"Follow the trail"),20,25);
        graphics.setFont(new Font(Font.MONOSPACED,Font.PLAIN,14));
        graphics.drawString(cleared ? "WASD: Move   |   Esc: Pause   |   Follow the trail into the mine entrance" : "WASD: Move   |   Esc: Pause   |   Walk into the clearing to face the guardian",20,47);
        graphics.setColor(mineReached?new Color(175,210,175):Color.LIGHT_GRAY);
        graphics.drawString(message,20,612);
    }
}
