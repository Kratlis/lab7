package ServerPart;

import work_with_collection.ConcurrentCollectionManager;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.channels.ClosedByInterruptException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;

public class Server {
    private ConcurrentCollectionManager manager;
    private ServerSocket serverSocket;
    private static int port = 8880;
    private static LinkedList<ServerOperator> serverList = new LinkedList<>(); // список всех нитей - экземпляров сервера, слушающих каждый своего клиента
    private Connection connection;
    
    private Server(int i) {
        this();
        port = i;
    }
    private Server() {
        manager = new ConcurrentCollectionManager();
    }

    static LinkedList<ServerOperator> getServerList() {
        return serverList;
    }

    public static void main(String[] args) throws IOException {

        Runtime.getRuntime().addShutdownHook(new Thread(() -> System.out.println("Завершаем работу.")));

        Server server;
            try {
                server = new Server(Integer.parseInt(args[0]));
            } catch (NumberFormatException e) {
                server = new Server();
            }

        System.out.println("Попытка запустить сервер...");
        DataBaseConnection dataBaseConnection = new DataBaseConnection();
        if (dataBaseConnection.connect()) {
            server.manager.setConnectionDB(dataBaseConnection);
            try {
                server.manager = new CollectionReader(dataBaseConnection.getConnection()).createCollection(server.manager);
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Не удалось считать коллекцию");
            }
            try {
                server.serverSocket = new ServerSocket();
                server.serverSocket.bind(new InetSocketAddress(port));
                System.out.println("Порт: " + server.serverSocket.getLocalPort());
            } catch (IOException e) {
                System.out.println("Не получилось создать ServerSocket.");
                System.exit(-1);
            }
    
            System.out.println("Сервер запущен");
            while (!server.serverSocket.isClosed()) {
                Socket client = server.waitConnection();
                if (client == null) {
                    break;
                }
                ServerOperator serverOperator = new ServerOperator(client, server.manager, dataBaseConnection);
                serverOperator.start();
            }
            server.serverSocket.close();
        }
    }

    private Socket waitConnection() {
        try {
            Socket client;
            if (!serverSocket.isClosed()) {
                System.out.println("Ждём подключения");
            }
            client = serverSocket.accept();
            System.out.println("Произошло подключение.");
            return client;
        } catch (ClosedByInterruptException e) {
            System.out.println("Сервер выключается");
            try {
                serverSocket.close();
                serverList.forEach(ServerOperator::interrupt);
            } catch (IOException ex) {
                System.out.println("Не удалось закрыть сетевой канал.");
            }
            return null;
        } catch (SocketException e){
            System.out.println("Завершаем работу.");
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Что-то не так");
            return null;
        }
    }

}
