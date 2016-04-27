import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class ContentManager {
	private ArrayList<Path> docStoreList;
    private Path docStore;
	
	public ContentManager(){
		this.docStore = Paths.get("").toAbsolutePath();
        this.docStore = Paths.get(this.docStore.toString(), "HTMLDocStore");
        this.docStoreList = DirectoryIterator.getHTMLFiles(this.docStore);
        
        for(Path p:docStoreList){
        	Path filepath = Paths.get(this.docStore.toString(), p.toString());
        	String path=filepath.toString();
        	ContentExtractor ce= new ContentExtractor(path);
        	ce.bodyProcessor();
        }
	}
	public static void main(String[] args) {
        ContentManager cm= new ContentManager();
        
    } 


}
