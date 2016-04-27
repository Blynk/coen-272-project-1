import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
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
		title = Paths.get(doc.location()).getFileName().toString();
	}

	public int countTags(Element e){
		return e.children().size() > 0 ? e.children().size() : 1;
	}

	public int countChars(Element e){
		int totalChars = 0;
		for(TextNode tn : e.textNodes())
			totalChars += tn.text().length();
		return totalChars;
	}

	public TagInfo countRatio(Element e){

		TagInfo thisTag = new TagInfo(e);
		for(Node n:e.children()){
			TagInfo childTag = countRatio((Element) n);
			thisTag.setLength(thisTag.getLength() + childTag.getLength());
		}
		thisTag.setTag(countTags(e));
		if(thisTag.getTagCount() == 0)
			thisTag.setTag(1);
		thisTag.setLength(thisTag.getLength() + countChars(e));

		return thisTag;
	}

	public void bodyProcessor(){	

		float densitySumMax = -100;
		float tempMax;
		TagInfo maxDSElement = null;

		//doc.select("*:matchesOwn((?is) )").remove(); //remove &nbsp;
		// preprocess page by removing the extraneous tags
		doc.select("script,noscript,style,iframe,br,a,nav,img,footer").remove();

		elements=doc.body().select("*");
		
		for(int i=0;i<elements.size();i++){

			Element e=elements.get(i);
			TagInfo taginfo=countRatio(e);
			//if(densitySumMax < taginfo.getDensitySum()) {
			//	densitySumMax = taginfo.getDensitySum();
			//	maxDSElement = taginfo;
			//}
			taginfo.setPos(i);
			map.put(e, taginfo);
			//System.out.println("DS max: " + densitySumMax);
		}

		//removeNoise(maxDSElement.getChildMinDS());
		removeNoise();
		finalContent();
	}

	void removeNoise(){
		//TODO: remove noise based on the length of content under each tag
		for(Map.Entry<Element, TagInfo> t : map.entrySet()){
			TagInfo tmp= t.getValue();
			// # chars / # of tags < 1 ==>
			if((float) tmp.getLength()/(float) tmp.getTagCount() < 0.1){
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
		OutputWriter.cleanWriter(doc, title);
	}

}
