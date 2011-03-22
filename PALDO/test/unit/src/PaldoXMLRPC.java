import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import org.apache.xmlrpc.XmlRpcClient;

/**
 *  * class PaldoXMLRPC
 *   */
public class PaldoXMLRPC implements Runnable
{

    private static String word = "";

    public static void main(String args[])
    {
//        word = args[0];
        word = "mouse";

        Thread t = new Thread(new PaldoXMLRPC());
        t.start();
    }

    @Override
    public void run()
    {
        try
        {
            XmlRpcClient client = new XmlRpcClient("http://dictionary.kasahorow.com/xmlrpc.php");

            Vector<String> params = new Vector<String>();

            params.addElement("{\"key\":\"arthur\"}");
            params.addElement(word + " type:kentry_sw");
            @SuppressWarnings("unchecked")

            Vector vector = (Vector) client.execute("get.most.relevant", params);

            String paldoKey = "";
            String title = "";
            String priority = "";

            for (int i = 0; i < vector.size(); i++)
            {
                Hashtable h = (Hashtable) vector.get(i);

                Hashtable entries = (Hashtable) h.get("node");

                // enumerate all the contents of the hashtable
                String key;

                Enumeration keys = entries.keys();

                while (keys.hasMoreElements())
                {
                    key = (String) keys.nextElement();

                    //System.out.println(key + " : " + entries.get(key));

                    if (!key.equalsIgnoreCase("body"))
                    {
                        if (key.equalsIgnoreCase("nid"))
                        {
                            paldoKey = (String) entries.get(key);
                            System.out.print(paldoKey + "\t" + word + "\t");
                        }
                        if (key.equalsIgnoreCase("title"))
                        {
                            title = (String) entries.get(key);
                            System.out.print(title + "\t");
                        }
                        if (key.equalsIgnoreCase("priority"))
                        {
                            priority = (String) entries.get(key);
                            System.out.println(priority);
                        }
                    }
                }
            }
        }
        catch (Exception ex)
        {
            //Exceptions.printStackTrace(ex);
        }
    }
}
