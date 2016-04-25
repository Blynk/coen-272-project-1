import org.jsoup.parser.Tag;

public class TagInfo {
	Tag tag;
	String content;
	int len;
	
	public TagInfo(Tag tag){
		this.tag=tag;
	}
	void setContent(String content){
		this.content=content;
	}
	void setLength(int len){
		this.len=len;
	}
	

}
