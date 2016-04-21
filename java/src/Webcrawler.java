import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class Webcrawler {

    // List of URLs to crawl through
    private Queue<String> newURLs;
    private Hashtable<String,Integer> oldURLs;
    private int maxToCrawl;
    private String domain;
    private boolean domainSet;
    private OutputWriter outputWriter;
    private Hashtable<String, ArrayList<String>> disallowedLists;


    //Function: Initialization of all class variables
    //Input: string array of arguments from readArgs
    //Output: none
    public void init(String [] args){
        this.newURLs = new ArrayDeque<>();
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
            Elements imgArr = doc.getElementsByTag("img");
            imgs = imgArr.size();
            Elements linkArr = doc.select("a[href]");
            links = linkArr.size();

            this.outputWriter.setPage(doc);
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

    // @TODO: get and parse robots.txt file
    public void parseRobots(String location) {
        String hostUrl;
        ArrayList<String> disallows = new ArrayList<>();
        try {
            hostUrl = new URL(location).getHost();
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

    // Add new URLs to the frontier
    // Checks for domain (if set)
    // Checks crawled URLs before adding to the frontier
    public void addURLs(Elements elements) {
        for(Element link : elements){
            String newLink = link.attr("href");
            if(domainSet && !newLink.contains(domain)){
                continue;
            }
            if(!oldURLs.contains(newLink)){
                newURLs.add(newLink);
            }
        }
    }

    public void run() {
        for (int i = 0; i<this.maxToCrawl; i++) {
            try {
                // Remove first URL from queue
                String loc = newURLs.poll();
                if(loc == null){
                    System.out.println("No more URLs to crawl");
                    System.out.println("Pages Crawled: " + i);
                    return;
                }
                // Check if the URL can be resolved, then get the base host of the page
                String host = new URL(loc).getHost();
                // Check if the robots.txt has been parsed for rules
                if(!disallowedLists.contains(host)){
                    // If not, we parse for rules and add to the list
                    parseRobots(host);
                }
                // Check if the host allows parsing of the current page
                if(checkRobots(loc, host)){
                    // We can parse, so add URL to list of crawled URLs
                    oldURLs.put(loc, 1);
                    // Parse the HTML page
                    parseHTML(loc);
                }
                // Peek at next URL in list,
                // If the next URL has the same host,
                // We need to wait for a small amount of time before continuing
                // Otherwise, another web server will serve us
                if(newURLs.peek().contains(host)) {
                    wait(30000);        // Wait for 30 seconds to
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
                System.out.println("URL could not be resolved");
                continue;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
