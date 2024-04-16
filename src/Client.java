import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import static java.lang.System.exit;

public class Client {
    public static Socket socket;
    public static Scanner scanner;
    public static Scanner reader;
    public static PrintStream writer;

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        socket = new Socket("localhost", 1312);
        scanner = new Scanner(System.in);
        writer = new PrintStream(socket.getOutputStream());
        reader = new Scanner(socket.getInputStream());
        runLogic();
        try {
            reader.close();
            writer.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendMessage(String message) {
        writer.println(message);
    }

    public static void getMessage() {
        System.out.println(reader.nextLine());
    }

    public static void runLogic() throws NoSuchAlgorithmException {
        System.out.println("Connected");
        System.out.println("1. login");
        System.out.println("2. register");
        System.out.println("3. exit");
        System.out.println("4. admin test");//remove later
        System.out.println("5. user test");//remove later
        System.out.println("Enter your choice");
        String message = scanner.nextLine();
        sendMessage(message);
        if (message.equals("1")) {
            login();
        } else if (message.equals("2")) {
            register();
        } else if (message.equals("3")) {
            exit(0);
        }
        else if (message.equals("4")) {
            adminmenu();//remove later
        }
        else if (message.equals("5")) {
            usermenu();//remove later
        }
    }

    public static void login() throws NoSuchAlgorithmException {
        System.out.println("Enter username");
        String username = scanner.nextLine();
        if (username.isEmpty()) exit(1);
        System.out.println("Enter password");
        String password = scanner.nextLine();
        password = PasswordHasher.hashPassword(password);
        //String message = username + ":" + password;
        sendMessage(username);
        sendMessage(password);
        //getMessage();
        if (reader.nextLine().equals("wrong username or password")) {
            System.out.println("Wrong username or password");
            login();
            exit(1);
        }
        int choice = Integer.parseInt(reader.nextLine());
        if (choice == 1) {
            usermenu();
        } else {
            adminmenu();
        }
    }

    public static void register() throws NoSuchAlgorithmException {
        System.out.println("Enter username");
        String username = scanner.nextLine();
        System.out.println("Enter password");
        String password = scanner.nextLine();
        password = PasswordHasher.hashPassword(password);
        String message = username + ":" + password;
        sendMessage(message);
        String line= reader.nextLine();
        if (line.equals("User registered successfully.")) {
            System.out.println(line);
            login();
        }
        else if (line.equals("Username is already taken. Please choose another username.")) {
            System.out.println(line);
            //register();
        }
        else {
            System.out.println("Something went wrong. Please try again");
           // register();
        }
    }

    public static void usermenu() {
        System.out.println("Меню за клиенти:");
        System.out.println("1. Разгледай всички налични продукти");
        System.out.println("2. Разгледай продукти от кампании с промоции и разпродажби");
        System.out.println("3. Поръчай продукт");
        System.out.println("0. Изход");
        System.out.print("Изберете опция: ");
        int choice = Integer.parseInt(scanner.nextLine());
        sendMessage(String.valueOf(choice));
        //scanner.nextLine();
        if (choice == 1) {
            browseAllProducts();
        } else if (choice == 2) {
            browsePromotionalProducts();
        } else if (choice == 3) {
            orderProduct();
        } else if (choice == 0) {
            exit(0);
        }
    }

    public static void browseAllProducts() {
        String line;
        while ((line = reader.nextLine()) != null) {
            if (line.equals("done")) break;
            System.out.println("Message from server: " + line);
        }
        usermenu();
//        try {
//            String line;
//            while (reader.hasNextLine()) {
//                line = reader.nextLine();
//                System.out.println("Message from server: " + line);
//            }
//            usermenu();
//        } catch (NoSuchElementException | IllegalStateException e) {
//            e.printStackTrace();
//            // Handle the exception here, such as closing resources or logging the error.
//        }
    }

    public static void browsePromotionalProducts() {
        String line;
        while ((line = reader.nextLine()) != null) {
            if (line.equals("done")) break;
            System.out.println("Message from server: " + line);
        }
        usermenu();
    }

    public static void orderProduct() {
        System.out.println("Enter product id");
        int id = Integer.parseInt(scanner.nextLine());
        String message = String.valueOf(id);
        sendMessage(message);
        System.out.println("Enter quantity");
        int quantity = Integer.parseInt(scanner.nextLine());
        message = String.valueOf(quantity);
        sendMessage(message);
        System.out.println("Enter card number");
        String cardNumber = scanner.nextLine();
        message = cardNumber;
        sendMessage(message);

        String response = reader.nextLine();
        switch (response) {
            case "invalid card number":
                System.out.println("Invalid card number");
                break;
            case "not enough quantity":
                System.out.println("Not enough quantity");
                break;
            case "Order successful":
                System.out.println("Order successful");
                break;
            default:
                System.out.println("Unknown response: " + response);
                break;
        }
        usermenu();
    }

    public static void adminmenu() throws NoSuchAlgorithmException {
        System.out.println("Admin menu");
        System.out.println("1. Spravka");
        System.out.println("2. Redact menu");
        System.out.println("3. Sales Menu");
        System.out.println("4. quantityCheck");
        System.out.println("5. Make admin");
        System.out.println("6. Remove admin");
        System.out.println("7. Exit");
        System.out.println("Enter your choice");
        int choice = Integer.parseInt(scanner.nextLine());
        sendMessage(String.valueOf(choice));
        switch (choice) {
            case 1:
                spravka();
                break;
            case 2:
                redactMenu();
                break;
            case 3:
                salesMenu();
                break;
            case 4:
                quantityCheck();
                break;
            case 5:
                addAdmin();
                break;
            case 6:
                removeAdmin();
                break;
            case 7:
                exit(0);
                break;
            default:
                System.out.println("Невалиден избор. Моля, опитайте отново.");
                break;
        }
    }

    public static void spravka() throws NoSuchAlgorithmException {
        System.out.println("Enter startDate and endDate in format: YYYY-MM-DD");
        String startDate = scanner.nextLine();
        String endDate = scanner.nextLine();
        // String message = startDate + ":" + endDate;
        sendMessage(startDate);
        sendMessage(endDate);
        String test = reader.nextLine();
        if (!test.equals("correct")) {
            System.out.println(test);
            adminmenu();
            return;
        }
        int line = Integer.parseInt(reader.nextLine());
        if (line == 0) {
            System.out.println("No sales between these dates");
        } else {
            System.out.println("Total sales: " + line);
        }
        adminmenu();
    }

    public static void redactMenu() throws NoSuchAlgorithmException {
        System.out.println("1. Add product");
        System.out.println("2. Redact product");
        System.out.println("3. Remove product");
        int choice = scanner.nextInt();
        sendMessage(String.valueOf(choice));
        switch (choice) {
            case 1:
                addProduct();
                break;
            case 2:
                redactProduct();
                break;
            case 3:
                removeProduct();
                break;
            default:
                System.out.println("Невалиден избор. Моля, опитайте отново.");
                break;
        }
        adminmenu();
    }

    public static void salesMenu() {
        System.out.println("1. Start sale");
        System.out.println("2. Stop sale");
        System.out.println("3. Manage sale");
        int choice = scanner.nextInt();
        sendMessage(String.valueOf(choice));
        switch (choice) {
            case 1:
                startSale();
                break;
            case 2:
                stopSale();
                break;
            case 3:
                manageSale();
                break;
            default:
                System.out.println("Невалиден избор. Моля, опитайте отново.");
                break;
        }
    }

    public static void startSale() {
        System.out.println("Enter campaign id");
        int id = scanner.nextInt();
        sendMessage(String.valueOf(id));
        String line = reader.nextLine();
        System.out.println(line);
    }

    public static void stopSale() {
        System.out.println("Enter campaign id");
        int id = scanner.nextInt();
        sendMessage(String.valueOf(id));
        String line = reader.nextLine();
        System.out.println(line);
    }

    public static void manageSale() {
        System.out.println("Select an option:");
        System.out.println("1. Add product to campaign");
        System.out.println("2. Remove product from campaign");
        System.out.println("3. Change discount percentage");
        System.out.println("4. Adjust start date");
        System.out.println("5. Adjust end date");
        System.out.println("6. Exit");
        int choice = scanner.nextInt();
        sendMessage(String.valueOf(choice));
        switch (choice) {
            case 1:
                addProductToCampaign();
                break;
            case 2:
                removeProductFromCampaign();
                break;
            case 3:
                changeDiscountPercentage();
                break;
            case 4:
                adjustStartDate();
                break;
            case 5:
                adjustEndDate();
                break;
            case 6:
                // Exit manageSale() method
                break;
        }
    }

    public static void addProductToCampaign() {
        System.out.println("Enter campaign id");
        int id = scanner.nextInt();
        sendMessage(String.valueOf(id));
        System.out.println("Enter product id");
        int productId = scanner.nextInt();
        sendMessage(String.valueOf(productId));
        System.out.println("Enter discount percentage");
        int discountPercentage = scanner.nextInt();
        sendMessage(String.valueOf(discountPercentage));
        while (true) {
            String response = reader.nextLine();
            if (response.equals("success")) break;
            else if (response.equals("Wrong product ID. Please enter a valid product ID.")) {
                System.out.println("Wrong product ID. Please enter a valid product ID.");
                productId = scanner.nextInt();
                sendMessage(String.valueOf(productId));
            } else if (response.equals("Wrong campaign ID. Please enter a valid campaign ID.")) {
                System.out.println("Wrong campaign ID. Please enter a valid campaign ID.");
                id = scanner.nextInt();
                sendMessage(String.valueOf(id));
            } else if (response.equals("Wrong discount percentage. Please enter a valid discount percentage.")) {
                System.out.println("Wrong discount percentage. Please enter a valid discount percentage.");
                discountPercentage = scanner.nextInt();
                sendMessage(String.valueOf(discountPercentage));
            }
        }
        String line = reader.nextLine();
        System.out.println(line);
    }

    public static void removeProductFromCampaign() {
        System.out.println("Enter campaign id");
        int id = scanner.nextInt();
        sendMessage(String.valueOf(id));
        System.out.println("Enter product id");
        int productId = scanner.nextInt();
        sendMessage(String.valueOf(productId));
        while (true) {
            String response = reader.nextLine();
            if (response.equals("success")) break;
            else if (response.equals("Wrong product ID. Please enter a valid product ID.")) {
                System.out.println(response);
                productId = scanner.nextInt();
                sendMessage(String.valueOf(productId));
            } else if (response.equals("Wrong campaign ID. Please enter a valid campaign ID.")) {
                System.out.println(response);
                id = scanner.nextInt();
                sendMessage(String.valueOf(id));
            }
        }
        String line = reader.nextLine();
        System.out.println(line);
    }

    public static void changeDiscountPercentage() {
        System.out.println("Enter campaign id");
        int id = scanner.nextInt();
        sendMessage(String.valueOf(id));
        System.out.println("Enter product id");
        int productId = scanner.nextInt();
        sendMessage(String.valueOf(productId));
        System.out.println("Enter discount percentage");
        int discountPercentage = scanner.nextInt();
        sendMessage(String.valueOf(discountPercentage));
        while (true) {
            String response = reader.nextLine();
            if (response.equals("success")) break;
            else if (response.equals("Wrong product ID. Please enter a valid product ID.")) {
                System.out.println(response);
                productId = scanner.nextInt();
                sendMessage(String.valueOf(productId));
            } else if (response.equals("Wrong campaign ID. Please enter a valid campaign ID.")) {
                System.out.println(response);
                id = scanner.nextInt();
                sendMessage(String.valueOf(id));
            } else if (response.equals("Wrong discount percentage. Please enter a valid discount percentage.")) {
                System.out.println(response);
                discountPercentage = scanner.nextInt();
                sendMessage(String.valueOf(discountPercentage));
            }
        }
        String line = reader.nextLine();
        System.out.println(line);
    }

    public static void adjustStartDate() {
        System.out.println("Enter campaign id");
        int id = scanner.nextInt();
        sendMessage(String.valueOf(id));
        System.out.println("Enter new start date");
        String startDate = scanner.nextLine();
        sendMessage(startDate);
        while (true) {
            String response = reader.nextLine();
            if (response.equals("success")) break;
            else if (response.equals("Wrong campaign ID. Please enter a valid campaign ID.")) {
                System.out.println(response);
                id = scanner.nextInt();
                sendMessage(String.valueOf(id));
            }
        }
        String line = reader.nextLine();
        System.out.println(line);
    }

    public static void adjustEndDate() {
        System.out.println("Enter campaign id");
        int id = scanner.nextInt();
        sendMessage(String.valueOf(id));
        System.out.println("Enter new end date");
        String endDate = scanner.nextLine();
        sendMessage(endDate);
        while (true) {
            String response = reader.nextLine();
            if (response.equals("success")) break;
            else if (response.equals("Wrong campaign ID. Please enter a valid campaign ID.")) {
                System.out.println(response);
                id = scanner.nextInt();
                sendMessage(String.valueOf(id));
            }
        }
        String line = reader.nextLine();
        System.out.println(line);
    }


    public static void addProduct() {
        scanner.nextLine();
        System.out.println("Enter product name");
        String name = scanner.nextLine();
        sendMessage(name);
        System.out.println("Enter price");
        double price = scanner.nextDouble();
        scanner.nextLine();
        sendMessage(String.valueOf(price));
        System.out.println("Enter quantity");
        int quantity = scanner.nextInt();
        scanner.nextLine();
        sendMessage(String.valueOf(quantity));
        System.out.println("Enter minPrice");
        double minPrice = scanner.nextDouble();
        scanner.nextLine();
        sendMessage(String.valueOf(minPrice));
        String line = reader.nextLine();
        System.out.println(line);
    }

    public static void redactProduct() {
        scanner.nextLine();
        System.out.println("Enter product id");
        int id = scanner.nextInt();
        scanner.nextLine();
        sendMessage(String.valueOf(id));
        System.out.println("Enter product name");
        String name = scanner.nextLine();
        sendMessage(name);
        System.out.println("Enter price");
        double price = scanner.nextDouble();
        scanner.nextLine();
        sendMessage(String.valueOf(price));
        System.out.println("Enter quantity");
        int quantity = scanner.nextInt();
        scanner.nextLine();
        sendMessage(String.valueOf(quantity));
        System.out.println("Enter minPrice");
        double minPrice = scanner.nextDouble();
        scanner.nextLine();
        sendMessage(String.valueOf(minPrice));
        String line = reader.nextLine();
        System.out.println(line);
    }

    public static void removeProduct() {
        scanner.nextLine();
        System.out.println("Enter product id");
        int id = scanner.nextInt();
        sendMessage(String.valueOf(id));
        String line = reader.nextLine();
        System.out.println(line);

    }

    public static void quantityCheck() throws NoSuchAlgorithmException {
        System.out.println("Enter minimum number of products");
        //int min = Integer.parseInt(scanner.nextLine());
        String min = scanner.nextLine();
        sendMessage(min);
        //sendMessage(String.valueOf(min));
        String line;
        while ((line = reader.nextLine()) != null) {
            if (line.equals("done")) break;
            System.out.println("Message from server: " + line);
        }
        adminmenu();
    }

    public static void addAdmin() throws NoSuchAlgorithmException {

        System.out.println("Enter id of the user you want to make admin");
        int id = scanner.nextInt();
        scanner.nextLine();
        sendMessage(String.valueOf(id));
        System.out.println("Enter your username");
        String username = scanner.nextLine();
        sendMessage(username);
        System.out.println("Enter your password");
        String password = scanner.nextLine();
        password = PasswordHasher.hashPassword(password);
        sendMessage(password);

        String response = reader.nextLine();
        System.out.println(response);
    }

    public static void removeAdmin() throws NoSuchAlgorithmException {
        System.out.println("Enter id of the user you want to remove from admin");
        int id = scanner.nextInt();
        scanner.nextLine();
        sendMessage(String.valueOf(id));
        System.out.println("Enter your username");
        String username = scanner.nextLine();
        sendMessage(username);
        System.out.println("Enter your password");
        String password = scanner.nextLine();
        password = PasswordHasher.hashPassword(password);
        sendMessage(password);

        String response = reader.nextLine();
        System.out.println(response);

        adminmenu();
    }


}