package finalgame;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.function.DoubleConsumer;

/** A small deterministic script runner. Gameplay clocks remain frozen while it is active. */
public final class StoryDirector {
    public static final class Beat {
        final String id;final double duration;final Runnable enter;final DoubleConsumer animate;
        final DialogueBox.Face face;final String text;
        Beat(String id,double duration,Runnable enter,DoubleConsumer animate,DialogueBox.Face face,String text) {
            this.id=id;this.duration=duration;this.enter=enter;this.animate=animate;this.face=face;this.text=text;
        }
    }
    public static Beat action(String id,Runnable enter){return new Beat(id,0,enter,null,null,null);}
    public static Beat motion(String id,double duration,DoubleConsumer animate){return new Beat(id,duration,null,animate,null,null);}
    public static Beat waitFor(String id,double duration){return new Beat(id,duration,null,null,null,null);}
    public static Beat say(DialogueBox.Face face,String text){return new Beat("dialogue",-1,null,null,face,text);}
    private final ArrayDeque<Beat> queue=new ArrayDeque<>();
    private final DialogueBox dialogue;
    private Beat current;
    private Runnable finish;
    private double elapsed;
    public StoryDirector(DialogueBox dialogue){this.dialogue=dialogue;}
    public boolean active(){return current!=null;}
    public String currentId(){return current==null?"":current.id;}
    public void play(Runnable finish,Beat... beats){cancel();this.finish=finish;queue.addAll(Arrays.asList(beats));next();}
    private void next() {
        dialogue.close();current=null;elapsed=0;
        while(!queue.isEmpty()) {
            current=queue.remove();if(current.enter!=null)current.enter.run();
            if(current.text!=null){dialogue.show(current.face,current.text);return;}
            if(current.animate!=null)current.animate.accept(0);
            if(current.duration>0)return;
        }
        current=null;Runnable callback=finish;finish=null;if(callback!=null)callback.run();
    }
    public void update(double seconds) {
        if(current==null)return;
        if(current.text!=null){dialogue.update(seconds);return;}
        elapsed+=Math.max(0,seconds);
        if(current.animate!=null)current.animate.accept(Math.min(1,elapsed/current.duration));
        if(elapsed>=current.duration)next();
    }
    public void advance(){if(current!=null&&current.text!=null&&dialogue.advance())next();}
    public void cancel(){queue.clear();current=null;finish=null;dialogue.close();}
}
