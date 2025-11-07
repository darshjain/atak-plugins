import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestCallsign {
    private static final Pattern CALLSIGN_ATTR_RE =
            Pattern.compile("<contact[^>]*\\bcallsign\\s*=\\s*\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);
    private static final Pattern CALLSIGN_NAME_RE =
            Pattern.compile("<contact[^>]*\\bname\\s*=\\s*\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);
    private static final Pattern CALLSIGN_TAG_RE =
            Pattern.compile("(?is)<callsign[^>]*>([^<]+)</callsign>");

    public static String extract(String xml) {
        if (xml == null) return "";
        Matcher m = CALLSIGN_ATTR_RE.matcher(xml);
        if (m.find()) return m.group(1);
        m = CALLSIGN_NAME_RE.matcher(xml);
        if (m.find()) return m.group(1);
        m = CALLSIGN_TAG_RE.matcher(xml);
        if (m.find()) return m.group(1).trim();
        return "";
    }

    public static void main(String[] args) {
        String[] samples = new String[]{
            "<event><detail><contact callsign=\"ALPHA01\"/></detail></event>",
            "<event><detail><contact name=\"BRAVO02\" something=\"x\"/></detail></event>",
            "<event><detail><contact><callsign>CHARLIE03</callsign></contact></detail></event>",
            "<event><detail><contact></contact></detail></event>"
        };
        for (String s : samples) {
            System.out.println("XML: " + s);
            System.out.println("Extracted: '" + extract(s) + "'\n");
        }
    }
}
