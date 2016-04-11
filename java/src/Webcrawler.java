import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;

public class Webcrawler implements Runnable{

    // List of URLs to crawl through
    private ArrayList<String> URLqueue;
    // Current webpage connected
    Document doc;
    // List of new URLs to be returned
    private ArrayList<String> newURLs;

    /*
        Need to pass a list of URLs to each web crawler
            - issue(?) If multi-threaded, may need to lock global list
                depending on size of queue..?
     */
    Webcrawler(ArrayList<String> queue, int val){
        URLqueue = queue;
        System.out.println("Starting Webcrawler " + val);
    }

    //@TODO: function: write HTML to file

    //@TODO: function: parse HTML for title, links, images
    // (?) - May need to write a separate class?

    //@TODO function: return Arraylist of new URLs to global list

    @Override
    public void run() {
        try {
            for (String url : URLqueue) {
                //Connect and scrape webpage
                doc = Jsoup.connect(url).get();
                // Sleep for 30 seconds
                Thread.sleep(30000);
            }
        } catch (IOException ie) {
            ie.printStackTrace();
        } catch (InterruptedException ie){
            ie.printStackTrace();
        }
    }
}