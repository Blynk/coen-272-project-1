import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Webcrawler {

    // List of URLs to crawl through
    private Queue<String> newURLs;
    private Hashtable<String,Integer> oldURLs;
    private int maxToCrawl;
    private String domain;
    private boolean domainSet;
    private OutputWriter outputWriter;
    private HashMap<String, ArrayList<String>> disallowedLists;


    //Function: Initialization of all class variables
    //Input: string array of arguments from readArgs
    //Output: none
    public void init(String [] args){
        this.newURLs = new ArrayDeque<>();
        this.oldURLs = new Hashtable<>();
        this.disallowedLists = new HashMap<>();
        this.outputWriter = new OutputWriter();

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
            return;
        }

        this.newURLs.add(args[0]);
        //this.oldURLs.put(args[0], 1);
        // max to crawl sets limit on how many pages to crawl -- minus 1 b/c seedURL is first page
        this.maxToCrawl = Integer.parseInt(args[1]) - 1;
        if(args.length > 2){
            try {
                URL hostDomain = new URL(args[2]);
                this.domain = hostDomain.getHost();
                this.domainSet = true;
            } catch (MalformedURLException e) {
                e.printStackTrace();
                System.out.println("Invalid domain given");
            }
        }
        else
            this.domainSet = false;

        this.outputWriter.initStore();
    }

    // Parses HTML page
    // Either writes HTML doc to store (if 200)
    // Else writes response code (does not parse HTML)
    public void parseHTML(String input) throws IOException{
        int links;
        int responseCode;
        int imgs;

        Connection.Response response = Jsoup.connect(input)
                .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_4) " +
                        "AppleWebKit/537.36 (KHTML, like Gecko) " +
                        "Chrome/49.0.2623.112 Safari/537.36")

                .timeout(0)
                .execute();
        responseCode = response.statusCode();
        if(responseCode == 200) {
            Document doc = response.parse();
            System.out.println("Page Retrieved: " + doc.location());
            Elements imgArr = doc.getElementsByTag("img");
            imgs = imgArr.size();
            Elements linkArr = doc.select("a[href]");
            links = linkArr.size();

            this.outputWriter.setPage(doc);
            this.outputWriter.writeHTMLToFile();
            this.outputWriter.writeHTMLStats(links, imgs);
            addURLs(linkArr);
        }
        else{
            System.out.println("Status Code: " + responseCode);
            this.outputWriter.writeErrorPage(input, responseCode);
        }
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

    public void parseRobots(String hostUrl) {
        int numRules = 0;
        ArrayList<String> disallows = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new URL(hostUrl + "/robots.txt").openStream()));
            String line;
            while ((line = br.readLine()) != null) {
                // We'll assume that any disallow rule applies to us
                if (line.indexOf("Disallow:") == 0) {
                    String disallowed = line.substring("Disallow:".length());

                    int commentIndex = disallowed.indexOf("#");
                    if (commentIndex != -1) {
                        disallowed = disallowed.substring(0, commentIndex);
                    }
                    // Add disallow rule
                    disallows.add(disallowed.trim());
                    numRules++;
                }
            }
            this.disallowedLists.put(hostUrl, disallows);
            System.out.println(numRules + " Rules added for: " + hostUrl);
        } catch (MalformedURLException e) {
            ArrayList<String> temp = new ArrayList<>();
            temp.add(hostUrl);
            this.disallowedLists.put(hostUrl, temp);
            // Not able to resolve robots.txt -- do not parse
            return;
        } catch (FileNotFoundException fe) {
            // No robots.txt found -- Ok.
            this.disallowedLists.put(hostUrl, null);
        } catch (IOException e) {
            ArrayList<String> temp = new ArrayList<>();
            temp.add(hostUrl);
            this.disallowedLists.put(hostUrl, temp);
            // 4xx -- do not parse
            return;
        }
    }

    public boolean checkRobots(String location, String host){
        ArrayList<String> ruleList = disallowedLists.get(host);
        // If no robots.txt -- we're free to do what we want
        if(ruleList == null)
            return true;
        for(int i=0; i<ruleList.size(); i++){
            if(location.startsWith(ruleList.get(i))){
                return false;
            }
        }
        return true;
    }

    // Add new URLs to the frontier
    // Checks for domain (if set)
    // Checks crawled URLs before adding to the frontier
    public void addURLs(Elements elements) {
        int URLsAdded = 0;
        for(Element link : elements){
            String newLink = link.attr("abs:href");
            if(newLink != null && newLink.length() > 0 && newLink.charAt(newLink.length()-1)=='/')
                newLink = newLink.substring(0, newLink.length()-1);
            if(domainSet && !newLink.contains(domain)){
                continue;
            }
            if(!oldURLs.containsKey(newLink.replaceFirst("^(http://www\\.|http://|www\\.)",""))){
                newURLs.add(newLink);
                URLsAdded++;
            }
        }
        System.out.println(URLsAdded + " URLs added to the list");
    }

    // I don't think this is very efficient, but hopefully it doesn't get called too often.
    public void cleanURLs(){
        newURLs.removeAll(oldURLs.keySet());
    }

    public void run() {
        String loc = null;
        for (int i = 0; i<this.maxToCrawl; i++) {
            try {
                // Remove first URL from queue
                loc = newURLs.poll();
                if(loc == null){
                    System.out.println("No more URLs to crawl");
                    System.out.println("Pages Crawled: " + i);
                    return;
                }
                // Check if the URL can be resolved, then get the domain URL of the page
                if(loc.isEmpty()){
                    i--;
                    continue;
                }
                URL location = new URL(loc);
                String hostUrl = location.getProtocol();
                hostUrl += "://" + location.getHost();
                if(hostUrl != null && hostUrl.length() > 0 && hostUrl.charAt(hostUrl.length()-1)=='/')
                    hostUrl = hostUrl.substring(0, hostUrl.length()-1);

                System.out.println("URL: " + loc.toString());
                System.out.println("Host: " + hostUrl);
                // Check if the robots.txt has been parsed for rules
                if(!disallowedLists.containsKey(hostUrl)){
                    // If not, we parse for rules and add to the list
                    parseRobots(hostUrl);
                }
                // Check if the host allows parsing of the current page
                if(checkRobots(loc, hostUrl)){
                    // We can parse, so add URL to list of crawled URLs
                    String pageName = loc.replaceFirst("^(http://www\\.|http://|www\\.)","");
                    if(!oldURLs.containsKey(pageName)) {
                        oldURLs.put(pageName, 1);
                        // Parse the HTML page
                        parseHTML(loc);
                    }
                    else {
                        // we ran into a collision here w/ a old URL
                        cleanURLs(); // Remove all oldURLs from frontier
                        i--;
                        continue;
                    }
                }
                else {
                    i--; // We are not allowed to parse this page -- go to the next page
                    continue;
                }
                // Peek at next URL in list,
                // If the next URL has the same host,
                // We need to wait for a small amount of time before continuing
                // Otherwise, another web server will serve us
                if(newURLs.peek().contains(hostUrl)) {
                    TimeUnit.SECONDS.sleep(5);        // Wait for a couple of seconds
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
                e.getMessage();
                System.out.println("URL could not be resolved: " + loc.toString());
                i--;
                continue;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Webcrawler wc = new Webcrawler();
        if(args.length == 0){
            System.out.println("No .csv file provided\nUsage: java Webcrawler <.csv file>");
        }
        wc.init(wc.readArgs(args[0]));
        wc.run();
    }
}
