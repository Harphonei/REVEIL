package finalgame;

import java.awt.*;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.imageio.ImageIO;

/** Name-free portrait dialogue with Thai shaping and grapheme-safe typewriter text. */
public final class DialogueBox {
    public enum Face { NEUTRAL, CONFUSED, SHOCKED, RELIEVED, NONE, SCIENTIST, MOTH }
    private static final Font FONT=loadFont();
    private static final BufferedImage PORTRAITS=loadPortraits();
    private static final BufferedImage SCIENTIST=loadScientist();
    private static final BufferedImage MOTH=loadMoth();
    private static BufferedImage loadMoth(){
        try{return ImageIO.read(DialogueBox.class.getResource("/finalgame/moth-portrait.png"));}
        catch(Exception e){throw new IllegalStateException("Cannot load moth portrait",e);}
    }
    private static BufferedImage loadScientist(){
        try{return ImageIO.read(DialogueBox.class.getResource("/finalgame/scientist-portrait.png"));}
        catch(Exception e){throw new IllegalStateException("Cannot load scientist portrait",e);}
    }
    private String text="";
    private Face face=Face.NONE;
    private int[] ends=new int[0];
    private double clock;
    private boolean open;
    private static Font loadFont() {
        try(InputStream in=DialogueBox.class.getResourceAsStream("/finalgame/dialogue-thai.ttf")) {
            if(in==null)throw new IllegalStateException("Missing dialogue font");
            return Font.createFont(Font.TRUETYPE_FONT,in).deriveFont(25f);
        } catch(Exception e){throw new IllegalStateException("Cannot load Thai dialogue font",e);}
    }
    private static BufferedImage loadPortraits() {
        try {return ImageIO.read(DialogueBox.class.getResource("/finalgame/hero-portraits.png"));}
        catch(Exception e){throw new IllegalStateException("Cannot load portraits",e);}
    }
    public static Font font(float size){return FONT.deriveFont(size);}
    public void show(Face face,String text) {
        this.face=face;this.text=text;clock=0;open=true;
        BreakIterator it=BreakIterator.getCharacterInstance(Locale.forLanguageTag("th"));
        it.setText(text);List<Integer> positions=new ArrayList<>();
        for(int n=it.first();(n=it.next())!=BreakIterator.DONE;)positions.add(n);
        ends=new int[positions.size()];for(int i=0;i<ends.length;i++)ends[i]=positions.get(i);
    }
    public void update(double seconds){if(open)clock+=Math.max(0,seconds);}
    public boolean isOpen(){return open;}
    public boolean complete(){return (int)(clock*32)>=ends.length;}
    /** First press reveals the line. A second press advances the script. */
    public boolean advance(){if(!complete()){clock=ends.length/32.0+1;return false;}return true;}
    public void close(){open=false;}
    private String visibleText(){int n=Math.min(ends.length,(int)(clock*32));return n==0?"":text.substring(0,ends[n-1]);}
    private static List<String> wrap(String text,FontMetrics fm,int maxWidth) {
        List<String> lines=new ArrayList<>();
        for(String paragraph:text.split("\\n",-1)) {
            if(paragraph.isEmpty()){lines.add("");continue;}
            BreakIterator it=BreakIterator.getLineInstance(Locale.forLanguageTag("th"));it.setText(paragraph);
            int start=0,last=0;
            for(int end=it.first();(end=it.next())!=BreakIterator.DONE;) {
                if(fm.stringWidth(paragraph.substring(start,end))>maxWidth && last>start){lines.add(paragraph.substring(start,last).trim());start=last;}
                last=end;
            }
            if(start<paragraph.length())lines.add(paragraph.substring(start).trim());
        }
        return lines;
    }
    public void draw(Graphics graphics) {
        if(!open)return;
        Graphics2D g=(Graphics2D)graphics.create();
        g.setColor(new Color(6,9,13,200));g.fillRect(16,432,928,194);
        g.setColor(new Color(27,30,39));g.fillRect(20,436,920,186);
        g.setColor(new Color(107,111,120));g.setStroke(new BasicStroke(3));g.drawRect(21,437,918,184);
        g.setColor(new Color(53,58,68));g.drawRect(27,443,906,172);
        int textX=face==Face.NONE?52:202;
        if(face==Face.SCIENTIST||face==Face.MOTH){
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.drawImage(face==Face.MOTH?MOTH:SCIENTIST,40,460,144,144,null);
        }else if(face!=Face.NONE) {
            int cell=PORTRAITS.getWidth()/2;
            int col=face.ordinal()%2,row=face.ordinal()/2;
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.drawImage(PORTRAITS,40,460,184,604,col*cell,row*cell,(col+1)*cell,(row+1)*cell,null);
        }
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(FONT);g.setColor(new Color(225,220,203));
        // Wrap the complete line so the typewriter never reflows a word mid-reveal.
        List<String> lines=wrap(text,g.getFontMetrics(),900-textX);
        int visible=visibleText().length(),used=0,y=482;
        for(String line:lines) {
            int length=Math.max(0,Math.min(line.length(),visible-used));
            if(length>0)new TextLayout(line.substring(0,length),FONT,g.getFontRenderContext()).draw(g,textX,y);
            used+=line.length();y+=37;
        }
        g.setFont(FONT.deriveFont(14f));g.setColor(new Color(150,155,164));
        g.drawString(complete()?"Enter / Space / คลิก เพื่ออ่านต่อ":"Enter / Space / คลิก เพื่อแสดงข้อความ",textX,601);
        g.dispose();
    }
}
