package hu.perit.spvitamin.core.crypto;

import hu.perit.spvitamin.core.exception.ServerException;
import lombok.experimental.UtilityClass;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

@UtilityClass
public class HashUtils
{
    public static String get32BytesSha256Hash(String value)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(value.getBytes(StandardCharsets.UTF_8));
            byte[] first24 = Arrays.copyOf(digest, 24); // 192 bit
            return Base64.getUrlEncoder().withoutPadding().encodeToString(first24); // 32 chars
        }
        catch (NoSuchAlgorithmException e)
        {
            // Extremely unlikely on a standard JDK
            return ServerException.throwFrom(e);
        }
    }
}
