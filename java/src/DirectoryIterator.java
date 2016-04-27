import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

/**
 * Created by dobatake on 4/25/16.
 */
public class DirectoryIterator {
    public static ArrayList<Path> getHTMLFiles(Path HTMLDir){
        ArrayList<Path> docFileList = new ArrayList<>();
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(HTMLDir, "*.html")) {
            for(Path entry : stream){
                docFileList.add(entry.getFileName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return docFileList;
    }

    public static ArrayList<Path> getTextFiles(Path textDir){
        ArrayList<Path> docFileList = new ArrayList<>();
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(textDir, "*.txt")) {
            for(Path entry : stream){
                docFileList.add(entry.getFileName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return docFileList;
    }
}
