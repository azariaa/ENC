package main.java.inmind.email;

/**
 * This class contains all the constants for Email related strings
 * @author suruchis (Suruchi Shah)
 */
public enum EmailConstants {
    CONTENT("CONTENT"),
    SUBJECT("SUBJECT"),
    SENDER("SENDER"),
    RECEIVEDATE("RECEIVEDATE"),
    RECIPIENT("RECIPIENT"),
    SENTDATE("SENTDATE"),
    SENDERSOURCE("SENDERSOURCE"),
    EMAILID("EMAILID"),
    TYPE("TYPE"),
    RAWCONTENT("RAWCONTENT")
    ;

    private final String text;

    private EmailConstants(final String text) {
        this.text = text;
    }

    public String toString() {
        return text;
    }
}