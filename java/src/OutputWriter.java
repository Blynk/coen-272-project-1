import org.jsoup.nodes.Document;

import java.io.IOException;
import java.nio.file.*;



/**
 * Created by dobatake on 4/13/16.
 */
public class OutputWriter {

    Document page;
    Path docStore;

    public OutputWriter(Document d){
        this.page = d;
    }

    public void initStore(){
        Path toDir = Paths.get("").toAbsolutePath();
        this.docStore = Paths.get(toDir.toString(), "HTMLDocStore");
        try {
            Files.createDirectory(this.docStore);
        } catch (FileAlreadyExistsException fe){
            fe.printStackTrace();
            System.out.println("Directory already exists");
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Created document store: " + this.docStore.toString());
    }

    public void writeHTMLToFile() {
        String toFile = page.outerHtml();
        Path newFile  = Paths.get(this.docStore.toString(), page.title() + ".html");

        try {
            Files.write(newFile, toFile.getBytes(), StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Stored page: " + page.title() + " in: " + newFile.toString());
    }

    public static void writeStatistics(){

    }
}
