/**
 * Translator.java
 * Created on Aug 18, 2010, 11:00:34 AM
 * @author arthur
 */
package Kinyarwanda;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import org.openide.awt.StatusDisplayer;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;

/**
 * Translator.java
 * Fetches translations from the database and renders them into a JTable
 */
public class Translator extends EditLogger// extends DefaultTableModel
{

    /**
     * The translation SQLite3 database
     *
     * The initial problem was;
     *
     * How do you bundle a file within your NetBeans module?
     * And then, how to you get access to it after NetBeans is installed?
     *
     * The solution was not very difficult. Here we go with the recipe:
     *
     * Select the "Files" tab in NetBeans, and create a "release/modules/ext" folder.
     * Place your files there.
     * In your module properties, search "InstalledFileLocator" and
     * add a dependency to the module containing it.
     *
     * In your code, whenever you want to access the file, use the following snippet:
     *
     *
     * File myFile = InstalledFileLocator.getDefault()
     * .locate("modules/ext/r5rs-completion.db.db",  // A
     * "net.antonioshome.scheme.completion", // B
     * false);
     *
     * Where:
     *
     * "A" is the name of your file (note the missing "release" directory in the path)
     * "B" is the name of your module (this isn't strictly neccessary,
     * but makes finding the file much faster).
     */
    private static final File database = getDictionaryFile();
    public static String DATABASE = "jdbc:sqlite:" + database;

    /**
     * Initializes the class
     * @param fromLanguage the language from which we are translating
     * @param word the word to translate
     * @param fields the fields which we are interested in
     */
    public Translator()
    {
    }

    public TableModel getTableModel(final String fromLanguage, 
            final String word, final Vector<String> fields,
            String paldoLanguage)
    {

        boolean wildCardSearch = word.contains("*");

        DefaultTableModel dataModel = new DefaultTableModel();

        try
        {
            Connection connection = DriverManager.getConnection(DATABASE);

            String query = "";

            Vector<String> headers = new Vector<String>();

            query = getQuery(fromLanguage, wildCardSearch);

            headers.addElement("English");
            headers.addElement(paldoLanguage);

            Enumeration<String> availableFields = fields.elements();

            while (availableFields.hasMoreElements())
            {
                String element = availableFields.nextElement();
                headers.addElement(element);
            }

            PreparedStatement statement = connection.prepareStatement(query);

            String parameters = wildCardSearch ? word.replaceAll("\\*", "%") : word;

            java.sql.ParameterMetaData p = statement.getParameterMetaData();

            //For each parameter interface the query
            for (int i = 1; i <= p.getParameterCount(); i++)
            {
                statement.setString(i, parameters);
            }

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next())
            {
                //create a row
                Vector<String> rowData = new Vector<String>();

                rowData.add(resultSet.getString("EnglishWord"));
                rowData.add(resultSet.getString(paldoLanguage + "Word"));

                //add to model
                dataModel.setColumnIdentifiers(headers);
                dataModel.addRow(rowData);
            }

            int row = dataModel.getRowCount();

            if (row > 0)
            {
                String message = row + " results matched for \"" + word + "\" from " + fromLanguage;
                message += (fields.isEmpty() ? "" : " " + fields);

                StatusDisplayer.getDefault().setStatusText(message);
            }
            else
            {
                StatusDisplayer.getDefault().setStatusText("Nothing found for " + word);
            }

            connection.close();
        }
        catch (Exception ex)
        {
            Exceptions.printStackTrace(ex);
            System.out.println("There is an error here");
        }


        return dataModel;
    }

    public String getDatabase()
    {
        return DATABASE;
    }

    /**
     * Returns the canonical path name of the database
     */
    public String getDictionaryPath()
    {
        try
        {
            return getDictionaryFile().getCanonicalPath();
        }
        catch (IOException ex)
        {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    /**
     * Returns the File Object of the database
     */
    private static File getDictionaryFile()
    {
        return InstalledFileLocator.getDefault().
                locate("modules/ext/paldo_en_rw.db", // A
                "kamusidesktop.db", // B
                false);
    }

    /**
     * The query used to perform searches in the database
     * @param language The language from which we are translating
     * @param wildcardSearch Is the search a wild card search?
     * @return The appropriate SQL query
     */
    public String getQuery(String language, boolean wildcardSearch)
    {
        language = language.trim();

        return (wildcardSearch)
                ? "SELECT * FROM dict "
                + "WHERE " + language + "Word LIKE ? "
                + "COLLATE NOCASE ORDER BY Id ASC"
                : "SELECT * FROM dict "
                + "WHERE " + language + "Word=? COLLATE NOCASE";
    }
}
