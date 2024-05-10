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

    public static String getMessage() {
        return reader.nextLine();
    }

    public static void runLogic() throws NoSuchAlgorithmException {
        System.out.println("Connected");
        System.out.println("1. login");
        System.out.println("2. register");
        System.out.println("3. exit");
//        System.out.println("4. admin test");//remove later
//        System.out.println("5. user test");//remove later



        String message;
        while (true) {
            System.out.println("Enter your choice");
            message = scanner.nextLine();

            if (message.matches("[1-3]")) {
                break;
            } else {
                System.out.println("Invalid choice. Please enter a number between 1 and 3.");
            }
        }
        sendMessage(message);
        switch (message) {
            case "1" -> login();
            case "2" -> register();
            case "3" -> exit(0);
//            case "4" -> adminmenu();//remove later
//            case "5" -> usermenu();//remove later
        }
    }

    public static void login() throws NoSuchAlgorithmException {

        while (true) {
            System.out.println("Enter username");
            String username = scanner.nextLine();
            if (username.isEmpty()) exit(1);
            System.out.println("Enter password");
            String password = scanner.nextLine();
            password = PasswordHasher.hashPassword(password);
            sendMessage(username);
            sendMessage(password);
            String line = getMessage();
            if (line.equals("Login Successful")) {
                System.out.println();
                System.out.println(line);
                break;
            }else if (line.equals("Max attempts reached. You are disconnected.")) {
                System.out.println();
                System.out.println(line);
                exit(1);
            }else {
                System.out.println(line);
            }
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
        if (username.isEmpty()) exit(1);
        sendMessage(message);
        String line = getMessage();
        if (line.equals("User registered successfully.")) {
            System.out.println(line);
            login();
        } else if (line.equals("Username is already taken. Please choose another username.")) {
            System.out.println(line);
            register();
        } else {
            System.out.println("Something went wrong. Please try again");
            register();
        }
    }

    public static void usermenu() {
        System.out.println();
        System.out.println("Меню за клиенти:");
        System.out.println("1. Разгледай всички налични продукти");
        System.out.println("2. Разгледай продукти от кампании с промоции и разпродажби");
        System.out.println("3. Поръчай продукт");
        System.out.println("0. Изход");

        String choice;
        while (true) {
            System.out.println("Enter your choice");
            choice = scanner.next();

            if (choice.matches("[0-3]")) {
                break;
            } else {
                System.out.println("Invalid choice. Please enter a number between 0 and 3.");
            }
        }
        sendMessage(choice);
        int choicet=Integer.parseInt(choice);
        if (choicet == 1) {
            browseAllProducts();
        } else if (choicet == 2) {
            browsePromotionalProducts();
        } else if (choicet == 3) {
            orderProduct();
        } else if (choicet == 0) {
            exit(0);
        }
    }

    public static void browseAllProducts() {
        String line;
        while ((line = reader.nextLine()) != null) {
            if (line.equals("done")) break;
            String parts[] = line.split(" ");
            System.out.println("Продукт: " + parts[0] + " Цена: " + parts[1] + "лв. Количество: " + parts[2]);
        }
        usermenu();

    }

    public static void browsePromotionalProducts() {
        System.out.println();
        String line;
        while ((line = reader.nextLine()) != null) {
            if (line.equals("done")) break;
            String parts[] = line.split(" ");
            System.out.println("Продукт: " + parts[0] + " Цена: " + parts[1] + "лв. Количество: " + parts[2]);
        }
        System.out.println();
        usermenu();
    }

    public static void orderProduct() {
        scanner.nextLine();
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

        String response = getMessage();
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

    public static void adminmenu() {
        System.out.println();
        System.out.println("Admin menu");
        System.out.println("1. Report");
        System.out.println("2. Redact menu");
        System.out.println("3. Sales Menu");
        System.out.println("4. quantityCheck");
        System.out.println("5. Make admin");
        System.out.println("6. Remove admin");
        System.out.println("7. Exit");

        String choice = scanner.nextLine();
//        while (true) {
//            System.out.println("Enter your choice");
//            choice = scanner.next();
//
//            if (choice.matches("[1-7]")) {
//                break;
//            } else {
//                System.out.println("Invalid choice. Please enter a number between 1 and 7.");
//            }
//        }
        sendMessage(choice);

        switch (choice) {
            case "1":
                spravka();
                break;
            case "2":
                redactMenu();
                break;
            case "3":
                salesMenu();
                break;
            case "4":
                quantityCheck();
                break;
            case "5":
                addAdmin();
                break;
            case "6":
                removeAdmin();
                break;
            case "7":
                exit(0);
                break;
            default:
                System.out.println("Невалиден избор. Моля, опитайте отново.");
                break;
        }
    }

    public static void spravka() {
        System.out.println("Enter startDate and endDate in format: YYYY-MM-DD");
        String startDate = scanner.nextLine();
        String endDate = scanner.nextLine();
        sendMessage(startDate);
        sendMessage(endDate);
        String test = getMessage();
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

    public static void redactMenu() {
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
        System.out.println("4. Create campaign");
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
            case 4:
                createCampaign();
                break;
            default:
                System.out.println("Невалиден избор. Моля, опитайте отново.");
                break;
        }
        adminmenu();
    }

    public static void startSale() {
        System.out.println("Enter campaign id");
        int id = scanner.nextInt();
        scanner.nextLine();
        sendMessage(String.valueOf(id));
        String line = getMessage();
        System.out.println(line);
    }

    public static void stopSale() {
        System.out.println("Enter campaign id");
        int id = scanner.nextInt();
        scanner.nextLine();
        sendMessage(String.valueOf(id));
        String line = getMessage();
        System.out.println(line);
    }

    public static void createCampaign() {
        scanner.nextLine();
        System.out.println("Enter campaign start date in format: YYYY-MM-DD");
        String startDate = scanner.nextLine();
        System.out.println("Enter campaign end date in format: YYYY-MM-DD");
        String endDate = scanner.nextLine();
        sendMessage(startDate);
        sendMessage(endDate);

        String line = getMessage();
        if (!line.equals("correct")) {
            System.out.println(line);
            adminmenu();
            return;
        }
        line = getMessage();
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
            String response = getMessage();
            if (response.equals("success")) break;
            else if (response.equals("Wrong product ID. Please enter a valid product ID:")) {
                System.out.println(response);
                productId = scanner.nextInt();
                sendMessage(String.valueOf(productId));
            } else if (response.equals("Wrong campaign ID. Please enter a valid campaign ID:")) {
                System.out.println(response);
                id = scanner.nextInt();
                sendMessage(String.valueOf(id));
            } else if (response.equals("Discount percentage is too high. Please enter a valid discount percentage:")) {
                System.out.println(response);
                discountPercentage = scanner.nextInt();
                sendMessage(String.valueOf(discountPercentage));
            }
        }
        String line = getMessage();
        System.out.println(line);

        adminmenu();

    }


    public static void removeProductFromCampaign() {
        System.out.println("Enter campaign id");
        int id = scanner.nextInt();
        sendMessage(String.valueOf(id));
        System.out.println("Enter product id");
        int productId = scanner.nextInt();
        sendMessage(String.valueOf(productId));
        while (true) {
            String response = getMessage();
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
        String line = getMessage();
        System.out.println(line);

        adminmenu();
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
            String response = getMessage();
            if (response.equals("success")) break;
            else if (response.equals("Wrong product ID. Please enter a valid product ID:")) {
                System.out.println(response);
                productId = scanner.nextInt();
                sendMessage(String.valueOf(productId));
            } else if (response.equals("Wrong campaign ID. Please enter a valid campaign ID:")) {
                System.out.println(response);
                id = scanner.nextInt();
                sendMessage(String.valueOf(id));
            } else if (response.equals("Discount percentage is too high. Please enter a valid discount percentage:")) {
                System.out.println(response);
                discountPercentage = scanner.nextInt();
                sendMessage(String.valueOf(discountPercentage));
            }
        }
        String line = getMessage();
        System.out.println(line);
        adminmenu();
    }


    public static void adjustStartDate() {
        System.out.println("Enter campaign id");
        int id = scanner.nextInt();
        scanner.nextLine();
        sendMessage(String.valueOf(id));
        System.out.println("Enter new start date");
        String startDate = scanner.nextLine();
        sendMessage(startDate);
        while (true) {
            String response = getMessage();
            if (response.equals("success")) break;
            else if (response.equals("Wrong campaign ID. Please enter a valid campaign ID.")) {
                System.out.println(response);
                id = scanner.nextInt();
                sendMessage(String.valueOf(id));
            }
        }
        String line = getMessage();
        System.out.println(line);
        adminmenu();
    }

    public static void adjustEndDate() {
        System.out.println("Enter campaign id");
        int id = scanner.nextInt();
        scanner.nextLine();
        sendMessage(String.valueOf(id));
        System.out.println("Enter new end date");
        String endDate = scanner.nextLine();
        sendMessage(endDate);
        while (true) {
            String response = getMessage();
            if (response.equals("success")) break;
            else if (response.equals("Wrong campaign ID. Please enter a valid campaign ID.")) {
                System.out.println(response);
                id = scanner.nextInt();
                sendMessage(String.valueOf(id));
            }
        }
        String line = getMessage();
        System.out.println(line);
        adminmenu();
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
        String line = getMessage();
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
        String line = getMessage();
        System.out.println(line);
    }

    public static void removeProduct() {
        scanner.nextLine();
        System.out.println("Enter product id");
        int id = scanner.nextInt();
        sendMessage(String.valueOf(id));
        String line = getMessage();
        System.out.println(line);

    }

    public static void quantityCheck() {
        System.out.println("Enter minimum number of products");
        //int min = Integer.parseInt(scanner.nextLine());
        String min = scanner.nextLine();
        sendMessage(min);
        //sendMessage(String.valueOf(min));
        String line;
        while ((line = reader.nextLine()) != null) {
            if (line.equals("done")) break;
            System.out.println("Product id: " + line +" Quantity left");
        }

        adminmenu();
    }

    public static void addAdmin() {

        System.out.println("Enter id of the user you want to make admin");
        int id = scanner.nextInt();
        scanner.nextLine();
        sendMessage(String.valueOf(id));

        System.out.println("Enter your password");
        String password = scanner.nextLine();
        try {
            password = PasswordHasher.hashPassword(password);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        sendMessage(password);

        String response = getMessage();
        System.out.println(response);


        adminmenu();

    }

    public static void removeAdmin() {
        System.out.println("Enter id of the user you want to remove from admin");
        int id = scanner.nextInt();
        scanner.nextLine();
        sendMessage(String.valueOf(id));

        System.out.println("Enter your password");
        String password = scanner.nextLine();
        try {
            password = PasswordHasher.hashPassword(password);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        sendMessage(password);

        String response = getMessage();
        System.out.println(response);


        adminmenu();

    }


    

}