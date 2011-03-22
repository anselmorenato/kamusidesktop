package org.kasahorow.PALDO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import org.apache.xmlrpc.XmlRpcClient;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;

/**
 *  * class PaldoXMLRPC
 *   */
public class PaldoXMLRPC extends EditLogger implements Runnable
{

    private static String word = "";

    public static void main(String args[])
    {
        word = args[0];

        Thread t = new Thread(new PaldoXMLRPC());
        t.start();
    }

    @Override
    public void run()
    {
        ProgressHandle progress = ProgressHandleFactory.createHandle(
                "Fetching from PALDO...");

        progress.start();

        progress.switchToIndeterminate();

        try
        {
            progress.progress(word);

            XmlRpcClient client = new XmlRpcClient("http://dictionary.kasahorow.com/xmlrpc.php");

            Vector<String> params = new Vector<String>();

            params.addElement("{\"key\":\"arthur\"}");
            params.addElement(word + " type:kentry_sw");
            @SuppressWarnings("unchecked")

            Vector vector = (Vector) client.execute("get.most.relevant", params);

            String paldoKey = "";
            String title = "";

            for (int i = 0; i < vector.size(); i++)
            {
                Hashtable h = (Hashtable) vector.get(i);

                Hashtable entries = (Hashtable) h.get("node");

                // enumerate all the contents of the hashtable
                String key;
                String stateName;

                Enumeration keys = entries.keys();

                while (keys.hasMoreElements())
                {
                    key = (String) keys.nextElement();

                    if (!key.equalsIgnoreCase("body"))
                    {
                        if (key.equalsIgnoreCase("nid"))
                        {
                            paldoKey = (String) entries.get(key);
//                            System.out.print(entries.get(key) + "\t" + args[0] + "\t");
                        }
                        if (key.equalsIgnoreCase("title"))
                        {
                            title = (String) entries.get(key);
//                            System.out.println(entries.get(key));
                        }
                    }
                }



                //Insert into db
                if (paldoKey.trim().length() != 0 && !word.contains("*"))
                {
                    Connection connection = DriverManager.getConnection(Translator.DATABASE);

                    String query = "INSERT INTO DICT (PaldoKey, EnglishWord, SwahiliWord) "
                            + "VALUES (?, ?, ?)";

                    PreparedStatement statement = connection.prepareStatement(query);

                    statement.setString(1, paldoKey);
                    statement.setString(2, word.trim());
                    statement.setString(3, title.trim());


                    statement.executeUpdate();

                    connection.close();

                    StatusDisplayer.getDefault().
                            setStatusText("Loaded entry for " + word + " from PALDO");
                }
            }
        }
        catch (java.lang.ClassCastException ex)
        {
            StatusDisplayer.getDefault().
                    setStatusText("Error while fetching from PALDO: "
                    + ex.getMessage());
        }
        catch (Exception ex)
        {
            Exceptions.printStackTrace(ex);
        }
        finally
        {
            progress.finish();
        }
    }
}
