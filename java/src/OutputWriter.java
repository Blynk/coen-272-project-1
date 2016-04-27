import com.hp.gagawa.java.elements.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;

import static com.sun.tools.doclets.formats.html.markup.HtmlStyle.title;


/**
 * Created by dobatake on 4/13/16.
 */
public class OutputWriter {

    Document page;
    Path docStore;
    Path reportPath;
    Path filepath;

    public void setPage(Document d){
        this.page = d;
    }

    // Call this method when starting program...
    public void initStore(){
        Path toDir = Paths.get("").toAbsolutePath();
        this.docStore = Paths.get(toDir.toString(), "HTMLDocStore");
        constructHTML();
        if(Files.exists(this.docStore, LinkOption.NOFOLLOW_LINKS))
            return;
        try {
            Files.createDirectory(this.docStore);
        } catch (FileAlreadyExistsException fe){
            fe.printStackTrace();
            System.out.println("Directory already exists");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Created document store: " + this.docStore.toString());
    }

    // Takes HTML Document from crawler and stores it
    // Sanity Check: Need to set page before calling this method
    public void writeHTMLToFile() {
        String toFile = page.outerHtml();
        filepath = Paths.get(this.docStore.toString(), page.location()
                .replaceFirst("^(http://www\\.|http://|www\\.)","").replaceAll("/", "_"));
        if(!filepath.toString().contains(".html"))
            filepath = Paths.get(filepath.toString() + ".html");
        try {
            Files.write(filepath, toFile.getBytes(), StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Stored page: " + page.title() + " in: " + filepath.toString());
    }

    // Initializes report.html w/ proper opening and closing tags
    // Call this method at the beginning of the program!
    public void constructHTML(){
        Html html = new Html();
        Head head = new Head();
        html.appendChild(head);

        Title title = new Title();
        title.appendChild(new Text("WebCrawler Report"));
        head.appendChild(title);

        Body body = new Body();
        html.appendChild(body);

        Div bodyDiv = new Div();
        body.appendChild(bodyDiv);

        String toWrite = html.write();
        this.reportPath = Paths.get(this.docStore.toString(), "..", "report.html").normalize();
        try {
            // Create report.html,
            // if already found, it was found from a previous run, so truncate.
            Files.write(this.reportPath, toWrite.getBytes("UTF-8"),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Created Report.html: " + this.reportPath.toString());
    }

    // Opens Current report.html and then appends new statistics to the report
    // Sanity Check: Need to verify that report.html exists!
    //      --> Call contstructHTML method at the beginning of the program!!
    // @TODO: Need to think about changing method from per page processing to batch processing to reduce file I/O
    public void writeHTMLStats(int nLinks, int nImgs){
        File reportFile = new File(this.reportPath.toString());
        Document reportDoc;
        try {
            reportDoc = Jsoup.parse(reportFile, "UTF-8");
            Element bodyDiv = reportDoc.select("div").first();
            bodyDiv.append("<h1><a href="+ page.location() + ">" + page.title() +"</a></h1>");
            bodyDiv.append("<p>Cached Version: <a href="+ filepath +">" + page.title() + "</a></p>");
            bodyDiv.append("<p>Number of Links: " + nLinks + "</p>");
            bodyDiv.append("<p>Number of Images: " + nImgs + "</p>");

            Files.write(this.reportPath, reportDoc.toString().getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeErrorPage(String location, int responseCode){
        File reportFile = new File(reportPath.toString());
        Document reportDoc;
        try {
            reportDoc = Jsoup.parse(reportFile, "UTF-8");
            Element bodyDiv = reportDoc.select("div").first();
            bodyDiv.append("<h1><a href="+ page.location() + ">" + page.title() +"</a></h1>");
            bodyDiv.append("<p>Response Code Found: " + responseCode + "</p>");

            Files.write(this.reportPath, reportDoc.toString().getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void cleanWriter(Document doc, String title){
        Path toDir = Paths.get("").toAbsolutePath();
        Path cleanStore = Paths.get(toDir.toString(), "CleanedHTML");
        if(!Files.exists(cleanStore, LinkOption.NOFOLLOW_LINKS)) {
            try {
                Files.createDirectory(cleanStore);
            } catch (FileAlreadyExistsException fe) {
                fe.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Path newCleanFile = Paths.get(cleanStore.toString(), title);
        try (BufferedWriter writer = Files.newBufferedWriter(newCleanFile, Charset.forName("UTF-8"),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                String cleanHTML = doc.outerHtml();
                writer.write(cleanHTML, 0,cleanHTML.length());
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }
        System.out.println("Stored page: " + title + " in: " + newCleanFile.toString());
    }

}
