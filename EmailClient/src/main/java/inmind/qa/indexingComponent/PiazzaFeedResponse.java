package EmailClient.src.main.java.inmind.qa.indexingComponent;

import java.util.List;

/**
 * This class defines the object which will store the data received from Piazza API's Feed Object.
 * Each object has a "log" which contains the list of follow-ups, feedbacks, etc. and their unique ids
 * @author suruchis (Suruchi Shah)
 * @date 9/29/15
 */
public class PiazzaFeedResponse {
    public String id;
    public String timeCreated;
    public String timeUpdated;

    public boolean isAnonymous; // "anon"
    public String content; // "subject" This is the content
    public String type;
    public List<PiazzaFeedResponse> subResponses;
    public int numberOfAnswers; // "no_answer"
}