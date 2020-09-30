import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexTest {
    public static void main(String[] args) {
        String S =
                "background-image: url(\"//s-delivery8.mxdcontent.net/thumbs/974f6227ed1653a87b4781bcbddc5bc3.jpg\");";
        String P = ".*//(s-delivery.*)/thumbs/(.*)\\.jpg.*";

        System.out.println("S: " + S);
        System.out.println("P: " + P);

        Pattern pattern = Pattern.compile(P);
        Matcher matcher = pattern.matcher(S);

        if (matcher.matches()) {
            System.out.println("matches");
            System.out.println(matcher.group(1));
            System.out.println(matcher.group(2));
        }
        else {
            System.out.println("no matches");
        }
    }
}
