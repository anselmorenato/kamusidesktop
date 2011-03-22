/**
 * Editor.java
 * Created on Aug 20, 2010, 10:01:58 AM
 * @author arthur
 */
package org.kamusi.dictionary;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 * class Editor
 */
public class Editor extends KamusiLogger implements TableModelListener
{

    private Translator translator;
    private String oldWord = "";
    private String columnName = "";

    public Editor()
    {
        translator = new Translator();
    }

    public void setOldWord(String oldWord)
    {
        this.oldWord = oldWord;
    }

    @Override
    public void tableChanged(TableModelEvent e)
    {
        int row = e.getFirstRow();
        int column = e.getColumn();
        TableModel model = (TableModel) e.getSource();
        columnName = model.getColumnName(column);
        Object newWord = model.getValueAt(row, column);

//        edit(oldWord, (String) newWord, row);
    }

//    private void edit(String oldWord, String newWord, int row)
//    {
//
//        String searchKey = SearchTopComponent.wordField.getText().trim();
//
//        String query = "UPDATE dict SET " + columnName.replaceAll(" ", "")
//                + "='" + newWord + "' WHERE Id=" + getWordPrimaryKey(searchKey, row);
//
//        JOptionPane.showMessageDialog(null, query);
//    }

//    public String getWordPrimaryKey(String searchkey, int rowNumber)
//    {
//        boolean wildCardSearch = searchkey.contains("*");
//
//        try
//        {
//            Connection connection = DriverManager.getConnection(translator.getDatabase());
//
//            String fromLanguage = "";
//
//            if(SearchTopComponent.enToSwButton.isSelected())
//            {
//                fromLanguage = "English";
//            }
//            else if(SearchTopComponent.swToEnButton.isSelected())
//            {
//                fromLanguage = "Swahili";
//            }
//            else
//            {
//                throw new NullPointerException("Language selection error!");
//            }
//
//            String query = translator.getQuery(fromLanguage, wildCardSearch);
//
//            PreparedStatement statement = connection.prepareStatement(query);
//
//            String parameters = wildCardSearch ? searchkey.replaceAll("\\*", "%") : searchkey;
//
//            //There are 5 parameters interface the query
//            for (int i = 1; i <= 5; i++)
//            {
//                statement.setString(i, parameters);
//            }
//
//            ResultSet resultSet = statement.executeQuery();
//
//            String index = "0";
//
//            //The ID -1 is used to exclude the table headers
//            // from the ID count
//            int rowCount = -1;
//
//            while (resultSet.next())
//            {
//                index = resultSet.getString("Id");
//
//                rowCount++;
//
//                if (rowCount == rowNumber)
//                {
//                    connection.close();
//                    return index;
//                }
//            }
//
//            connection.close();
//
//            return null;
//        }
//        catch (Exception ex)
//        {
//            logExceptionStackTrace(ex);
//            return "";
//        }
//    }
}
