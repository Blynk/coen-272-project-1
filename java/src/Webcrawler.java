import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;

public class Webcrawler {

    // List of URLs to crawl through
    private ArrayList<String> newURLs;
    private Hashtable<String,Integer> oldURLs;
    private int maxToCrawl;
    private URL domain;
    private boolean domainSet;
    private OutputWriter outputWriter;
    private Hashtable<String, ArrayList<String>> disallowedLists;


    //Function: Initialization of all class variables
    //Input: string array of arguments from readArgs
    //Output: none
    public void init(String [] args){
        this.newURLs = new ArrayList<>();
        this.oldURLs = new Hashtable<>();
        this.disallowedLists = new Hashtable<>();

        if(args.length < 2){
            System.out.println("Not enough arguments given!");
            return;
        }

        URL seedURL = null;
        try {
            seedURL = new URL(args[0]);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            System.out.println("Invalid seed URL given");
        }

        this.newURLs.add(args[0]);
        this.oldURLs.put(args[0], 1);
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

    // @TODO: get and parse robots.txt file
    public void parseRobots(String location) {
        String hostUrl;
        ArrayList<String> disallows = new ArrayList<>();
        try {
            hostUrl = new URL(location).getHost();
            BufferedReader br = new BufferedReader(new InputStreamReader(new URL(hostUrl + "/robots.txt").openStream()));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.indexOf("Disallow:") == 0) {
                    String disallowed = line.substring("Disallow:".length());

                    int commentIndex = disallowed.indexOf("#");
                    if (commentIndex != -1) {
                        disallowed = disallowed.substring(0, commentIndex);
                    }
                    // Add disallow rule
                    disallows.add(disallowed.trim());
                }
            }
            this.disallowedLists.put(hostUrl, disallows);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            // Not able to resolve robots.txt -- do not parse
            return;
        } catch (IOException e) {
            e.printStackTrace();
            // No robots.txt found -- Ok.
            return;
        }
    }

    public boolean checkRobots(String location, String host){
        ArrayList<String> ruleList = disallowedLists.get(host);
        for(int i=0; i<ruleList.size(); i++){
            if(location.startsWith(ruleList.get(i))){
                return false;
            }
        }
        return true;
    }

    //@TODO: function: parse HTML for title, links, # of images
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
    	    Connection.Response response= Jsoup.connect(seedURL).execute();
            responseCode = response.statusCode();
            Document doc= Jsoup.connect(seedURL).get();
            Elements imgArr=doc.getElementsByTag("img");
            imgs=imgArr.size();
            Elements linkArr= doc.select("a[href]");
            links=linkArr.size();

    	/*for (Element e:linkArr){
    		String href = linkArr.attr("abs:href");
    		String[] s= {href, Integer.toString(limit) ,domain};
    		if(domainLimit){
    			if(e.attr("href").contains(domain)){
    				parseHTML(s);
    			}
    		}else{
    			parseHTML(s);
    		}
    		
    	}*/
    }

    //@TODO function: return Arraylist of new URLs to global list
    // (also needs to check knownURLs before adding)
    public void addURLs(Elements elements) {
        for(Element link : elements){
            String newLink = link.attr("href");
            if(!oldURLs.contains(newLink)){
                newURLs.add(newLink);
            }
        }
    }

    public int checkResponse(Connection.Response response){
        return response.statusCode();
    }

    /*@Override
    public void run() {
        for (URL url : newURLs) {

        }
    }*/
}
