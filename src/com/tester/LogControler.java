package com.tester;


import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;
import java.util.logging.Formatter;
import java.util.logging.*;

import static processing.core.PApplet.*;


/**
 * <b>LogControler</b> Handles logging of data and maintaining an archive of
 * logs.
 *
 * @author dhahaj
 */
public class LogControler {

    static private FileHandler textHandler = null;
    static private SimpleFormatter textFormatter = null;

    static private FileHandler htmlHandler;
    static private Formatter htmlFormatter;
    static public String FilePath;
    static public boolean Append = true;
    static public String ARCHIVE_FOLDER_NAME = "LogArchive";
    public static boolean SAVE_TEXT_LOG = true;
    public static boolean SAVE_HTML_LOG = true;
    public static User USER = null;
    private static boolean DEBUG = true;

    static final Logger logger = Logger.getLogger("");

    /**
     * <b>setup</b> Initializes the logger/FileHandlers/formatter, and creates
     * both the text and html log files.
     *
     * @throws IOException IOException
     */
    static public void setup() throws IOException {

        if (textHandler != null)
            return;
        DEBUG("Setup called from: " + getCallerClassName());

        checkDate(FilePath);

        // Create Logger
        logger.setLevel(Level.INFO);

        // Create the FileHandlers
        textHandler = new FileHandler(FilePath + ".txt", Append);
        htmlHandler = new FileHandler(FilePath + ".html", Append);

        // Create txt Formatter
        textFormatter = new SimpleFormatter();
        textHandler.setFormatter(textFormatter);
        logger.addHandler(textHandler);

        // Create HTML Formatter
        htmlFormatter = new HtmlFormatter();
        htmlHandler.setFormatter(htmlFormatter);
        logger.addHandler(htmlHandler);

    }

    /**
     * Checks the modified date of latest log to the current date. If they are
     * different it renames the log file and relocates it to an archive folder.
     *
     * @param FilePath The path to the current log file.
     */
    @SuppressWarnings("deprecation")
    private static void checkDate(String FilePath) {

        DEBUG("Check Date called.");

        // Check the dates to see if the logs should be archived
        File file = new File(FilePath + ".txt");
        Date lastModified = new Date(file.lastModified());

        if (checkFileDate(file)) {
            DEBUG("Not archiving log files");
            return;
        }
        DEBUG("Archiving log files");
        try {
            Path path = Paths.get(FilePath + ".html");
            BasicFileAttributeView view = Files.getFileAttributeView(path, BasicFileAttributeView.class);
            // BasicFileAttributes attributes = view.readAttributes();
            long currentTime = Calendar.getInstance().getTimeInMillis();
            FileTime lastAccessTime = FileTime.fromMillis(currentTime);
            view.setTimes(null, null, lastAccessTime);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);

        long newDiff = (lastModified.getTime() - c.getTimeInMillis()) / 1000 / 60 / 60;
        DEBUG("diff in hours=" + newDiff);

        // Paths for the text and html files to be archived to
        final String textFilePath = (FilePath + ".txt");
        final String htmlFilePath = (FilePath + ".html");

        // Handle the text formatted log.
        File textFile = new File(textFilePath);
        final String absPath = textFile.getAbsolutePath();
        final String archiveFolerDir = absPath.substring(0, absPath.lastIndexOf(File.separator)) + File.separator + ARCHIVE_FOLDER_NAME;
        final String newname = "Log_" + (lastModified.getMonth() + 1) + "." + lastModified.getDate() + "." + (lastModified.getYear() + 1900);

        // Create the new text file
        String archiveFileName = archiveFolerDir + File.separator + newname + ".txt";
        int n = 1;
        while (fileExists(archiveFileName)) {
            archiveFileName = archiveFolerDir + File.separator + newname + "_" + n + ".txt";
            n++;
        }

        boolean ok, archived;
        if (SAVE_TEXT_LOG) {
            File newTextFile = new File(archiveFileName);
            ok = textFile.renameTo(newTextFile);

            archived = archiveFile(newTextFile);
            if (!archived || !ok)
                System.err.println("Error archiving file: " + newTextFile);
        }

        if (SAVE_HTML_LOG) {

            // Create the new html file
            File htmlFile = new File(htmlFilePath);
            File newHtmlFile = new File(archiveFileName.replaceAll(".txt", ".html"));
            Path htmlPath = Paths.get(archiveFileName.replaceAll(".txt", ".html"));
            if (Files.exists(htmlPath))
                println("File already exists!");
            ok = htmlFile.renameTo(newHtmlFile);

            archived = archiveFile(newHtmlFile);
            if (!archived || !ok)
                System.err.println("Error archiving file: " + newHtmlFile);
        }
    }

    private static boolean archiveFile(File file) {
        FileWriter out = null;
        boolean b = false;
        try {
            out = new FileWriter(file, true);
            b = true;
        } catch (IOException e) {
            b = false;
            try {
                out = new FileWriter(file, false);
                b = true;
                e.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } finally {
            try {
                if (out != null)
                    out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return b;
    }

    /**
     * <b>fileExists</b> Checks for the existence of a file.
     *
     * @param filename The filename in String format.
     * @return boolean True if the file exists, False otherwise.
     */
    public static boolean fileExists(String filename) {
        Path p = Paths.get(filename);
        return Files.exists(p);
    }

    /**
     * <b>fileExists</b> Checks for the existence of a file.
     *
     * @param f The file to check.
     * @return True if file is found, False otherwise.
     */
    public static boolean fileExists(File f) {
        return fileExists(f.getAbsolutePath());
    }

    private static void DEBUG(String msg) {
        if (DEBUG)
            println(msg);
    }

    static Logger getLogger() {
        return logger;
    }

    private static boolean checkFileDate(File f) {
        long t = f.lastModified();
        Calendar c = Calendar.getInstance();
        c.setTime(new Date(t));

        int todaysDate = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

        return (todaysDate == c.get(Calendar.DAY_OF_MONTH));
    }

    public static String getCallerClassName() {
        StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
        for (int i = 1; i < stElements.length; i++) {
            StackTraceElement ste = stElements[i];
            if (!ste.getClassName().equals(LogControler.class.getName()) && ste.getClassName().indexOf("java.lang.Thread") != 0)
                return ste.getClassName();
        }
        return null;
    }


}
