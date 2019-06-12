package work_with_collection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import story.Jail;
import story.JailComparator;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

public class ConcurrentCollectionManager extends CollectionManager {
    private ConcurrentLinkedDeque<Jail> jailConcurrentStack;

    /**
     *Конструктор в входными параметрами.
     * @param collectionFile - файл, из которого будет считываться коллекция.
     * @throws FileNotFoundException Ошибка пояаляется, когда файл с коллекцией не был найден.
     * @see FileManager
     * @see Date
     */
    public ConcurrentCollectionManager(File collectionFile) throws FileNotFoundException {
        fileWorker = new FileManager(collectionFile);
        while (fileWorker.readFile().equals("")){
            System.out.println("Данных для чтения в файле нет. Введите имя файла.");
            fileWorker = new FileManager(new File(new Scanner(System.in).next()));
        }
        while (!checkSource(fileWorker.readFile())){
            System.out.println("Невозможно прочитать коллекцию из файла. Введите имя файла.");
            fileWorker = new FileManager(new File(new Scanner(System.in).next()));
        }
        this.initDate = new Date();
        jailConcurrentStack = createConcurrentStack(fileWorker.readFile());
    }

    /**
     * Конструктор без аргументов.
     */
    public ConcurrentCollectionManager() {
        jailConcurrentStack = new ConcurrentLinkedDeque<>();
        initDate = new Date();
    }

    public String add(Jail jailForAdding){
        if (jailForAdding != null) {
            if (!jailConcurrentStack.contains(jailForAdding)) {
                jailConcurrentStack.add(jailForAdding);
                if (jailConcurrentStack.size()>1) {
                    sort();
                }
            } else {
                return "Элемент " + jailForAdding + " уже содержится в коллекции.";
            }
            if (!(jailConcurrentStack.isEmpty())) {
                return "Элемент " + jailForAdding + " добавлен.";
            } else {
                return "Коллекция пуста.";
            }
        } else{
            return "Элемент задан неверно.";
        }
    }
    
    
    
    @Override
    public String info() {
        return "\nКоллекция имеет тип \"ConcurrentArrayDeque\" и содержит объекты класса \"story.Jail\".\n" +
                "Дата инициализации: " + initDate +
                "\nКолличество элементов в коллекции - " + jailConcurrentStack.size() + ".";
    }

    @Override
    public String show(){
        if (jailConcurrentStack.isEmpty()){
            return "Коллекция пуста.";
        } else {
            return new GsonBuilder().setPrettyPrinting().create().
                    toJson(jailConcurrentStack);
        }
    }

    /**
     * Метод добавляет в коллекцию элементы.
     * @param jailsForPushing - элементы коллекции, которые надо добавить в текущую коллекцию.
     * @see ConcurrentLinkedDeque
     */
    public String doImport(Stack<Jail> jailsForPushing) {
        if (jailsForPushing != null) {
            if (!jailsForPushing.isEmpty()) {
                jailConcurrentStack.addAll(jailsForPushing);
                sort();
                return "Элементы добавлены.";
            }
            return "Ничего не добавлено: импортируемая коллекция пуста.";
        } else{
            return "Элемент задан неверно.";
        }
    }

    public String insert(String place, Jail jailForInserting) {
        try {
            if (jailForInserting == null) {
                return "Элемент задан неверно.";
            }
            StringBuilder stringBuilder = new StringBuilder();
            int index = Integer.parseInt(place);
            Stack<Jail> stack2 = new Stack<>();
            if ((index < jailConcurrentStack.size()) && (index >= 0)) {
                for (int i = 0; i < index; i++) {
                    stack2.add(jailConcurrentStack.pop());
                }
                jailConcurrentStack.add(jailForInserting);
                stringBuilder.append(jailConcurrentStack.peek()).append(" вставлен на место ").append(index);
                jailConcurrentStack.addAll(stack2);
                return stringBuilder.toString();
            } else return "Индекс не входит в допустимые пределы.";
        } catch (NumberFormatException e) {
            return "Индекс не число.";
        }
    }

    public String remove(Jail jailForRemoving) {
        if (jailForRemoving == null) {
            return "Элемент для удаления задан неверно.";
        }
        try {
            if (jailConcurrentStack.contains(jailForRemoving)) {
                jailConcurrentStack.stream().filter(t -> t.equals(jailForRemoving)).forEach(t -> jailConcurrentStack.remove(t));
                return "Элемент " + jailForRemoving + " удален.";
            } else {
                return "Такого элемента в коллекции нет.";
            }
        } catch (NullPointerException e) {
            return "Из коллекции невозможно удалить элемент.";
        }
    }

    @Override
    public void load(String fileName) throws FileNotFoundException, NullPointerException {
        jailConcurrentStack = createConcurrentStack(new FileManager(new File(fileName)).readFile());
        jailConcurrentStack.stream().filter(t -> t.getName()==null).forEach(t -> t.setName("NoName"));
        sort();
    }

    public void load() throws FileNotFoundException, NullPointerException {
        jailConcurrentStack = createConcurrentStack(fileWorker.readFile());
        sort();
    }

    @Override
    public String save() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try{
            fileWorker.writeToFile(new GsonBuilder().setPrettyPrinting().create().toJson(jailConcurrentStack));
        } catch (FileNotFoundException | NullPointerException e){
            stringBuilder.append("Файл для сохранения коллекции не найден. Будет создан новый файл.");
            File newFile = new File("saveFile" + new SimpleDateFormat("yyyy.MM.dd.hh.mm.ss").format(new Date()) + ".txt");
            if (newFile.createNewFile()) {
                new FileManager(newFile).writeToFile(new GsonBuilder().setPrettyPrinting().create().toJson(jailConcurrentStack));
                stringBuilder.append("Коллекция сохранена в файл ").append(newFile.getAbsolutePath());
            } else {
                throw new FileNotFoundException();
            }
            return stringBuilder.toString();
        }
        return "Коллекция сохранена в исходный файл.";
    }

    @Override
    public void finishWork() {
        try{
            fileWorker.writeToFile(new GsonBuilder().setPrettyPrinting().create().toJson(jailConcurrentStack));
        } catch (IOException e) {
            try {
                save();
            }catch (IOException ex){
                System.out.println("Сохранение коллекции не удалось.");
            }
        }
    }

    private static ConcurrentLinkedDeque<Jail> createConcurrentStack(String str) throws JsonSyntaxException {
        Gson gson = new Gson();
        Type type = new TypeToken<ConcurrentLinkedDeque<Jail>>(){}.getType();
        ConcurrentLinkedDeque<Jail> newStack = new ConcurrentLinkedDeque<>(gson.fromJson(str, type));
        for (Jail jail:newStack){
             newStack.stream().filter(t -> t.equals(jail)&&(t!=jail)).forEach(newStack::remove);
        }
        return newStack;
    }

    public static boolean checkSource(String source) {
        try {
            createConcurrentStack(source);
            return true;
        } catch (JsonSyntaxException e) {
            System.out.println("Содержимое файла не удовлетворяет формату JSON.");
            return false;
        }
    }
    
    private void sort() {
        jailConcurrentStack = jailConcurrentStack.stream().sorted(new JailComparator()).
                collect(Collectors.toCollection(ConcurrentLinkedDeque::new));
    }

}
