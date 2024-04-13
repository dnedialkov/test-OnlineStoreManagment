import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static Socket socket;
    public static Scanner scanner;
    public static Scanner reader;
    public static PrintStream writer;

    public static void main(String[] args) throws IOException {
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

    public static void runLogic() {
        System.out.println("Connected");
        String message = scanner.nextLine();
        while (!message.equals("bye")) {
            sendMessage(message);
            getMessage();
            message = scanner.nextLine();
        }
        System.out.println("Disconnected");
    }
}