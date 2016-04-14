import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

/**
 * Created by dobatake on 4/13/16.
 */
public class GetPage {
    public static void main(String[] args) {
        try {
            Document doc = Jsoup.connect("http://www.google.com").get();
            String title = doc.title();
            System.out.println("Title: " + title);

            OutputWriter ow = new OutputWriter(doc);
            ow.initStore();
            ow.writeHTMLToFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
