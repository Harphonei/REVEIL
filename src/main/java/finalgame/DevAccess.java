package finalgame;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
/** The public game stores only the digest; the owner launcher supplies the secret. */
final class DevAccess {
    private static final String DIGEST="3f2f1d61093f4a8bbaf56aa05eba3f053c2e49c88a4efbd65ce0cb5a7422631e";
    static boolean authorized(){return matches(System.getenv("REVEIL_DEV_KEY"));}
    static boolean matches(String value){
        if(value==null||value.length()!=64)return false;
        try{return MessageDigest.isEqual(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.US_ASCII)),HexFormat.of().parseHex(DIGEST));}
        catch(java.security.NoSuchAlgorithmException e){throw new IllegalStateException(e);}
    }
    private DevAccess(){}
}
