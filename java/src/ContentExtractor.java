import java.util.ArrayList;
import java.util.HashMap;

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
		//filter the doc
		String html = doc.html();
		String clean=Jsoup.clean(html, Whitelist.basic());
		//remove extra spaces
		clean=clean.replaceAll("&nbsp", " ");
		
		System.out.println(clean); //for testing
		
		Elements elements=doc.body().select("*");
		for(Element e: elements){
			Tag tag=e.tag();
			TagInfo taginfo= new TagInfo(tag);
			String context=e.text();
			taginfo.setContent(context);//difference between ownText() and text()?
			int len=context.length();
			taginfo.setLength(len);
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
	}
	void finalContent(){
		//TODO: generate final text and output title and body to file
	}

}
