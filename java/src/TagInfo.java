import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;

import static java.lang.Float.valueOf;

public class TagInfo {
	Element e;
	String content;
	int len;
	int position;
	int tag;
	Float densitySum;
	Float childMinDS;

	public TagInfo(Element e){
		this.e=e;
		tag=0;
		len=0;
		densitySum = new Float(0);
		childMinDS = new Float(100);
	}

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

	public Float getDensitySum() {
		return densitySum;
	}

	public void setDensitySum(int tags, int chars) {
		this.densitySum += valueOf((float) chars/ (float) tags);
	}

	public Float getChildMinDS(){
		return childMinDS;
	}

	public void setChildMinDS(int tags, int chars){
		Float tempMinDS = valueOf((float) chars / (float) tags);
		if(tempMinDS < childMinDS)
			childMinDS = tempMinDS;
	}

}
