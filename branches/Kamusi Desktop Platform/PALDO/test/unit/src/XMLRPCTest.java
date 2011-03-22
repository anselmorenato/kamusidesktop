
/**
 * XMLRPCTest.java
 * Created on Sep 23, 2010, 8:46:45 AM
 * @author arthur
 */
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import org.apache.xmlrpc.XmlRpcClient;

public class XMLRPCTest
{

    public static void main(String[] args)
    {
        try
        {
            XmlRpcClient client = new XmlRpcClient("http://dictionary.kasahorow.com/xmlrpc.php");

            Vector<String> params = new Vector<String>();

            params.addElement("{\"key\":\"arthur\"}");
            params.addElement("cat type:kentry_sw");
            @SuppressWarnings("unchecked")

            Vector vector = (Vector) client.execute("get.most.relevant", params);

            outer:
            for (int i = 0; i < vector.size(); i++)
            {
                System.out.println("%-%-%-%-%-%-%-%-%-%-%-%-%");
                System.out.println(" ENTRY " + (i + 1));
                System.out.println("%-%-%-%-%-%-%-%-%-%-%-%-%");

                Hashtable h = (Hashtable) vector.get(i);

                Hashtable entries = (Hashtable) h.get("node");

                // enumerate all the contents of the hashtable
                String key;

                Enumeration keys = entries.keys();

                inner:
                while (keys.hasMoreElements())
                {
                    key = (String) keys.nextElement();

                    if (!key.equalsIgnoreCase("body"))
                    {
                        System.out.println(key + " -> " + entries.get(key));
                    }

                    System.out.println("----------------------------------------");
                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
