/**
 * MessageLocalizer.java
 * Created on Nov 14, 2009, 10:34:06 PM
 * @author arthur
 */
package org.kamusi;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * class MessageLocalizer
 */
public class MessageLocalizer extends KamusiLogger
{

    public static String formatMessage(String template, Object[] messageArguments)
    {

        try
        {
            String language;
            String country;

            Locale currentLocale = Locale.getDefault();
            ResourceBundle messages;

            File file = new File("./");
            URL[] urls =
            {
                file.toURI().toURL()
            };
            ClassLoader loader = new URLClassLoader(urls);
            final File localeFile = new File("conf/KamusiDesktop");

            messages = ResourceBundle.getBundle(localeFile.getPath(), currentLocale, loader);

            MessageFormat formatter = new MessageFormat("");

            formatter.setLocale(currentLocale);

            formatter.applyPattern(messages.getString(template));
            String output = formatter.format(messageArguments);

            return output;
        }
        catch (MalformedURLException ex)
        {
            MainWindow.showError(ex);
            return null;
        }
        catch (NumberFormatException ex)
        {
            MainWindow.showError(ex);
            return null;
        }
        catch (MissingResourceException ex)
        {
            MainWindow.showError(ex);
            return null;
        }
    }
    public static String getQuery(String language)
    {
        try
        {
            Locale currentLocale = Locale.getDefault();
            ResourceBundle messages;

            File file = new File("./");
            URL[] urls =
            {
                file.toURI().toURL()
            };
            ClassLoader loader = new URLClassLoader(urls);
            final File localeFile = new File("conf/app");

            messages = ResourceBundle.getBundle(localeFile.getPath(), currentLocale, loader);

            MessageFormat formatter = new MessageFormat("");

            formatter.applyPattern(messages.getString("query"));

            Object[] messageArguments =
            {
                language
            };

            String output = formatter.format(messageArguments);

            return output;
        }
        catch (MalformedURLException ex)
        {
            MainWindow.showError(ex);
            return null;
        }
    }
}
