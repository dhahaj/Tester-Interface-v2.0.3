package com.tester;

import java.io.*;
import java.nio.file.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.*;
import processing.core.*;

import static com.dhahaj.HexUtilities.*;

/**
 * Class to run the programming software
 *
 * @author dmh
 */
public class RunProgramming {

    private static java.lang.Runtime rt = null;
    private static RunProgramming programming;
    private static boolean running = false;

    private RunProgramming() {
	super();
    }

    public static RunProgramming getInstance() {
	if (programming == null) {
	    programming = new RunProgramming();
	}
	return programming;
    }

    /**
     * Runs the executable file.
     *
     * @param firmware The absolute path of the firmware.
     *
     * @return The RunTime exit value.
     */
    public static int run(String firmware) {
	running = true;
	int exitValue;
	if (rt == null) {
	    rt = Runtime.getRuntime();
	}

	Path path;
	try {
	    // Modify the control file to point to the correct hex file.
	    path = Helpers.getQuickWriterPath(GUIFrame.selectedDevice);
	    modifyControlFileData(path);
	} catch (IOException ex) {
	    LogControler.getLogger().log(Level.SEVERE, null, ex);
	    running = false;
	    return -1;
	}

	String qwPath = GUIFrame.getPreferenceString("QW_PATH");
	Process p = PApplet.launch(qwPath, path.toString(), "/D16F627", "/A", "/X");
	synchronized (p) {
	    try {
		p.waitFor();
	    } catch (InterruptedException ex) {
		running = false;
	    }
	    exitValue = p.exitValue();
	    running = false;
	}
	running = false;
	return exitValue;
    }

    /**
     * Is it currently programming
     *
     * @return
     */
    public static boolean isRunning() {
	return running;
    }

    /**
     * Modifies the QuickWriter's control file EE Data to include the current
     * user, the date, and the selected firmware.
     *
     * @param path Path to the control file.
     */
    private static void modifyControlFileData(Path path) {
	SimpleDateFormat sdf = new SimpleDateFormat("MMddyy");
	String now = sdf.format(Calendar.getInstance().getTime());

	String hex = ascii2Hex(now + GUIFrame.getUser().getUsername() + GUIFrame.selectedDevice, true);
	try {
	    // Replace the line that starts with "2=" because that is the start address for writing to the EEPROM.
	    if (!Helpers.replaceDataLine(path, "2=", hex)) {
		JOptionPane.showMessageDialog(GUIFrame.frame, "Error modifying quickwriter control file.",
			"Error", JOptionPane.ERROR_MESSAGE);
	    }
	} catch (IOException ex) {
	    Logger.getLogger(RunProgramming.class.getName()).log(Level.SEVERE, null, ex);
	}
    }

}
