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
	int tagCount;
	int textCount;

	public ContentExtractor(String path){
		map= new HashMap<Element, TagInfo>();
		Document doc=Jsoup.parse(path);
		this.doc=doc;
		
	}

	// countRatio -- called for every element of the document's body
	// Will keep calculate text to tag ratio or density
	// params: 	e - current node traversing
	// 			tagCount - total tag count for a node; shows for calling parent node
	//			textCount - total character count for a node; shows for calling parent node
	public float countRatio(Element e,int tagCount, int textCount){
		// local elements used per child node
		int tempCharCount = 0;
		int tempTagCount = 0;
		float densitySum = 0;
		for(Node n:e.childNodes()){
			// found an HTML tag
			if(n instanceof Element){
				// increase tag count by one
				int tmp=tagCount+1;
				tempTagCount++;
				// recursively search the HTML tag for nested tags
				densitySum = countRatio((Element)n, tmp, textCount);
			}else if(n instanceof TextNode){ // Otherwise, we found text
				String text=((TextNode)n).text();
				textCount+=text.length(); // get the number of text characters
				tempCharCount += text.length();
			}else{
				// Else we don't know what this is, ignore
				continue;
			}
		}
		// If tagCount == 0, division will be undefined; set to 1
		if(tempTagCount == 0)
			tempTagCount = 1;
		return densitySum = (float) tempCharCount/ (float) tempTagCount;
	}

	public float findMinDS(Element element, float currentMin){
		float minDS = currentMin;
		float tempMin = currentMin;
		int tags = 0;
		int chars = 0;

		for(Node n : element.childNodes()){
			if(n instanceof Element) {
				tags++;
				tempMin = findMinDS((Element) n, minDS);
			}
			else if(n instanceof TextNode) {
				String text = ((TextNode) n).text();
				chars = text.length();
			}
			else
				continue;

			if(tempMin < minDS)
				minDS = tempMin;
		}

		if(tags == 0)
			tags = 1;
		return (float)chars/(float)tags;
	}
	 
	public void bodyProcessor(){	

		float densitySumMax = -100;
		float tempMax;
		Element maxDSElement = null;

		doc.select("*:matchesOwn((?is) )").remove(); //remove &nbsp;
		// preprocess page by removing the extraneous tags
		doc.select("script,noscript,style,iframe,br,a,nav").remove();
		
		elements=doc.body().select("*");
		
		for(int i=0;i<elements.size();i++){
			tagCount=0;
			textCount=0;
			Element e=elements.get(i);
			TagInfo taginfo= new TagInfo(e);
			tempMax = countRatio(e, tagCount, textCount);
			if(tempMax > densitySumMax) {
				densitySumMax = tempMax;
				maxDSElement = e;
			}
			if(tagCount == 0)
				tagCount = 1;
			taginfo.setTag(tagCount);
			taginfo.setLength(textCount);
			taginfo.setPos(i);
			map.put(e, taginfo);
		}
		float minDSThreshold = findMinDS(maxDSElement, densitySumMax);

		removeNoise(minDSThreshold);
		finalContent();
	}

	void removeNoise(float threshold){
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
	}

}
