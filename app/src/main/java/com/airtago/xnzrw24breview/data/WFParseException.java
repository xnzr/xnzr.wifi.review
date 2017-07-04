package com.airtago.xnzrw24breview.data;

/**
 * Created by alexe on 04.07.2017.
 */

public class WFParseException extends Exception {
    public WFParseException() {
    }
    public WFParseException(String detailMessage) {
        super(detailMessage);
    }
    public WFParseException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
    public WFParseException(Throwable throwable) {
        super(throwable);
    }
}
