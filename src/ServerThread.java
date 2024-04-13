import org.apache.commons.dbcp2.BasicDataSource;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.sql.*;
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
        String sql="select id,role from user where username=? and passwordH=?";
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
            System.out.println("wrong username or password");
            return;
        }
        connection.close();
        if (role.equals("user"))
            customerMenu(new Customer(id,username));
        else adminMenu(new Admin(id,username));
    }
    public void registerU() throws SQLException {
        String sql="insert into user(username,passwordH,role) values(?,?,?)";
        String message=getMessage();
        String[] parts=message.split(":");
        String username=parts[0];
        String password=parts[1]; //should be hashed client side and we receive the hash only
        String role=parts[2];
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

    public void customerMenu(Customer customer){

    }
    public void adminMenu(Admin admin) throws SQLException {
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
                break;
        }
    }

    public void spravka() throws SQLException {
        //first=ot koga, last=do koga sig trq ima nqkva proverka tuka
        String first=getMessage();
        String second=getMessage();
        Connection newconnection = DatabaseManager.getConnection();
        String sql="SELECT SUM(p.price * pur.quantity) AS total_sales FROM purchases pur JOIN products p ON pur.product_id = p.product_id WHERE pur.purchaseDate >= ? AND pur.purchaseDate < ?";
        PreparedStatement statement = newconnection.prepareStatement(sql);
        statement.setString(1,first);
        statement.setString(2,second);
        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()){
            System.out.println(resultSet.getInt("total_sales"));
        }else {
            System.out.println("no sales");
        }
        newconnection.close();
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
        System.out.println(/*"product_id_,*/"name,price,quantity,minPrice");
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
        String sql="select id,quantity from products where quantity < 10";
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        while (resultSet.next()){
            System.out.println(resultSet.getString("id")+" "+resultSet.getInt("quantity"));
        }
        connection.close();
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
    }

    public void stopSale() throws SQLException {
        // Implement logic to stop a sales campaign
        // Prompt user to select the campaign to stop
        // Update database to mark the campaign as stopped
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
    public void addProductToCampaign() throws SQLException {
        int campaign_id= Integer.parseInt(getMessage());
        int product_id= Integer.parseInt(getMessage());
        int discount_percentage= Integer.parseInt(getMessage());
        connection=DatabaseManager.getConnection();

        double discount_price=checkDiscount(product_id,discount_percentage,connection);
        if (discount_price==0) {
            System.out.println("discount too high");
            connection.close();
            return;
        }
        if(discount_price==-1){
            System.out.println("wrong product id");
            connection.close();
            return;
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

        double discount_price=checkDiscount(product_id,discount_percentage,connection);
        if (discount_price==0) {
            System.out.println("discount too high");
            connection.close();
            return;
        }
        if(discount_price==-1){
            System.out.println("wrong product id");
            connection.close();
            return;
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
        String sql="update salesCampain set start_date=? where campain_id=?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1,start_date);
        statement.setInt(2,campaign_id);
        int rowsAffected = statement.executeUpdate();
        if (rowsAffected > 0) {
            System.out.println("Start date updated successfully.");
        } else {
            System.out.println("Failed to update start date.");
        }
        connection.close();
    }
    public void adjustEndDate() throws SQLException {
        connection=DatabaseManager.getConnection();
        int campaign_id= Integer.parseInt(getMessage());
        String end_date= getMessage();
        String sql="update salesCampain set end_date=? where campain_id=?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1,end_date);
        statement.setInt(2,campaign_id);
        int rowsAffected = statement.executeUpdate();
        if (rowsAffected > 0) {
            System.out.println("End date updated successfully.");
        } else {
            System.out.println("Failed to update end date.");
        }
        connection.close();
    }


}