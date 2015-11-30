package main.java.inmind.qa.utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

import java.io.File;

/**
 * This class contains the utility methods AND string constants
 * @author suruchis (Suruchi Shah)
 * Date: 10/19/2015
 */
public class Utility {

    // TAGS
    public static String INSTRUCTOR_NOTE = "instructor-note";
    public static String UNANSWERED = "unanswered";

    // POST TYPES
    public static String POST_TYPE_QUESTION = "question";

    // POST KEYS
    public static String SOURCE_TYPE = "sourceType";
    public static String SOURCE_TYPE_ATTACHMENT = "attachment";
    public static String SOURCE_TYPE_POST = "post";
    public static String ID = "id";
    public static String NR = "nr";
    public static String SUBJECT = "subject";
    public static String IS_NEW = "is_new";
    public static String M = "m";
    public static String STATUS = "status";
    public static String FOLDERS = "folders";
    public static String TAGS = "tags";
    public static String TYPE = "type";
    public static String NO_ANSWER_FOLLOWUP = "no_answer_followup";
    public static String MODIFIED = "modified";
    public static String UPDATED = "updated";
    public static String TAG_GOOD_PROF = "tag_good_prof";
    public static String HISTORY = "history";
    public static String CONTENT = "content";
    public static String ANONYMOUS = "anon";
    public static String NO_ANSWER = "no_answer";
    public static String CREATED = "created";
    public static String TAG_ENDORSE = "tag_endorse";
    public static String CHILDREN = "children";
    public static String VERSION = "version";

    // PIAZZA EMAIL CONSTANTS
    public static String PIAZZA = "PIAZZA";
    public static String SENDER_SOURCE = "SENDERSOURCE";
    public static String RAW_EMAIL_CONTENT = "RAWCONTENT";

    /**
     * This method takes a PDF file as input and extracts the text from it using Apache PDF Box
     * @param fileName Input PDF File
     * @author suruchishah
     */
    public String convertPdfToText(String fileName)  {
        File inputFile = new File(fileName);
        String output = "";
        try {
            PDDocument pd = PDDocument.load(inputFile);
            PDFTextStripper stripper = new PDFTextStripper();
            output = stripper.getText(pd);
            if (pd != null) {
                pd.close();
            }
        } catch (Exception e)   {
            e.printStackTrace();
        }
        return output;
    }
}
