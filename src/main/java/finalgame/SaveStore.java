package finalgame;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Properties;

/** A map-entry checkpoint, never a snapshot of a half-finished fight. */
public final class SaveStore {
    public record Checkpoint(int map,boolean skillE,boolean skillQ,int clearedMask){
        public Checkpoint{
            if(map<1||map>5||clearedMask<0||clearedMask>31||(clearedMask&(1<<(map-1)))!=0)
                throw new IllegalArgumentException("Invalid checkpoint");
        }
    }
    public record Profile(boolean completed,Checkpoint checkpoint){}
    private final Path path;
    private String warning="";
    public SaveStore(){this(Path.of(System.getProperty("final.saveDir",Path.of(System.getProperty("user.home"),".final-game").toString())).resolve("save.properties"));}
    public SaveStore(Path path){this.path=path;}
    public String warning(){return warning;}
    public Profile load(){
        warning="";
        if(!Files.exists(path)&&!Files.exists(backup()))return new Profile(false,null);
        try{return read(path);}catch(Exception bad){
            try{Profile recovered=read(backup());warning="Recovered the previous autosave.";return recovered;}
            catch(Exception missing){warning="Saved progress could not be read.";return new Profile(false,null);}
        }
    }
    private Path backup(){return path.resolveSibling(path.getFileName()+".bak");}
    private static String canonical(Profile p){
        Checkpoint c=p.checkpoint();
        return "1|"+p.completed()+"|"+(c==null?"0|false|false|0":c.map()+"|"+c.skillE()+"|"+c.skillQ()+"|"+c.clearedMask());
    }
    private static String checksum(Profile p)throws Exception{
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(canonical(p).getBytes(StandardCharsets.UTF_8)));
    }
    private static boolean bool(Properties p,String key)throws IOException{
        String v=p.getProperty(key);if(!"true".equals(v)&&!"false".equals(v))throw new IOException("Invalid field");return Boolean.parseBoolean(v);
    }
    private static Profile read(Path file)throws Exception{
        if(Files.size(file)>8192)throw new IOException("Invalid save size");
        Properties p=new Properties();try(InputStream in=Files.newInputStream(file)){p.load(in);}
        if(!"1".equals(p.getProperty("format")))throw new IOException("Unknown save format");
        int map=Integer.parseInt(p.getProperty("map")),mask=Integer.parseInt(p.getProperty("cleared"));
        boolean e=bool(p,"skillE"),q=bool(p,"skillQ"),complete=bool(p,"completed");
        if(map==0&&(mask!=0||e||q))throw new IOException("Invalid empty save");
        Profile result=new Profile(complete,map==0?null:new Checkpoint(map,e,q,mask));
        if(!checksum(result).equals(p.getProperty("checksum")))throw new IOException("Save checksum mismatch");
        return result;
    }
    public void save(Profile profile)throws IOException{
        Path absolute=path.toAbsolutePath();Files.createDirectories(absolute.getParent());
        Path temp=Files.createTempFile(absolute.getParent(),"final-save-",".tmp");
        try{
            Checkpoint c=profile.checkpoint();Properties p=new Properties();p.setProperty("format","1");
            p.setProperty("completed",Boolean.toString(profile.completed()));p.setProperty("map",Integer.toString(c==null?0:c.map()));
            p.setProperty("skillE",Boolean.toString(c!=null&&c.skillE()));p.setProperty("skillQ",Boolean.toString(c!=null&&c.skillQ()));
            p.setProperty("cleared",Integer.toString(c==null?0:c.clearedMask()));
            try{p.setProperty("checksum",checksum(profile));}catch(Exception ex){throw new IOException(ex);}
            try(FileOutputStream out=new FileOutputStream(temp.toFile())){p.store(out,"FINAL autosave");out.getFD().sync();}
            // A damaged primary never replaces the usable backup.
            if(Files.exists(path)){try{read(path);Files.copy(path,backup(),StandardCopyOption.REPLACE_EXISTING);}catch(Exception ignored){}}
            try{Files.move(temp,absolute,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}
            catch(AtomicMoveNotSupportedException ex){Files.move(temp,absolute,StandardCopyOption.REPLACE_EXISTING);}
        }finally{Files.deleteIfExists(temp);}
    }
}
