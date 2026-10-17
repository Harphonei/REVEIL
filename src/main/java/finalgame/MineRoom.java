package finalgame;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

/** Mine world uses native artwork pixels; collision is always at the feet. */
public final class MineRoom {
    private static final double ZOOM=1.35, SPEED=90;
    private final BufferedImage background;
    private final Area floor=new Area();
    private final Shape[] obstacles={
        new Rectangle(570,145,75,118), new Rectangle(566,277,46,70),
        new Rectangle(611,73,100,54), new Rectangle(714,78,77,51),
        new Rectangle(801,49,74,78), new Rectangle(884,75,119,52),
        new Rectangle(1033,79,86,50), new Rectangle(1115,57,80,70),
        new Rectangle(1135,145,79,123), new Rectangle(1169,265,51,93),
        new Rectangle(674,397,25,56), new Rectangle(794,478,21,40),
        new Rectangle(414,668,63,49), new Rectangle(397,686,23,45),
        new Rectangle(588,726,21,25), new Rectangle(584,826,23,22),
        new Rectangle(1236,484,35,31), new Rectangle(1307,486,26,29),
        new Rectangle(55,834,23,14), new Rectangle(350,881,22,24),
        new Rectangle(482,892,22,24), new Rectangle(598,870,18,22),
        new Rectangle(1026,481,29,27), new Rectangle(1060,508,24,25)
    };
    private final Rectangle bossZone=new Rectangle(850,205,120,90);
    private final Rectangle gate=new Rectangle(1005,357,119,22);
    private final SpriteAnimator animation=new SpriteAnimator();
    private double x,y,cameraX,cameraY;
    private boolean cleared,battleRequested,backRequested,exitReached,showCollision;

    public MineRoom() {
        try {
            background=ImageIO.read(MineRoom.class.getResource("/finalgame/mine-map.png"));
            if(background==null || background.getWidth()!=1536 || background.getHeight()!=1024)
                throw new IllegalStateException("Invalid mine map");
        } catch(Exception ex) {throw new IllegalStateException("Cannot load mine-map.png",ex);}
        floor.add(polygon(new int[][]{{0,839},{293,840},{322,850},{517,850},
            {520,684},{540,660},{551,626},{592,580},{637,532},{681,488},
            {704,477},{704,350},{814,350},{814,495},{786,518},{741,552},
            {699,597},{655,641},{617,680},{599,704},{599,892},{576,917},
            {527,925},{327,921},{299,903},{0,900}}));
        floor.add(polygon(new int[][]{{586,119},{1209,119},{1219,353},
            {1117,353},{1117,493},{1158,511},{1536,511},{1536,554},
            {1104,554},{1052,538},{1028,511},{1017,350},{586,350}}));
        floor.add(new Area(new Rectangle(704,337,110,40)));
        floor.add(new Area(new Rectangle(370,686,159,44)));
        reset();
    }
    private static Area polygon(int[][] points) {
        Path2D p=new Path2D.Double();p.moveTo(points[0][0],points[0][1]);
        for(int i=1;i<points.length;i++)p.lineTo(points[i][0],points[i][1]);
        p.closePath();return new Area(p);
    }
    public void reset() {
        cleared=false;exitReached=false;enter();
    }
    public void enter() {
        x=40;y=847;battleRequested=false;backRequested=false;
        animation.reset(SpriteAnimator.RIGHT);updateCamera();
    }
    public void stopMovement(){animation.stop();}
    public void toggleCollisionView(){showCollision=!showCollision;}
    private boolean canStand(double px,double py) {
        Rectangle2D feet=new Rectangle2D.Double(px+4,py+10,8,6);
        if(!floor.contains(feet))return false;
        if(!cleared && gate.intersects(feet))return false;
        for(Shape obstacle:obstacles)if(obstacle.intersects(feet))return false;
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
        if(cleared && x>1480)exitReached=true;
        updateCamera();
    }
    private void updateCamera() {
        cameraX=Math.max(0,Math.min(x+8-960/(2*ZOOM),background.getWidth()-960/ZOOM));
        cameraY=Math.max(0,Math.min(y+8-520/(2*ZOOM),background.getHeight()-520/ZOOM));
    }
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
        g.translate(0,60);g.scale(ZOOM,ZOOM);g.translate(-Math.round(cameraX),-Math.round(cameraY));
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
        animation.draw(g,(int)Math.round(x+8),(int)Math.round(y+16),36,54);g.dispose();
        graphics.setColor(new Color(15,20,19));graphics.fillRect(0,0,960,60);graphics.fillRect(0,580,960,60);
        graphics.setColor(Color.WHITE);graphics.setFont(new Font(Font.MONOSPACED,Font.BOLD,18));
        graphics.drawString("MINE v3.14  |  "+(cleared?"Laboratory cleared":"Follow the rails"),20,25);
        graphics.setFont(new Font(Font.MONOSPACED,Font.PLAIN,14));
        graphics.drawString("WASD: Walk | Shift: Run | Esc: Pause | F3: Collision view",20,47);
        graphics.setColor(Color.LIGHT_GRAY);
        graphics.drawString(exitReached?"Butterfly forest entrance reached. Next map is coming soon.":
            cleared?"The eastern passage is open. Follow it out of the mine.":
            "A hidden laboratory lies ahead. Explore the room to meet its keeper.",20,612);
    }
}
