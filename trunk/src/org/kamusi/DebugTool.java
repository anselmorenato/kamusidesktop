/**
 * DebugTool.java
 * Created on Nov 5, 2009, 10:41:10 AM
 * @author arthur
 */
package org.kamusi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import javax.swing.JOptionPane;

/**
 * class DebugTool
 */
public class DebugTool extends KamusiLogger
{

    private HexConverter converter = new HexConverter();

    /**
     * Sends error stack traces to developers
     */
    public void sendErrorDetails()
    {
        try
        {
            File inputFile = new File(getLogFileName());
            FileReader fileReader = new FileReader(inputFile);
            BufferedReader buff = new BufferedReader(fileReader);

            StringBuffer log = new StringBuffer();

            String line; // <<-- added
            while (((line = buff.readLine()) != null)) // <<-- modified
            {
                log.append(line);
                log.append("\n");
            }

            // Send data
            String toSend = converter.getHex(log.toString());

            String data = URLEncoder.encode("report", "UTF-8") + "=" + URLEncoder.encode(toSend, "UTF-8");
            final String SYNC_URL = System.getProperty("bug_report_url");
            URL url = new URL(SYNC_URL);
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter streamWriter = new OutputStreamWriter(conn.getOutputStream());

            streamWriter.write(data);
            streamWriter.flush();

            // Get the response
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String responseLine;
            while ((responseLine = bufferedReader.readLine()) != null)
            {
                // Process line...
                String response = converter.getHex("Server response from bug report - > " + line);
                logApplicationMessage(response);
            }
            streamWriter.close();
            bufferedReader.close();

            fileReader.close();

            MainWindow.showInfo("Thank you for sending the report.");

        }
        catch (Exception e)
        {
            logExceptionStackTrace(e);
            JOptionPane.showMessageDialog(null, e.toString(),
                    "Kamusi Desktop", JOptionPane.INFORMATION_MESSAGE);
        }

    }
}
