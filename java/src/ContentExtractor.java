import java.io.File;
import java.io.IOException;
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
		File input = new File(path);
		try {
			doc=Jsoup.parse(input, "UTF-8");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("File parsing failed");
		}
		
	}

	public TagInfo countRatio(Element e, TagInfo ti){

		TagInfo childTagInfo = null;
		for(Node n:e.childNodes()){
			// found an HTML tag
			if(n instanceof Element){
				// recurse upon the child tag we found
				childTagInfo = countRatio((Element)n, ti);
				// update current tag count w/ child's tag count + 1 (i.e. the child)
				int tagCount = ti.getTagCount() + 1 + childTagInfo.getTagCount();
				ti.setTag(tagCount);
				// Update current char count w/ child's text count
				int charCount = ti.getLength() + childTagInfo.getLength();
				ti.setLength(charCount);
				// Set density sum metrics for tag density comparison
				ti.setDensitySum(tagCount, charCount);
				ti.setChildMinDS(tagCount, charCount);
			}else if(n instanceof TextNode){
				String text=((TextNode)n).text();
				int tmp =ti.getLength()+text.length();
				ti.setLength(tmp);
			}else{
				continue;
			}
		}
		// Check if there are no children tags
		// If so, set to one. Otherwise density sum metric will fail
		if(ti.getTagCount() == 0)
			ti.setTag(1);
		ti.setDensitySum(ti.getTagCount(), ti.getLength());
		ti.setChildMinDS(ti.getTagCount(), ti.getLength());
		return ti;
	}

	public void bodyProcessor(){	

		float densitySumMax = -100;
		float tempMax;
		TagInfo maxDSElement = null;

		doc.select("*:matchesOwn((?is) )").remove(); //remove &nbsp;
		// preprocess page by removing the extraneous tags
		doc.select("script,noscript,style,iframe,br,a,nav").remove();
		
		elements=doc.body().select("*");
		
		for(int i=0;i<elements.size();i++){

			Element e=elements.get(i);
			TagInfo taginfo= new TagInfo(e);
			taginfo=countRatio(e, taginfo);
			if(densitySumMax < taginfo.getDensitySum()) {
				densitySumMax = taginfo.getDensitySum();
				maxDSElement = taginfo;
			}
			taginfo.setPos(i);
			map.put(e, taginfo);
		}

		removeNoise(maxDSElement.getChildMinDS());
		finalContent();
	}

	void removeNoise(Float threshold){
		//TODO: remove noise based on the length of content under each tag
		Iterator<Map.Entry<Element, TagInfo>> it=map.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<Element, TagInfo> pair=it.next();
			TagInfo tmp=pair.getValue();
			if((float) tmp.getLength()/(float) tmp.getTagCount() < threshold){
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
		for(Element e : elements){
			System.out.println(e.outerHtml());
		}
	}

}
