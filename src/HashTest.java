//import org.mindrot.jbcrypt.BCrypt;
//
//public class HashTest {
//    public static void main(String[] args) {
//        String plainPassword = "myPassword123";
//
//        // Хеширане на паролата
//        String hashedPassword = hashPassword(plainPassword);
//
//        System.out.println("Plain password: " + plainPassword);
//        System.out.println("Hashed password: " + hashedPassword);
//
//        // Проверка на паролата
//        String userInputPassword = "myPassword123";
//        boolean passwordMatch = checkPassword(userInputPassword, hashedPassword);
//
//        if (passwordMatch) {
//            System.out.println("Password is correct.");
//        } else {
//            System.out.println("Password is incorrect.");
//        }
//    }
//
//    // Метод за хеширане на парола
//    public static String hashPassword(String plainPassword) {
//        // Генериране на сол за хеширане
//        String salt = BCrypt.gensalt();
//
//        // Хеширане на паролата със солта
//        String hashedPassword = BCrypt.hashpw(plainPassword, salt);
//
//        return hashedPassword;
//    }
//
//    // Метод за проверка на парола
//    public static boolean checkPassword(String userInputPassword, String hashedPassword) {
//        // Проверка дали въведената парола съвпада с хешираната парола
//        return BCrypt.checkpw(userInputPassword, hashedPassword);
//    }
//}
