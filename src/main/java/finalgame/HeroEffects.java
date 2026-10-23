package finalgame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

/** Harold's fractured power. Integer, stepped pixels; painting never advances time. */
public final class HeroEffects {
    public static final Color SHADOW=new Color(52,35,46),ASH=new Color(99,81,94);
    public static final Color BLOOD=new Color(124,38,58),RED=new Color(196,65,83);
    public static final Color LIGHT=new Color(244,184,174),PALE=new Color(229,211,201);
    private HeroEffects(){}
    private static int snap(double n){return (int)Math.round(n/2)*2;}
    private static void pixel(Graphics g,double x,double y,int size){g.fillRect(snap(x),snap(y),size,size);}
    private static void line(Graphics g,double x1,double y1,double x2,double y2,int size){
        int steps=Math.max(1,(int)(Math.max(Math.abs(x2-x1),Math.abs(y2-y1))/2));
        for(int i=0;i<=steps;i++){double p=(double)i/steps;pixel(g,x1+(x2-x1)*p,y1+(y2-y1)*p,size);}
    }
    /** An eight-sided, deliberately broken seal rather than a smooth glow. */
    private static void seal(Graphics g,int x,int y,double radius,double phase,Color color){
        g.setColor(color);
        for(int i=0;i<8;i++){
            double a=i*Math.PI/4+phase,b=a+Math.PI/4*.59;
            line(g,x+Math.cos(a)*radius,y+Math.sin(a)*radius,
                    x+Math.cos(b)*radius,y+Math.sin(b)*radius,2);
        }
    }
    public static void shard(Graphics g,Rectangle b,double age){
        int cx=b.x+b.width/2,y=b.y;
        g.setColor(SHADOW);g.fillRect(cx-2,y+10,4,10);
        g.setColor(BLOOD);g.fillRect(cx-1,y+10,2,6);
        g.setColor(RED);g.fillRect(cx-3,y+4,6,6);g.fillRect(cx-1,y,2,12);
        g.setColor(LIGHT);g.fillRect(cx-1,y+2,2,6);
        if(((int)(age*18)&1)==0){g.setColor(ASH);pixel(g,cx+5,y+13,2);}
    }
    public static void heavy(Graphics g,Rectangle b,double age){
        int cx=b.x+b.width/2,cy=b.y+b.height/2;
        g.setColor(SHADOW);line(g,cx-6,cy+12,cx-8,cy+26,4);line(g,cx+6,cy+12,cx+4,cy+34,2);
        g.setColor(BLOOD);line(g,cx,cy+12,cx,cy+28,4);
        g.fillRect(cx-4,cy-16,8,32);g.fillRect(cx-8,cy-10,16,24);g.fillRect(cx-14,cy-4,28,10);
        g.setColor(RED);g.fillRect(cx-2,cy-16,4,28);g.fillRect(cx-10,cy-2,20,6);
        g.setColor(PALE);g.fillRect(cx-2,cy-12,4,18);g.fillRect(cx-6,cy-2,12,4);
        g.setColor(SHADOW);line(g,cx+2,cy-6,cx-2,cy,2);line(g,cx-2,cy,cx+2,cy+4,2);
        g.setColor(RED);pixel(g,cx-18,cy+6+(int)(age*30)%8,2);pixel(g,cx+16,cy+14-(int)(age*24)%10,2);
    }
    public static void chargeE(Graphics g,int cx,int cy,double progress){
        double p=Math.max(0,Math.min(1,progress));
        seal(g,cx,cy,27-14*p,-p*.7,BLOOD);
        for(int i=0;i<6;i++){
            double a=i*Math.PI/3+.45,r=34-25*((p+i*.14)%1);
            g.setColor(i%2==0?RED:ASH);pixel(g,cx+Math.cos(a)*r,cy+Math.sin(a)*r,2);
            g.setColor(SHADOW);pixel(g,cx+Math.cos(a)*(r+4),cy+Math.sin(a)*(r+4),2);
        }
        int length=4+(int)(p*12);
        g.setColor(RED);g.fillRect(cx-2,cy-length,4,length+4);g.fillRect(cx-4,cy-4,8,6);
        g.setColor(LIGHT);g.fillRect(cx-1,cy-length+2,2,length);
    }
    public static void chargeQ(Graphics g,int cx,int cy,double progress){
        double p=Math.max(0,Math.min(1,progress));
        seal(g,cx,cy,38-21*p,p*.5,BLOOD);
        seal(g,cx,cy,27-12*p,-p*.8,ASH);
        for(int i=0;i<8;i++){
            double a=i*Math.PI/4,r=46-31*p;
            g.setColor(i%2==0?RED:SHADOW);
            line(g,cx+Math.cos(a)*r,cy+Math.sin(a)*r,
                    cx+Math.cos(a)*(r+5),cy+Math.sin(a)*(r+5),2);
        }
    }
    public static void aura(Graphics g,int cx,int cy,double time,double remaining,boolean guarded){
        Color c=remaining<.65?BLOOD:RED;
        seal(g,cx,cy,19+Math.sin(time*5)*2,time*.45,c);
        for(int i=0;i<5;i++){
            double a=i*Math.PI*.4+time*.8,r=27+(i%2)*4;
            g.setColor(i%2==0?BLOOD:ASH);pixel(g,cx+Math.cos(a)*r,cy+Math.sin(a)*r,2);
        }
        if(guarded)seal(g,cx,cy,23,-time*.5,LIGHT);
    }
    public static void burst(Graphics g,int cx,int cy,double progress,boolean heavy){
        double p=Math.max(0,Math.min(1,progress));
        double r=8+(heavy?24:32)*p;
        seal(g,cx,cy,r,.15,p<.4?LIGHT:BLOOD);
        for(int i=0;i<8;i++){
            double a=i*Math.PI/4+.15,inner=r+(heavy?3:5),outer=inner+8*(1-p);
            g.setColor(i%2==0?RED:ASH);
            line(g,cx+Math.cos(a)*inner,cy+Math.sin(a)*inner,
                    cx+Math.cos(a)*outer,cy+Math.sin(a)*outer,2);
        }
    }
    public static void muzzle(Graphics g,int cx,int cy,double progress){
        int r=3+(int)(progress*6);g.setColor(progress<.4?LIGHT:BLOOD);
        line(g,cx-r,cy,cx+r,cy,2);line(g,cx,cy-r,cx,cy+3,2);
    }
    public static void beam(Graphics g,Rectangle b,double phase){
        int cx=b.x+b.width/2;
        // The solid body matches the existing 12-pixel damaging column exactly.
        g.setColor(SHADOW);g.fillRect(b.x-2,b.y,b.width+4,b.height);
        g.setColor(BLOOD);g.fillRect(b.x,b.y,b.width,b.height);
        g.setColor(RED);g.fillRect(cx-4,b.y,8,b.height);
        g.setColor(LIGHT);g.fillRect(cx-2,b.y,4,b.height);
        int offset=(int)(phase*80)%24;
        for(int y=b.y+offset;y<b.y+b.height;y+=24){
            g.setColor(SHADOW);line(g,cx-4,y,cx,y+4,2);line(g,cx,y+4,cx+2,y+8,2);
            g.setColor(PALE);g.fillRect(cx-2,y+10,4,4);
        }
        seal(g,cx,b.y+b.height,16,-phase*2,RED);
    }
    public static void impact(Graphics g,int cx,int cy,double progress,boolean heavy){
        double p=Math.max(0,Math.min(1,progress));int count=heavy?8:5;
        if(p<.35){g.setColor(LIGHT);line(g,cx-5,cy,cx+5,cy,2);line(g,cx,cy-7,cx,cy+7,2);}
        if(heavy)seal(g,cx,cy,6+20*p,.2,p<.4?LIGHT:BLOOD);
        for(int i=0;i<count;i++){
            double a=i*Math.PI*2/count+.4,r=3+(heavy?28:14)*p;
            g.setColor(p>.7?SHADOW:i%2==0?RED:ASH);
            line(g,cx+Math.cos(a)*r,cy+Math.sin(a)*r,
                    cx+Math.cos(a)*(r+4*(1-p)),cy+Math.sin(a)*(r+4*(1-p)),2);
        }
    }
}
