package com.tester;

import static com.tester.Arduino.LOW;
import static com.tester.Arduino.OUTPUT;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JFrame;

import static com.tester.Arduino.*;
import static processing.core.PApplet.println;

/**
 * Window that displays a manual interface for control the IO pins.
 *
 * @author Daniel
 */
public final class ManualWindow extends JFrame
        implements WindowListener, ActionListener {

    RunTest.Pins pins = RunTest.getPins();
    java.util.List<JButton> buttons = new ArrayList<>();

    public ManualWindow(String title) {
        super(title);
        setLayout(new GridLayout(3, 3));
        super.setSize(400, 500);
        init();
    }

    public void init() {
        for (RunTest.Pins p : RunTest.Pins.values()) {
            if (p.IO == Arduino.INPUT) {
                continue;
            }
            JButton b = new JButton(p.name());
            b.setSize(200, 130);
            b.setBackground(Color.GRAY);
            b.setActionCommand("" + p.pin);
            b.addActionListener(this);
            getContentPane().add(b);
            buttons.add(b);
        }
//    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(this);
        //pack();
        setLocation(GUIFrame.frame.getLocation().x + 300, GUIFrame.frame.getLocation().y + 300);
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
        try {
            setAllLow();
        } catch (Exception ex) {
        }
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }

    private void setAllLow() {
        for (RunTest.Pins p : RunTest.Pins.values()) {
            if (p.IO == OUTPUT) {
                GUIFrame.getArduino().dWrite(p.pin, LOW);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JButton b = (JButton) e.getSource();
        String command = e.getActionCommand();
        int value = new Integer(command);
        if (b.getBackground() == Color.GRAY) {
            GUIFrame.getArduino().dWrite(value, Arduino.HIGH);
            b.setBackground(Color.DARK_GRAY);
            println("Pin=" + value + ", value=" + 1);
        } else {
            GUIFrame.getArduino().dWrite(value, Arduino.LOW);
            b.setBackground(Color.GRAY);
            println("Pin=" + value + ", value=" + 0);
        }
    }

}
