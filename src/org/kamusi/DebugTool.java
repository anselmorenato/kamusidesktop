/**
 * DebugTool.java
 * Created on Nov 5, 2009, 10:41:10 AM
 * @author arthur
 */
package org.kamusi;

import java.io.File;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.swing.JOptionPane;

/**
 * class DebugTool
 */
public class DebugTool extends KamusiLogger
{

    public void sendErrorDetails()
    {
        // Send an email to the developer. Attach the log file
        try
        {
            String server = "kamusi.org";
            String from = "arthur@kamusi.org";
            String to = "arthur@kamusi.org";
            String subject = "Kamusi Desktop Error Report";
            String fileAttachment = getLogFileName();
            String messageBody =
                    "This email is sent automatically by a computer. Do NOT reply to it.\n\n" +
                    "This is an error report sent in by a Kamusi Desktop user\n\n" +
                    "SYSTEM DETAILS\n" +
                    "Java Vendor: " + System.getProperty("java.vendor") + "\n" +
                    "Java vendor URL: " + System.getProperty("java.vendor.url") + "\n" +
                    "Java Version: " + System.getProperty("java.version") + "\n" +
                    "Operating System Architecture: " + System.getProperty("os.arch") + "\n" +
                    "Operating System Name: " + System.getProperty("os.name") + "\n" +
                    "Operating System Version: " + System.getProperty("os.version") +
                    "\n\n\n\n---------------------------\n" +
                    "End Of Message";
            // Get system properties
            Properties props = System.getProperties();

            // Setup mail server
            props.put("mail.smtp.host", server);

            // Get session
            Session sess = Session.getInstance(props, null);

            // Define message
            MimeMessage message = new MimeMessage(sess);
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(subject);

            // create the message part
            MimeBodyPart messageBodyPart = new MimeBodyPart();

            //fill message
            messageBodyPart.setText(messageBody);

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

            // Part two is attachment
            messageBodyPart = new MimeBodyPart();
            DataSource source = new FileDataSource(fileAttachment);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(new File(fileAttachment).getName());
            multipart.addBodyPart(messageBodyPart);

            // Put parts in message
            message.setContent(multipart);

            // Send the message
            Transport.send(message);

            MainWindow.showInfo("Thank you for sending the report.");

        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(null, e.getMessage(),
                "Kamusi Desktop", JOptionPane.INFORMATION_MESSAGE);
        }

    }
}
