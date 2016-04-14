import com.sun.tools.doclets.internal.toolkit.util.DocFinder;

/**
 * Created by dobatake on 4/14/16.
 */
public class OutputWriterTest {
    public static void main(String[] args) {
        OutputWriter ow = new OutputWriter();
        ow.initStore();
        ow.constructHTML();
    }
}
