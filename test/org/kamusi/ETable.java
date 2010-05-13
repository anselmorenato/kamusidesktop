/**
 * ETest.java
 * Created on Dec 9, 2009, 2:36:50 PM
 * @author arthur
 */
package org.kamusi;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;



public class ETable
{

    public static void main(String[] args)
    {
        new ETable();
    }

    private ETable()
    {
        JFrame f = new JFrame();

        JPanel c = (JPanel) f.getContentPane();
        c.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

// Create the iTunes-like table
        JTable t = new JTable(new MyTableModel());
        t.getTableHeader().setDefaultRenderer(new MyHeaderRenderer());
        t.setDefaultRenderer(Object.class, new MyCellRenderer());
        t.setIntercellSpacing(new Dimension(1, 1));
        t.setShowHorizontalLines(false);
        t.setShowVerticalLines(true);
        t.setGridColor(Color.lightGray);

        JScrollPane sp = new JScrollPane(t,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        sp.setBackground(Color.white);
        c.add(sp);

        f.setSize(500, 500);
        f.setVisible(true);
    }

    /**
     * The cell renderer.
     */
    private class MyCellRenderer extends DefaultTableCellRenderer
    {

        private Color whiteColor = new Color(254, 254, 254);
        private Color alternateColor = new Color(237, 243, 254);
//private Color selectedColor = new Color(61, 128, 223);
        private Color selectedColor = Color.LIGHT_GRAY;

        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean selected, boolean focused,
                int row, int column)
        {
            super.getTableCellRendererComponent(table, value,
                    selected, focused, row, column);

// Set the background color
            Color bg;
            if (!selected)
            {
                bg = (row % 2 == 0 ? alternateColor : whiteColor);
            }
            else
            {
                bg = selectedColor;
            }
            setBackground(bg);

// Set the foreground to white when selected
            Color fg;
            if (selected)
            {
                fg = Color.white;
            }
            else
            {
                fg = Color.black;
            }
            setForeground(fg);

            return this;
        }
    }

    /**
     * The header renderer. All this does is make the text left aligned.
     */
    public class MyHeaderRenderer extends DefaultTableCellRenderer
    {

        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean selected, boolean focused,
                int row, int column)
        {
            super.getTableCellRendererComponent(table, value,
                    selected, focused, row, column);
            setBorder(UIManager.getBorder("TableHeader.cellBorder"));
            return this;
        }
    }

    /**
     * Some bogus data to populate the table.
     */
    private class MyTableModel extends DefaultTableModel
    {

        public int getRowCount()
        {
            return 10;
        }

        public int getColumnCount()
        {
            return 3;
        }

        public String getColumnName(int column)
        {
            switch (column)
            {
                case 0:
                    return "Song Name";
                case 1:
                    return "Time";
                default:
                    return "Artist";
            }
        }

        public Object getValueAt(int row, int column)
        {
            switch (column)
            {
                case 0:
                    return "Fooing In The Wind";
                case 1:
                    return "3:51";
                default:
                    return "Foo Guy";
            }
        }

        public boolean isCellEditable(int row, int column)
        {
            return false;
        }
    }
}
