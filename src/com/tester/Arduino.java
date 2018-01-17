package com.tester;

import static java.lang.System.err;
import processing.core.PApplet;

/**
 * Arduino Class
 *
 * @author dmh
 */
public class Arduino {

    /**
     * Constant to set a pin to input mode (in a call to pinMode()).
     */
    public static final int INPUT = 0;
    /**
     * Constant to set a pin to output mode (in a call to pinMode()).
     */
    public static final int OUTPUT = 1;
    /**
     * Constant to set a pin to analog mode (in a call to pinMode()).
     */
    public static final int ANALOG = 2;
    /**
     * Constant to set a pin to PWM mode (in a call to pinMode()).
     */
    public static final int PWM = 3;
    /**
     * Constant to set a pin to servo mode (in a call to pinMode()).
     */
    public static final int SERVO = 4;
    /**
     * Constant to set a pin to shiftIn/shiftOut mode (in a call to pinMode()).
     */
    public static final int SHIFT = 5;
    /**
     * Constant to set a pin to I2C mode (in a call to pinMode()).
     */
    public static final int I2C = 6;

    /**
     * Constant to write a high value (+5 volts) to a pin (in a call to
     * digitalWrite()).
     */
    public static final int LOW = 0;
    /**
     * Constant to write a low value (0 volts) to a pin (in a call to
     * digitalWrite()).
     */
    public static final int HIGH = 1;

    GUIFrame parent;
    Serial serial;
    SerialProxy serialProxy;
    Firmata firmata;

    // We need a class descended from GUIFrame so that we can override the
    // serialEvent() method to capture serial data.  We can't use the Arduino
    // class itself, because PApplet defines a list() method that couldn't be
    // overridden by the static list() method we use to return the available
    // serial ports.  This class needs to be public so that the Serial class
    // can access its serialEvent() method.
    public class SerialProxy extends GUIFrame {

	private static final long serialVersionUID = 2835202402963705869L;

	public SerialProxy() {
	}

	public void serialError(Serial s) {
	    err.println(s.readString());
	}

	public void serialAvailable(Serial s) {
	    err.println(s.readString());
	}

	@Override
	public void serialEvent(Serial which) {
	    try {
		// Notify the Arduino class that there's serial data for it to process.
		while (which.available() > 0) {
		    firmata.processInput(which.read());
		}
	    } catch (Exception e) {
		throw new RuntimeException("Error inside Arduino.serialEvent()");
	    }
	}

    }

    public class FirmataWriter implements Firmata.Writer {

	@Override
	public void write(int val) {
	    try {
		serial.write(val);
	    } catch (Exception e) {
	    }
	    System.out.print("<" + PApplet.hex(val, 2) + " ");
	}

    }

    public void dispose() {
	System.out.println("disposing from Arduino class.");
	this.serial.dispose();
    }

    /**
     * Get a list of the available Arduino boards; currently all serial devices
     * (i.e. the same as Serial.list()). In theory, this should figure out
     * what's an Arduino board and what's not.
     *
     * @return A String Array of available Serial Ports.
     */
    public static String[] list() {
	return Serial.list();
    }

    /**
     * Create a proxy to an Arduino board running the Firmata 2 firmware at the
     * default baud rate of 57600.
     *
     * @param parent the Processing sketch creating this Arduino board (i.e.
     * "this").
     * @param iname the name of the serial device associated with the Arduino
     * board (e.g. one the elements of the array returned by Arduino.list())
     */
    public Arduino(GUIFrame parent, String iname) {
	this(parent, iname, 57600);
    }

    public Arduino(GUIFrame parent) {
	this(parent, list()[0], 57600);
    }

    /**
     * Create a proxy to an Arduino board running the Firmata 2 firmware.
     *
     * @param parent the Processing sketch creating this Arduino board (i.e.
     * "this").
     * @param iname the name of the serial device associated with the Arduino
     * board (e.g. one the elements of the array returned by Arduino.list())
     * @param irate the baud rate to use to communicate with the Arduino board
     * (the Firmata library defaults to 57600, and the examples use this rate,
     * but other firmwares may override it)
     */
    public Arduino(GUIFrame parent, String iname, int irate) {
	this.parent = parent;
	firmata = new Firmata(new FirmataWriter());
	serialProxy = new SerialProxy();
	serial = new Serial(serialProxy, iname, irate);
	// parent.registerMethod("dispose", this);
	try {
	    Thread.sleep(1000); // let bootloader timeout
	} catch (InterruptedException e) {
	}
	firmata.init();
    }

    /**
     * Returns the last known value read from the digital pin - HIGH or LOW.
     *
     * @param pin the digital pin whose value should be returned (from 2 to 13,
     * since pins 0 and 1 are used for serial communication)
     *
     * @return The value of the pin.
     */
    public int digitalRead(int pin) {
	return firmata.digitalRead(pin);
    }

    /**
     * @see com.tester.Arduino
     * @param p The digital Input pin.
     *
     * @return The value of the pin.
     */
    public int read(int p) {
	return digitalRead(p);
    }

    /**
     * Set a digital pin to input or output mode.
     *
     * @param pin the pin whose mode to set (from 2 to 13)
     * @param mode either Arduino.INPUT or Arduino.OUTPUT
     */
    public void pinMode(int pin, int mode) {
	try {
	    firmata.pinMode(pin, mode);
	} catch (Exception e) {
	    throw new RuntimeException("Error inside Arduino.pinMode()");
	}
    }

    /**
     * Sets the IO mode for a pin.
     *
     * @param p The pin
     * @param m The mode.
     */
    public void mode(int p, int m) {
	pinMode(p, m);
    }

    /**
     * Sets the pin as an output.
     *
     * @param p The Pin.
     */
    public void output(int p) {
	pinMode(p, OUTPUT);
    }

    /**
     * Set the pin as an input (note: no pullup resistor).
     *
     * @param p The Pin.
     */
    public void input(int p) {
	pinMode(p, INPUT);
    }

    /**
     * Write to a digital pin (the pin must have been put into output mode with
     * pinMode()).
     *
     * @param pin the pin to write to (from 2 to 13)
     * @param value the value to write: Arduino.LOW (0 volts) or Arduino.HIGH (5
     * volts)
     */
    public void digitalWrite(int pin, int value) {
	try {
	    firmata.digitalWrite(pin, value);
	} catch (Exception e) {
	    throw new RuntimeException("Error inside Arduino.digitalWrite()");
	}
    }

    /**
     * @see Arduino
     * @param p The Pin.
     * @param v The pins value.
     */
    public void dWrite(int p, int v) {
	digitalWrite(p, v);
    }

    /**
     * Toggles the output of a pin.
     *
     * @param pin The pin to toggle.
     */
    public void toggle(int pin) {
	// if(pin==GUIFrame.getArduino().)
	int val = digitalRead(pin);
	switch (val) {
	    case HIGH:
		digitalWrite(pin, LOW);
		break;
	    case LOW:
		dWrite(pin, HIGH);
		break;
	    default:
		GUIFrame.showYesNoDialog("ERROR");
		break;
	}
    }

    /**
     * Write an analog value (PWM-wave) to a digital pin.
     *
     * @param pin the pin to write to (must be 9, 10, or 11, as those are they
     * only ones which support hardware PWM)
     * @param value the value: 0 being the lowest (always off), and 255 the
     * highest (always on)
     */
    public void analogWrite(int pin, int value) {
	try {
	    firmata.analogWrite(pin, value);
	} catch (Exception e) {
	    throw new RuntimeException("Error inside Arduino.analogWrite()");
	}
    }

}
