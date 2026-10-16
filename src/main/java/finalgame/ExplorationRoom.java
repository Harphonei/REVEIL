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
    private final Rectangle2D mineZone = new Rectangle2D.Double(1940, 312, 70, 90);
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
        if(!cleared && body.getMaxX()>1658) return false;
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
            int bx=(int)(1658*scaleX);
            g.fillRect(bx,(int)(345*scaleY),8,(int)(85*scaleY));
        }
        if(!cleared) BossArtwork.drawMap(g,(int)Math.round(1485*scaleX),(int)Math.round(350*scaleY));
        int feetX=(int)Math.round((x+SIZE/2)*scaleX);
        int feetY=(int)Math.round((y+SIZE)*scaleY);
        animation.draw(g,feetX,feetY,36,54);
        g.dispose();
        graphics.setColor(new Color(15,20,19));
        graphics.fillRect(0,0,VIEW_W,TOP);
        graphics.fillRect(0,TOP+VIEW_H,VIEW_W,60);
        graphics.setFont(new Font(Font.MONOSPACED,Font.BOLD,18));
        graphics.setColor(Color.WHITE);
        graphics.drawString("FOREST v3.9  |  "+(cleared?"Route to mine: OPEN":"Follow the trail"),20,25);
        graphics.setFont(new Font(Font.MONOSPACED,Font.PLAIN,14));
        graphics.drawString(cleared ? "WASD: Walk | Shift: Run | Esc: Pause | F3: Collision view" : "WASD: Walk | Shift: Run | Esc: Pause | F3: Collision view",20,47);
        graphics.setColor(mineReached?new Color(175,210,175):Color.LIGHT_GRAY);
        graphics.drawString(message,20,612);
    }
}
