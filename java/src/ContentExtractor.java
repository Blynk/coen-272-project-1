import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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
			doc=Jsoup.parse(input, null);
			title = doc.title();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("File parsing failed");
		}
		
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
		elements=doc.body().children();
		
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
//		finalContent();
	}
	public void removeNoise(){
		//TODO: remove noise based on the length of content under each tag
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
		//System.out.println(title);
		output(title +"\n"+clean);
//		//remove extra spaces
//		clean=clean.replaceAll("&nbsp", " ");
	}
	public void output(String res){
		Path dirPath = Paths.get("").toAbsolutePath();
		dirPath = Paths.get(dirPath.toString(), "output");
		
		try {
			Path resPath = Paths.get(dirPath.toString() +"/"+ title + ".txt");
			Files.createFile(resPath);
			Files.write(resPath, res.getBytes(), StandardOpenOption.CREATE);
		} catch (IOException e) {
			try {
				Files.createDirectory(dirPath);
				output(res);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		}
	}

}
