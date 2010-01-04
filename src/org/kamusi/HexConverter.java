/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kamusi;

/**
 * Encodes a String to its Hexadecimal equivalent
 * @author arthur
 */
public class HexConverter extends KamusiLogger
{

    private final int DEFAULT_BIT_SIZE = 2;
    private final int MINIMUM_BIT_SIZE = 2;

    /**
     * Entry point of the class
     */
    public HexConverter()
    {}

    /**
     * Converts a String to its hexadecimal equivaent
     * @param text the String to convert
     * @param bitSize How many bit ASCII code is needed?
     * @return the hexadecimal equivalent of text
     */
    protected String getHex(String text, int bitSize)
    {
        char[] characters = new char[text.length()];

        for (int i = 0; i < text.length(); i++)
        {
            characters[i] = text.charAt(i);
        }

        return convertToHex(characters, bitSize);
    }

    /**
     * Converts a String to its hexadecimal equivaent
     * @param text the String to convert
     * @return the hexadecimal equivalent of text
     */
    protected String getHex(String text)
    {
        return getHex(text, DEFAULT_BIT_SIZE);
    }

    /**
     * Converts a HEX String to its ASCII equivaent
     * @param hexString the String to convert
     * @param bit The bit size of each ASCII character
     * @return the hexadecimal equivalent of text
     */
    protected String getAscii(String hexString, int bit)
    {
        String hex = "";
        StringBuffer ascii = new StringBuffer();

        for (int i = 0; i < (hexString.length()) / bit; i++)
        {
            hex = hexString.substring((i * bit), (((i * bit)) + bit));
            ascii.append((char) Integer.parseInt(hex, 16));
        }

        return ascii.toString();
    }

    /**
     * Converts a HEX String to its ASCII equivaent
     * @param hexString the String to convert
     * @return the hexadecimal equivalent of text
     */
    protected String getAscii(String hexString)
    {
        return getAscii(hexString, DEFAULT_BIT_SIZE);
    }

    /**
     * Makes the ascii code to have a certain number of bit length
     * @param string The string to be lengthened
     * @param bitSize The number of leading zeroes to be added
     * @return The string with appropriate leading zeroes
     */
    private String makeSignificant(String string, int bitSize)
    {
        int stringLength = string.length();
        int bitDifference = bitSize - stringLength;
        String leadingZeroes = "";

        if (bitSize < MINIMUM_BIT_SIZE)
        {
            return string;
        }
        else
        {
            for (int i = 0; i < bitDifference; i++)
            {
                leadingZeroes += "0";
            }
            return leadingZeroes + string;
        }
    }

    /**
     * Converts characters in an array of characters into hexadecimals
     * @param characters the array of characters to be converted
     * @return the concatenated hexadecimal equivalent of the array chars
     */
    private String convertToHex(char[] characters, int bitSize)
    {
        StringBuffer buffer = new StringBuffer();

        for (int i = 0; i < characters.length; i++)
        {
            String hex = Integer.toHexString((int) characters[i]);
            buffer.append(makeSignificant(hex, bitSize));
        }
        return buffer.toString().toUpperCase();
    }
}
