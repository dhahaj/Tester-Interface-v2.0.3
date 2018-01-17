/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tester;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import static processing.core.PApplet.println;

/**
 * Class containing helper functions.
 *
 * @author dmh
 */
public class Helpers {

    protected static Helpers helper = null;

    private Helpers() {
        super();
    }

    public static Helpers getInstance() {
        if (helper == null) {
            helper = new Helpers();
        }
        return helper;
    }

    /**
     * Writes a text string to the given file.
     *
     * @param file The file to be written to.
     * @param content The content to write.
     * @throws FileNotFoundException Thrown if the file doesn't exist, is null,
     * a directory or is read only.
     * @throws IOException IOException.
     */
    @SuppressWarnings("ConvertToTryWithResources")
    static public void setContents(File file, String content) throws FileNotFoundException, IOException {
        if (file == null) {
            throw new IllegalArgumentException("File should not be null.");
        } else if (!file.exists()) {
            throw new FileNotFoundException("File does not exist: " + file);
        } else if (!file.isFile()) {
            throw new IllegalArgumentException("File is a directory: " + file);
        } else if (!file.canWrite()) {
            throw new IllegalArgumentException("File cannot be written to: " + file);
        }

        //use buffering
        Writer output = new BufferedWriter(new FileWriter(file));
        try {
            output.write(content); //FileWriter always assumes default encoding is OK!
        } finally {
            output.close();
        }
    }

    /**
     * Writes a text string to the given file path.
     *
     * @param path Path to the file.
     * @param content Content for writing.
     * @throws IOException IOException
     */
    static public void setContents(Path path, String content) throws IOException {
        setContents(path.toFile(), content);
    }

    /**
     * Writes a text string to the given file.
     *
     * @param path A string representing the path to the file.
     * @param content The content to write.
     * @throws IOException IOException
     */
    static public void setContents(String path, String content) throws IOException {
        setContents(new File(path), content);
    }

    /**
     * Reads text from a <b>File</b> object.
     *
     * @param file A <b>File</b> object to read.
     * @return A string of the <b>File's</b> contents.
     */
    @SuppressWarnings("ConvertToTryWithResources")
    static public String getContents(File file) {
//    if (file == null || file.isDirectory() || !file.exists()) {
//      throw new IllegalArgumentException("Error reading file: " + file.getName());
//    }

        List<String> lines;
        try {
            lines = new ArrayList<>(Files.readAllLines(file.toPath()));
        } catch (IOException ex) {
            Logger.getLogger(Helpers.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

        StringBuilder contents = new StringBuilder();
        lines.forEach((line) -> {
            contents.append(line).append(System.getProperty("line.separator"));
        });
        return contents.toString();
    }

    /**
     * Replaces a line of EE Data in the quickwriter control file.
     *
     * @param path Path to the control file.
     * @param regex The expression to look for.
     * @param newLine The replace String.
     * @return True if line was replaced.
     * @throws IOException IOException
     */
    static public boolean replaceDataLine(Path path, String regex, String newLine) throws IOException {
        boolean found = false;
        List<String> fileContent = new ArrayList<>(Files.readAllLines(path, StandardCharsets.UTF_8));
        for (String line : fileContent) {
            if (line.contains(regex)) {
                found = true;
                println("Found line: " + line);
                println("Replacement: " + newLine);
                fileContent.set(fileContent.indexOf(line), regex + newLine);
                break;
            }
        }
        Files.write(path, fileContent, StandardCharsets.UTF_8);
        return found;
    }

    /**
     * Edits a quickwriter control file to point to the correct firmware.
     *
     * @param file String path to the file to modify.
     * @param device Name of the firmware
     * @return A path object for the quickwriter control file.
     * @throws java.io.IOException IOException
     */
    static public Path quickwriterEdit(String file, String device) throws IOException {
        File f = new File(file);
        Path p = f.toPath();
        return quickwriterEdit(p, device);
    }

    /**
     * Edits a quickwriter control file to point to the correct firmware.
     *
     * @param path String path to the file to modify.
     * @param device Name of the firmware.
     * @return A path object for the modified quickwriter control file.
     * @throws IOException IOException
     */
    static public Path quickwriterEdit(Path path, String device) throws IOException {
        List<String> fileContent = new ArrayList<>(Files.readAllLines(path, StandardCharsets.UTF_8));
        String newLine = "HEXFILE=" + GUIFrame.getPreferenceString("FIRMWARE_DIR") + device + "_73.OBJ";
        for (String line : fileContent) {
            if (line.contains("HEXFILE")) {
                println("Found line: " + line);
                println("Replacement = " + newLine);
                int i = fileContent.indexOf(line);
                fileContent.set(i, newLine);
                break;
            }
        }
        return Files.write(path, fileContent, StandardCharsets.UTF_8);
    }

    /**
     * Edits a quickwriter control file to point to the correct firmware.
     *
     * @param device String path to the file to modify.
     * @return A path object for the modified quickwriter control file.
     * @throws IOException IOException
     */
    static public Path getQuickWriterPath(String device) throws IOException {
        String s = GUIFrame.getPreferenceString("FIRMWARE_DIR") + "\\" + "QWControl.qwc";
        File f = new File(s);
        return quickwriterEdit(f.toPath(), device);
    }

    /**
     * Reads user information from the given file.
     *
     * @param fileName String path to the file.
     * @param delimnator The char delimnator separating the username and
     * password.
     * @return A List containing the parsed <b>Users</b>.
     */
    static public List<User> parseUserFile(String fileName, String delimnator) {
        List<User> list = new ArrayList<>();
        String contents = getContents(new File(fileName));
        String[] splitContent = contents.split("\n");
        for (String s : splitContent) {
            String[] namePass = s.split(delimnator);
            list.add(new User(namePass[0], namePass[1]));
        }
        return list;
    }

    /**
     * Export provided Preferences to an XML document.
     *
     * @param path Path to the file.
     * @param prefs Preferences to save.
     */
    public static void exportPrefs(String path, Preferences prefs) {
        try {
            prefs.flush();
            prefs.sync();
        } catch (BackingStoreException e1) {
        }
        OutputStream os = null;
        try {
            os = new FileOutputStream(path);
            prefs.exportNode(os);
        } catch (IOException | BackingStoreException e) {
        } finally {
            try {
                os.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * Converts a character array to a String object.
     *
     * @param buffer The character array.
     * @return The String object
     */
    public static String char2String(char[] buffer) {
        if (buffer.length == 0) {
            return null;
        }
        String s = "";
        for (int i = 0; i < buffer.length; i++) {
            char c = buffer[i];
            s += c;
        }
        return s.trim();
    }

    public static String readInputStream(InputStream is) throws IOException {
        StringBuilder result = new StringBuilder("");
        Scanner s = new Scanner(is, "UTF-8");
        while(s.hasNext()){
            result.append(s.nextLine());
            result.append("\n");
        }
        //        while (is.available() > 0) {
        //            result.append((char) is.read());
        //        }
        return result.toString();
    }

    /**
     * Reads a file from the class loader.
     *
     * @param c the class
     * @param fileName the filename
     * @return the contents
     */
    public static String getFile(Class c, String fileName) {

        StringBuilder result = new StringBuilder("");

        //Get file from resources folder
        ClassLoader classLoader = c.getClass().getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                result.append(line).append("\n");
            }
            scanner.close();
        } catch (IOException e) {
        }
        return result.toString();
    }
}
