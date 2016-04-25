import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by dobatake on 4/25/16.
 */
public class WordAnalyzer {
    private HashMap<String, Integer> wordCount;
    private ArrayList<Path> docStoreList;

    public WordAnalyzer(){
        Path toDir = Paths.get("").toAbsolutePath();
        toDir = Paths.get(toDir.toString(), "HTMLDocStore");
        this.wordCount = new HashMap<>();
        this.docStoreList = DirectoryIterator.getHTMLFiles(toDir);
    }

    public void parseDocument(Path htmldoc){
        File currentPage = new File(htmldoc.toString());
        try {
            Document doc = Jsoup.parse(currentPage, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void writeStatistics(){

    }
}
