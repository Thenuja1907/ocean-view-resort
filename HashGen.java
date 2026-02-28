import at.favre.lib.crypto.bcrypt.BCrypt;

public class HashGen {
    public static void main(String[] args) {
        String hash = BCrypt.withDefaults().hashToString(12, "Admin@1234".toCharArray());
        System.out.println(hash);
    }
}
