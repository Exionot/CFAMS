package util;
import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {
	public static String hashPassword(String plainPassword) {
		return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
	}

	public static boolean checkPassword(String plainPassword, String storedHash) {
		if (storedHash == null || !storedHash.startsWith("$2a$")) {
			throw new IllegalArgumentException("Invalid hash provided for comparison");
		}
		return BCrypt.checkpw(plainPassword, storedHash); 
	}
}
