import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    public static void main(String[] args) throws IOException {
        String url = "jdbc:mysql://localhost:3306/storedb";
        String username = "root";
        String password = "root";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(url, username, password);

            ServerSocket serverSocket = new ServerSocket(1312);

            while (true){
                Socket socket=serverSocket.accept();
                new Thread(new ServerThread(socket,connection)).start();
            }
        }catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }
}
