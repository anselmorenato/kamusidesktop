/*
 * InstallerWindow.java
 *
 * Created on Jul 29, 2009, 7:49:02 PM
 */
package installer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 *
 * @author arthur
 */
public class InstallerWindow extends javax.swing.JFrame 
{

    private ApplicationProperties properties;
    private FileCopier fileCopier;
    private ProgressViewer progressViewer;
    private File file;
    private InstallLogger logger = new InstallLogger();

    /** Creates new form InstallerWindow */
    public InstallerWindow()
    {
        initComponents();
        loadInstallProperties();
    }

    /**
     * Gets the size of the specified file
     */
    public long getSizeOfDatabase()
    {
        long size = 0;

        File database = file;
        size = database.length();

        return size;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        installDirBrowseButton = new javax.swing.JButton();
        installButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        installationDirectoryField = new javax.swing.JTextField();
        directoryLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        installDirBrowseButton.setMnemonic('B');
        installDirBrowseButton.setText("Browse");
        installDirBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                installDirBrowseButtonActionPerformed(evt);
            }
        });

        installButton.setMnemonic('I');
        installButton.setText("Install");
        installButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                installButtonActionPerformed(evt);
            }
        });

        cancelButton.setMnemonic('C');
        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        installationDirectoryField.setColumns(30);

        directoryLabel.setText("Directory to Install to:");

        progressBar.setString("Ready to Install");
        progressBar.setStringPainted(true);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 521, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(directoryLabel, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(installationDirectoryField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 434, Short.MAX_VALUE)
                            .addComponent(installButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(installDirBrowseButton))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(115, Short.MAX_VALUE)
                .addComponent(directoryLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(installationDirectoryField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(installDirBrowseButton))
                .addGap(18, 18, 18)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(41, 41, 41)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(installButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelButtonActionPerformed
    {//GEN-HEADEREND:event_cancelButtonActionPerformed
        // TODO add your handling code here:
        ScriptCreator.removeDesktopShortcut();
        System.exit(0);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void installButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_installButtonActionPerformed
    {//GEN-HEADEREND:event_installButtonActionPerformed
        {

            try
            {
                String installPath = installationDirectoryField.getText().trim();
                if (installPath.length() == 0)
                {
                    throw new java.lang.NullPointerException("Required value missing");
                }

                file = new File(installPath);

                if (file.exists())
                {
                    if (file.isDirectory())
                    {
                        copyFiles();
                    }
                    else
                    {
                        JOptionPane.showMessageDialog(null, file.getCanonicalPath() +
                                " is an (existing) file. Please select a directory.",
                                properties.getMainApplicationName(), JOptionPane.WARNING_MESSAGE);
                    }
                }
                else
                {
                    String message = "The directory you have specified does not exist.\n" +
                            "Would you like " + properties.getMainApplicationName() + " to create " +
                            "it for you?";

                    Object[] options =
                    {
                        "Yes",
                        "No"
                    };

                    int choice = JOptionPane.showOptionDialog(null,
                            message,
                            properties.getMainApplicationName(),
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null, //do not use a custom Icon
                            options, //the titles of buttons
                            options[0]); //default button title

                    switch (choice)
                    {
                        case 0: //YES

                            file.mkdirs();
                            copyFiles();
                            break;

                        case 1: //NO
                            break;

                        case -1: //Closed Window
                            break;

                        default:
                            break;
                    }
                }

            }
            catch (FileNotFoundException ex)
            {
                logger.log(ex.toString());
            }
            catch (IOException ex)
            {
                logger.log(ex.toString());
            }
            catch (java.lang.NullPointerException ex)
            {
                logger.log(ex.toString());
                JOptionPane.showMessageDialog(null, "Required \"Install Directory\" field cannot be left empty",
                        properties.getMainApplicationName(), JOptionPane.WARNING_MESSAGE);
            }
        }

    }//GEN-LAST:event_installButtonActionPerformed

    private void installDirBrowseButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_installDirBrowseButtonActionPerformed
    {//GEN-HEADEREND:event_installDirBrowseButtonActionPerformed
        // TODO add your handling code here:
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.showOpenDialog(this);
        try
        {
            String chosenDirectory = fileChooser.getSelectedFile().getCanonicalPath();
            installationDirectoryField.setText(chosenDirectory);
        }
        catch (IOException ex)
        {
            logger.log(ex.toString());
        }
        catch (java.lang.NullPointerException ex)
        {
            // No file selected
            logger.log(ex.toString());
        }
    }//GEN-LAST:event_installDirBrowseButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel directoryLabel;
    private javax.swing.JButton installButton;
    private javax.swing.JButton installDirBrowseButton;
    private javax.swing.JTextField installationDirectoryField;
    private javax.swing.JProgressBar progressBar;
    // End of variables declaration//GEN-END:variables

    private void loadInstallProperties()
    {
            properties = new ApplicationProperties();

            String title = properties.getMainApplicationName() + " Installation";
            setTitle(title);

            String defaultInstallDirectory = properties.getDefaultLocation();
            installationDirectoryField.setText(defaultInstallDirectory);
    }

    private void createRunScript()
    {
        try
        {
            ScriptCreator.createDesktopShortcut(file.getCanonicalPath());
        }
        catch (IOException ex)
        {
            logger.log(ex.toString());
        }
    }

    class FileCopier extends Thread
    {

        private boolean success = true;

        @Override
        public void run()
        {
            // Do the following for each file:
            // 1. Copy the main jar file
            String mainJarFile = properties.getAppJar();
            copyFile(new File(mainJarFile));
            // 2. Copy the app logo
            String logo = properties.getLogo();
            copyFile(new File(logo));
            // 3. copy the libraries accordint to their folders
            String[] requiredLibraries = properties.getRequiredLibraries();

            for (String library : requiredLibraries)
            {
                File fileToBeCopied = new File(library);
                logger.log("Copying " + library);
                copyFile(fileToBeCopied);
            }
            // 4. Create the bat or sh file depending on the target os
            progressBar.setIndeterminate(false);
            progressBar.setValue(100);
            progressBar.setString("Installing Launcher ( 100% )");
            createRunScript();
            JOptionPane.showMessageDialog(null, properties.getMainApplicationName() +
                    " has been installed successfully.\n\n" +
                    "You may launch the application by double-clicking its icon on your desktop.",
                    properties.getMainApplicationName(), JOptionPane.INFORMATION_MESSAGE);

            System.exit(0);
//            
        }

        private void copyFile(File fileToCopy)
        {
            try
            {
                // Copy the files into the chosen directory
                String directoriesIfAny = file.getCanonicalPath() +
                        System.getProperty("file.separator") + fileToCopy.getParent();
                if (fileToCopy.getParent() != null)
                {
                    new File(directoriesIfAny).mkdirs();
                }
                String original = fileToCopy.getPath();
                String nameOfCopy = file.getCanonicalPath() + System.getProperty("file.separator") +
                        original;
                File copy = new File(nameOfCopy);

                FileInputStream fileInputStream = new FileInputStream(original);
                FileOutputStream fileOutputStream = new FileOutputStream(copy);
                logger.log("Copying to: " + copy.getCanonicalPath());
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fileInputStream.read(buffer)) > 0)
                {
                    fileOutputStream.write(buffer, 0, length);
                    progressViewer.setFileInFocus(original);
                    progressViewer.run();
                }
                fileInputStream.close();
                fileOutputStream.close();
            }
            catch (FileNotFoundException ex)
            {
                success = false;
                logger.log(ex.toString());
            }
            catch (IOException ex)
            {
                success = false;
                logger.log(ex.toString());
            }
            catch (java.lang.NullPointerException ex)
            {
                success = false;
                logger.log(ex.toString());
            }
            finally
            {
                if (!success)
                {
                    JOptionPane.showMessageDialog(null, "Errors were encountered during installation.\n" +
                            "The installation of " + properties.getMainApplicationName() + " will now be aborted.\n\n" +
                            "Please check the logs for more details",
                            properties.getMainApplicationName(), JOptionPane.ERROR_MESSAGE);
                    System.exit(0);
                }
            }
        }
    }

    class ProgressViewer extends Thread
    {

        private String fileInFocus = "";

        public void setFileInFocus(String fileInFocus)
        {
            this.fileInFocus = fileInFocus;
        }

        private void updateProgressbar()
        {
            progressBar.setIndeterminate(false);
            try
            {
                String original = this.fileInFocus;
                String nameOfCopy = file.getCanonicalPath() + System.getProperty("file.separator") +
                        original;
                File originalFile = new File(original);
                File copyOfOriginalFile = new File(nameOfCopy);

                double downloadedSize = copyOfOriginalFile.length();
                double totalDownloadSize = originalFile.length();
                int percentage = (int) Math.floor((downloadedSize / totalDownloadSize) * 100);

                progressBar.setString(originalFile.getName() + " (" + percentage + "% )");
                progressBar.setValue(percentage);

                if (percentage == 100)
                {
                    logger.log(originalFile.getName() + " copied successfully");
                    progressBar.setString("Please wait...");
                    progressBar.setIndeterminate(true);
                }
            }
            catch (FileNotFoundException ex)
            {
                logger.log(ex.toString());
            }
            catch (IOException ex)
            {
                logger.log(ex.toString());
            }
            catch (java.lang.NullPointerException ex)
            {
                logger.log(ex.toString());
            }
        }

        @Override
        public void run()
        {
            updateProgressbar();
        }
    }

    public synchronized void copyFiles()
    {
        // Create and run the two threads
        fileCopier = new FileCopier();
        progressViewer = new ProgressViewer();
        fileCopier.start();
    }
}