package s4y.solutions.waytoday.sdk.wsse;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;


public class Wsse {
    static private String sha1(String text) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA");
        md.update(text.getBytes(StandardCharsets.UTF_8));
        byte[] digest = md.digest();
        return Base64.getEncoder().encodeToString(digest);
    }

    private static String digest(String password, String nonce, String created) throws NoSuchAlgorithmException {
        String text = nonce + created + password;
        return sha1(text);
    }

    static String testDigest(String password, String nonce, String created) throws NoSuchAlgorithmException {
        return digest(password, nonce, created);
    }

    public static String getToken(String principal, String secret) throws NoSuchAlgorithmException {
        String nonce = String.valueOf(Math.random());
        String created = new Date().toString();
        String digest = digest(secret, nonce, created);
        return "Username=\"" + principal + "\"," +
                "PasswordDigest=\"" + digest + "\"," +
                "nonce=\"" + nonce + "\"," +
                "Created=\"" + created + "\"";
    }
}
