import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class ServerThread implements Runnable {

    private final Socket socket;
    private Scanner reader;
    private PrintStream writer;

    private Customer customer;
    private Admin admin;


    private Connection connection;


    public ServerThread(Socket socket, Connection connection/*,BasicDataSource dataSource*/) {
        this.socket = socket;
        this.connection = connection;
    }

    public static void logAdm(int adminId, int userId, String action, Connection connection) {
        String sql = "insert into adminLog(admin_id,user_id,action,date) values(?,?,?,now())";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, userId);
            statement.setInt(2, adminId);
            statement.setString(3, action);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean checkCampain(int id, Connection connection) throws SQLException {
        String sql = "select campain_id from salesCampain where campain_id=?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, id);
        ResultSet resultSet = statement.executeQuery();
        return resultSet.next();
    }

    public static boolean checkProduct(int id, Connection connection) throws SQLException {
        String sql = "select product_id from products where product_id=?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, id);
        ResultSet resultSet = statement.executeQuery();
        return resultSet.next();
    }

    public void run() {
        try {
            reader = new Scanner(socket.getInputStream());
            writer = new PrintStream(socket.getOutputStream());


        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Connected");
        try {
            int choice = Integer.parseInt(getMessage());
            switch (choice) {
                case 1:
                    loginL();
                    break;
                case 2:
                    registerU();
                    break;
                case 3:
                    break;
//                case 4://remove later
//                    admin = new Admin(1, "admin");
//                    adminMenu();
//                    break;
//                case 5://remove later
//                    customer = new Customer(2, "user");
//                    customerMenu();
            }


        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (NoSuchElementException e) {
            System.out.println("Connection closed?");
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (writer != null) {
                    writer.close();
                }
                socket.close();
                if (connection != null) {
                    connection.close();
                }
                System.out.println("Disconnected");
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        }

    }

    public void sendMessage(String message) {
        try {
            writer.println(message);
        } catch (NoSuchElementException e) {
            System.out.println("Connection closed?");
        }
    }

    public String getMessage() {
//        try {
        return reader.nextLine();
//        }catch (NoSuchElementException e){
//            System.out.println("Connection closed?");
//            return null;
//        }
    }

    public void loginL() throws SQLException {
        int maxAttempts = 3, attempts = 0;
        String sql = "select user_id,role from user where username=? and passwordH=?";
        String role;
        int id;
        String username;
        String password;

        while (true) {
            username = getMessage();
            if (username.isEmpty()) return;

            password = getMessage();
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                id = resultSet.getInt("user_id");
                role = resultSet.getString("role");
                sendMessage("Login Successful");
                break; // Exit the loop if login successful
            } else {
                attempts++;
                if (attempts >= maxAttempts) {
                    sendMessage("Max attempts reached. You are disconnected.");
                    return; // Disconnect user
                }
                sendMessage("Wrong username or password. Attempts left: " + (maxAttempts - attempts));
            }
        }

        connection.close();

        if (role.equals("user")) {
            sendMessage("1");
            customer = new Customer(id, username);
            customerMenu();
        } else {
            sendMessage("2");
            admin = new Admin(id, username);
            adminMenu();
        }
    }

    public void registerU() throws SQLException {
        String sql = "insert into user(username,passwordH,role) values(?,?,?)";
        String message = getMessage();
        String[] parts = message.split(":");
        String username = parts[0];
        String password = parts[1];
        String role = "user";

        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, username);
            statement.setString(2, password);
            statement.setString(3, role);
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("User registered successfully.");
            } else {
                System.out.println("Failed to register user.");
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            sendMessage("Username is already taken. Please choose another username.");
        } catch (SQLException e) {
            // Handle other SQL exceptions
            e.printStackTrace(); // You can log the exception for debugging purposes
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace(); // Handle connection close exception
            }
        }


        assert connection != null;
        connection.close();
        loginL();
    }

    public void customerMenu() throws SQLException {
        int choice;

        do {
            choice = Integer.parseInt(getMessage());

            switch (choice) {
                case 1:
                    browseAllProducts();
                    break;
                case 2:
                    browsePromotionalProducts();
                    break;
                case 3:
                    orderProduct();
                    break;
                case 0:
                    System.out.println("Изход от менюто за клиенти.");
                    break;
                default:
                    System.out.println("Невалиден избор. Моля, опитайте отново.");
            }
        } while (choice != 0);

    }

    public void browseAllProducts() throws SQLException {
        connection = DatabaseManager.getConnection();
        String sql = "select products.name,products.price,products.quantity from products";
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        while (resultSet.next()) {
            sendMessage(resultSet.getString("name") + " " + String.valueOf(resultSet.getDouble("price")) + " " + String.valueOf(resultSet.getInt("quantity")));
        }
        sendMessage("done");
        connection.close();
        customerMenu();
    }

    public void browsePromotionalProducts() throws SQLException {
        connection = DatabaseManager.getConnection();
        String sql = """
                SELECT p.name AS product_name, s.new_price AS sale_price, p.quantity
                FROM products p
                JOIN sales s ON p.product_id = s.product_id
                JOIN salesCampain sc ON s.campain_id = sc.campain_id
                WHERE sc.isActive = 1
                """;
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        while (resultSet.next()) {
            String productName = resultSet.getString("product_name");
            double salePrice = resultSet.getDouble("sale_price");
            int quantity = resultSet.getInt("quantity");

            sendMessage(productName + " " + salePrice + " " + quantity);
        }
        sendMessage("done");
        connection.close();
        customerMenu();
    }

    public void orderProduct() throws SQLException {
        connection = DatabaseManager.getConnection();
        int product_id = Integer.parseInt(getMessage());
        int quantity = Integer.parseInt(getMessage());
        String cardNumber = getMessage();
        if (!CardValidator.validateCardNumber(cardNumber)) { //tuka s while sig trq
            System.out.println("invalid card number");
            connection.close();
            return;
        }

        String sql2 = "select quantity,price from products where product_id=?";
        PreparedStatement statement2 = connection.prepareStatement(sql2);
        statement2.setInt(1, product_id);
        ResultSet resultSet = statement2.executeQuery();

        double price = 0;

        if (resultSet.next()) {
            if (resultSet.getInt("quantity") < quantity) {
                System.out.println("not enough quantity");
                connection.close();
                return;
            }
            price = resultSet.getDouble("price");
        }

        String sqlSale = "SELECT new_price FROM sales WHERE product_id = ? AND campain_id IN (SELECT campain_id FROM salesCampain WHERE isActive = 1)";
        PreparedStatement statementSale = connection.prepareStatement(sqlSale);
        statementSale.setInt(1, product_id);
        ResultSet resultSetSale = statementSale.executeQuery();
        double reducedPrice = -1; // Default value indicating no reduction
        if (resultSetSale.next()) {
            reducedPrice = resultSetSale.getDouble("new_price");
        }

        double finalPurchasePrice = reducedPrice != -1 ? reducedPrice : price;

        String sql = "insert into purchases(product_id,quantity,purchase_price,user_id,purchaseDate) values(?,?,?,?,now())";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, product_id);
        statement.setInt(2, quantity);
        statement.setDouble(3, finalPurchasePrice);
        statement.setInt(4, customer.getId());
        statement.executeUpdate();

        String sql3 = "update products set quantity=quantity-? where product_id=?";
        PreparedStatement statement3 = connection.prepareStatement(sql3);
        statement3.setInt(1, quantity);
        statement3.setInt(2, product_id);
        statement3.executeUpdate();
        connection.close();
        sendMessage("Order successful");
        customerMenu();
    }

    public void adminMenu(/*Admin admin*/) throws SQLException {
        int choice = Integer.parseInt(getMessage());
        /*
        1-справка за оборота на магазина за определен период от време
        2-Добавяне, редактиране и изтриване на продукти, които се предлагат в магазина;
        3-стартиране и спиране на кампания с разпродажби. Намаленията могат да са само за част от продуктите, като се задава намаление на цените с конкретен процент. Всеки продукт има минимална цена, под която не може да бъде предлаган.
        4-Следене на наличности на продукти;(nqkoi produkt ako ima malko da izliza?)
         */

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
                makeAdmin();
                break;
            case 6:
                removeAdmin();
                break;
        }
    }

    public void makeAdmin() throws SQLException {
        String sql = "update user set role='admin' where user_id=?";

        int id = Integer.parseInt(getMessage());
        String username = admin.getUsername();
        String password = getMessage();
        String checkSql = "select * from user where username=? and passwordH=?";
        connection = DatabaseManager.getConnection();
        PreparedStatement checkStatement = connection.prepareStatement(checkSql);
        checkStatement.setString(1, username);
        checkStatement.setString(2, password);
        ResultSet resultSet = checkStatement.executeQuery();
        if (!resultSet.next()) {
            sendMessage("Wrong credentials");
            adminMenu();
            return;
        }
        int adminId = admin.getId();
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, id);
        int rowsAffected = statement.executeUpdate();
        if (rowsAffected > 0) {
            sendMessage("Admin created successfully.");
            logAdm(adminId, id, "made admin", connection);
        } else {
            sendMessage("Failed to create admin.");
        }
        connection.close();
        adminMenu();
    }

    public void removeAdmin() throws SQLException {
        String sql = "update user set role='user' where user_id=?";

        int id = Integer.parseInt(getMessage());
        String username = admin.getUsername();
        String password = getMessage();
        String checkSql = "select * from user where username=? and passwordH=?";
        connection = DatabaseManager.getConnection();
        PreparedStatement checkStatement = connection.prepareStatement(checkSql);
        checkStatement.setString(1, username);
        checkStatement.setString(2, password);
        ResultSet resultSet = checkStatement.executeQuery();
        if (!resultSet.next()) {
            sendMessage("Wrong credentials");
            adminMenu();
            return;
        }
        int adminId = admin.getId();
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, id);
        int rowsAffected = statement.executeUpdate();
        if (rowsAffected > 0) {
            sendMessage("Admin removed successfully.");
            logAdm(adminId, id, "removed admin", connection);
        } else {
            sendMessage("Failed to remove admin.");
        }
        connection.close();
        adminMenu();
    }

    public void spravka() throws SQLException {
        String first = getMessage();
        String second = getMessage();

        LocalDate firstDate, secondDate;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            firstDate = LocalDate.parse(first, formatter);
            secondDate = LocalDate.parse(second, formatter);
        } catch (DateTimeParseException e) {
            sendMessage("Wrong date format");
            adminMenu();
            return;
        }
        sendMessage("correct");

        if (secondDate.isBefore(firstDate)) {
            LocalDate temp = firstDate;
            firstDate = secondDate;
            secondDate = temp;
        }


        Connection newconnection = DatabaseManager.getConnection();
        //String sql="SELECT SUM(p.price * pur.quantity) AS total_sales FROM purchases pur JOIN products p ON pur.product_id = p.product_id WHERE pur.purchaseDate >= ? AND pur.purchaseDate < ?";
        String sql = "SELECT SUM(pur.purchase_price*pur.quantity) as total_sales from purchases pur where pur.purchaseDate >= ? AND pur.purchaseDate < ?";
        PreparedStatement statement = newconnection.prepareStatement(sql);
        statement.setString(1, firstDate.toString());
        statement.setString(2, secondDate.toString());

        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            int totalSales = resultSet.getInt("total_sales");
            writer.println(totalSales);
        } else {
            writer.println(0);
        }
        newconnection.close();

        adminMenu();
    }

    public void redactMenu() throws SQLException {
        int choice = Integer.parseInt(getMessage());
        connection = DatabaseManager.getConnection();
        switch (choice) {
            case 1:
                addProduct(connection);
                break;
            case 2:
                redactProduct(connection);
                break;
            case 3:
                removeProduct(connection);
                break;
            case 4:
                break;
        }
        connection.close();
        adminMenu();
    }

    public void addProduct(Connection connection) throws SQLException {
        System.out.println("name,price,quantity,minimalPrice");
        String sql = "insert into products(name,price,quantity,minimalPrice) values(?,?,?,?)";
        String name = getMessage();
        double price = Double.parseDouble(getMessage());
        int quantity = Integer.parseInt(getMessage());
        double minPrice = Double.parseDouble(getMessage());

        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, name);
        statement.setDouble(2, price);
        statement.setInt(3, quantity);
        statement.setDouble(4, minPrice);

        int rowsAffected = statement.executeUpdate();
        if (rowsAffected > 0) {
            sendMessage("Product added successfully.");
        } else {
            sendMessage("Failed to add product.");
        }
    }

    public void redactProduct(Connection connection) throws SQLException {
        System.out.println("enter product id");
        int id = Integer.parseInt(getMessage());
        String sql = "update products set name=?,price=?,quantity=?,minimalPrice=? where product_id=?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, getMessage());
        statement.setDouble(2, Double.parseDouble(getMessage()));
        statement.setInt(3, Integer.parseInt(getMessage()));
        statement.setDouble(4, Double.parseDouble(getMessage()));
        statement.setInt(5, id);
        int rowsAffected = statement.executeUpdate();
        if (rowsAffected > 0) {
            sendMessage("Product updated successfully.");
        } else {
            sendMessage("Failed to update product.");
        }
    }

    public void removeProduct(Connection connection) throws SQLException {

        System.out.println("enter product id");
        int id = Integer.parseInt(getMessage());
        String sql = "delete from products where product_id=?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, id);
        int rowsAffected = statement.executeUpdate();
        if (rowsAffected > 0) {
            sendMessage("Product removed successfully.");
        } else {
            sendMessage("Failed to remove product.");
        }
    }

    public void quantityCheck() throws SQLException {
        connection = DatabaseManager.getConnection();
        int numberOfProducts = Integer.parseInt(getMessage());
        String sql = "select product_id,quantity from products where quantity < ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, numberOfProducts);

        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
            writer.println(resultSet.getString("product_id") + " " + resultSet.getInt("quantity"));
        }
        sendMessage("done");
        connection.close();

        adminMenu();
    }

    public void salesMenu() throws SQLException {
        int choice = Integer.parseInt(getMessage());
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
            case 5:
                break;
        }
        adminMenu();
    }

    public void startSale() throws SQLException {
        Connection con = DatabaseManager.getConnection();
        int campaign_id = Integer.parseInt(getMessage());
        String sql = "update salesCampain set isActive=1 where campain_id=?";
        PreparedStatement statement = con.prepareStatement(sql);
        statement.setInt(1, campaign_id);
        int rowsAffected = statement.executeUpdate();
        if (rowsAffected > 0) {
            sendMessage("Campaign started successfully.");
        } else {
            sendMessage("Failed to start campaign.");
        }
        con.close();
    }

    public void stopSale() throws SQLException {
        int campaign_id = Integer.parseInt(getMessage());
        Connection con = DatabaseManager.getConnection();
        String sql = "update salesCampain set isActive=0 where campain_id=?";
        PreparedStatement statement = con.prepareStatement(sql);
        statement.setInt(1, campaign_id);
        int rowsAffected = statement.executeUpdate();
        if (rowsAffected > 0) {
            sendMessage("Campaign stopped successfully.");
        } else {
            sendMessage("Failed to stop campaign.");
        }
        con.close();
    }

    public void manageSale() throws SQLException {
        sendMessage("Select an option:");
        sendMessage("1. Add product to campaign");
        sendMessage("2. Remove product from campaign");
        sendMessage("3. Change discount percentage");
        sendMessage("4. Adjust start date");
        sendMessage("5. Adjust end date");
        sendMessage("6. Exit");

        int choice = Integer.parseInt(getMessage());

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
            default:
                sendMessage("Invalid choice");
                break;
        }
    }

    public void createCampaign() throws SQLException {
        connection = DatabaseManager.getConnection();
        String sql = "insert into salesCampain(campainStart,campainEnd,isActive) values(?,?,0)";
        PreparedStatement statement = connection.prepareStatement(sql);


        String first = getMessage();
        String second = getMessage();
        LocalDate firstDate, secondDate;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            firstDate = LocalDate.parse(first, formatter);
            secondDate = LocalDate.parse(second, formatter);
        } catch (DateTimeParseException e) {
            sendMessage("Wrong date format");
            adminMenu();
            return;
        }
        sendMessage("correct");
        if (secondDate.isBefore(firstDate)) {
            LocalDate temp = firstDate;
            firstDate = secondDate;
            secondDate = temp;
        }
        first = firstDate.toString();
        second = secondDate.toString();


        statement.setString(1, first);
        statement.setString(2, second);
        int rowsAffected = statement.executeUpdate();
        if (rowsAffected > 0) {
            sendMessage("Campaign created successfully.");
        } else {
            sendMessage("Failed to create campaign.");
        }

    }

    public double checkDiscount(int product_id, int discount, Connection connection1) throws SQLException {
        String sql = "select price,minimalPrice from products where product_id=?";
        PreparedStatement statement = connection1.prepareStatement(sql);
        statement.setInt(1, product_id);
        double price;
        double minimalPrice;
        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            price = resultSet.getDouble("price");
            minimalPrice = resultSet.getDouble("minimalPrice");
        } else {
            System.out.println("wrong product id");
            return -1;
        }
        double discount_price = price * (1 - (double) discount / 100);
        return !(discount_price < minimalPrice) ? discount_price : 0;
    }

    public void addProductToCampaign() throws SQLException {
        int campaign_id = Integer.parseInt(getMessage());
        int product_id = Integer.parseInt(getMessage());
        int discount_percentage = Integer.parseInt(getMessage());
        connection = DatabaseManager.getConnection();

        double discount_price;

        while (true) {

            discount_price = checkDiscount(product_id, discount_percentage, connection);
            System.out.println(discount_price);

            if (discount_price == 0.0) {
                // System.out.println("discount");
                sendMessage("Discount percentage is too high. Please enter a valid discount percentage:");
                discount_percentage = Integer.parseInt(getMessage());
            } else if (discount_price == -1) {
                // System.out.println("product id");
                sendMessage("Wrong product ID. Please enter a valid product ID:");
                product_id = Integer.parseInt(getMessage());
            } else if (!checkCampain(campaign_id, connection)) {
                // System.out.println("wrong campaign id");
                sendMessage("Wrong campaign ID. Please enter a valid campaign ID:");
                campaign_id = Integer.parseInt(getMessage());
            } else {
                sendMessage("success");
                break;
            }
        }


        String sql = "insert into sales (campain_id,product_id,discount,new_price) values (?,?,?,?)";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, campaign_id);
        statement.setInt(2, product_id);
        statement.setInt(3, discount_percentage);
        statement.setDouble(4, discount_price);
        int rowsAffected = statement.executeUpdate();
        if (rowsAffected > 0) {
            sendMessage("Product added to campaign successfully.");
        } else {
            sendMessage("Failed to add product to campaign.");
        }

        connection.close();
        adminMenu();
    }

    public void removeProductFromCampaign() throws SQLException {
        connection = DatabaseManager.getConnection();
        int campaign_id = Integer.parseInt(getMessage());
        int product_id = Integer.parseInt(getMessage());

        while (true) {
            if (!checkCampain(campaign_id, connection)) {
                sendMessage("Wrong campaign ID. Please enter a valid campaign ID:");
                campaign_id = Integer.parseInt(getMessage());
            } else if (!checkProduct(product_id, connection)) {
                sendMessage("Wrong product ID. Please enter a valid product ID:");
                product_id = Integer.parseInt(getMessage());
            } else {
                sendMessage("success");
                break;
            }
        }


        String sql = "delete from sales where campain_id=? and product_id=?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, campaign_id);
        statement.setInt(2, product_id);
        int rowsAffected = statement.executeUpdate();
        if (rowsAffected > 0) {
            sendMessage("Product removed successfully.");
        } else {
            sendMessage("Failed to remove product.");
        }
        connection.close();
        adminMenu();
    }

    public void changeDiscountPercentage() throws SQLException {
        connection = DatabaseManager.getConnection();
        int campaign_id = Integer.parseInt(getMessage());
        int product_id = Integer.parseInt(getMessage());
        int discount_percentage = Integer.parseInt(getMessage());

        double discount_price;
        while (true) {
            discount_price = checkDiscount(product_id, discount_percentage, connection);
            if (discount_price == 0) {
                sendMessage("Discount percentage is too high. Please enter a valid discount percentage:");
                discount_percentage = Integer.parseInt(getMessage());
            } else if (discount_price == -1) {
                sendMessage("Wrong product ID. Please enter a valid product ID:");
                product_id = Integer.parseInt(getMessage());
            } else if ((!checkCampain(campaign_id, connection))) {
                sendMessage("Wrong campaign ID. Please enter a valid campaign ID:");
                campaign_id = Integer.parseInt(getMessage());
            } else {
                sendMessage("success");
                break;
            }
        }

        String sql = "update sales set discount=?,new_price=? where campain_id=? and product_id=?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, discount_percentage);
        statement.setDouble(2, discount_price);
        statement.setInt(3, campaign_id);
        statement.setInt(4, product_id);
        int rowsAffected = statement.executeUpdate();
        if (rowsAffected > 0) {
            sendMessage("Discount percentage updated successfully.");
        } else {
            sendMessage("Failed to update discount percentage.");
        }
        connection.close();
        adminMenu();
    }

    public void adjustStartDate() throws SQLException {
        connection = DatabaseManager.getConnection();
        int campaign_id = Integer.parseInt(getMessage());
        String start_date = getMessage();

        while (true) {
            if (!checkCampain(campaign_id, connection)) {
                sendMessage("Wrong campaign ID. Please enter a valid campaign ID:");
                campaign_id = Integer.parseInt(getMessage());
            } else {
                sendMessage("success");
                break;
            }
        }
        String queryEndDate = "SELECT campainEnd FROM salesCampain WHERE campain_id = ?";
        PreparedStatement statement = connection.prepareStatement(queryEndDate);
        statement.setInt(1, campaign_id);
        ResultSet resultSet = statement.executeQuery();
        resultSet.next();
        String endDate = resultSet.getString("campainEnd");
        if (start_date.compareTo(endDate) < 0) {
            String sql = "update salesCampain set campainStart=? where campain_id=?";
            PreparedStatement statement1 = connection.prepareStatement(sql);
            statement1.setString(1, start_date);
            statement1.setInt(2, campaign_id);
            int rowsAffected = statement1.executeUpdate();
            if (rowsAffected > 0) {
                sendMessage("Start date updated successfully.");
            } else {
                sendMessage("Failed to update start date.");
            }
        } else {
            sendMessage("Start date must be before end date");
            connection.close();
            adminMenu();
            return;
        }
        connection.close();
        adminMenu();
    }

    public void adjustEndDate() throws SQLException {
        connection = DatabaseManager.getConnection();
        int campaign_id = Integer.parseInt(getMessage());
        String end_date = getMessage();

        while (true) {
            if (!checkCampain(campaign_id, connection)) {
                sendMessage("Wrong campaign ID. Please enter a valid campaign ID:");
                campaign_id = Integer.parseInt(getMessage());
            } else {
                sendMessage("success");
                break;
            }
        }
        String queryStartDate = "SELECT campainStart FROM salesCampain WHERE campain_id = ?";
        PreparedStatement statement = connection.prepareStatement(queryStartDate);
        statement.setInt(1, campaign_id);
        ResultSet resultSet = statement.executeQuery();
        resultSet.next();
        String startDate = resultSet.getString("campainStart");
        if (end_date.compareTo(startDate) > 0) {
            String sql = "update salesCampain set campainEnd=? where campain_id=?";
            PreparedStatement statement1 = connection.prepareStatement(sql);
            statement1.setString(1, end_date);
            statement1.setInt(2, campaign_id);
            int rowsAffected = statement1.executeUpdate();
            if (rowsAffected > 0) {
                sendMessage("End date updated successfully.");
            } else {
                sendMessage("Failed to update end date.");
            }
        } else {
            sendMessage("End date must be after start date");
            connection.close();
            adminMenu();
        }
        connection.close();
        adminMenu();
    }

}