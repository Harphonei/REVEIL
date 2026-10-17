package finalgame;

import java.awt.*;
import java.awt.image.BufferedImage;

/** Small hand-authored tile sample, 2 native map pixels per logical pixel. */
final class TempleTileSample {
    private static final BufferedImage PATCH=build();
    private TempleTileSample() {}
    private static BufferedImage build() {
        BufferedImage image=new BufferedImage(76,108,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g=image.createGraphics();
        // Reusable 10x10 stone tiles, with a few deliberate chipped corners.
        for(int y=0;y<108;y+=10)for(int x=0;x<76;x+=10) {
            int variation=(x/10+y/10)%3;
            g.setColor(new Color(18,25,35));g.fillRect(x,y,10,10);
            g.setColor(variation==0?new Color(36,45,59):new Color(40,49,62));
            g.fillRect(x+1,y+1,9,9);
            g.setColor(new Color(48,57,70));g.fillRect(x+2,y+1,6,1);
            if(variation==1){g.setColor(new Color(25,33,44));g.fillRect(x+1,y+8,2,2);}
            if(variation==2){g.setColor(new Color(35,41,29));g.fillRect(x+8,y+1,2,2);}
        }
        Shape floorClip=g.getClip();g.clipRect(1,0,74,86);
        // Bottom tread starts at world y346, fifth tread at y266.
        for(int step=0;step<9;step++) {
            int y=76-step*10;
            g.setColor(new Color(43,53,68));g.fillRect(1,y,74,8);
            g.setColor(new Color(53,63,78));g.fillRect(1,y,74,1);
            g.setColor(new Color(34,43,56));g.fillRect(1,y+6,74,2);
            g.setColor(new Color(17,24,34));g.fillRect(1,y+8,74,2);
            for(int x=1+(step%2)*7;x<75;x+=15) {
                g.setColor(new Color(28,37,50));g.fillRect(x,y+1,1,6);
                g.setColor(new Color(47,57,71));g.fillRect(x+1,y+2,1,4);
            }
        }
        g.setClip(floorClip);g.dispose();return image;
    }
    static void draw(Graphics graphics) {
        Graphics2D g=(Graphics2D)graphics.create();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(PATCH,1076,194,152,216,null);g.dispose();
    }
}
