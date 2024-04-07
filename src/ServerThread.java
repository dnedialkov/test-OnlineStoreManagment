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

    private Connection connection;

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
        String password=parts[1];
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
    public void adminMenu(Admin admin){
        int choice= Integer.parseInt(getMessage());
        /*
        1-справка за оборота на магазина за определен период от време
        2-Добавяне, редактиране и изтриване на продукти, които се предлагат в магазина;
        3-стартиране и спиране на кампания с разпродажби. Намаленията могат да са само за част от продуктите, като се задава намаление на цените с конкретен процент. Всеки продукт има минимална цена, под която не може да бъде предлаган.
        4-Следене на наличности на продукти;(nqkoi produkt ako ima malko da izliza?)
         */

        switch (choice){
            case 1:
                //spravka();
            case 2:
                //redactMenu();
            case 3:
                //saleMenu();
            case 4:
                //quantityCheck();
        }
    }
}