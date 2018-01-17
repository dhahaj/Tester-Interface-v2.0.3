/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tester;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import static processing.core.PApplet.println;

/**
 * Class for evaluating return values from the QuickWriter Software.
 *
 * @author daniel
 */
public enum QWErrorMessage {

    EF_port(0x1, "Error Communicating with Port"),
    EF_timeout(0x2, "Communication timeout, Hardware not Responding"),
    EF_RX(0x4, "Communication Error Detected, BAD Data received"),
    EF_failed(0x8, "Current Programming Task Failed"),
    EF_badfw(0x10, "Firmware update Required"),
    EF_HIGHBAUD(0x20, "Higher Transfer Speed Failed"),
    EF_abort(0x40, "User Aborted Task in progress"),
    EF_unknown(0x80, "Unknown Error has Occured"),
    EF_VDDLV(0x100, "VDD Voltage TOO LOW (+5)"),
    EF_VDDHV(0x200, "VDD Voltage TOO HIGH"),
    EF_VDDTIME(0x400, "VDD not rising"),
    EF_VPPLV(0x800, "MCLR Voltage TOO LOW"),
    EF_VPPHV(0x1000, "MCLR Voltage TOO HIGH"),
    EF_VPPTIME(0x2000, "MCLR not rising"),
    EF_VDD5(0x4000, "Voltage detected on VDD"),
    EF_VPPNZ(0x8000, "Can not pull MCLR low");

    private final String message;
    private final int errorCode;

    private QWErrorMessage(int code, String message) {
	this.errorCode = code;
	this.message = message;
    }

    public String getErrorMessage() {
	return message;
    }

    public int getErrorCode() {
	return errorCode;
    }

    private static QWErrorMessage getError(int code) {
	for (QWErrorMessage q : QWErrorMessage.values()) {
	    if (q.errorCode == code) {
		return q;
	    }
	}
	return null;
    }

    /**
     * Evaluates the return value and returns a String of all the error
     * messages.
     *
     * @param returnValue The exit code from the software.
     * @return The String result
     */
    public static String evalResult(int returnValue) {
	List<QWErrorMessage> list = new ArrayList<>();
	int mask = 0x0001;
	while (mask <= 0x8000) {
	    int v = mask & returnValue;
	    if (v != 0) {
		list.add(getError(v));
	    }
	    mask = mask << 1;
	}
	StringBuilder sb = new StringBuilder(50);
	sb.append("[");
	for (QWErrorMessage q : list) {
	    sb.append(q.message);
	    sb.append(", ");
	}
	return sb.replace(sb.lastIndexOf(", "), sb.length(), "]").toString();
    }

}
