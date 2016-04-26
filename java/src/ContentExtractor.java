import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;
//maybe wanna use similar method in https://github.com/CrawlScript/WebCollector/blob/master/WebCollector/src/main/java/cn/edu/hfut/dmic/contentextractor/ContentExtractor.java
//to compute score?
public class ContentExtractor {
	Document doc;
	HashMap<Element, TagInfo> map;
	String title;
	Elements elements;
//	int tagCount;
//	int textCount;

	public ContentExtractor(String path){
		map= new HashMap<Element, TagInfo>();
		Document doc=Jsoup.parse(path);
		this.doc=doc;
		
	}
	public TagInfo countRatio(Element e, TagInfo ti){
		for(Node n:e.childNodes()){
			if(n instanceof Element){
				int tmp=ti.getTagCount()+1;
				ti.setTag(tmp);
				return countRatio((Element)n, ti);
			}else if(n instanceof TextNode){
				String text=((TextNode)n).text();
				int tmp =ti.getLength()+text.length();
				ti.setLength(tmp);
				return ti; 
			}else{
				return ti;
			}
		}
		return ti;
	}
	 
	public void bodyProcessor(){	
		
		doc.select("*:matchesOwn((?is) )").remove(); //remove &nbsp;
		doc.select("script,noscript,style,iframe,br,a,nav").remove(); 
		
		elements=doc.body().select("*");
		
		for(int i=0;i<elements.size();i++){

			Element e=elements.get(i);
			TagInfo taginfo= new TagInfo(e);
			taginfo=countRatio(e, taginfo);
			taginfo.setPos(i);
			map.put(e, taginfo);
//			String context=elements.get(i).text();
//			taginfo.setContent(context);//difference between ownText() and text()?
//			int len=context.length();
//			taginfo.setLength(len);
//			taginfo.position=i;
//			if(map.containsKey(tag)){
//				ArrayList<TagInfo> arr= map.get(tag);
//				arr.add(taginfo);
//				map.put(tag, arr);
//			}else{
//				ArrayList<TagInfo> arr=new ArrayList<TagInfo>();
//				arr.add(taginfo);
//				map.put(tag, arr);
//			}
			
		}
		removeNoise();
		// finalContent();
	}
	void removeNoise(){
		//TODO: remove noise based on the length of content under each tag
		Iterator<Map.Entry<Element, TagInfo>> it=map.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<Element, TagInfo> pair=it.next();
			TagInfo tmp=pair.getValue();
			if(tmp.getLength()/tmp.getTagCount()<4){
				elements.get(tmp.getPos()).remove();
			}
				
			
		}
//		String html = doc.html();
//		String clean=Jsoup.clean(html, Whitelist.basic());
//		//remove extra spaces
//		clean=clean.replaceAll("&nbsp", " ");
	}
	void finalContent(){
		//TODO: generate final text and output title and processed body to file
	}

}
