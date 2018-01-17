package com.tester;

import static com.tester.Arduino.*;

import java.awt.EventQueue;
import java.awt.event.*;
import java.util.logging.*;
import javax.swing.*;

import static processing.core.PApplet.*;

/**
 * Class for handling the testing process using a <i>Timer</i> object for timing
 * control and the <i>ActionEvents</i> and called by the <i>Timer</i> are
 * implemented in this class.
 *
 * @author dmh
 */
public class RunTest implements ActionListener {

    private Timer timer = null;
    private int testState;
    private int defaultPeriod;
    private final int initialDelay = 100;
    private GUIFrame parent;
    private static Pins pins;

    static private Arduino arduino;
    protected static boolean running = false;
    protected static boolean finished = false;
    protected static boolean waiting = false;
    static private boolean failed = false;
    static private int waitCount = 0;

    /**
     * RunTest - Handles the EAX-2500 testing sequence.
     *
     * @param parent The main GUI
     * @param arduino The instance
     */
    public RunTest(GUIFrame parent, Arduino arduino) {
	RunTest.arduino = arduino;
	this.parent = parent;
	LogControler.getLogger().info("Testing started.");
	testState = 0;
	waitCount = 0;
	running = false;
	finished = false;
	init();
    }

    /**
     * Initializes local variables and sets up the timer, and calls the start
     * method for the <i>Timer</i>.
     */
    private void init() {
	if (timer == null) {
	    timer = new Timer(initialDelay, this);
	}
	// Set the default testing speed set from the preference file.
	// Set the preference if it does not exists
	defaultPeriod = GUIFrame.getPreferences().getInt("TESTING_SPEED", 2400);
	println("Default Testing Period = " + defaultPeriod);
	adjustDelayFromSlider(defaultPeriod);
	setupPins();
	timer.addActionListener(this);
	arduino.dWrite(Pins.QW_RELAY.pin, LOW);
    }

    /**
     * ActionListener for handling the <b>Timer</b> instance.
     *
     * @param evt The ActionEvent callback.
     */
    @Override
    public void actionPerformed(ActionEvent evt) {
	if (!finished) {
	    //println("State=" + testState);
	    //println(waitCount);
	    switch (testState) {
		case 0:
		    parent.clearScreen();
		    parent.append("**** Starting EAX-2500 Test ****\n");
		    arduino.dWrite(Pins.TEST.pin, HIGH);
		    arduino.dWrite(Pins.IND.pin, HIGH);
		    //GUIFrame.sleep(35);
		    break;

		case 2:
		    parent.append(" → DC Power ON\n\t   Red & Green LEDs turn ON");
		    arduino.dWrite(Pins.BATT.pin, HIGH);
		    break;

		case 4:
		    parent.append(" → Starting Low Battery Test\n\t   Red & Green LED's blink alternately");
		    arduino.dWrite(Pins.PAD.pin, HIGH);
		    arduino.dWrite(Pins.LOW_BATT.pin, HIGH);
		    break;

		case 6:
		    parent.append(" → AC Power ON");// / DC Power OFF");
		    //arduino.dWrite(Pins.BATT.pin, LOW);
		    arduino.dWrite(Pins.AC.pin, HIGH);
		    arduino.dWrite(Pins.LOW_BATT.pin, LOW);
		    break;

		case 8:
		    parent.append(" → Sending the call signal\n\t   Red & Green LED's blink in unison");
		    arduino.dWrite(Pins.OKC.pin, HIGH);
		    //		    try {
		    //			Thread.currentThread().sleep(1000);
		    //		    } catch (InterruptedException ex) {
		    //			Logger.getLogger(RunTest.class.getName()).log(Level.SEVERE, null, ex);
		    //		    }
		    break;

		case 10:
		    parent.append(" → Call Signal OFF.");
		    arduino.dWrite(Pins.OKC.pin, LOW);
		    break;

		case 12:
		    parent.append(" → Turning ON Door switch signal\n\t   Red LED turns ON");
		    arduino.dWrite(Pins.PAD.pin, LOW);
		    timer.setDelay(200); // Speed the delay time between the next two test states.
		    break;

		case 14:
		    parent.append("\n → Press the key switch\n\t   The siren sounds • Checking the relay state...");
		    arduino.dWrite(Pins.PAD.pin, HIGH);
		    //parent.append(".");
		    testState = 16;

		case 16:
		    // First check if we've timedout waiting for the relay
		    if ((++waitCount) >= 110) {
			parent.append("\n → Relay Failed‼ ↔ Continuing the test.");
			//testState = 16;
			failed = true;
			break;
		    }

		    int relayValue = arduino.read(Pins.RELAY_IN.pin);
		    if (relayValue == HIGH) { // Relay has not changed states.
			testState--;
			GUIFrame.textArea.append(".");
		    } else if (relayValue == LOW) { // Relay has changed states.
			parent.append("\n  → Relay OK!");
			adjustDelayFromSlider(defaultPeriod);
		    }
		    break;

		case 17:
		    parent.append("\nPerform the following:");
		    parent.append("  → Push the slide tab in\n\t  The Red & Green LED's turn ON");
		    parent.append("  → Pull the slide tab out\n\t  The Red & Green LED's continue to blink");
		    parent.append("  → Flip the Low Battery Slide Switch to ON\n\t  Red & Green LED's turn OFF");
		    parent.append("  → Flip the Low Battery Slide Switch to OFF\n\t  Red & Green LED's blink alternately\n"
			    + "  ☼ Press continue button to complete the test ☼   ");
		    parent.testButton.setEnabled(true);
		    parent.testButton.setText("Continue");
		    waiting = true;
		    arduino.dWrite(Pins.PAD.pin, HIGH);
		    arduino.dWrite(Pins.LOW_BATT.pin, HIGH);
		    timer.setDelay(100);

		    break;

		case 20:
		    if (waiting) {
			return;
		    } else {
			adjustDelayFromSlider(defaultPeriod);
			finished = true;
			parent.testButton.setText("Test");
		    }
		    break;

		default:
		    break;
	    }

	    /* See if we've finishec the test */
	    if (finished) {
		timer.stop();
		GUIFrame.getLogger().info("Testing Completed.");
		parent.append(" Testing Complete! ☻");
		if (failed) {
		    JOptionPane.showConfirmDialog(GUIFrame.frame, "Relay failed test, please confirm using "
			    + "the LED indicator on the test head.", "Confirm Relay", JOptionPane.OK_OPTION);
		}
		try {
		    Thread.sleep(250);
		} catch (InterruptedException ex) {
		    Logger.getLogger(RunTest.class.getName()).log(Level.SEVERE, null, ex);
		}
		setAllOff();
		parent.showInstructions();
		parent.testButton.setEnabled(true);
		parent.pgmButton.setEnabled(true);
		failed = false;
	    } else {// Continue Testing
		testState++;
	    }
	}
    }

    private void adjustDelayFromSlider(int defaultPeriod) {
	// Set the timing period proportional to the slider relayValue
	float sv = (float) (parent.slider.getValue() / 100.0);
	println("Slider value = " + sv + "%");
	int extraTime = (int) (sv * 1000.0);
	println(extraTime);
	timer.setDelay(extraTime + defaultPeriod);
	println("Delay = " + timer.getDelay());
    }

    /**
     * Initializes the pins on the arduino as inputs or outputs.
     */
    private void setupPins() {
	for (Pins p : Pins.values()) {
	    if (p.IO == OUTPUT) { // OUTPUTS
		arduino.pinMode(p.pin, OUTPUT);
		arduino.dWrite(p.pin, LOW);
	    } else { // INPUTS
		arduino.dWrite(p.pin, LOW);
		arduino.pinMode(p.pin, INPUT);
	    }
	}
    }

    /**
     * Convince Method to show the values of all the pins on the arduino board.
     */
    public void displayValues() {
	EventQueue.invokeLater(() -> {
	    synchronized (RunTest.this) {
		Pins[] mPins = Pins.values();
		String s = "";
		for (Pins p : mPins) {
		    s += "name: " + p.name() + ", pin:" + p.pin + " = " + arduino.digitalRead(p.pin) + "\n";
		}
		JOptionPane.showMessageDialog(parent, s);
	    }
	});
    }

    void start() {
	timer.start();
    }

    /**
     * Iterates thru all the outputs and sets them <i>LOW</i>.
     */
    public static void setAllOff() {
	for (Pins p : Pins.values()) {
	    if (p.IO == OUTPUT) {
//                if (p.name().startsWith("QW")) {
//                    arduino.dWrite(p.pin, HIGH);
//                } else {
		arduino.dWrite(p.pin, LOW);
//                }
	    }
	}
    }

    public static Pins getPins() {
	return pins;
    }

    /**
     * Internal Enumeration of the Pins used for this design.
     */
    public enum Pins {
	TEST(3), // Testing Jumper 
	BATT(4), // Battery Output
	LOW_BATT(2), // Low Battery Enable
	OKC(8), // OKC Signal
	REMOTE(5), // Remote Signal
	PAD(6), // Door Signla
	AC(9), // AC Power Output Relay
	IND(13), // Testing Indicator
	RELAY_IN(7, INPUT), // Input for checking the relay states.
	QW_RELAY(10);
	//QW_RELAY2(11);

	int pin;
	int IO;

	/**
	 * Constructor that defaults the mode as an <i>OUTPUT</i>.
	 *
	 * @param pin An integer representing the pin on the Arduino board.
	 */
	private Pins(int pin) {
	    this.pin = pin;
	    this.IO = OUTPUT;
	}

	/**
	 * Constructor that defaults the mode as an <i>INPUT</i> or as an
	 * <i>INPUT_PULLUP</i>.
	 *
	 * @param pin An integer representing the pin on the Arduino board.
	 * @param IO Input or Output mode.
	 */
	private Pins(int pin, int IO) {
	    this.pin = pin;
	    this.IO = INPUT;
	}
    }

}
