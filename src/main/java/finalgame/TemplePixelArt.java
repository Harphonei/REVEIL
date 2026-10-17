package finalgame;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

/** Hand-authored tiles and object sprites on a 768x512 pixel canvas, enlarged 2x. */
public final class TemplePixelArt {
    private static final Color INK=new Color(12,18,27), SHADOW=new Color(19,27,39);
    private static final Color STONE=new Color(43,53,68), EDGE=new Color(60,70,84);
    private static final Color LIGHT=new Color(78,87,99), MOSS=new Color(43,48,31);
    private static final int[][] CORPSES={
        {67,420,94,59},{298,554,110,64},{748,222,78,61},{847,312,82,65},
        {730,382,71,70},{1400,261,72,68},{1335,362,74,62},{1399,441,88,69},
        {791,596,69,67},{854,697,104,61},{1042,745,80,68},{1160,797,116,63},
        {1305,655,95,61},{981,403,105,64},{1070,570,103,72}};
    private TemplePixelArt() {}
    private static void box(Graphics2D g,Color c,int x,int y,int w,int h) {
        g.setColor(c);g.fillRect(x,y,w,h);
    }
    private static void polygon(Graphics2D g,Color c,int...p) {
        int[]x=new int[p.length/2],y=x.clone();
        for(int i=0;i<x.length;i++){x[i]=p[i*2];y[i]=p[i*2+1];}
        g.setColor(c);g.fillPolygon(x,y,x.length);
    }
    private static int pattern(int x,int y){return Math.floorMod(x*37+y*19+x*y,17);}
    private static BufferedImage upscale(BufferedImage small) {
        BufferedImage result=new BufferedImage(1536,1024,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g=result.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(small,0,0,1536,1024,null);g.dispose();return result;
    }
    public static BufferedImage background() {
        BufferedImage canvas=new BufferedImage(768,512,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g=canvas.createGraphics();box(g,INK,0,0,768,512);
        // Interlocking rock tiles form a dark, solid bank around the walkable plane.
        for(int y=-8;y<520;y+=12)for(int x=-12;x<780;x+=16)
            rock(g,x+(y/12%2)*8,y,16,12,0);
        // Forest is left of the ruin; all silhouettes are deliberately stepped pixels.
        for(int y=-28;y<520;y+=44)for(int x=-20;x<352;x+=45) {
            int xx=x+(y/44%2)*20,yy=y+pattern(x,y)-8;
            tree(g,xx,yy,pattern(x,y));
        }
        // The exact floor mask is also the visual edge, so no guessed walking boundary.
        BufferedImage mask;
        try{mask=ImageIO.read(TemplePixelArt.class.getResource("/finalgame/temple-floor.png"));}
        catch(Exception e){throw new IllegalStateException("Missing temple floor",e);}
        BufferedImage floor=floorTiles();
        for(int y=0;y<512;y++)for(int x=0;x<768;x++) {
            boolean walk=true;
            for(int dy=0;dy<2;dy++)for(int dx=0;dx<2;dx++)
                if((mask.getRGB(x*2+dx,y*2+dy)&255)<128)walk=false;
            if(walk)canvas.setRGB(x,y,floor.getRGB(x,y));
        }
        // Back walls and temple facade. None of these tiles cover a walking lane.
        facade(g);
        // Low ruined wall shoulders along the long left approach.
        for(int x=0;x<314;x+=20) {
            int y=219+pattern(x,0)%4;wallBlock(g,x,y,19,26);
            if(x>46&&x<278&&pattern(x,2)%3==0)rock(g,x+7,y-7,13,11,1);
        }
        for(int x=0;x<295;x+=22)wallBlock(g,x,280+pattern(x,1)%4,21,21);
        // Column bases, plinths, and rubble use the same anchors as the existing masks.
        plinth(g,467,104,56,69);plinth(g,625,104,57,69);
        brokenStatue(g,478,94);
        column(g,6,170,27,56,true);column(g,325,112,31,93,false);
        column(g,365,275,30,63,true);column(g,501,345,30,61,true);
        column(g,707,289,31,77,false);column(g,740,176,28,58,true);
        column(g,218,287,25,49,true);
        // The overhead arch has a separately drawn front pier for actor occlusion.
        arch(g,false);
        for(int[]c:CORPSES) {
            // Author each sprite in a transparent tile, then fit its original footprint.
            BufferedImage sprite=fallenKnight(pattern(c[0],c[1])%2==0);
            g.drawImage(sprite,c[0]/2,c[1]/2,(c[2]+1)/2,(c[3]+1)/2,null);
        }
        int[][] rubble={{351,467,33,22},{497,479,25,25},{409,599,31,31},
            {698,418,29,24},{757,309,26,23},{831,292,24,24},{1020,384,26,21},
            {1284,388,25,22},{1410,544,29,29},{1363,603,26,25},{1272,738,29,28},
            {1127,776,27,25},{945,720,32,30},{797,674,30,32}};
        for(int[]r:rubble)rock(g,r[0]/2,r[1]/2,r[2]/2,r[3]/2,1);
        g.dispose();return upscale(canvas);
    }
    private static BufferedImage floorTiles() {
        BufferedImage image=new BufferedImage(768,512,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g=image.createGraphics();
        for(int y=0;y<512;y+=10)for(int x=0;x<768;x+=10) {
            int v=pattern(x/10,y/10);
            box(g,SHADOW,x,y,10,10);
            box(g,v%3==0?new Color(36,45,59):new Color(40,49,62),x+1,y+1,9,9);
            box(g,new Color(48,57,70),x+2,y+1,6,1);
            if(v%5==0){box(g,SHADOW,x+1,y+8,2,2);box(g,MOSS,x+8,y+1,2,2);}
            if(v==4){box(g,SHADOW,x+5,y+4,1,3);box(g,SHADOW,x+6,y+6,2,1);}
        }
        // Approach uses shorter cobbles, keeping its horizontal flow readable.
        for(int y=247;y<274;y+=6)for(int x=-4;x<359;x+=9) {
            int xx=x+(y/6%2)*4;
            box(g,SHADOW,xx,y,9,6);box(g,new Color(44,53,65),xx+1,y+1,8,5);
            box(g,new Color(55,64,76),xx+2,y+1,5,1);
        }
        g.dispose();return image;
    }
    private static void rock(Graphics2D g,int x,int y,int w,int h,int v) {
        polygon(g,INK,x+3,y,x+w-3,y,x+w,y+3,x+w,y+h-3,x+w-3,y+h,x+2,y+h,x,y+h-3,x,y+3);
        polygon(g,v==0?SHADOW:STONE,x+3,y+1,x+w-3,y+1,x+w-1,y+4,x+w-2,y+h-3,x+3,y+h-2,x+1,y+4);
        box(g,v==0?new Color(34,44,59):EDGE,x+4,y+1,Math.max(2,w-8),2);
        box(g,SHADOW,x+w-4,y+4,2,Math.max(2,h-6));
        if(v==2){box(g,MOSS,x+2,y+3,4,2);box(g,MOSS,x+4,y+5,3,1);}
    }
    private static void tree(Graphics2D g,int x,int y,int v) {
        // Layered serrated crown, never a smoothed ellipse.
        Color dark=new Color(15,27,41),mid=new Color(23,38,54),leaf=new Color(33,49,65);
        polygon(g,dark,x+13,y,x+28,y,x+28,y+4,x+36,y+4,x+36,y+10,x+42,y+10,
            x+42,y+29,x+36,y+29,x+36,y+37,x+8,y+37,x+8,y+32,x+2,y+32,x+2,y+13,x+8,y+13,x+8,y+5,x+13,y+5);
        polygon(g,mid,x+12,y+5,x+29,y+5,x+29,y+10,x+35,y+10,x+35,y+25,
            x+29,y+25,x+29,y+30,x+9,y+30,x+9,y+24,x+6,y+24,x+6,y+14,x+12,y+14);
        box(g,leaf,x+13,y+8,12,3);box(g,leaf,x+8,y+16,7,3);box(g,leaf,x+27,y+15,6,3);
        if(v%3==0){box(g,MOSS,x+17,y+11,9,3);box(g,MOSS,x+8,y+23,5,2);}
        // Bare angular branches in front of the canopy.
        box(g,new Color(26,23,30),x+21,y+19,3,31);
        for(int i=0;i<8;i++) {
            box(g,new Color(33,27,34),x+22-i*2,y+27-i*2,3,3);
            box(g,new Color(33,27,34),x+23+i*2,y+32-i*2,3,3);
        }
        box(g,INK,x+20,y+46,7,5);
    }
    private static void wallBlock(Graphics2D g,int x,int y,int w,int h) {
        box(g,INK,x,y,w,h);box(g,STONE,x+1,y+2,w-2,h-3);
        box(g,EDGE,x+1,y+1,w-2,3);box(g,SHADOW,x+w-4,y+4,3,h-5);
        box(g,SHADOW,x+1,y+h-4,w-2,2);
        if(pattern(x,y)%3==0){box(g,MOSS,x+2,y+5,3,4);box(g,MOSS,x+4,y+9,3,2);}
    }
    private static void facade(Graphics2D g) {
        // Upper facade, including side towers, built from 20x12 reusable stone blocks.
        for(int y=-8;y<98;y+=12)for(int x=358;x<768;x+=20)
            wallBlock(g,x+(y/12%2)*10,y,19,11);
        // Taller side retaining walls are behind the plinths, not over open floor.
        for(int y=98;y<158;y+=12)for(int x=350;x<371;x+=20)wallBlock(g,x,y,19,11);
        for(int y=98;y<140;y+=12)wallBlock(g,744,y,23,11);
        window(g,447,50,17,41);window(g,657,43,18,44);window(g,724,61,17,41);
        for(int x:new int[]{410,508,616,693}) {
            box(g,INK,x,0,17,91);box(g,STONE,x+2,0,13,88);
            box(g,EDGE,x+3,0,3,86);box(g,SHADOW,x+12,0,3,88);
            box(g,EDGE,x-1,24,19,4);box(g,SHADOW,x-1,28,19,3);
            box(g,EDGE,x-2,86,21,5);box(g,SHADOW,x-2,91,21,5);
        }
        // Dark doorway is an actual opening; no glow or lit floor beyond it.
        polygon(g,INK,548,97,548,28,554,18,563,11,581,11,593,17,601,28,601,97);
        polygon(g,new Color(5,10,17),554,97,554,29,559,22,565,18,584,18,591,25,595,32,595,97);
        for(int y=32;y<94;y+=10){wallBlock(g,537,y,13,9);wallBlock(g,600,y,13,9);}
        int[][] arch={{540,22},{544,14},{552,7},{562,2},{573,0},{584,2},{595,8},{603,16}};
        for(int[]p:arch)wallBlock(g,p[0],p[1],10,8);
        // Stairs use the exact approved sample dimensions and fifth-step location.
        Shape old=g.getClip();g.clipRect(538,97,76,86);
        for(int i=0;i<9;i++) {
            int y=173-i*10;
            box(g,STONE,539,y,74,8);box(g,new Color(53,63,78),539,y,74,1);
            box(g,new Color(34,43,56),539,y+6,74,2);box(g,INK,539,y+8,74,2);
            for(int x=539+(i%2)*7;x<613;x+=15)box(g,SHADOW,x,y+1,1,6);
        }
        g.setClip(old);
        for(int y=97;y<183;y+=10){wallBlock(g,529,y,8,9);wallBlock(g,615,y,8,9);}
        // Small ivy clusters lie only on wall blocks, never on the opening.
        for(int[]v:new int[][]{{393,10},{479,0},{632,6},{704,4}}) {
            for(int i=0;i<9;i++) {int x=v[0]+(i%3)*2,y=v[1]+i*4;box(g,MOSS,x,y,3,3);box(g,new Color(57,60,36),x-2,y+1,2,1);}
        }
    }
    private static void window(Graphics2D g,int x,int y,int w,int h) {
        box(g,EDGE,x,y,w,h);box(g,INK,x+2,y+2,w-4,h-3);
        box(g,new Color(6,12,21),x+4,y+5,w-8,h-7);box(g,STONE,x-2,y+h-3,w+4,4);
    }
    private static void plinth(Graphics2D g,int x,int y,int w,int h) {
        box(g,INK,x,y,w,h);box(g,SHADOW,x+2,y+3,w-4,h-5);
        box(g,STONE,x+2,y+2,w-4,h-19);box(g,EDGE,x+3,y+3,w-6,2);
        box(g,new Color(54,63,76),x+5,y+6,w-10,h-27);
        box(g,STONE,x+7,y+8,w-14,h-31);
        box(g,EDGE,x+3,y+h-18,w-6,2);box(g,INK,x+1,y+h-12,w-2,2);
        box(g,EDGE,x+4,y+h-10,w-8,2);box(g,MOSS,x+2,y+h-7,12,3);
        for(int i=0;i<3;i++)box(g,SHADOW,x+12+i*8,y+h-16,1,5);
        // Subtle carved corner marks, not high-frequency texture.
        for(int[]q:new int[][]{{x+8,y+10},{x+w-11,y+10},{x+8,y+h-27},{x+w-11,y+h-27}}) {
            box(g,EDGE,q[0],q[1],3,1);box(g,EDGE,q[0],q[1],1,3);
        }
    }
    private static void column(Graphics2D g,int x,int y,int w,int h,boolean broken) {
        int top=broken?y+8:y;
        box(g,INK,x+2,top,w-4,h-(top-y));box(g,STONE,x+5,top,w-10,h-8-(top-y));
        box(g,EDGE,x+6,top+2,3,h-12-(top-y));box(g,SHADOW,x+w-9,top+1,3,h-10-(top-y));
        for(int i=11;i<w-9;i+=5)box(g,SHADOW,x+i,top+3,1,h-15-(top-y));
        box(g,INK,x,y+h-8,w,8);box(g,EDGE,x+1,y+h-8,w-2,2);box(g,STONE,x+2,y+h-5,w-4,3);
        if(broken)rock(g,x+3,top-3,w-6,8,1);else{box(g,EDGE,x,top,w,3);box(g,STONE,x+1,top+3,w-2,4);}
        box(g,MOSS,x+4,y+h-13,5,3);
    }
    private static void brokenStatue(Graphics2D g,int x,int y) {
        // Helmet, shoulder and split breastplate echo the intact guardian silhouette.
        rock(g,x+13,y-3,13,17,1);box(g,INK,x+16,y+4,8,2);
        rock(g,x-2,y+8,14,15,1);rock(g,x+29,y+15,13,14,0);
        polygon(g,INK,x+7,y+10,x+28,y+10,x+33,y+30,x+21,y+42,x+5,y+32);
        polygon(g,new Color(67,77,91),x+10,y+12,x+25,y+12,x+29,y+28,x+19,y+36,x+8,y+29);
        box(g,LIGHT,x+12,y+14,10,2);box(g,SHADOW,x+18,y+22,2,12);
        rock(g,x+2,y+37,16,13,1);rock(g,x+25,y+42,13,10,0);
        box(g,MOSS,x+12,y+30,6,3);
    }
    private static BufferedImage fallenKnight(boolean reverse) {
        BufferedImage sprite=new BufferedImage(52,34,BufferedImage.TYPE_INT_ARGB);Graphics2D g=sprite.createGraphics();
        if(reverse){g.translate(52,0);g.scale(-1,1);}
        // Sideways armored body, two boots, bent arms, shield and dropped sword.
        polygon(g,INK,3,6,12,3,18,8,29,8,34,13,43,13,49,19,46,23,30,19,20,21,12,15,4,15);
        rock(g,3,5,12,11,1);box(g,LIGHT,6,6,4,1);box(g,INK,5,11,9,2);
        polygon(g,STONE,15,9,26,9,31,14,25,19,17,17,13,13);
        box(g,EDGE,17,10,8,2);box(g,SHADOW,24,14,6,3);
        rock(g,13,17,8,7,1);rock(g,27,7,7,7,1);
        box(g,STONE,28,16,9,4);box(g,EDGE,30,16,6,1);
        box(g,STONE,36,19,9,4);box(g,SHADOW,43,18,6,6);
        polygon(g,INK,24,21,33,18,40,22,39,29,32,33,25,29);
        polygon(g,new Color(49,48,54),25,22,33,20,38,23,37,28,32,31,26,28);
        box(g,new Color(87,82,78),27,22,7,1);box(g,EDGE,25,23,1,4);
        // Fixed pixel staircase line, avoiding vector antialiasing.
        for(int i=0;i<15;i++){box(g,INK,37-i,2+i,2,2);box(g,LIGHT,37-i,2+i,1,1);}
        box(g,STONE,33,4,6,1);g.dispose();
        // Armor must remain readable against the dark floor, like the original corpses.
        for(int y=0;y<34;y++)for(int x=0;x<52;x++) {
            int pixel=sprite.getRGB(x,y);
            if(pixel==STONE.getRGB())sprite.setRGB(x,y,new Color(64,74,89).getRGB());
            else if(pixel==EDGE.getRGB())sprite.setRGB(x,y,new Color(87,96,108).getRGB());
            else if(pixel==LIGHT.getRGB())sprite.setRGB(x,y,new Color(110,116,126).getRGB());
        }
        return sprite;
    }
    private static void arch(Graphics2D g,boolean frontOnly) {
        if(!frontOnly) {
            for(int y=177;y<246;y+=11)wallBlock(g,283,y,20,10);
            int[][] stones={{285,176},{292,179},{299,185},{305,192},{311,201},{316,211}};
            for(int[]p:stones)wallBlock(g,p[0],p[1],15,12);
        }
        // Front pier ends precisely at the existing collision base (x633..675,y570..598).
        for(int y=210;y<296;y+=11)wallBlock(g,318,y,20,10);
        box(g,INK,317,295,21,4);box(g,EDGE,319,294,17,2);
    }
    public static BufferedImage foreground() {
        BufferedImage image=new BufferedImage(768,512,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g=image.createGraphics();arch(g,true);g.dispose();return upscale(image);
    }
}
