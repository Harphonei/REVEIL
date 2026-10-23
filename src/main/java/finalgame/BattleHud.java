package finalgame;

import java.awt.*;

/** Presentation only: the arena and all collision coordinates stay in game space. */
public final class BattleHud {
    private static final Color INK=new Color(22,24,31),EDGE=new Color(157,150,146),TEXT=new Color(229,220,210);
    private BattleHud(){}
    private static void centered(Graphics g,String s,int cx,int y){g.drawString(s,cx-g.getFontMetrics().stringWidth(s)/2,y);}
    public static void boss(Graphics g,Boss boss,Rectangle arena){
        int x=arena.x,w=arena.width;
        g.setColor(TEXT);g.setFont(new Font(Font.MONOSPACED,Font.BOLD,16));centered(g,boss.getName(),x+w/2,23);
        g.setColor(INK);g.fillRect(x,36,w,9);
        g.setColor(new Color(155,68,77));g.fillRect(x,36,(int)Math.round(w*boss.getHp()/(double)boss.getMaxHp()),9);
        g.setColor(EDGE);g.drawRect(x,36,w,9);
    }
    public static void player(Graphics g,Player p,Rectangle arena,boolean eVisible,boolean qVisible){
        int x=arena.x,y=arena.y+arena.height+18;
        g.setFont(new Font(Font.MONOSPACED,Font.BOLD,15));g.setColor(TEXT);g.drawString("PLAYER",x,y+26);
        int slotX=x+70;
        if(eVisible){skill(g,slotX,y,"E",p.getSkillECooldown(),p.getSkillEChargeProgress(),false);slotX+=52;}
        if(qVisible){skill(g,slotX,y,"Q",p.getSkillQCooldown(),p.isUltimateActive()?p.getUltimateProgress():p.getSkillQChargeProgress(),p.isUltimateActive());slotX+=52;}
        int hpX=slotX+4,w=arena.x+arena.width-hpX;
        g.setFont(new Font(Font.MONOSPACED,Font.PLAIN,13));g.setColor(TEXT);g.drawString("HP",hpX,y+14);
        g.setColor(INK);g.fillRect(hpX,y+23,w,12);
        g.setColor(new Color(173,62,78));g.fillRect(hpX,y+23,w*p.getHp()/p.getMaxHp(),12);
        g.setColor(EDGE);g.drawRect(hpX,y+23,w,12);
    }
    private static void skill(Graphics g,int x,int y,String key,double cooldown,double progress,boolean active){
        int size=42;g.setColor(INK);g.fillRect(x,y,size,size);
        if(progress>0){
            int height=(int)Math.round((size-4)*Math.max(0,Math.min(1,progress)));
            g.setColor(active?new Color(124,43,62):new Color(71,53,66));g.fillRect(x+2,y+size-2-height,size-4,height);
        }
        g.setColor(active?HeroEffects.LIGHT:cooldown>0?new Color(81,77,82):EDGE);g.drawRect(x,y,size,size);
        if(cooldown>0){
            g.setColor(EDGE);g.setFont(new Font(Font.MONOSPACED,Font.PLAIN,11));g.drawString(key,x+4,y+12);
            g.setColor(TEXT);g.setFont(new Font(Font.MONOSPACED,Font.BOLD,17));centered(g,Integer.toString((int)Math.ceil(cooldown)),x+21,y+33);
        }else{
            g.setColor(active?HeroEffects.LIGHT:TEXT);g.setFont(new Font(Font.MONOSPACED,Font.BOLD,22));centered(g,key,x+21,y+29);
        }
    }
}
