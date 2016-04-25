import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;
//maybe wanna use similar method in https://github.com/CrawlScript/WebCollector/blob/master/WebCollector/src/main/java/cn/edu/hfut/dmic/contentextractor/ContentExtractor.java
//to compute score?
public class ContentExtractor {
	Document doc;
	HashMap<Tag, ArrayList<TagInfo>> map;
	String title;
	Elements elements;
	//TODO: need to iterate over repository to get all files

	public ContentExtractor(String path){
		map= new HashMap<Tag, ArrayList<TagInfo>>();
		Document doc=Jsoup.parse(path);
		this.doc=doc;
	}
	public void getTitle(){
		title=doc.select("title").text();
	}
	public void bodyProcessor(Document doc){
//		//filter the doc
//		String html = doc.html();
//		String clean=Jsoup.clean(html, Whitelist.basic());
//		//remove extra spaces
//		clean=clean.replaceAll("&nbsp", " ");
		
//		System.out.println(clean); //for testing
		
		doc.select("script,noscript,style,iframe,br").remove(); //from webCollector on github
		
		elements=doc.body().select("*");
		
		for(int i=0;i<elements.size();i++){
			Tag tag=elements.get(i).tag();
			TagInfo taginfo= new TagInfo(tag);
			String context=elements.get(i).text();
			taginfo.setContent(context);//difference between ownText() and text()?
			int len=context.length();
			taginfo.setLength(len);
			taginfo.position=i;
			if(map.containsKey(tag)){
				ArrayList<TagInfo> arr= map.get(tag);
				arr.add(taginfo);
				map.put(tag, arr);
			}else{
				ArrayList<TagInfo> arr=new ArrayList<TagInfo>();
				arr.add(taginfo);
				map.put(tag, arr);
			}
			
		}
	}
	void removeNoise(){
		//TODO: remove noise based on the length of content under each tag
		Iterator<Map.Entry<Tag, ArrayList<TagInfo>>> it=map.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<Tag, ArrayList<TagInfo>> pair=it.next();
			ArrayList<TagInfo> arr=pair.getValue();
			for(TagInfo entry:arr){
				if(entry.getLength()<4){ //just come up with this number, need more discuss on that
					int index=entry.getPos();
					elements.get(index).text(""); //should be able to set text to "", not sure about this yet
				}
			}
		}
	}
	void finalContent(){
		//TODO: generate final text and output title and processed body to file
	}

}
