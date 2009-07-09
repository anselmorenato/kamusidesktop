/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package englistswahiliitglossary;

import javax.swing.*;
import java.sql.*;
import java.util.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public class ResultTable extends DefaultTableModel
{

    String database = "jdbc:sqlite:kamusiproject.db";
    String dbuser = "";
    String dbpass = "";
    JTable table;
    Connection connection;
    Statement stmt;
    ResultSet rs;
    String englishWord, swahiliWord, englishExample, swahiliExample;

    public ResultTable(String fromLanguage, String word)
    {
        Vector heading = new Vector();

        Vector data = new Vector();

        try
        {
            Class.forName("org.sqlite.JDBC").newInstance();
            connection = DriverManager.getConnection(database, dbuser, dbpass);
            stmt = connection.createStatement();

            String query = "";

            if (fromLanguage.equalsIgnoreCase("ENGLISH"))
            {
                query = "select EnglishSortBy, SwahiliSortBy, EnglishExample, SwahiliExample from " +
                        "dict where EnglishSortBy like ?";
                heading.addElement("English");
                heading.addElement("Swahili");
                heading.addElement("English Example");
                heading.addElement("Swahili Example");
            }
            else if (fromLanguage.equalsIgnoreCase("SWAHILI"))
            {
                query = "select SwahiliSortBy, EnglishSortBy, EnglishExample, SwahiliExample from " +
                        "dict where SwahiliSortBy like ?";
                heading.addElement("Swahili");
                heading.addElement("English");
                heading.addElement("English Example");
                heading.addElement("Swahili Example");

            }
            else
            {
                throw new Exception("Undefined language!");
            }

            PreparedStatement statement = connection.prepareStatement(query);

            statement.setString(1, "%" + word + "%");

            rs = statement.executeQuery();

            while (rs.next())
            {
                englishWord = rs.getString("EnglishSortBy");
                swahiliWord = rs.getString("SwahiliSortBy");
                englishExample = rs.getString("EnglishExample");
                swahiliExample = rs.getString("SwahiliExample");

                //create a row
                Vector tmp = new Vector();

                if (fromLanguage.equalsIgnoreCase("ENGLISH"))
                {
                    tmp.addElement(englishWord);
                    tmp.addElement(swahiliWord);
                    tmp.addElement(englishExample);
                    tmp.addElement(swahiliExample);
                }
                else if (fromLanguage.equalsIgnoreCase("SWAHILI"))
                {
                    tmp.addElement(swahiliWord);
                    tmp.addElement(englishWord);
                    
                    tmp.addElement(englishExample);
                    tmp.addElement(swahiliExample);
                }

                //add to model
                data.addElement(tmp);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        this.table = new JTable(data, heading);

        TableModel model = this.table.getModel();

    }

    public JTable getTable()
    {
        return this.table;
    }

    public TableModel getTableModel()
    {
        TableModel model = this.table.getModel();

        return model;
    }
}

