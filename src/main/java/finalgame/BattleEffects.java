package finalgame;

import java.awt.Graphics;
import java.util.ArrayList;

/** Short, bounded hit feedback, independent of damage and collision. */
public final class BattleEffects {
    private static final class Hit {
        final int x,y;final boolean heavy;final double duration;double age;
        Hit(int x,int y,boolean heavy){this.x=x;this.y=y;this.heavy=heavy;duration=heavy?.38:.22;}
    }
    private final ArrayList<Hit> hits=new ArrayList<>();
    public void hit(int x,int y,boolean heavy){
        if(hits.size()>=24)hits.remove(0);
        hits.add(new Hit(x,y,heavy));
    }
    public void update(double dt){
        for(int i=hits.size()-1;i>=0;i--){Hit h=hits.get(i);h.age+=dt;if(h.age>=h.duration)hits.remove(i);}
    }
    public void draw(Graphics g){for(Hit h:hits)HeroEffects.impact(g,h.x,h.y,h.age/h.duration,h.heavy);}
    public void reset(){hits.clear();}
}
