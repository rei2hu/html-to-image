package HTMLParser.TagNodes;

public class content extends InlineTagNode {

    private String content = "";

    public content(String s) {
        content = s;
    }

    public String toString() {
        return content;
    }
}