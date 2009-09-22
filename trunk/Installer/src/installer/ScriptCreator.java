package installer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author arthur
 */
public class ScriptCreator
{

    /**
     * Removes the desktop shortcut for the application
     */
    public static void removeDesktopShortcut()
    {
        ApplicationProperties properties = null;
        
        properties = new ApplicationProperties();
        String userDirectory = System.getProperty("user.home");
        String path = userDirectory + System.getProperty("file.separator") +
                "Desktop" + System.getProperty("file.separator");

        String appName = properties.getMainApplicationName();

        String scriptName = path + appName + ".sh";

        new File(scriptName).delete();
    }

    /**
     * Creates a dektop shortcut for the application
     * @param installDir The directory holding the application install files
     */
    public static void createDesktopShortcut(String installDir)
    {
        ApplicationProperties properties = null;

        try
        {
            properties = new ApplicationProperties();

            String targetOs = properties.getTargetOS();

            if (targetOs.equalsIgnoreCase("LINUX"))
            {
                // Create run.sh in the InstallDir
                File runFile = new File(installDir + System.getProperty("file.separator") +
                        "run.sh");

                String userDirectory = System.getProperty("user.home");

                String path = userDirectory + System.getProperty("file.separator") +
                        "Desktop" + System.getProperty("file.separator");

                String shortcut = properties.getMainApplicationName() + ".desktop";

                String sheBang = "#!/bin/bash\n";

                String changeDir = "cd \"" + installDir + "\"\n";

                String[] libraries = properties.getRequiredLibraries();

                StringBuffer command = new StringBuffer();
                command.append("java -cp ");

                for (String library : libraries)
                {
                    command.append(library);
                    command.append(":");
                }

                command.append(".");
                command.append(" -jar " + properties.getAppJar());

                FileOutputStream fos = new FileOutputStream(runFile);
                fos.write(sheBang.getBytes());
                fos.write(changeDir.getBytes());
                fos.write(command.toString().getBytes());
                fos.write("\n".getBytes());
                fos.close();

                // Set the file as executable
                setExecutable(runFile.getCanonicalPath());

                File desktopShortcut = new File(path + shortcut);
                FileOutputStream dsc = new FileOutputStream(desktopShortcut);

                dsc.write("[Desktop Entry]\n".getBytes());
                dsc.write("Version=1.0\n".getBytes());
                dsc.write("Encoding=UTF-8\n".getBytes());
                dsc.write(("Name=" + properties.getMainApplicationName() + "\n").getBytes());
                dsc.write("Type=Application\n".getBytes());
                dsc.write("Terminal=false\n".getBytes());
                dsc.write(("Exec=" + runFile.getCanonicalPath() + "\n").getBytes());
                dsc.write(("Comment=Launches " + properties.getMainApplicationName() + "\n").getBytes());
                dsc.write(("Icon=" + installDir + System.getProperty("file.separator") +
                        properties.getLogo() + "\n").getBytes());
                dsc.close();

            }
            else if (targetOs.equalsIgnoreCase("WINDOWS"))
            {
                String userDirectory = System.getProperty("user.home");
                String path = userDirectory + System.getProperty("file.separator") +
                        "Desktop" + System.getProperty("file.separator");

                String appName = properties.getMainApplicationName();


                // Run the Install script VB file

                File temporaryFile = new File(installDir + System.getProperty("file.separator") +
                        "CreateShortcut.vbs");

                BufferedWriter writer = new BufferedWriter(new FileWriter(temporaryFile, false));

                writer.write("Set oWS = WScript.CreateObject(\"WScript.Shell\")");
                writer.newLine();
                writer.write(("sLinkFile = \"" + path + "Kamusi Desktop.LNK\""));
                writer.newLine();
                writer.write("Set oLink = oWS.CreateShortcut(sLinkFile)");
                writer.newLine();
                writer.write("oLink.TargetPath = \"" + installDir +
                        System.getProperty("file.separator") +
                        "Kamusi Desktop.exe\"");
                writer.newLine();
                writer.write(("oLink.Save"));
                writer.newLine();
                writer.close();
                //Run the vb script
                String[] command =
                {
                    "cmd",
                    "/c",
                    temporaryFile.getCanonicalPath()
                };
                System.out.println(temporaryFile.getCanonicalPath());
                Runtime.getRuntime().exec(command);
                try
                {
                    Thread.sleep(3000);
                }
                catch (InterruptedException ex)
                {
                    Logger.getLogger(ScriptCreator.class.getName()).log(Level.SEVERE, null, ex);
                }
                temporaryFile.deleteOnExit();

            }
            Logger.getLogger(InstallerWindow.class.getName()).log(Level.INFO,
                    "Desktop shortcut created successfully");
        }
        catch (FileNotFoundException ex)
        {
            Logger.getLogger(ScriptCreator.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IOException ex)
        {
            Logger.getLogger(ScriptCreator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Makes a file executable (Linux platform)
     * @param filename the file to be made executable
     */
    private static void setExecutable(String fileName)
    {
        // Set the file as executable
        String[] exec =
        {
            "chmod", "+x", fileName
        };
        try
        {
            Runtime.getRuntime().exec(exec);
            Logger.getLogger(InstallerWindow.class.getName()).log(Level.INFO,
                    "Shortcut permissions changed successfully");
        }
        catch (IOException ex)
        {
            Logger.getLogger(ScriptCreator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
