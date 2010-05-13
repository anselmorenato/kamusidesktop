/**
 * RawTest.java
 * Created on Dec 15, 2009, 11:02:19 PM
 * @author arthur
 */
package org.kamusi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.IXMLReader;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLParserFactory;
import net.sf.json.JSON;
import net.sf.json.JSONSerializer;
import net.sf.json.xml.XMLSerializer;

/**
 * class RawTest
 */
public class RawTest
{

    public static void main(String[] args)
    {
        try
        {

            String from = "en";
            String to = "sw";
            String word = "cat";
            String key = "arthur";

            String link = "http://translate.fienipa.com/?q=kjson&method=get.links&args="
                    + word + ","
                    + from + ","
                    + to
                    + "&key=" + key
                    + "&format=json";

            URL url = new URL(link);
            URLConnection conn = url.openConnection();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String responseLine;

            String flag = "";

            while ((responseLine = bufferedReader.readLine()) != null)
            {
                // Process line...
                flag = responseLine;
            }

            flag = flag.replaceFirst("kasahorow_translation\\(\\{\"translations\": \\[(.*?)\\]\\}\\);", "");

            flag = flag.replaceFirst("kasahorow_callback\\(", "");

            flag = flag.replaceFirst("\\);", "");

            XMLSerializer serializer = new XMLSerializer();
            JSON json = JSONSerializer.toJSON(flag);
            String xml = serializer.write(json);

            File xmlFile = new File("test.xml");
            FileOutputStream fos = new FileOutputStream(xmlFile);
            fos.write(xml.getBytes());
            fos.close();

            IXMLParser parser //2
                    = XMLParserFactory.createDefaultXMLParser();
            IXMLReader reader //3
                    = StdXMLReader.fileReader("test.xml");
            parser.setReader(reader);

            IXMLElement xml2 //4
                    = (IXMLElement) parser.parse();

            int numberOfResults = xml2.getChildAtIndex(0).getChildrenCount();

            for (int i = 0; i < numberOfResults; i++)
            {
                //System.out.println("============================= RESULT " + (i + 1) + " =============================");

                int childCount = xml2.getChildAtIndex(0).getChildAtIndex(i).getChildrenCount();

                for (int j = 0; j < childCount; j++)
                {
                    String attrib = xml2.getChildAtIndex(0).getChildAtIndex(i).getChildAtIndex(j).getFullName();
                    String value = xml2.getChildAtIndex(0).getChildAtIndex(i).getFirstChildNamed(attrib).getContent();

                    if (attrib.trim().equalsIgnoreCase("otitle"))
                    {
                        System.out.println((i + 1) + ". " + value);
                    }
                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
