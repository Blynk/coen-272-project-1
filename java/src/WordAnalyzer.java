import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * Created by dobatake on 4/25/16.
 */
public class WordAnalyzer {
    private HashMap<String, Integer> wordCount;
    private ArrayList<Path> docStoreList;
    private Path docStore;

    public WordAnalyzer(){
        this.docStore = Paths.get("").toAbsolutePath();
        this.docStore = Paths.get(this.docStore.toString(), "HTMLDocStore");
        this.wordCount = new HashMap<>();
        this.docStoreList = DirectoryIterator.getHTMLFiles(this.docStore);
    }

    public void parseDocument(Path htmldoc){
        File currentPage = new File(htmldoc.toString());
        try {
            Document doc = Jsoup.parse(currentPage, "UTF-8");
            Elements elements = doc.body().select("*");

            for (Element element : elements) {
                // Remove all punctuation, convert to lowercase then split on the whitespace
                String [] words = element.ownText().replaceAll("\\p{P}", "").toLowerCase().split("\\s+");
                for(String w : words){
                    Integer count = this.wordCount.get(w);
                    if (count == null) {
                        wordCount.put(w, 1);
                    }
                    else {
                        wordCount.put(w, count + 1);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // Sorting a hashmap based on values; creates a descending list
    // http://stackoverflow.com/a/11648106
    public static <K,V extends Comparable<? super V>>
    List<Map.Entry<K, V>> entriesSortedByValues(Map<K,V> map) {

        List<Map.Entry<K,V>> sortedEntries = new ArrayList<Map.Entry<K,V>>(map.entrySet());

        Collections.sort(sortedEntries,
                new Comparator<Map.Entry<K,V>>() {
                    @Override
                    public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) {
                        return e2.getValue().compareTo(e1.getValue());
                    }
                }
        );

        return sortedEntries;
    }

    public int getWordCount(){
        int wordCount = 0;
        for(Map.Entry<String, Integer> entry : this.wordCount.entrySet()) {
            if(entry.getKey().isEmpty())
                continue;
            Integer value = entry.getValue();
            wordCount += value;
        }
        return wordCount;
    }

    // Output .csv file w/ text statistics
    public void writeStatistics(int totalWordCount){
        int rank = 1;

        List<Map.Entry<String, Integer>> wordList = entriesSortedByValues(wordCount);
        for(Map.Entry<String, Integer> entry : wordList) {
            String key = entry.getKey();
            // ignore empty string that gets added from splitting
            if(key.isEmpty())
                continue;
            Integer value = entry.getValue();
            float probability = (float) value/ (float) totalWordCount;
            float zipf = rank * probability;
            String output = key + ", " + rank + ", " + value + ", " + zipf + ",\n";
            rank++;


            Path statfile = Paths.get(this.docStore.toString(), "..", "stats.csv").normalize();
            try (BufferedWriter writer = Files.newBufferedWriter(statfile, Charset.forName("UTF-8"),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                writer.write(output, 0, output.length());
            } catch (IOException x) {
                System.err.format("IOException: %s%n", x);
            }
        }

    }

    public void run(){
        for(Path p : docStoreList){
            parseDocument(Paths.get(this.docStore.toString(), p.toString()));
        }
        writeStatistics(getWordCount());
    }
}
