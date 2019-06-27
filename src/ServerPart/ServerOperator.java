package ServerPart;

import com.sun.deploy.association.RegisterFailedException;
import story.Jail;
import work_with_collection.CommandReaderAndExecutor;
import work_with_collection.ConcurrentCollectionManager;

import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    private DataBaseConnection connectionDB;
    private String login;
    boolean g = true;
    boolean in = false;
    
    ServerOperator(Socket socket, ConcurrentCollectionManager concurrentCollectionManager, DataBaseConnection con){
        this.id = ++all;
        this.socket = socket;
        manager = concurrentCollectionManager;
        connectionDB = con;
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
                sendMessage("Введите \"enter\", чтобы подключиться к базе данных. \n" +
                       "Если вы еще не зарегистрированы, введите \"reg\"");
                String message = readMessage();
                if (message.equals("enter")) {
                    enter();
                } else {
                    if (message.equals("reg")) {
                        register();
                    } else {
                        sendMessage("unknown command");
                        continue;
                    }
                }
                if (!in) continue;
                while (!socket.isClosed()) {
                    sendMessage("\nВведите команду");
                    login = readMessage();
                    String password = readMessage();
                    message = readMessage();
                    String command = message.split(" ", 2)[0];
                    System.out.println("Получено: " + message);
                    switch (command) {
                        case "add":
                            sendMessage(manager.add(readJail(), login));
                            break;
                        case "help":
                            sendMessage(CommandReaderAndExecutor.getInfoForHelp());
                            break;
                        case "exit":
                            sendMessage("Соединение разорвано.");
                            System.out.println("Клиент " + id + " отключился.");
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
                            } catch (FileNotFoundException e) {
                                sendMessage("Перезаписать коллекцию не удалось - файл не найден.");
                            } catch (NullPointerException e) {
                                sendMessage("Перезаписать коллекцию не удалось - пусто.");
                            }
                            break;
                        case "remove":
                            sendMessage(manager.remove(readJail(), login));
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
                }
            } catch (IOException e) {
                try {
                    System.out.println(e.getMessage());
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e1
                    ) {}
                    sendMessage("Клиент не передал команду.");
                } catch (IOException e1) {
                    System.out.println("Клиент "+id+" отключился.");
                    --all;
                }
                finishOperator();
                break;
            } catch (ClassNotFoundException e) {
                System.out.println("Не удалось получить объект");
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (RegisterFailedException ignored) {}
        }
    }
    
    private void register() throws IOException, ClassNotFoundException, SQLException, RegisterFailedException {
        sendMessage("Введите логин");
        login = readMessage();
        if (existLogin(login)) {
            sendMessage("Логин занят");
            throw new RegisterFailedException();
        }
        sendMessage("Введите адрес электронной почты");
        String email = readMessage();
        if (existMail(email)){
            sendMessage("Пользователь с такой почтой уже существует");
            throw new RegisterFailedException();
        }
        String password = new JavaMail().registration(email);
        sendMessage("Пароль был отправлен Вам на почту. Введите его.");
        String pswd = readMessage();
        if (!pswd.equals(password)){
            sendMessage("Пароли не совпадают. Попробуйте ещё раз ввести пароль.");
            pswd = readMessage();
            if (!pswd.equals(password)){
                sendMessage("Вы ввели неверный пароль. Регистрация отменена.");
                throw new RegisterFailedException();
            }
        }
        insertUser(login, password, email);
        sendMessage("Вы успешно зарегестрировались.");
        in = true;
    }
    
    private void insertUser(String login, String password, String email) throws SQLException {
        String insertUser = "insert into users(name, password, mail) values (?, ?, ?)";
        PreparedStatement ps = connectionDB.getConnection().prepareStatement(insertUser);
        ps.setString(1, login);
        ps.setString(2, encryptThisString(password));
        ps.setString(3, email);
        ps.executeUpdate();
        ps.close();
    }
    public static String encryptThisString(String input)
    {
        //TODO: know it
        try {
            // getInstance() method is called with algorithm SHA-384
            MessageDigest md = MessageDigest.getInstance("SHA-384");
            
            // digest() method is called
            // to calculate message digest of the input string
            // returned as array of byte
            byte[] messageDigest = md.digest(input.getBytes());
            
            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);
            
            // Convert message digest into hex value
            String hashtext = no.toString(16);
            
            // Add preceding 0s to make it 32 bit
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            
            // return the HashText
            return hashtext;
        }
        
        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    
    private boolean existMail(String email) throws SQLException {
        if (email == null){
            finishOperator();
            return false;
        }
        ResultSet resultSet = connectionDB.getConnection().createStatement().executeQuery("select mail from users");
        while (resultSet.next()){
            if (resultSet.getString("mail").equals(email)){
                return true;
            }
        }
        return false;
    }
    
    private void enter() throws IOException, ClassNotFoundException, SQLException, RegisterFailedException {
        sendMessage("Введите логин");
        login = readMessage();
        if (!checkLogin(login)) {
            return;
        }
        sendMessage("Введите пароль");
        String password = readMessage();
        if (!checkPassword(login, password)){
            sendMessage("Пароль неверен");
            throw new RegisterFailedException();
        }
        sendMessage("Добро пожаловать, "+login);
        System.out.println(login+" is here");
        Server.getServerList().add(this);
        in = true;
    }
    
    private boolean checkPassword(String login, String password) throws SQLException {
        if (password == null){
            System.out.println("NULL");
            finishOperator();
            return false;
        }
        ResultSet resultSet = connectionDB.getConnection().createStatement().executeQuery("select name, password from users");
        while (resultSet.next()){
            if (resultSet.getString("name").equals(login) &&
            resultSet.getString("password").equals(password)){
                return true;
            }
        }
        return false;
    }
    
    private boolean checkLogin(String login) throws SQLException, IOException {
        if (login == null) {
            System.out.println("NULL");
            finishOperator();
            return false;
        }
        if (!existLogin(login)) {
            sendMessage("Вы еще не зарегестрированы");
            return false;
        }
        for (ServerOperator so: Server.getServerList()){
            if (so.equals(login)){
                sendMessage("Вы уже авторизованы");
                return false;
            }
        }
        return true;
    }
    
    private boolean existLogin(String login) throws SQLException {
        ResultSet resultSet = connectionDB.getConnection().createStatement().executeQuery("select name from users");
        while (resultSet.next()){
            String log = resultSet.getString("name");
            if (log.equals(login)){
                return true;
            }
        }
        return false;
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
        g = false;
        System.out.println("Отправляем: " + msg);
        writer.writeObject("!" + msg);
        writer.flush();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        g = true;
    }

    private void finishOperator() {
        try {
            if(!socket.isClosed()) {
                reader.close();
                writer.close();
                socket.close();
                Server.getServerList().remove(this);
                in = false;
            }
        } catch (IOException | NullPointerException ignored) {}
    }

}
