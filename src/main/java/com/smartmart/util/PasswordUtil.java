
package com.smartmart.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordUtil {

    private static final int ITERATIONS = 210000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;

    // Membuat salt secara acak
    public static String generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];

        new SecureRandom().nextBytes(salt);

        return Base64.getEncoder().encodeToString(salt);
    }

    // Mengubah password menjadi hash
    public static String hashPassword(String password, String salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        byte[] saltBytes = Base64.getDecoder().decode(salt);

        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                saltBytes,
                ITERATIONS,
                KEY_LENGTH
        );

        SecretKeyFactory factory = SecretKeyFactory.getInstance(
                "PBKDF2WithHmacSHA256"
        );

        byte[] hash = factory.generateSecret(spec).getEncoded();

        spec.clearPassword();

        return Base64.getEncoder().encodeToString(hash);
    }

    // Memeriksa kesesuaian password saat login
    public static boolean verifyPassword(
            String password,
            String salt,
            String storedHash
    ) throws NoSuchAlgorithmException, InvalidKeySpecException {

        String inputHash = hashPassword(password, salt);

        return java.security.MessageDigest.isEqual(
                Base64.getDecoder().decode(inputHash),
                Base64.getDecoder().decode(storedHash)
        );
    }
}
