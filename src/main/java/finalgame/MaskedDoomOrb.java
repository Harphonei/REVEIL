package finalgame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;

/** Final-boss-only variant; other bosses keep their original orbs. */
public final class MaskedDoomOrb extends EnemyProjectile {
    private final double burstY;
    private final int volley;
    private final boolean phaseTwo;
    private boolean burstDone;
    public MaskedDoomOrb(double x,double y,double burstY,int volley,boolean phaseTwo) {
        super(x,y,0,85,44,new Color(190,80,160));
        withDamage(20);
        this.burstY=burstY;this.volley=volley;this.phaseTwo=phaseTwo;
    }
    @Override public boolean shouldSplit(){return !burstDone && y>=burstY;}
    @Override public ArrayList<EnemyProjectile> split() {
        ArrayList<EnemyProjectile> waves=new ArrayList<>();
        if(burstDone)return waves;
        burstDone=true;
        for(int ring=0;ring<3;ring++) {
            // Downward escape sector rotates one spoke with each delayed ring.
            int gap=6+((volley&1)==0?ring-1:1-ring);
            double speed=(phaseTwo?150:130)+ring*12;
            for(int i=0;i<24;i++) {
                int delta=Math.floorMod(i-gap+12,24)-12;
                if(delta>=-2 && delta<=(phaseTwo?1:2))continue;
                double angle=i*Math.PI/12;
                waves.add(new DelayedProjectile(x+16,y+16,
                        Math.cos(angle)*speed,Math.sin(angle)*speed,ring*0.45));
            }
        }
        return waves;
    }
    @Override public void draw(Graphics g) {
        super.draw(g);Rectangle b=getBounds();
        g.setColor(Color.WHITE);g.drawOval(b.x,b.y,b.width,b.height);
        g.drawString("!",b.x+18,b.y+29);
        if(burstY-y<55) {
            g.setColor(Color.YELLOW);g.drawOval(b.x-7,b.y-7,58,58);
        }
    }
}
