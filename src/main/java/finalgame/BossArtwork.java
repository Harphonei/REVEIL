package finalgame;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/** Static artwork; animation/cutscene poses will be added later. */
public final class BossArtwork {
    private static final BufferedImage MAP=load("forest-boss-map.png");
    private static final BufferedImage BATTLE=load("forest-boss-battle.png");
    private BossArtwork() {}
    private static BufferedImage load(String name) {
        try {
            java.net.URL url=BossArtwork.class.getResource("/finalgame/"+name);
            if(url==null) throw new IOException("Missing "+name);
            BufferedImage result=ImageIO.read(url);
            if(result==null) throw new IOException("Invalid "+name);
            return result;
        } catch(IOException e) { throw new IllegalStateException("Cannot load boss artwork",e); }
    }
    public static void drawMap(Graphics g,int footX,int footY) {
        draw(g,MAP,footX-30,footY-100,60,100);
    }
    public static void drawBattle(Graphics g,Rectangle bounds) {
        draw(g,BATTLE,bounds.x,bounds.y,bounds.width,bounds.height);
    }
    private static void draw(Graphics graphics,BufferedImage image,int x,int y,int w,int h) {
        Graphics2D g=(Graphics2D)graphics.create();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(image,x,y,w,h,null);g.dispose();
    }
}
