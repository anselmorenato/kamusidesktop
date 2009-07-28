/*
 * AboutBox.java
 *
 * Created on Jul 23, 2009, 10:32:03 AM
 */
package org.kamusi;

import javax.swing.ImageIcon;

/**
 *
 * @author arthur
 */
public class AboutBox extends javax.swing.JDialog
{

    /** Creates new form AboutBox */
    public AboutBox(java.awt.Frame parent, boolean modal)
    {
        super(parent, modal);
        initComponents();
        updateAboutBox();
    }

    private void initComponents()
    {

        buttonPanel = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        contentPanel = new javax.swing.JPanel();
        imagePanel = new javax.swing.JPanel();
        imageLabel = new javax.swing.JLabel();
        contentPane = new javax.swing.JScrollPane();
        contentArea = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("About");
        setAlwaysOnTop(true);
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setIconImage(null);
        setMinimumSize(new java.awt.Dimension(500, 250));
        setResizable(false);

        okButton.setFont(new java.awt.Font("Dialog", 0, 12));
        okButton.setMnemonic('C');
        okButton.setText("Close");
        okButton.setToolTipText("Close this dialog");
        okButton.addActionListener(new java.awt.event.ActionListener()
        {

            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                okButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(okButton);

        getContentPane().add(buttonPanel, java.awt.BorderLayout.SOUTH);

        contentPanel.setLayout(new java.awt.BorderLayout());

        imageLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/kamusi/resources/logo.png"))); // NOI18N
        imagePanel.add(imageLabel);

        contentPanel.add(imagePanel, java.awt.BorderLayout.NORTH);

        contentArea.setColumns(20);
        contentArea.setLineWrap(true);
        contentArea.setRows(5);
        contentArea.setWrapStyleWord(true);
        contentPane.setViewportView(contentArea);

        contentPanel.add(contentPane, java.awt.BorderLayout.CENTER);

        getContentPane().add(contentPanel, java.awt.BorderLayout.CENTER);

        pack();
    }

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_okButtonActionPerformed
    {
        dispose();
    }
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JTextArea contentArea;
    private javax.swing.JScrollPane contentPane;
    private javax.swing.JPanel contentPanel;
    private javax.swing.JLabel imageLabel;
    private javax.swing.JPanel imagePanel;
    private javax.swing.JButton okButton;

    private void updateAboutBox()
    {
        final String about =
                "Product Version: " + "Kamusi Project Desktop Build 7" + "\n" +
                "Java: " + System.getProperty("java.version") + "\n" +
                "System: " + System.getProperty("os.name") + " version " + System.getProperty("os.version") + "\n";

        final String disclaimer =
                "\nKamusi Desktop and Kamusi Project are based on software from kamusi.org " +
                "which has been licensed under the GNU General Public License version 2. " +
                "For more information, please visit www.kamusi.org.\n\n";

        contentArea.setEditable(false);
        contentArea.setText(about + disclaimer);

        setIconImage(new ImageIcon("favicon.png").getImage());
    }
}
