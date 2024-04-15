import org.apache.commons.dbcp2.BasicDataSource;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Scanner;

public class ServerThread implements Runnable {
   // private ArrayList<User> users;
    private Socket socket;
    private Scanner reader;
    private PrintStream writer;


    private Connection connection; //Conection pool?
    //private BasicDataSource dataSource;

    public ServerThread(Socket socket,Connection connection/*,BasicDataSource dataSource*/) {
        this.socket = socket;
        this.connection = connection;
        //this.dataSource=dataSource;
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
            int choice=Integer.parseInt(getMessage());
            switch (choice){
                case 1:
                    loginL();
                    break;
                case 2:
                    registerU();
                    break;
                case 3:
                    break;
            }
            //loginL();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try {
            reader.close();
            writer.close();
            socket.close();
            connection.close();
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(String message) {
        writer.println(message);
    }

    public String getMessage() {
        return reader.nextLine();
    }

    public void loginL() throws SQLException {
        String sql="select user_id,role from user where username=? and passwordH=?";
        String message=getMessage();
        String[] parts=message.split(":");
        String username=parts[0];
        String password=parts[1]; //should be hashed client side and we receive the hash only
        String role;
        int id;
        //Statement statement = connection.createStatement();
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1,username);
        statement.setString(2,password);
        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()){
            id=resultSet.getInt("user_id");
            role=resultSet.getString("role");
        }else {
            sendMessage("wrong username or password");
            return;
        }
        connection.close();
        sendMessage("Login Successful");
        if (role.equals("user")){
            sendMessage("1");
            customerMenu(new Customer(id,username));
        }
        else {
            sendMessage("2");
            adminMenu(/*new Admin(id,username*/);
        }
    }
    public void registerU() throws SQLException {
        String sql="insert into user(username,passwordH,role) values(?,?,?)";
        String message=getMessage();
        String[] parts=message.split(":");
        String username=parts[0];
        String password=parts[1]; //should be hashed client side and we receive the hash only
        String role="user";

        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1,username);
        statement.setString(2,password);
        statement.setString(3,role);
        int rowsAffected = statement.executeUpdate();
        if (rowsAffected > 0) {
            System.out.println("User registered successfully.");
        } else {
            System.out.println("Failed to register user.");

        }
        connection.close();
        loginL();
    }

    public void customerMenu(Customer customer) throws SQLException {
        int choice;


        do {
            System.out.println("Меню за клиенти:");
            System.out.println("1. Разгледай всички налични продукти");
            System.out.println("2. Разгледай продукти от кампании с промоции и разпродажби");
            System.out.println("3. Поръчай продукт");
            System.out.println("0. Изход");
            System.out.print("Изберете опция: ");

            choice = Integer.parseInt(getMessage());

            switch (choice) {
                case 1:
                    browseAllProducts(customer);
                    break;
                case 2:
                    browsePromotionalProducts(customer);
                    break;
                case 3:
                    orderProduct(customer);
                    break;
                case 0:
                    System.out.println("Изход от менюто за клиенти.");
                    break;
                default:
                    System.out.println("Невалиден избор. Моля, опитайте отново.");
            }
        } while (choice != 0);

    }

    public void browseAllProducts(Customer customer) throws SQLException {
        connection=DatabaseManager.getConnection();
        String sql="select products.name,products.price,products.quantity from products";
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        while (resultSet.next()){
            sendMessage(resultSet.getString("name")+" "+String.valueOf(resultSet.getDouble("price"))+" "+String.valueOf(resultSet.getInt("quantity")));
//            sendMessage(String.valueOf(resultSet.getDouble("price")));
//            sendMessage(String.valueOf(resultSet.getInt("quantity")));
        }//sig trq da e do while ili nesh podobno kato quantityCheck
        sendMessage("done");
        connection.close();
        customerMenu(customer);
    }

    public void browsePromotionalProducts(Customer cu) throws SQLException {
        connection=DatabaseManager.getConnection();
        String sql="SELECT p.name AS product_name, s.new_price AS sale_price, p.quantity\n" +
                "FROM products p\n" +
                "JOIN sales s ON p.product_id = s.product_id\n" +
                "JOIN salesCampain sc ON s.campain_id = sc.campain_id\n" +
                "WHERE sc.isActive = 1\n";
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        while (resultSet.next()){
            sendMessage(resultSet.getString("product_name"+" "+String.valueOf(resultSet.getDouble("sale_price"))+" "+String.valueOf(resultSet.getInt("quantity"))));
//            System.out.println(resultSet.getDouble("sale_price"));
//            System.out.println(resultSet.getInt("quantity"));
        }//sig trq da e do while ili nesh podobno kato quantityCheck
        sendMessage("done");
        connection.close();
        customerMenu(cu);
    }

    public void orderProduct(Customer customer) throws SQLException {
        connection=DatabaseManager.getConnection();
        int product_id= Integer.parseInt(getMessage());
        int quantity= Integer.parseInt(getMessage());
        String cardNumber=getMessage();
        if (!CardValidator.validateCardNumber(cardNumber)){ //tuka s while sig trq
            System.out.println("invalid card number");
            connection.close();
            return;
        }

        String sql2="select quantity,price from products where product_id=?";
        PreparedStatement statement2 = connection.prepareStatement(sql2);
        statement2.setInt(1,product_id);
        ResultSet resultSet = statement2.executeQuery();

        double price = 0;

        if (resultSet.next()){
            if (resultSet.getInt("quantity")<quantity){
                System.out.println("not enough quantity");
                connection.close();
                return;
            }
            price=resultSet.getDouble("price");
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

        String sql="insert into purchases(product_id,quantity,purchase_price,user_id,purchaseDate) values(?,?,?,?,now())";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1,product_id);
        statement.setInt(2,quantity);
        statement.setDouble(3,finalPurchasePrice);
        statement.setInt(4,customer.getId());
        statement.executeUpdate();

        String sql3="update products set quantity=quantity-? where product_id=?";
        PreparedStatement statement3 = connection.prepareStatement(sql3);
        statement3.setInt(1,quantity);
        statement3.setInt(2,product_id);
        statement3.executeUpdate();
        connection.close();
        sendMessage("Order successful");
        customerMenu(customer);
    }
    public void adminMenu(/*Admin admin*/) throws SQLException {
        int choice= Integer.parseInt(getMessage());
        /*
        1-справка за оборота на магазина за определен период от време
        2-Добавяне, редактиране и изтриване на продукти, които се предлагат в магазина;
        3-стартиране и спиране на кампания с разпродажби. Намаленията могат да са само за част от продуктите, като се задава намаление на цените с конкретен процент. Всеки продукт има минимална цена, под която не може да бъде предлаган.
        4-Следене на наличности на продукти;(nqkoi produkt ako ima malko da izliza?)
         */

        switch (choice){
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
        }
    }
    public void makeAdmin() throws SQLException {
        String sql="update user set role='admin' where id=?";
        int id= Integer.parseInt(getMessage());
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1,id);
        int rowsAffected = statement.executeUpdate();
        if (rowsAffected > 0) {
            sendMessage("Admin created successfully.");
        } else {
            sendMessage("Failed to create admin.");
        }
        connection.close();
    }

    public void spravka() throws SQLException {
        //first=ot koga, last=do koga sig trq ima nqkva proverka tuka
        String first=getMessage();
        String second=getMessage();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate firstDate = LocalDate.parse(first, formatter);
        LocalDate secondDate = LocalDate.parse(second, formatter);
        if(secondDate.isBefore(firstDate)){
            LocalDate temp=firstDate;
            firstDate=secondDate;
            secondDate=temp;
        }
        first=firstDate.toString();
        second=secondDate.toString();


        Connection newconnection = DatabaseManager.getConnection();
        //String sql="SELECT SUM(p.price * pur.quantity) AS total_sales FROM purchases pur JOIN products p ON pur.product_id = p.product_id WHERE pur.purchaseDate >= ? AND pur.purchaseDate < ?";
        String sql="SELECT SUM(pur.purchase_price*pur.quantity) as total_sales from purchases pur where pur.purchaseDate >= ? AND pur.purchaseDate < ?";
        PreparedStatement statement = newconnection.prepareStatement(sql);
        statement.setString(1,first);
        statement.setString(2,second);
//        statement.setDate(1, Date.valueOf(firstDate));
//        statement.setDate(2, Date.valueOf(secondDate));

        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()){
            int totalSales = resultSet.getInt("total_sales");
            writer.println(totalSales);
        }else {
            writer.println(0);
        }
        newconnection.close();

        adminMenu();
    }


    public void redactMenu() throws SQLException {
        int choice= Integer.parseInt(getMessage());
        connection=DatabaseManager.getConnection();
        switch (choice){
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
    }
    public void addProduct(Connection connection) throws SQLException {
        System.out.println("name,price,quantity,minPrice");
        String sql="insert into products values(?,?,?,?,?)";
        String name=getMessage();
        double price= Double.parseDouble(getMessage());
        int quantity= Integer.parseInt(getMessage());
        double minPrice= Double.parseDouble(getMessage());

        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1,name);
        statement.setDouble(2,price);
        statement.setInt(3,quantity);
        statement.setDouble(4,minPrice);
        statement.setInt(5,1);
        int rowsAffected = statement.executeUpdate();
        if (rowsAffected > 0) {
            System.out.println("Product added successfully.");
        } else {
            System.out.println("Failed to add product.");
        }
    }

    public void redactProduct(Connection connection) throws SQLException {
        System.out.println("enter product id");
        int id= Integer.parseInt(getMessage());
        String sql="update products set name=?,price=?,quantity=?,minPrice=? where product_id=?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1,getMessage());
        statement.setDouble(2,Double.parseDouble(getMessage()));
        statement.setInt(3,Integer.parseInt(getMessage()));
        statement.setDouble(4,Double.parseDouble(getMessage()));
        statement.setInt(5,id);
        int rowsAffected = statement.executeUpdate();
        if (rowsAffected > 0) {
            System.out.println("Product updated successfully.");
        } else {
            System.out.println("Failed to update product.");
        }
    }
    public void removeProduct(Connection connection) throws SQLException {
        System.out.println("enter product id");
        int id= Integer.parseInt(getMessage());
        String sql="delete from products where product_id=?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1,id);
        int rowsAffected = statement.executeUpdate();
        if (rowsAffected > 0) {
            System.out.println("Product removed successfully.");
        } else {
            System.out.println("Failed to remove product.");
        }
    }
    public void quantityCheck() throws SQLException {
        connection=DatabaseManager.getConnection();
        int numberOfProducts= Integer.parseInt(getMessage());
        String sql="select id,quantity from products where quantity < ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1,numberOfProducts);
       // Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        while (resultSet.next()){
            writer.println(resultSet.getString("id")+" "+resultSet.getInt("quantity"));
        }
        connection.close();
        /*
         public void receiveMessages() {
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("Message from server: " + line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        }
        nesh takova trq da e v clienta
         */
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
                break;
        }
    }

    public void startSale() throws SQLException {
        // Implement logic to start a sales campaign
        // Prompt user for campaign details: name, products included, discount percentage, start date, end date
        // Perform necessary database operations to store campaign details

        Connection con = DatabaseManager.getConnection();
        int campaign_id = Integer.parseInt(getMessage());
        String sql = "update salesCampain set isActive=1 where campain_id=?";
        PreparedStatement statement = con.prepareStatement(sql);
        statement.setInt(1, campaign_id);
        int rowsAffected = statement.executeUpdate();
        if (rowsAffected > 0) {
            System.out.println("Campaign started successfully.");
        } else {
            System.out.println("Failed to start campaign.");
        }
        con.close();
        //? nz dali da e taka
    }

    public void stopSale() throws SQLException {
        // Implement logic to stop a sales campaign
        // Prompt user to select the campaign to stop
        // Update database to mark the campaign as stopped
        sendMessage("Select campaign to stop:");
        int campaign_id = Integer.parseInt(getMessage());
        Connection con = DatabaseManager.getConnection();
        String sql = "update salesCampain set isActive=0 where campain_id=?";
        PreparedStatement statement = con.prepareStatement(sql);
        statement.setInt(1, campaign_id);
        int rowsAffected = statement.executeUpdate();
        if (rowsAffected > 0) {
            System.out.println("Campaign stopped successfully.");
        } else {
            System.out.println("Failed to stop campaign.");
        }
        con.close();
        //? nz dali da e taka pak
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
    public double checkDiscount(int product_id,int discount,Connection connection1) throws SQLException {
        String sql="select price,minimalPrice from products where product_id=?";
        PreparedStatement statement = connection1.prepareStatement(sql);
        statement.setInt(1,product_id);
        double price;
        double minimalPrice;
        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()){
            price=resultSet.getDouble("price");
            minimalPrice=resultSet.getDouble("minimalPrice");
        }else {
            System.out.println("wrong product id");
            return -1;
        }
        double discount_price=price*(1- (double) discount /100);
        return !(discount_price < minimalPrice) ? discount_price : 0;
    }
    public static boolean checkCampain(int id,Connection connection) throws SQLException {
        String sql="select campain_id from salesCampain where campain_id=?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1,id);
        ResultSet resultSet = statement.executeQuery();
        return resultSet.next();
    }
    public static boolean checkProduct(int id,Connection connection) throws SQLException {
        String sql="select product_id from products where product_id=?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1,id);
        ResultSet resultSet = statement.executeQuery();
        return resultSet.next();
    }
    public void addProductToCampaign() throws SQLException {
        int campaign_id= Integer.parseInt(getMessage());
        int product_id= Integer.parseInt(getMessage());
        int discount_percentage= Integer.parseInt(getMessage());
        connection=DatabaseManager.getConnection();

        double discount_price = 0; // Initialize discount_price outside the loop

        while (true) {
            discount_price = checkDiscount(product_id, discount_percentage, connection);

            if (discount_price == 0) {
                sendMessage("Discount percentage is too high. Please enter a valid discount percentage:");
                discount_percentage = Integer.parseInt(getMessage());
            } else if (discount_price == -1) {
                sendMessage("Wrong product ID. Please enter a valid product ID:");
                product_id = Integer.parseInt(getMessage());
            } else if (checkCampain(campaign_id,connection)) {
                sendMessage("Wrong campaign ID. Please enter a valid campaign ID:");
                campaign_id = Integer.parseInt(getMessage());
            }else {
                sendMessage("success");
                break;
            }
        }



        String sql="insert into sales (campain_id,product_id,discount_percentage,new_price) values (?,?,?,?)";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1,campaign_id);
        statement.setInt(2,product_id);
        statement.setInt(3,discount_percentage);
        statement.setDouble(4,discount_price);
        int rowsAffected = statement.executeUpdate();
        if (rowsAffected > 0) {
            System.out.println("Product added to campaign successfully.");
        } else {
            System.out.println("Failed to add product to campaign.");
        }

        connection.close();
    }

    public void removeProductFromCampaign() throws SQLException {
        connection=DatabaseManager.getConnection();
        int campaign_id= Integer.parseInt(getMessage());
        int product_id= Integer.parseInt(getMessage());

        while (true){
            if (!checkCampain(campaign_id,connection)){
                sendMessage("Wrong campaign ID. Please enter a valid campaign ID:");
                campaign_id = Integer.parseInt(getMessage());
            }else if (!checkProduct(product_id,connection)){
                sendMessage("Wrong product ID. Please enter a valid product ID:");
                product_id = Integer.parseInt(getMessage());
            } else {
                sendMessage("success");
                break;
            }
        }



        String sql="delete from sales where campain_id=? and product_id=?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1,campaign_id);
        statement.setInt(2,product_id);
        int rowsAffected = statement.executeUpdate();
        if (rowsAffected > 0) {
            System.out.println("Product removed successfully.");
        } else {
            System.out.println("Failed to remove product.");
        }
        connection.close();
    }
    public void changeDiscountPercentage() throws SQLException {
        connection=DatabaseManager.getConnection();
        int campaign_id= Integer.parseInt(getMessage());
        int product_id= Integer.parseInt(getMessage());
        int discount_percentage= Integer.parseInt(getMessage());

//        double discount_price=checkDiscount(product_id,discount_percentage,connection);
//        if (discount_price==0) {
//            System.out.println("discount too high");
//            connection.close();
//            return;
//        }
//        if(discount_price==-1){
//            System.out.println("wrong product id");
//            connection.close();
//            return;
//        }
        double discount_price;
        while (true) {
            discount_price = checkDiscount(product_id, discount_percentage, connection);
            if (discount_price == 0) {
                sendMessage("Discount percentage is too high. Please enter a valid discount percentage:");
                discount_percentage = Integer.parseInt(getMessage());
            } else if (discount_price == -1) {
                sendMessage("Wrong product ID. Please enter a valid product ID:");
                product_id = Integer.parseInt(getMessage());
            } else if ((checkCampain(campaign_id,connection))) {
                sendMessage("Wrong campaign ID. Please enter a valid campaign ID:");
                campaign_id = Integer.parseInt(getMessage());
            }
            else{
                sendMessage("success");
                break;
            }
        }

        String sql="update sales set discount_percentage=?,new_price=? where campain_id=? and product_id=?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1,discount_percentage);
        statement.setDouble(2,discount_price);
        statement.setInt(3,campaign_id);
        statement.setInt(4,product_id);
        int rowsAffected = statement.executeUpdate();
        if (rowsAffected > 0) {
            System.out.println("Discount percentage updated successfully.");
        } else {
            System.out.println("Failed to update discount percentage.");
        }
        connection.close();
    }
    public void adjustStartDate() throws SQLException {
        connection=DatabaseManager.getConnection();
        int campaign_id= Integer.parseInt(getMessage());
        String start_date= getMessage();

        while (true) {
            if (!checkCampain(campaign_id,connection)){
                sendMessage("Wrong campaign ID. Please enter a valid campaign ID:");
                campaign_id = Integer.parseInt(getMessage());
            }else{
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
        if(start_date.compareTo(endDate) < 0) {
            String sql="update salesCampain set start_date=? where campain_id=?";
            PreparedStatement statement1 = connection.prepareStatement(sql);
            statement1.setString(1,start_date);
            statement1.setInt(2,campaign_id);
            int rowsAffected = statement1.executeUpdate();
            if (rowsAffected > 0) {
                sendMessage("Start date updated successfully.");
            } else {
                sendMessage("Failed to update start date.");
            }
        }else{
            sendMessage("Start date must be before end date");
            connection.close();
            return;
        }

//        String sql="update salesCampain set start_date=? where campain_id=?";
//        PreparedStatement statement = connection.prepareStatement(sql);
//        statement.setString(1,start_date);
//        statement.setInt(2,campaign_id);
//        int rowsAffected = statement.executeUpdate();
//        if (rowsAffected > 0) {
//            System.out.println("Start date updated successfully.");
//        } else {
//            System.out.println("Failed to update start date.");
//        }
//        connection.close();
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
            String sql = "update salesCampain set end_date=? where campain_id=?";
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
        }


//        String sql="update salesCampain set end_date=? where campain_id=?";
//        PreparedStatement statement = connection.prepareStatement(sql);
//        statement.setString(1,end_date);
//        statement.setInt(2,campaign_id);
//        int rowsAffected = statement.executeUpdate();
//        if (rowsAffected > 0) {
//            System.out.println("End date updated successfully.");
//        } else {
//            System.out.println("Failed to update end date.");
//        }
//        connection.close();

    }

}