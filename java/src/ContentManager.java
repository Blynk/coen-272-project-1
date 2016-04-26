import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

public class ContentManager {
	private ArrayList<Path> docStoreList;
    private Path docStore;
	
	public ContentManager(){
		this.docStore = Paths.get("").toAbsolutePath();
        this.docStore = Paths.get(this.docStore.toString(), "HTMLDocStore");
        this.docStoreList = DirectoryIterator.getHTMLFiles(this.docStore);
        
        for(Path p:docStoreList){
        	String path=p.toString();
        	ContentExtractor ce= new ContentExtractor(path);
        	ce.bodyProcessor();
        }
	}


}
