package Servlet;

import work_with_collection.ConcurrentCollectionManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.channels.ClosedByInterruptException;
import java.util.LinkedList;

public class Server {
    private ConcurrentCollectionManager manager;
    private ServerSocket serverSocket;
    private static int port = 8080;
    private static LinkedList<ServerOperator> serverList = new LinkedList<>(); // список всех нитей - экземпляров сервера, слушающих каждый своего клиента

    private Server(int i, File file) throws FileNotFoundException {
        manager = new ConcurrentCollectionManager(file);
        port = i;
    }
    private Server(int i) {
        this();
        port = i;
    }
    private Server(File file) throws FileNotFoundException {
        manager = new ConcurrentCollectionManager(file);
    }
    private Server() {
        manager = new ConcurrentCollectionManager();
    }

    static LinkedList<ServerOperator> getServerList() {
        return serverList;
    }
    public static void main(String[] args) throws IOException {

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Завершаем работу.");
        }));

        Server server;
        if (args.length > 1){
            try{
                server = new Server(Integer.parseInt(args[1]), new File(args[0]));
            } catch (NumberFormatException e){
                System.out.println("Номер порта задан неверно");
                server = new Server(new File(args[0]));
            }
        } else
        if (args.length == 1) {
            try{
                server = new Server(Integer.parseInt(args[0]));
            }catch (NumberFormatException e){
                server = new Server(new File(args[0]));}
        } else server = new Server();


        System.out.println("Попытка запустить сервер...");
        try {
            server.serverSocket = new ServerSocket();
            server.serverSocket.bind(new InetSocketAddress(port));
            System.out.println("Порт: "+server.serverSocket.getLocalPort());
        } catch (IOException e) {
            System.out.println("Что-то пошло не так.");
            System.exit(-1);
        }

        System.out.println("Сервер запущен");
        while (!server.serverSocket.isClosed()) {
            Socket client = server.waitConnection();
            if (client == null) break;
            ServerOperator serverOperator = new ServerOperator(client, server.manager);
            serverList.add(serverOperator);
            serverOperator.start();
        }
        server.serverSocket.close();
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

//    private void exit(){
//        boolean needExit = false;
//        while (!needExit){
//            String scannerCommand = new Scanner(System.in).next();
//            if (scannerCommand.equals("show")){
//                serverList.forEach(System.out::println);
//            }
//            needExit = scannerCommand.equals("exit");
//        }
//        try {
//            this.serverSocket.close();
//        } catch (IOException ignored) {}
//    }
}
