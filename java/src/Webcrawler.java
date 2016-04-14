import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;

public class Webcrawler implements Runnable{

    // List of URLs to crawl through
    private ArrayList<URL> newURLs;
    private Hashtable<URL,Integer> oldURLs;
    private int maxToCrawl;
    private URL domain;
    private boolean domainSet;


    //Function: Initialization of all class variables
    //Input: string array of arguments from readArgs
    //Output: none
    public void init(String [] args){
        this.newURLs = new ArrayList<>();
        this.oldURLs = new Hashtable<>();

        if(args.length < 2){
            System.out.println("Not enough arguments given!");
            return;
        }

        URL seedURL;
        try {
            seedURL = new URL(args[0]);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            System.out.println("Invalid Seed URL given.");
            return;
        }
        this.newURLs.add(seedURL);
        this.oldURLs.put(seedURL, 1);
        this.maxToCrawl = Integer.parseInt(args[1]);
        if(args.length > 2){
            try {
                this.domain = new URL(args[2]);
                this.domainSet = true;
            } catch (MalformedURLException e) {
                e.printStackTrace();
                System.out.println("Invalid domain given");
            }
        }
        else
            this.domainSet = false;
    }
    //Function: read in arguments from .csv file
    // Input: string denoting location of .csv file
    // Return: string array containing seedURL, pages to crawl, (optional) domain restriction
    public static String[] readArgs(String csvFile){
        BufferedReader br = null;
        String line;
        String[] args;
        try {
            br = new BufferedReader(new FileReader(csvFile));
            if((line = br.readLine()) != null) {
                args = line.split(",");
                return args;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("Invalid file or file not found.");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Unable to read file");
        } finally {
            if(br != null)
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return null;
    }


    //@TODO: function: write HTML document to stored file

    //@TODO: function: write HTML statistics to output file

    //@TODO: function: parse HTML for title, links, images
    // (?) - May need to write a separate class?
    // -- Might be able to follow this example:
    // see: http://jsoup.org/cookbook/extracting-data/example-list-links
        public static void parseHTML(String[] input) throws IOException{
    	int links;
    	int responseCode;
    	int imgs; //still need to output the links/responseCode/imgs
    	String domain=null;
    	boolean domainLimit=false;
    	String seedURL= input[0];
    	int limit= Integer.parseInt(input[1]);
    	if(input.length>2){
    		domain=input[2];
    		domainLimit=true;
    	}
    	Connection.Response response=Jsoup.connect(seedURL).execute();
    	responseCode = response.statusCode();
    	Document doc= Jsoup.connect(seedURL).get();
    	Elements imgArr=doc.getElementsByTag("img");
    	imgs=imgArr.size();
    	Elements linkArr= doc.select("a[href]");
    	links=linkArr.size();
    	for (Element e:linkArr){
    		String href = linkArr.attr("abs:href");
    		String[] s= {href, Integer.toString(limit) ,domain};
    		if(domainLimit){
    			if(e.attr("href").contains(domain)){
    				parseHTML(s);
    			}
    		}else{
    			parseHTML(s);
    		}
    		
    	}
    }

    //@TODO function: return Arraylist of new URLs to global list

    @Override
    public void run() {
        for (URL url : newURLs) {
            
        }
    }
}
