package finalgame;

import java.awt.*;

/** Shared pixel grammar, distinct material and palette for each monster. */
public final class BossEffects {
    private static final Color[][] PALETTE={
        {new Color(57,47,46),new Color(151,133,115),new Color(231,220,190)},
        {new Color(32,54,49),new Color(97,158,111),new Color(195,229,165)},
        {new Color(48,39,64),new Color(148,115,179),new Color(220,202,240)},
        {new Color(44,48,58),new Color(142,156,176),new Color(230,225,210)},
        {new Color(58,29,42),new Color(177,57,78),new Color(242,179,171)}
    };
    private BossEffects(){}
    public static Color color(int theme,int shade){return PALETTE[Math.max(0,Math.min(4,theme-1))][shade];}
    private static void line(Graphics g,int x1,int y1,int x2,int y2,int size){
        int steps=Math.max(1,Math.max(Math.abs(x2-x1),Math.abs(y2-y1))/2);
        for(int i=0;i<=steps;i++){double p=(double)i/steps;g.fillRect(x1+(int)((x2-x1)*p)/2*2,y1+(int)((y2-y1)*p)/2*2,size,size);}
    }
    public static void seal(Graphics g,int cx,int cy,int r,int theme){
        g.setColor(color(theme,1));
        for(int i=0;i<8;i++){
            double a=i*Math.PI/4,b=a+.48;
            line(g,cx+(int)(Math.cos(a)*r),cy+(int)(Math.sin(a)*r),
                    cx+(int)(Math.cos(b)*r),cy+(int)(Math.sin(b)*r),2);
        }
    }
    public static void projectile(Graphics g,Rectangle b,int theme,boolean phaseTwo,int kind,boolean reacting){
        int x=b.x,y=b.y,w=b.width,h=b.height,cx=x+w/2,cy=y+h/2;
        Color dark=color(theme,0),mid=color(theme,1),light=color(theme,2);
        // A stepped silhouette retains the original diameter, including for large orbs.
        g.setColor(dark);g.fillRect(x+2,y,w-4,h);g.fillRect(x,y+2,w,h-4);
        g.setColor(mid);g.fillRect(x+2,y+2,w-4,h-4);
        switch(theme){
            case 1: // Pale bone, red fissure; a blunt edge keeps its collision legible.
                g.setColor(light);g.fillRect(cx-2,y,4,h);g.fillRect(x,y+2,w,2);g.fillRect(x,y+h-4,w,2);
                g.setColor(new Color(135,63,66));g.fillRect(cx,y+h/2,2,4);break;
            case 2: // Glass reagent, liquid surface and a tiny air bubble.
                g.setColor(light);g.fillRect(x+2,y+2,2,Math.max(2,h/2-2));g.fillRect(x+4,y+2,Math.max(2,w/2-2),2);
                g.setColor(new Color(136,105,163));g.fillRect(x+2,cy,w-4,Math.max(2,h/2-2));
                g.setColor(light);g.fillRect(cx+2,cy-2,2,2);break;
            case 3: // Four wing scales around a clear central spindle.
                g.setColor(light);g.fillRect(cx-1,y,2,h);g.fillRect(x+2,cy-3,2,2);g.fillRect(x+w-4,cy+1,2,2);
                g.setColor(dark);g.fillRect(x,cy-1,w,2);g.setColor(light);g.fillRect(cx-1,cy-1,2,2);break;
            case 4: // Stone tablet, pale bevel and an engraved vertical rune.
                g.setColor(light);g.fillRect(x+2,y,w-4,2);g.fillRect(x,y+2,2,h-4);
                g.setColor(dark);g.fillRect(cx-1,y+3,2,h-6);g.fillRect(cx-3,cy-1,6,2);break;
            default: // Same broken-power motif as Harold; phase two burns hotter.
                g.setColor(phaseTwo?light:mid);g.fillRect(cx-2,y,4,h);g.fillRect(x,cy-2,w,4);
                g.setColor(dark);line(g,cx+2,y+2,cx-2,cy,2);line(g,cx-2,cy,cx+2,y+h-3,2);
                g.setColor(light);g.fillRect(cx-1,cy-1,2,2);break;
        }
        if(kind>0){
            seal(g,cx,cy,w/2-3,theme);g.setColor(light);
            line(g,cx,cy-6,cx,cy+2,2);g.fillRect(cx,cy+6,2,2);
            if(reacting){seal(g,cx,cy,w/2+5,theme);g.setColor(light);g.fillRect(cx-1,y-7,2,4);g.fillRect(cx-1,y+h+3,2,4);}
        }
    }
    public static void zone(Graphics graphics,Rectangle b,int theme,boolean active,double progress){
        Graphics2D g=(Graphics2D)graphics.create();g.clip(b);
        g.setColor(color(theme,0));g.fillRect(b.x,b.y,b.width,b.height);
        g.setColor(color(theme,active?2:1));g.drawRect(b.x,b.y,b.width-1,b.height-1);
        if(active){
            g.setColor(color(theme,1));
            if(theme==1){
                for(int x=b.x+4;x<b.x+b.width;x+=12){line(g,x,b.y+b.height,x+6,b.y+4,3);line(g,x+4,b.y+b.height/2,x+10,b.y+b.height/2-8,2);}
            }else{
                for(int y=b.y+4;y<b.y+b.height;y+=12)for(int x=b.x+4;x<b.x+b.width;x+=12){
                    g.fillRect(x,y,6,4);g.setColor(color(theme,2));g.fillRect(x+2,y-2,2,2);g.setColor(color(theme,1));
                }
            }
        }else{
            int len=5+(int)(Math.max(0,Math.min(1,progress))*10);
            g.setColor(color(theme,2));
            for(int dx:new int[]{0,1})for(int dy:new int[]{0,1}){
                int x=dx==0?b.x:b.x+b.width-1,y=dy==0?b.y:b.y+b.height-1;
                line(g,x,y,x+(dx==0?len:-len),y,2);line(g,x,y,x,y+(dy==0?len:-len),2);
            }
            int cx=(int)b.getCenterX(),cy=(int)b.getCenterY();
            if(theme==3){seal(g,cx,cy,Math.min(b.width,b.height)/2-5,theme);g.setColor(color(theme,2));line(g,cx,cy-8,cx,cy+8,2);}
            else {g.fillRect(cx-1,cy-7,2,8);g.fillRect(cx-1,cy+5,2,2);}
        }
        g.dispose();
    }
    public static void strike(Graphics graphics,Shape hitArea,Shape axis,Rectangle arena,int theme,boolean active,double progress){
        Graphics2D g=(Graphics2D)graphics.create();g.clip(arena);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setColor(color(theme,0));g.fill(hitArea);
        g.setColor(color(theme,active?2:1));g.setStroke(new BasicStroke(1));g.draw(hitArea);
        if(active){
            g.clip(hitArea);Rectangle b=hitArea.getBounds();
            g.setColor(color(theme,1));g.fill(hitArea);
            g.setColor(color(theme,2));g.setStroke(new BasicStroke(theme==5?3:2));g.draw(axis);
            g.setColor(color(theme,0));
            for(int y=b.y;y<b.y+b.height;y+=18)for(int x=b.x+(y/18%2)*8;x<b.x+b.width;x+=24){line(g,x,y,x+8,y+6,2);line(g,x+8,y+6,x+4,y+12,2);}
        }else{
            g.setColor(color(theme,2));
            g.setStroke(new BasicStroke(2,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER,10,new float[]{6,6},(float)(progress*12)));
            g.draw(axis);
        }
        g.dispose();
    }
    public static void cast(Graphics g,Rectangle body,int theme,double remaining){
        if(remaining<=0)return;
        int cx=(int)body.getCenterX(),cy=body.y+body.height+5,r=10+(int)((1-remaining/.35)*14);
        seal(g,cx,cy,r,theme);
        g.setColor(color(theme,2));g.fillRect(cx-1,cy-4,2,8);
    }
}
