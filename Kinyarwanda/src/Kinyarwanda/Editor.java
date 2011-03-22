/**
 * Editor.java
 * Created on Sep 15, 2010, 9:44:20 AM
 * @author arthur
 */
package Kinyarwanda;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Vector;
import org.apache.xmlrpc.XmlRpcClient;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;

/**
 * class Editor
 */
public class Editor extends EditLogger implements Runnable
{

    private final String SEPARATOR = "@";
    private String oldWord;
    private String newWord;
    private String columnName;
    private int row;
    private ArrayList<String> languageSet;
    private String searchKey;
    private String language;

    public Editor(String language, String oldWord, String newWord,
            String columnName, int row, String searchKey)
    {
        this.row = row;
        this.columnName = columnName;
        this.newWord = newWord;
        this.oldWord = oldWord;
        this.searchKey = searchKey;
        this.language = language;
        languageSet = new ArrayList<String>();
        languageSet.add("en");
        languageSet.add("sw");

    }

    public String getPaldoKey(int rowNumber)
    {
        int rowFetched = 0;

        boolean wildCardSearch = searchKey.contains("*");

        String query = new Translator().getQuery(language, wildCardSearch);

        try
        {
            Connection connection = DriverManager.getConnection(Translator.DATABASE);

            PreparedStatement statement = connection.prepareStatement(query);

            String parameters = wildCardSearch ? searchKey.replaceAll("\\*", "%") : searchKey;

            ParameterMetaData parametaMetaData = statement.getParameterMetaData();

            //For each parameter interface the query
            for (int i = 1; i <= parametaMetaData.getParameterCount(); i++)
            {
                statement.setString(i, parameters);
            }

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next())
            {
                //create a row
                if (rowFetched == row)
                {
                    return resultSet.getString("PaldoKey");
                }

                rowFetched++;
            }

            connection.close();
        }
        catch (Exception ex)
        {
            Exceptions.printStackTrace(ex);
        }

        return String.valueOf(rowFetched);
    }

    @Override
    public void run()
    {
        if (newWord.contains(SEPARATOR))
        {
            boolean interrupted = Thread.interrupted();
        }

        ProgressHandle progress = ProgressHandleFactory.createHandle(
                "Editing PALDO...");

        progress.start(1000);
        progress.switchToIndeterminate();

        if (newWord.trim().equalsIgnoreCase(oldWord.trim()))
        {
            StatusDisplayer.getDefault().setStatusText("No changes made!");
            progress.finish();
        }
        else
        {
            //Get the PALDO key
            String paldoKey = getPaldoKey(row);

            String logMessage = oldWord + SEPARATOR
                    + newWord + SEPARATOR
                    + columnName + SEPARATOR
                    + paldoKey + SEPARATOR
                    + columnName;

            // Edit local file
            String query = "UPDATE DICT SET " + columnName + " = ? WHERE PaldoKey = ?";

            Connection connection;

            try
            {
                connection = DriverManager.getConnection(Translator.DATABASE);
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, newWord);
                statement.setString(2, paldoKey);

                //TODO: Enable this flag
                //statement.executeUpdate();

                XmlRpcClient client = new XmlRpcClient("http://dictionary.kasahorow.com/xmlrpc.php");

                Vector<String> params = new Vector<String>();

                params.addElement("{'key':'4010802052'}");
                params.addElement("{'iso':'en', {'nid':'" + paldoKey + "', 'title':'" + newWord + "'}");

                client.execute("save.node", params);

                //Try sending it to PALDO
                StringBuilder requestURL = new StringBuilder();

                requestURL.append(SYNC_URL).
                        append("?update=").append(URLEncoder.encode(logMessage, "UTF-8"));

                URL url = new URL(requestURL.toString());

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; JVM)");
                conn.setRequestProperty("Pragma", "no-cache");
                conn.setDoOutput(true);
                conn.connect();

                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                String line = null;

                while ((line = rd.readLine()) != null)
                {
                    progress.progress(line);
                    StatusDisplayer.getDefault().setStatusText(line);
                }

            }
            catch (Exception ex) //Or commit it later
            {
                //Log to local file
                logApplicationMessage(logMessage);

                Exceptions.printStackTrace(ex);
            }

            //Get the row'th entry of the search string interface PALDO

            //Log to online app

            //Commit to db
            progress.finish();
        }
    }
}
