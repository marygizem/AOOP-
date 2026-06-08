package service;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordService {
    private PasswordService() {
    }

    public static String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
    }

    public static boolean isHashed(String storedPassword) {
        if (storedPassword == null) {
            return false;
        }
        return storedPassword.matches("^\\$2[aby]\\$\\d{2}\\$.{53}$");
    }

    public static boolean matches(String plainPassword, String storedPassword) {
        if (plainPassword == null || storedPassword == null) {
            return false;
        }

        String stored = storedPassword.trim();
        if (stored.isEmpty()) {
            return false;
        }

        if (isHashed(stored)) {
            try {
                return BCrypt.checkpw(plainPassword, stored);
            } catch (IllegalArgumentException ex) {
                return false;
            }
        }

        // Backward compatibility with legacy plaintext rows.
        return plainPassword.equals(stored);
    }
}
