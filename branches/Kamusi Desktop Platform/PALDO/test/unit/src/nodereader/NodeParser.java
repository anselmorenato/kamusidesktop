package nodereader;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import javax.swing.JOptionPane;

/**
 * NodeParser.java
 * Created on Mar 9, 2011, 9:44:39 AM
 * @author arthur
 */
/**
 * class NodeParser
 */
public class NodeParser
{

    /**
     * Gets the value of a key from a Drupal Node content
     * @param key The key to get the value of (eg nid, uid, body, title etc)
     * @param node The node content
     * @return Value of the key
     */
    public static String getKey(String key, String node)
    {
        //Parse the node content
        Map<String, String> map = new LinkedHashMap<String, String>();

        for (String keyValue : node.split(" *, *"))
        {
            String[] pairs = keyValue.split(" *= *", 2);
            map.put(pairs[0], pairs.length == 1 ? "" : pairs[1]);
        }

        return map.get(key);
    }

    public static void main(String[] args)
    {
        String node = "{uid=3, body=Editing <i><b>English Swahili</b></i> Dictionary<hr>PALDO Entry: http://words.fienipa.com/node/119488"
                + "Editing <b>SwahiliWord</b>"
                + "Old Word: sampletest2"
                + "New Word: sampletest2vvvv, data=a:1:{s:13:\"form_build_id\";s:37:\"form-ed31658feb6b05b507fc07bfe9282d1d\";}, type=story, last_comment_name=, changed=1299742071, title=KamusiDesktop Edit (1299673699256), created=1299673700, name=arthur, revision_uid=1, taxonomy=[Ljava.lang.Object;@a9c165, tnid=0, vid=25, comment_count=0, status=1, nid=25, moderate=0, format=2, log=, picture=, sticky=0, promote=1, teaser=Editing <i><b>English Swahili</b></i> Dictionary<hr>PALDO Entry: http://words.fienipa.com/node/119488"
                + "Editing <b>SwahiliWord</b>"
                + "Old Word: sampletest2"
                + "New Word: sampletest2vvvv, last_comment_timestamp=1299673700, revision_timestamp=1299742071, translate=0, language=, comment=2}";

        Map<String, String> map = new LinkedHashMap<String, String>();

        for (String keyValue : node.split(" *, *"))
        {
            String[] pairs = keyValue.split(" *= *", 2);
            map.put(pairs[0], pairs.length == 1 ? "" : pairs[1]);
        }

        System.out.println(map.get("body"));

        String newContent = map.get("body");

        String columnName = newContent.substring(
                        newContent.indexOf("Editing <b>", newContent.indexOf("words.fienipa.com")) + "Editing <b>".length(),
                        newContent.indexOf("</b>", newContent.indexOf("Editing <b>",
                        newContent.indexOf("words.fienipa.com")))).trim();

        System.out.println("SET: " + columnName);

        System.out.println("NEWWORD: "
                + newContent.substring(newContent.indexOf("New Word:") + "New Word:".length()).trim());

        System.out.println("PALDOKEY: "
                + newContent.substring(newContent.indexOf("http://words.fienipa.com/node/")
                + "http://words.fienipa.com/node/".length(),
                newContent.indexOf("Editing", newContent.indexOf("http://words.fienipa.com/node/"))));

    }
}
