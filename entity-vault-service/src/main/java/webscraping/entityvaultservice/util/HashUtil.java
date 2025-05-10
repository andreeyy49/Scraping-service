package webscraping.entityvaultservice.util;

import lombok.extern.slf4j.Slf4j;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Slf4j
public class HashUtil {

    public static String getMd5Hash(String input) {
        try {
            byte[] textBytes = input.getBytes();
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(textBytes);

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
