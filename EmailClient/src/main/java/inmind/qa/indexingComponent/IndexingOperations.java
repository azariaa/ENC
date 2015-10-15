package EmailClient.src.main.java.inmind.qa.indexingComponent;

import EmailClient.src.main.java.inmind.piazza.PiazzaApi;
import EmailClient.src.main.java.inmind.qa.answeringComponent.AnswerPreprocess;
import org.apache.lucene.index.Term;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class contains the logic for all indexing operations
 * @author suruchis (Suruchi Shah)
 * @date 9/26/15
 */
public class IndexingOperations {

    private static LuceneEngine LUCENE_ENGINE;
    private static String SCRIPTS_PATH;
    private static PiazzaApi PIAZZA_API = null;


    public IndexingOperations(String pythonScriptPaths, String userName, String password) {
        this.SCRIPTS_PATH = pythonScriptPaths;
        PIAZZA_API = new PiazzaApi(userName, password);
        try {
            LUCENE_ENGINE = new LuceneEngine();
        } catch (Exception e)   {
            e.printStackTrace();
        }
    }

    /**
     * Gets the Piazza Posts and Indexes them
     * @param feedArray
     * @param piazzaApi
     * @param courseNumber
     */
    public void addPiazzaPosts(JSONArray feedArray, PiazzaApi piazzaApi, String courseNumber)    {
        PiazzaFeedObject feedObject;
        try {
            for (int i = 0; i < feedArray.length(); i++) {
                JSONObject object = feedArray.getJSONObject(i);
                String isReindexingRequiredResult = isReindexingRequired(object);
                if (isReindexingRequiredResult.equals("TRUE")) {
                    // Delete the document from the index first
                    LUCENE_ENGINE.deleteDocument(new Term("id", object.get("id").toString()));
                }
                if (!isReindexingRequiredResult.equals("FALSE"))  {
                    feedObject = new PiazzaFeedObject();
                    feedObject.postId = object.get("id").toString();
                    feedObject.courseId = courseNumber;
                    feedObject.content = piazzaApi.getFeedFullContent(courseNumber, object.get("nr").toString());
                    feedObject.subject = (object.get("subject").toString());
                    feedObject.nr = (object.get("nr").toString());
                    feedObject.isNew = (Boolean.parseBoolean(object.get("is_new").toString()));
                    feedObject.m = (object.get("m").toString());
                    feedObject.status = (object.get("status").toString());
                    String[] folders = object.get("folders").toString().replace("[", "").replace("]", "").replace("\"", "").split(",");
                    feedObject.folders = (Arrays.asList(folders));
                    String[] tags = object.get("tags").toString().replace("[", "").replace("]", "").replace("\"", "").split(",");
                    feedObject.tags = (Arrays.asList(tags));
                    feedObject.type = object.get("type").toString();
                    String noAnswerFollowup = object.get("no_answer_followup").toString();
                    feedObject.numberOfAnswerFollowup = (Integer.parseInt(noAnswerFollowup));
                    feedObject.followups = getPiazzaPostFollowups(object, piazzaApi, courseNumber);
                    feedObject.updated = (object.getString("updated"));
                    feedObject.modified = (object.getString("modified"));
                    feedObject.indexedDocId = Integer.MIN_VALUE;

                    // NOTE: we do this step before the indexing so that it doesn't return the same post
                    // Now that the post has been indexed, we check if it is a question and needs to find the answer
                    if(isPostQuestion(feedObject))  {
                        // Instantiate Answering Component and find the answer
                        answerQuestion(feedObject);
                    }

                    LUCENE_ENGINE.addDocument(feedObject);

                }

            }
        } catch (Exception e)   {
            e.printStackTrace();
        }
        System.out.println("All recent feed has been indexed");
    }

    /**
     * Gets the followup posts and comments for a given Piazza Post
     * @param object
     * @param piazzaApi
     * @param courseNumber
     * @return
     */
    public List<PiazzaFeedResponse> getPiazzaPostFollowups(JSONObject object, PiazzaApi piazzaApi, String courseNumber) {
        List<PiazzaFeedResponse> followups = new ArrayList<PiazzaFeedResponse>();
        try {
            // If version is greater than 1, there are followups
            if (object.has("version") && Integer.parseInt(object.get("version").toString()) > 1) {
                JSONArray feedFollowupArray = piazzaApi.getFeedFollowup(courseNumber, object.get("nr").toString());
                PiazzaFeedResponse feedResponse;
                for (int j = 0; j < feedFollowupArray.length(); j++) {
                    JSONObject feedFollowupObject = feedFollowupArray.getJSONObject(j);
                    feedResponse = new PiazzaFeedResponse();
                    if(feedFollowupObject.has("subject")) {
                        feedResponse.content = feedFollowupObject.get("subject").toString();
                    } else if(feedFollowupObject.has("history"))    {
                        JSONArray jsonArrayForHistory = feedFollowupObject.getJSONArray("history");
                        JSONObject historyObject = jsonArrayForHistory.getJSONObject(jsonArrayForHistory.length()-1);
                        feedResponse.content = historyObject.get("content").toString();
                    }
                    if(feedFollowupObject.has("anon")) {
                        feedResponse.isAnonymous = Boolean.parseBoolean(feedFollowupObject.get("anon").toString());
                    }
                    if(feedFollowupObject.has("type")) {
                        feedResponse.type = feedFollowupObject.get("type").toString();
                    }
                    if(feedFollowupObject.has("no_answer")) {
                        feedResponse.numberOfAnswers = Integer.parseInt(feedFollowupObject.get("no_answer").toString());
                    }
                    if(feedFollowupObject.has("created")) {
                        feedResponse.timeCreated = feedFollowupObject.get("created").toString();
                    }
                    if(feedFollowupObject.has("updated")) {
                        feedResponse.timeUpdated = feedFollowupObject.get("updated").toString();
                    }
                    feedResponse.id = feedFollowupObject.get("id").toString();

                    List<PiazzaFeedResponse> subFollowups = new ArrayList<PiazzaFeedResponse>();
                    if(feedResponse.numberOfAnswers > 0)    {
                        PiazzaFeedResponse response;
                        JSONArray feedbackArray = feedFollowupObject.getJSONArray("children");
                        for (int k = 0; k < feedbackArray.length(); k++)    {
                            JSONObject feedbackObject = feedbackArray.getJSONObject(k);
                            response = new PiazzaFeedResponse();
                            response.isAnonymous = Boolean.parseBoolean(feedbackObject.get("anon").toString());
                            response.content = feedbackObject.get("subject").toString();
                            response.type = feedbackObject.get("type").toString();
                            response.id = feedbackObject.get("id").toString();
                            response.timeCreated = feedbackObject.get("created").toString();
                            response.timeUpdated = feedbackObject.get("updated").toString();
                            subFollowups.add(response);
                        }
                    }
                    feedResponse.subResponses = subFollowups;
                    followups.add(feedResponse);
                }
            }

        } catch (Exception e)   {
            e.printStackTrace();
        }
        return followups;
    }

    /**
     * Method to check if Reindexing for a given post is required or not.
     * We first check if the post id exists or not, if it does we check if the last modified date is the same or not
     * @param object JSONObject
     * @return  "FALSE" - No indexing requred, "TRUE" - Re-indexing Required, "NO-ID" index the brand new post
     */
    public String isReindexingRequired(JSONObject object)  {
        try {
            // Check if id exists in the index
            String currentId = object.get("id").toString();
            Boolean postExists = LUCENE_ENGINE.ifPostExists(currentId, "postId", 1);
            if(postExists)   {
                // Now that the post exists, we check for modified date
                String currentModifiedDate = object.get("modified").toString();
                List<String> output = LUCENE_ENGINE.searchIndexDefineReturnObjects(currentId, "postId", new ArrayList<String>(Arrays.asList("modified")), 1);
                String newModifiedDate = output.get(0).split("~:")[1];
                if(newModifiedDate.equals(currentModifiedDate))  {
                    System.out.println("Piazza Post: " + currentId + " exists");
                    return "FALSE";
                }
                return "TRUE";
            }
        } catch (Exception e)   {
            e.printStackTrace();
        }
        return "NO-ID";
    }

    /**
     * Method to check if this post is a new question that needs to be answered
     * @param feedObject
     * @return
     */
    public boolean isPostQuestion(PiazzaFeedObject feedObject)    {
        if(feedObject.type.equals("question")
//                && feedObject.isNew == true
                && feedObject.tags.contains("unanswered"))  {
            return true;
        }
        return false;
    }

    public void answerQuestion(PiazzaFeedObject feedObject) {
        String rawQuery = feedObject.content;
        rawQuery = rawQuery.replace("?","").replace("<p>","").replace("</p>","");
        // Assumption: if content area is blank, we do not attempt to answer the question
        if(rawQuery.trim().length() < 1)    {
            return;
        }
        AnswerPreprocess answering = new AnswerPreprocess(SCRIPTS_PATH);
        StringBuilder answers = answering.getAnswer(rawQuery);
        if(answers == null || answers.toString() == "") {
            System.out.println("Tried to answer: " +  rawQuery + "\t No response found.");
        }
        else    {
            PIAZZA_API.answerQuestion(feedObject.postId, feedObject.courseId, answers.toString());
        }
    }

}
