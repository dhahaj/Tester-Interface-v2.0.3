package com.tester;


import java.awt.event.*;


/**
 * Interface to receive login and logout events.
 *
 * @author dhahaj
 */
public class LoginEvent extends ActionEvent {

    private static final long serialVersionUID = -4861981272535322698L;

    // private final String cmd = null;

    public LoginEvent(Object source, int id, String command) {
        super(source, id, command);
    }


}
