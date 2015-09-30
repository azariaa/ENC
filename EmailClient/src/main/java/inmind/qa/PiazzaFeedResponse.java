package EmailClient.src.main.java.inmind.qa;

import java.util.List;

/**
 * This class defines the object which will store the data received from Piazza API's Feed Object.
 * Each object has a "log" which contains the list of follow-ups, feedbacks, etc. and their unique ids
 * @author suruchis (Suruchi Shah)
 * @date 9/29/15
 */
public class PiazzaFeedResponse {
    private static String id; // denoted by u
    private static String time; // denoted by t
    private static String code; // denoted by n

    private static boolean isAnonymous; // "anon"
    private static List<String> folders;
    private static String content; // "subject" This is the content
    private static String type;
    private static List<PiazzaFeedResponse> subResponses;
    private static int numberOfAnswers;

}

enum FeedbackType  {
    create("create"),
    followup("followup"),
    feedback("feedback");

    private final String text;

    private FeedbackType(final String text) {
        this.text = text;
    }

    public String toString() {
        return text;
    }
}