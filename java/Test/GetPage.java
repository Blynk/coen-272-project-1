import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

/**
 * Created by dobatake on 4/13/16.
 */

/* Class for Testing Output Writer class */
public class GetPage {
    public static void main(String[] args) {
        try {
            Document doc = Jsoup.connect("http://www.google.com").get();
            String title = doc.title();
            System.out.println("Title: " + title);

            OutputWriter ow = new OutputWriter();
            ow.setPage(doc);
            ow.initStore();
            ow.writeHTMLToFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
