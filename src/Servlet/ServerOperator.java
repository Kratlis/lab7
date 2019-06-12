package Servlet;

import story.Jail;
import work_with_collection.CommandReaderAndExecutor;
import work_with_collection.ConcurrentCollectionManager;

import javax.management.remote.rmi.RMIConnectionImpl;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

class ServerOperator extends Thread {
    private int id;
    private static int all = 0;
    private Socket socket; // сокет, через который сервер общается с клиентом,
    private ObjectInputStream reader;
    private ObjectOutputStream writer;
    private InputStream inputStream;
    private OutputStream outputStream;
    private ConcurrentCollectionManager manager;
    boolean g = true;
    
    ServerOperator(Socket socket, ConcurrentCollectionManager concurrentCollectionManager){
        this.id = ++all;
        this.socket = socket;
        manager = concurrentCollectionManager;
        try {
            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();
            writer = new ObjectOutputStream(outputStream);
            System.out.println("Клиент " + id + " подключился к серверу");
        } catch (IOException e) {
            System.out.println("Поток ввода не получен");
            System.exit(0);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                finishOperator();
            } catch (NullPointerException e) {
                System.out.println("Это конец.");
            }
        }));
        
    }

    @Override
    public void run() {
        while (!socket.isClosed()){
            System.out.println("Socket is open...");
            if (!g) {
                continue;
            }
            try {
                sendMessage("\nВведите команду");
                String message = readMessage();
                String command = message.split(" ", 2)[0];
                System.out.println("Получено: "+message);
                switch (command) {
                    case "add":
                        sendMessage(manager.add(readJail()));
                        break;
                        
                    case "help":
                        sendMessage(CommandReaderAndExecutor.getInfoForHelp());
                        break;
                    case "exit":
                        sendMessage("Соединение разорвано.");
                        System.out.println("Клиент "+id+" отключился.");
                        --all;
                        finishOperator();
                        break;
                    case "info":
                        sendMessage(manager.info());
                        break;
                    case "import":
                        sendMessage(manager.doImport(readStack()));
                        break;
                    case "insert":
                        sendMessage(manager.insert(readMessage(), readJail()));
                        break;
                    case "load":
                        try {
                            if (readMessage().equals("this")) {
                                manager.load();
                                sendMessage("Коллекция перезаписана.");
                                break;
                            }
                            manager.load(readMessage());
                            sendMessage("Коллекция перезаписана.");
                        } catch (FileNotFoundException e){
                            sendMessage("Перезаписать коллекцию не удалось - файл не найден.");
                        } catch (NullPointerException e){
                            sendMessage("Перезаписать коллекцию не удалось - пусто.");
                        }
                        break;
                    case "remove":
                        sendMessage(manager.remove(readJail()));
                        break;
                    case "show":
                        sendMessage(manager.show());
                        break;
                    case "save":
                        sendMessage(manager.save());
                        break;
                    case "":
                        sendMessage("Команда записана неверно.");
                        break;
                    default:
                        sendMessage("Такой команды нет");
                        break;
                }
            } catch (IOException e) {
                try {
//                    System.out.println(e.getMessage());
                    sendMessage("Клиент не передал команду.");
                } catch (IOException e1) {
                    System.out.println("Клиент "+id+" отключился.");
                    --all;
                }
                finishOperator();
                break;
            } catch (ClassNotFoundException e) {
                System.out.println("Не удалось получить объект");
            }
        }
    }

    private String readMessage() throws IOException, ClassNotFoundException {
        reader = new ObjectInputStream(inputStream);
        String msg =(String)reader.readObject();
        return msg;
    }
    private Jail readJail() throws IOException, ClassNotFoundException {
        reader = new ObjectInputStream(inputStream);
        Jail jail = (Jail)reader.readObject();
        System.out.println("Jail: "+jail);
        return jail;
    }
    private Stack<Jail> readStack() throws IOException, ClassNotFoundException {
        reader = new ObjectInputStream(inputStream);
        return (Stack<Jail>) reader.readObject();
    }

    private void sendMessage(String msg) throws IOException {
        //            if (!readMessage().equals("Ready")){
//                return;
//            }
        g = false;
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("Отправляем: " + msg);
                try {
                    writer.writeObject("!"+msg);
                    writer.flush();

                } catch (IOException e) {
                    System.out.println("Соединение прервано.");
                    try {
//                    System.out.println(e.getMessage());
                        sendMessage("Клиент не передал команду.");
                    } catch (IOException e1) {
                        System.out.println("Клиент "+id+" отключился.");
                        --all;
                    }
                    finishOperator();
                }
                g = true;
            }
        },20);
//            System.out.println("writing: " + msg);
//            writer.writeObject("!"+msg);
//            writer.flush();
//            Thread.currentThread().sleep(100);
//            outputStream.write(msg.getBytes());
    }

    private void finishOperator() {
        try {
            if(!socket.isClosed()) {
                reader.close();
                writer.close();
                socket.close();
                LinkedList<ServerOperator> serverList = Server.getServerList();
                for (ServerOperator serverOperator: serverList){
                    serverOperator.finishOperator();
                }
            }
        } catch (IOException | NullPointerException ignored) {}
    }

}
