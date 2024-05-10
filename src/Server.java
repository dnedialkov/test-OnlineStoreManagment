import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;


public class Server {

    public static void main(String[] args) throws IOException {
        try {
            ServerSocket serverSocket = new ServerSocket(1312);

            while (!serverSocket.isClosed()) {
                Socket socket=serverSocket.accept();
                Connection connection = DatabaseManager.getConnection();
                new Thread(new ServerThread(socket,connection)).start();
            }
        }catch ( IOException|SQLException e ){
            e.printStackTrace();
        }
    }
}
