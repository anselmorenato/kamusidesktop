/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package englistswahiliitglossary;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Vector;

/**
 *
 * @author arthur
 */
public class Translator
{

    public static void main(String[] args) throws Exception
    {
        Class.forName("org.sqlite.JDBC");
        Connection conn = DriverManager.getConnection("jdbc:sqlite:translate.sqlite3");
        Statement stat = conn.createStatement();

        //conn.setAutoCommit(false);
        //conn.setAutoCommit(true);

        Vector heading = new Vector();
        heading.addElement("English");
        heading.addElement("Swahili");


        Vector data = new Vector();

        ResultSet rs = stat.executeQuery("select English, Swahili from glossary where english like '%cert%'");
        while (rs.next())
        {
            data.addElement(rs.getString("English"));
            data.addElement(rs.getString("swahili"));
            System.out.println("Swahili = " + rs.getString("swahili"));
        }
        rs.close();
        conn.close();
    }
}
