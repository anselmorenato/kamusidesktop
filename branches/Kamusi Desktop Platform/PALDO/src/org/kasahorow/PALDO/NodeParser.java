package org.kasahorow.PALDO;

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
        String node = "uid=3, body=My name is Graham Masinde, data=a:1:{s:13:\"form_build_id\";s:37:\"form-ed31658feb6b05b507fc07bfe9282d1d\";}, type=story, last_comment_name=, changed=1299584224, title=Name, created=1299157495, name=arthur, revision_uid=1, taxonomy=[Ljava.lang.Object;@16e0054, tnid=0, vid=16, comment_count=0, status=1, nid=16, moderate=0, format=1, log=, picture=, sticky=0, promote=1, teaser=My name is Graham Masinde, last_comment_timestamp=1299157495, revision_timestamp=1299584224, translate=0, language=, comment=2";

        Map<String, String> map = new LinkedHashMap<String, String>();

        for (String keyValue : node.split(" *, *"))
        {
            String[] pairs = keyValue.split(" *= *", 2);
            map.put(pairs[0], pairs.length == 1 ? "" : pairs[1]);
        }

        System.out.println(map.get("body"));

    }
}
