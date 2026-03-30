package util;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class PasswordHash {
    private static final int COST = 12;

    private PasswordHash() {
    }

    public static String hashPassword(String rawPassword) {
        validatePassword(rawPassword);
        return BCrypt.withDefaults().hashToString(COST, rawPassword.toCharArray());
    }

    public static boolean verifyPassword(String rawPassword, String hashedPassword) {
        validatePassword(rawPassword);
        if (hashedPassword == null || hashedPassword.isBlank()) {
            return false;
        }
        BCrypt.Result result = BCrypt.verifyer().verify(rawPassword.toCharArray(), hashedPassword);
        return result.verified;
    }

    private static void validatePassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Mat khau khong duoc de trong.");
        }
    }
}
