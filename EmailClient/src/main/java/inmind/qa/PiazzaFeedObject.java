package EmailClient.src.main.java.inmind.qa;

import com.google.api.client.util.DateTime;

import java.util.Date;
import java.util.List;

/**
 * This class defines the object which will store the data received from Piazza API
 * @author suruchis (Suruchi Shah)
 * @date 9/29/15
 */
public class PiazzaFeedObject {
    private static List<String> folders; // Tags (Example HW1, Other, HW2..)
    private static String nr; // same is cid (unique identifier of the created post
    private static boolean isNew; // Is the post new or not. What defines new?
    private static String mainVersion; // ??
    private static String m; // ??
    private static String requestInstructor;
    private static String subject; // Subject of the post/note
    private static int numberOfAnswerFollowup;
    private static String type;
    private static List<String> tags; //Example: "["hw1","instructor-note"]" "other" "student" "unanswered" ???
    private static String content;
    private static String status;
    private static Date modified;
    private static Date updated;
    private static List<PiazzaFeedResponse> logs; //denoted by "log", size defines everything about the list
}

enum PiazzaFeedType  {
    question("question"),
    note("note"),
    post("post");

    private final String text;

    private PiazzaFeedType(final String text) {
        this.text = text;
    }

    public String toString() {
        return text;
    }
}
