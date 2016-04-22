/**
 * Created by dobatake on 4/21/16.
 * Test Harness for Webcrawler
 */
public class WebCrawlerTest {
    public static void main(String[] args) {
        Webcrawler wc = new Webcrawler();

        //@TODO: Need to test parsing .csv file
        wc.init(new String[]{"http://www.google.com", "5"});
        wc.run();
    }
}
