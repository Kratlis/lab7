package Servlet;

import story.Jail;
import work_with_collection.CommandReaderAndExecutor;
import work_with_collection.ConcurrentCollectionManager;

import java.io.*;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Stack;

class ServerOperator extends Thread {
    private int id;
    private static int all = 0;
    private Socket socket; // сокет, через который сервер общается с клиентом,
    private BufferedReader in; // поток чтения из сокета
    private BufferedWriter out; // поток записи в сокет
    private ObjectInputStream reader;
    private ObjectOutputStream writer;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private ConcurrentCollectionManager manager;

    ServerOperator(Socket socket, ConcurrentCollectionManager concurrentCollectionManager){
        this.id = ++all;
        this.socket = socket;
        manager = concurrentCollectionManager;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            writer = new ObjectOutputStream(socket.getOutputStream());
            reader = new ObjectInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataInputStream=new DataInputStream(socket.getInputStream());
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
            try {
                sendMessage("Введите команду");
                System.out.println("Sended message");
                String message = readMessage();
                System.out.println(message);
                String command = message.split(" ", 2)[0];
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
                    sendMessage("Клиент не передал команду.");
                } catch (IOException e1) {
                    System.out.println("Клиент "+id+" отключился.");
                    --all;
                }
                finishOperator();
                break;
            } catch (ClassNotFoundException e) {
                System.out.println("Class in ServerOperator.add");
            }
        }
    }

    private String readMessage() throws IOException {
        return dataInputStream.readUTF();
    }
    private Jail readJail() throws IOException, ClassNotFoundException {
        return (Jail)reader.readObject();
    }
    private Stack<Jail> readStack() throws IOException, ClassNotFoundException {
            return (Stack<Jail>) reader.readObject();
    }

    private void sendMessage(String msg) throws IOException {
        try {
            dataOutputStream.writeUTF(msg);
        } catch (IOException e) {
            System.out.println("Установить связь с клиентом "+id+" не удалось.");
            throw new IOException();
        }
    }

    private void finishOperator() {
        try {
            if(!socket.isClosed()) {
                dataInputStream.close();
                dataOutputStream.close();
                reader.close();
                writer.close();
                out.close();
                in.close();
                socket.close();
                LinkedList<ServerOperator> serverList = Server.getServerList();
            }
        } catch (IOException ignored) {}
    }

}
