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

    public ServerThread(Socket socket,Connection connection) {
        this.socket = socket;
        this.connection = connection;
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
            loginL();
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
        String password=parts[1]; //hash?
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
        if (role.equals("user"))
            customerMenu(new Customer(id,username));
        else adminMenu(new Admin(id,username));
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
                //saleMenu();
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
        String sql="SELECT SUM(p.price * pur.quantity) AS total_sales FROM purchases pur JOIN products p ON pur.product_id = p.product_id WHERE pur.purchaseDate >= ? AND pur.purchaseDate < ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1,first);
        statement.setString(2,second);
        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()){
            System.out.println(resultSet.getInt("total_sales"));
        }else {
            System.out.println("no sales");
        }
    }

    public void redactMenu() throws SQLException {
        int choice= Integer.parseInt(getMessage());
        switch (choice){
            case 1:
                addProduct();
                break;
            case 2:
                redactProduct();
                break;
            case 3:
                removeProduct();
                break;
            case 4:
                break;
        }
    }
    public void addProduct() throws SQLException {
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

    public void redactProduct() throws SQLException {
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
    public void removeProduct() throws SQLException {
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
        String sql="select id,quantity from products where quantity < 10";
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        while (resultSet.next()){
            System.out.println(resultSet.getString("id")+" "+resultSet.getInt("quantity"));
        }
    }
}