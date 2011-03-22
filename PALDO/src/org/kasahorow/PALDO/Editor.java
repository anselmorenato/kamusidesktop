/**
 * Editor.java
 * Created on Sep 15, 2010, 9:44:20 AM
 * @author arthur
 */
package org.kasahorow.PALDO;

import credentials.UserManagementPanel;
import drupaljava.DrupalNode;
import drupaljava.DrupalService;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Vector;
import org.apache.xmlrpc.XmlRpcClient;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

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
    private String username;
    private String password;

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

    public String getPaldoID(int rowNumber)
    {
        int rowFetched = 0;

        boolean wildCardSearch = searchKey.contains("*");

        String query = new Translator().getQuery(language, wildCardSearch);

        Connection connection = null;

        try
        {
            connection = DriverManager.getConnection(Translator.DATABASE);

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
        finally
        {
            try
            {
                connection.close();
            }
            catch (SQLException ex)
            {
                Exceptions.printStackTrace(ex);
            }
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
            String paldoID = getPaldoID(row);

            this.username = NbPreferences.forModule(UserManagementPanel.class).get("username", "");
            this.password = NbPreferences.forModule(UserManagementPanel.class).get("password", "");

            String logMessage = oldWord + SEPARATOR
                    + newWord + SEPARATOR
                    + columnName + SEPARATOR
                    + paldoID + SEPARATOR
                    + username;

            // Edit local file
            String query = "UPDATE DICT SET " + columnName + " = ? WHERE PaldoKey = ?";

            Connection connection;

            try
            {
                connection = DriverManager.getConnection(Translator.DATABASE);
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, newWord);
                statement.setString(2, paldoID);
                statement.executeUpdate();

                connection.close();

                //Try sending it to PALDO Edit Cache
                DrupalService service = new DrupalService(
                        "http://kamusi.org",
                        "6a52873678803be88b9aa0bf5d0460ff",
                        "http://173.203.102.17/?q=services/xmlrpc");
                service.connect();
                service.login(username, password);

                DrupalNode node = new DrupalNode();
                node.setType(DrupalNode.TYPE_STORY);
                node.setTitle("KamusiDesktop Edit (" + System.currentTimeMillis() + ")");

                StringBuilder bodyContent = new StringBuilder();
                bodyContent.append("Editing <i><b>English Swahili</b></i> Dictionary<hr>");
                bodyContent.append("PALDO Entry: http://words.fienipa.com/node/");
                bodyContent.append(paldoID);
                bodyContent.append("\n\n");
                bodyContent.append("Editing <b>").append(columnName).append("</b>\n\n");
                bodyContent.append("Old Word: ").append(oldWord).append("\n\n");
                bodyContent.append("New Word: ").append(newWord);

                node.setBody(bodyContent.toString());
                
                service.nodeSave(node);
                service.logout();

                logApplicationMessage(String.valueOf(node));

                StatusDisplayer.getDefault().setStatusText("Posted for editor review");

            }
            catch (Exception ex)
            {
                Exceptions.printStackTrace(ex);
            }

            progress.finish();
        }
    }
}
