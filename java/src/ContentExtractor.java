import java.io.File;
import java.io.IOException;
import java.nio.file.*;
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

	public ContentExtractor(String path){
		map= new HashMap<Element, TagInfo>();
		File input = new File(path);
		try {
			doc=Jsoup.parse(input, "UTF-8");
			title = doc.title();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("File parsing failed");
		}
		
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

	// Not used -- want to keep
	public TagInfo countRatio(Element e) {

		TagInfo thisTag = new TagInfo(e);
		for (Node n : e.children()) {
			TagInfo childTag = countRatio((Element) n);
			thisTag.setLength(thisTag.getLength() + childTag.getLength());
		}
		thisTag.setTag(countTags(e));
		if (thisTag.getTagCount() == 0)
			thisTag.setTag(1);
		thisTag.setLength(thisTag.getLength() + countChars(e));

		return thisTag;
	}

	public TagInfo countRatio(Element e, TagInfo ti){
		for(Node n:e.childNodes()){
			if(n instanceof Element){
				int tmp=ti.getTagCount()+1;
				ti.setTag(tmp);
				ti = countRatio((Element)n, ti);
			}else if(n instanceof TextNode){
				String text=((TextNode)n).text().trim();
				int tmp =ti.getLength()+text.split(" ").length;
				ti.setLength(tmp);
			}
		}
		return ti;
	}
	 
	public void bodyProcessor(){
		//doc.select("*:matchesOwn((?is) )").remove(); //remove &nbsp;
		doc.select("script,noscript,style,iframe,br,nav, head,footer,img,header,button,input,form,a").remove();
		//elements=doc.body().select("*");
		elements=doc.body().children();
		
		for(int i=0;i<elements.size();i++){

			Element e=elements.get(i);
			TagInfo taginfo= new TagInfo(e);
			taginfo=countRatio(e, taginfo);
			taginfo.setPos(i);
			map.put(e, taginfo);
		}
		removeNoise();
	}


	// Not used -- want to keep
	public void removeNoise(float threshold) {
		for (Map.Entry<Element, TagInfo> t : map.entrySet()) {
			TagInfo tmp = t.getValue();
			if ((float) tmp.getLength() / (float) tmp.getTagCount() < threshold) {
				elements.get(tmp.getPos()).remove();
			}
		}
	}

	public void removeNoise() {
			Iterator<Map.Entry<Element, TagInfo>> it=map.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<Element, TagInfo> pair=it.next();
			TagInfo tmp=pair.getValue();
			if((double)tmp.getLength()/(double)(tmp.getTagCount()+1)<2){
				elements.get(tmp.getPos()).remove();
			}	
		}
		
		String html = doc.text();
		String clean=Jsoup.clean(html, Whitelist.basic()); 
		clean = clean.replaceAll("&nbsp", " ");
		output(title +"\n"+clean);
	}

	public void output(String res) {
		Path dirPath = Paths.get("").toAbsolutePath();
		dirPath = Paths.get(dirPath.toString(), "output");
		if (!Files.exists(dirPath, LinkOption.NOFOLLOW_LINKS)) {
			try {
				Files.createDirectory(dirPath);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		try {
			if(title.isEmpty())
				return;

			String filename = Paths.get(doc.location())
					.getFileName()
					.toString();
			Path resPath = Paths.get(dirPath.toString() + "/" + filename
					.replaceFirst("^(http://www\\.|http://|www\\.)","")
					.replaceAll("/", "-")
					.substring(0, filename.length()-5) + ".txt");
			//Files.createFile(resPath);
			Files.write(resPath, res.getBytes(), StandardOpenOption.CREATE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
