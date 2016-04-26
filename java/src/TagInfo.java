import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;

public class TagInfo {
	Element e;
	String content;
	int len;
	int position;
	int tag;
	
	public TagInfo(Element e){
		this.e=e;
	}
//	void setContent(String content){
//		this.content=content;
//	}
	void setLength(int len){
		this.len=len;
	}
	void setPos(int position){
		this.position=position;
	}
	int getPos(){
		return position;
	}
	int getLength(){
		return len;
	}
	int getTagCount(){
		return tag;
	}
	void setTag(int tag){
		this.tag=tag;
	}
	

}
