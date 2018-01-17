package com.tester;

import java.beans.*;
import static processing.core.PApplet.println;

/**
 *
 * @author dhahaj
 */
public class LoginHandler extends EventHandler {

    public LoginHandler(Object target, String action, String eventPropertyName, String listenerMethodName) {
        super(target, action, eventPropertyName, listenerMethodName);
        println(eventPropertyName);
    }

}
