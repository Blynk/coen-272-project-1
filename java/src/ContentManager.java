import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import static java.nio.file.Paths.get;

public class ContentManager {
	private ArrayList<Path> docStoreList;
    private Path docStore;
	
	public ContentManager(){
		this.docStore = get("").toAbsolutePath();
        this.docStore = get(this.docStore.toString(), "HTMLDocStore");
        this.docStoreList = DirectoryIterator.getHTMLFiles(this.docStore);

	}

    public void run(){
        for(Path p:docStoreList){
            // docStoreList only contains name of the file, not absolute path to file
            // So we append the name to the path of the HTMLDocStore directory
            Path filepath = Paths.get(this.docStore.toString(), p.toString());
            String path=filepath.toString();
            ContentExtractor ce= new ContentExtractor(path);
            ce.bodyProcessor();
        }
    }


}
