package sync;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Alexey on 03.05.2015.
 */
public class SyncFolder implements Runnable {
    public static Set<FileInfo> a = new HashSet<>(); //коллекции для каждой из папок;
    public static Set<FileInfo> b = new HashSet<>();
    public static Set<FileInfo> uni = new HashSet<>(); //создаем объединенную коллекцию
    public static String a_path = "";
    public static String b_path = "";
    public static File lastFile;

    /**
     * Метод копирует файлы из одной директории в лругую
     *
     * @param a    Путь к 1-й директории
     * @param b    Путь к 2-й директории
     * @param file Имя файла
     */
    public static void CopyFiles(String a, String b, String file) {
        try {
            Files.copy(Paths.get(a, file), Paths.get(b, file), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(file + " has been moved!");
    }


    /**
     * Метод сохраняет файл, основываясь на общей сумме файлов в обоих папках
     */
    public static void saveOver() {
        a.clear();
        b.clear();
        uni.clear();
        scanDir(a_path, a, a_path);
        scanDir(b_path, b, b_path);
        uni.addAll(a);
        uni.addAll(b);
        saveToBinaryFile(uni, lastFile);
    }

    /**
     * Метод осуществляет рекурсивный поиск файлов и каталогов начиная с
     * указанного в параметре path.
     *
     * @param path текущая директория сканирования.
     * @param s    коллекция, содержащая список всех найденных файлов и каталогов.
     */
    private static void scanDir(String path, Set<FileInfo> s, String fold) {
        File root = new File(path);
        File[] list = root.listFiles();
        if (list == null) return;
        for (File f : list) {
            boolean isDirectory = f.isDirectory();
            long lastModified = f.lastModified();
            Path file = Paths.get(f.getPath());
            Path folder = Paths.get(fold);
            String fPath = folder.relativize(file).toString();
            s.add(new FileInfo(fPath, lastModified, isDirectory));
            if (isDirectory) {
                scanDir(file.toString(), s, fold);
            }
        }
    }


    /**
     * Вспомогательный метод используется для закрытия потоков.
     *
     * @param closeable ссылка на поток.
     */
    private static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ex) {
                // ignore
            }
        }
    }

    /**
     * Метод используется чтения из файла сериализованной коллекции.
     *
     * @param f файл для чтения.
     * @return коллекция.
     */
    private static Set<FileInfo> loadFromBynaryFile(File f) {
        Set<FileInfo> s = null;
        FileInputStream fs = null;
        ObjectInputStream os = null;
        try {
            if (!f.exists()) return null;
            fs = new FileInputStream(f);
            os = new ObjectInputStream(fs);
            s = (Set<FileInfo>) os.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(Sync.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            closeQuietly(os);
            closeQuietly(fs);
        }
        return s;
    }

    /**
     * Метод используется для сериализации (сохранения в файл) коллекции.
     *
     * @param s коллекция, которую следуют сериализовать.
     * @param f файл для сохранения.
     * @return возвращает true в случае удачной сериализации.
     */
    private static boolean saveToBinaryFile(Set<FileInfo> s, File f) {
        boolean result = false;
        FileOutputStream fs = null;
        ObjectOutputStream os = null;
        try {
            fs = new FileOutputStream(f);
            os = new ObjectOutputStream(fs);
            os.writeObject(s);
            result = true;
        } catch (IOException ex) {
            Logger.getLogger(Sync.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            closeQuietly(os);
            closeQuietly(fs);
        }
        return result;
    }

    public void run() {

        a_path = ParseXML.getContent("path_a");
        b_path = ParseXML.getContent("path_b");

        scanDir(a_path, a, a_path);
        scanDir(b_path, b, b_path);
        uni.addAll(a);
        uni.addAll(b);

        lastFile = new File(ParseXML.getContent("path_file"));


//если файл есть--------------------------------------------------------------------------------------------------------
        if (lastFile.exists()) {
            System.out.println("Sync file exist");
            Set<FileInfo> s = loadFromBynaryFile(lastFile); //загрузка файла в коллекцию
            //поиск и обработка измененных файлов
            HashSet<String> changePath = new HashSet<>();
            for (FileInfo af : a) {
                for (FileInfo bf : b) {
                    if (af.getPath().equals(bf.getPath()) && !af.isDirectory()) {
                        String Path = af.getPath();
                        if (af.compareTo(bf) != 0) {
                            if (af.compareTo(bf) > 0) {
                                CopyFiles(a_path, b_path, Path);
                            } else {
                                CopyFiles(b_path, a_path, Path);
                            }
                            changePath.add(Path);
                        }
                    }
                }
            }

            // поиск добавленных в А
            a.removeAll(s);
            for (FileInfo af : a) {
                if (!changePath.contains(af.getPath())) {
                    CopyFiles(a_path, b_path, af.getPath());
                }
            }
            scanDir(a_path, a, a_path);

            // поиск добавленных в B
            b.removeAll(s);
            for (FileInfo bf : b) {
                if (!changePath.contains(bf.getPath())) {
                    CopyFiles(b_path, a_path, bf.getPath());
                }
            }
            scanDir(b_path, b, b_path);
            // поиск удаленных из A
            s.removeAll(a);
            for (FileInfo af : s) {
                if (!changePath.contains(af.getPath())) {
                    if (!af.isDirectory()) {
                        String Path = b_path.concat("\\").concat(af.getPath());
                        File b_tmp = new File(Path);
                        if (b_tmp.exists()) {
                            b_tmp.delete();
                            System.out.println(af.getPath() + " has been deleted!");
                        }
                    }
                }
            }
            s = loadFromBynaryFile(lastFile);
            // поиск удаленных из B
            s.removeAll(b);
            for (FileInfo bf : s) {
                if (!changePath.contains(bf.getPath())) {
                    if (!bf.isDirectory()) {
                        String Path = a_path.concat("\\").concat(bf.getPath());
                        File a_tmp = new File(Path);
                        if (a_tmp.exists()) {
                            a_tmp.delete();
                            System.out.println(bf.getPath() + " has been deleted!");
                        }
                    }
                }
            }
            saveOver();
        } else

//если файла нет--------------------------------------------------------------------------------------------------------
        {
            System.out.println("Sync file does not exist");
            for (FileInfo f : uni) {
                if (!a.contains(f)) { //копируем в a те файлы, которых там нет
                    CopyFiles(b_path, a_path, f.getPath());

                }
                if (!b.contains(f)) { //копируем в b те файлы, которых там нет
                    CopyFiles(a_path, b_path, f.getPath());

                }
            }
            saveOver();

        }
        System.out.println("Sync is complete!");
    }
}
