import com.hp.gagawa.java.elements.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;



/**
 * Created by dobatake on 4/13/16.
 */
public class OutputWriter {

    Document page;
    Path docStore;
    Path reportPath;

    public void setPage(Document d){
        this.page = d;
    }

    public void initStore(){
        Path toDir = Paths.get("").toAbsolutePath();
        this.docStore = Paths.get(toDir.toString(), "HTMLDocStore");
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

    public void writeHTMLToFile() {
        String toFile = page.outerHtml();
        Path newFile  = Paths.get(this.docStore.toString(), page.title() + ".html");
        try {
            Files.write(newFile, toFile.getBytes(), StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Stored page: " + page.title() + " in: " + newFile.toString());
    }

    //Function: Write statistics to report.html
    // Input: String of HTML to write to file
    public void writeStatistics(String html){

    }

    // @TODO: Function to start and end report.html w/ opening and closing tags
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
        reportPath = Paths.get(this.docStore.toString(), "..", "report.html");
        try {
            Files.write(this.reportPath, toWrite.getBytes("UTF-8"), StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // @TODO: Function to construct HTML from page statistics
    public void writeHTMLStats(int nLinks, int nImgs){
        File reportFile = new File(reportPath.toString());
        Document reportDoc;
        try {
            reportDoc = Jsoup.parse(reportFile, "UTF-8");
            Element bodyDiv = reportDoc.select("div").first();
            bodyDiv.append("<h1><a href="+ page.location() + ">" + page.title() +"</a></h1>");
            bodyDiv.append("<p>Cached Version: <a href="+ docStore + ">" + page.title() + "</a></p>");
            bodyDiv.append("<p>Number of Links: " + nLinks + "</p>");
            bodyDiv.append("<p>Number of Images: " + nImgs + "</p>");

            Files.write(this.reportPath, reportDoc.toString().getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
