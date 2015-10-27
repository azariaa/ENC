package EmailClient.src.main.java.inmind.qa.indexingComponent;

import EmailClient.src.main.java.inmind.piazza.PiazzaApi;
import EmailClient.src.main.java.inmind.qa.answeringComponent.AnswerPreprocess;
import EmailClient.src.main.java.inmind.qa.models.PiazzaAttachment;
import EmailClient.src.main.java.inmind.qa.models.PiazzaFeedObject;
import EmailClient.src.main.java.inmind.qa.models.PiazzaFeedResponse;
import EmailClient.src.main.java.inmind.qa.utils.Utility;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.index.Term;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class contains the logic for all indexing operations
 * @author suruchis (Suruchi Shah)
 * Date: 9/26/15
 */
public class IndexingOperations {

    private static LuceneEngine LUCENE_ENGINE;
    private static String SCRIPTS_PATH;
    private static PiazzaApi PIAZZA_API = null;
    private static Logger LOG;

    public IndexingOperations(String pythonScriptPaths, String userName, String password) {
        LOG = Logger.getLogger(IndexingOperations.class.getName());
        SCRIPTS_PATH = pythonScriptPaths;
        PIAZZA_API = new PiazzaApi(userName, password);
        try {
            LUCENE_ENGINE = new LuceneEngine();
        } catch (Exception e)   {
            e.printStackTrace();
        }
    }

    /**
     * Gets the Piazza Posts and Indexes them
     * @param feedArray Array of Piazza Posts
     * @param piazzaApi piazza api object
     * @param courseNumber course number that we are looking at
     */
    public void addPiazzaPosts(JSONArray feedArray, PiazzaApi piazzaApi, String courseNumber, boolean answerQuestion)    {
        PiazzaFeedObject feedObject;

        // Get all Post IDs to be used for answering
        LOG.info("Getting all POST IDs to be used for answering");
        List<String> postIds = getAllPostIds(feedArray);

        try {
            for (int i = 0; i < feedArray.length(); i++) {
                JSONObject object = feedArray.getJSONObject(i);
                String isReindexingRequiredResult = isReindexingRequired(object);
                LOG.info("Post: " + object.getString(Utility.ID) + " indexing required:" + isReindexingRequiredResult);
                if (isReindexingRequiredResult.equals("TRUE")) {
                    // Delete the document from the index first
                    LOG.info("Post: " + object.getString(Utility.ID) + " being deleted");
                    LUCENE_ENGINE.deleteDocument(new Term(Utility.ID, object.get(Utility.ID).toString()));
                }
                if (!isReindexingRequiredResult.equals("FALSE"))  {
                    LOG.info("Post: " + object.getString(Utility.ID) + " start indexing");
                    feedObject = new PiazzaFeedObject();
                    feedObject.postId = object.get(Utility.ID).toString();
                    feedObject.courseId = courseNumber;
                    feedObject.content = piazzaApi.getFeedFullContent(courseNumber, object.get(Utility.NR).toString());
                    feedObject.subject = (object.get(Utility.SUBJECT).toString());
                    feedObject.nr = (object.get(Utility.NR).toString());
                    feedObject.isNew = (Boolean.parseBoolean(object.get(Utility.IS_NEW).toString()));
                    feedObject.m = (object.get(Utility.M).toString());
                    feedObject.status = (object.get(Utility.STATUS).toString());
                    String[] folders = object.get(Utility.FOLDERS).toString().replace("[", "").replace("]", "").replace("\"", "").split(",");
                    feedObject.folders = (Arrays.asList(folders));
                    String[] tags = object.get(Utility.TAGS).toString().replace("[", "").replace("]", "").replace("\"", "").split(",");
                    feedObject.tags = (Arrays.asList(tags));
                    feedObject.type = object.get(Utility.TYPE).toString();
                    String noAnswerFollowup = object.get(Utility.NO_ANSWER_FOLLOWUP).toString();
                    feedObject.numberOfAnswerFollowup = (Integer.parseInt(noAnswerFollowup));
                    feedObject.followups = getPiazzaPostFollowups(object, piazzaApi, courseNumber);
                    feedObject.updated = (object.getString(Utility.UPDATED));
                    feedObject.modified = (object.getString(Utility.MODIFIED));
                    if(object.has(Utility.TAG_GOOD_PROF)) {
                        feedObject.upVotes = Integer.parseInt(object.get(Utility.TAG_GOOD_PROF).toString());
                    } else  {
                        feedObject.upVotes = 0;
                    }
                    feedObject.indexedDocId = Integer.MIN_VALUE;

                    // Check for attachments in the content to extract the pdf content and index that
                    checkForAttachments(feedObject.content, feedObject);

                    // NOTE: we do this step before the indexing so that it doesn't return the same post
                    // Now that the post has been indexed, we check if it is a question and needs to find the answer
                    if(isPostQuestion(feedObject) && answerQuestion)  {
                        // Instantiate Answering Component and find the answer
                        LOG.info("Post: " + object.getString(Utility.ID) + " is a question which needs to be answered");
                        answerQuestion(feedObject, postIds);
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
     * @param object post object
     * @param piazzaApi piazza api object
     * @param courseNumber course number
     * @return list of PiazzaFeedResponse
     */
    public List<PiazzaFeedResponse> getPiazzaPostFollowups(JSONObject object, PiazzaApi piazzaApi, String courseNumber) {
        List<PiazzaFeedResponse> followups = new ArrayList<PiazzaFeedResponse>();
        try {
            // If version is greater than 1, there are followups
            if (object.has(Utility.VERSION) && Integer.parseInt(object.get(Utility.VERSION).toString()) > 1) {
                JSONArray feedFollowupArray = piazzaApi.getFeedFollowup(courseNumber, object.get(Utility.NR).toString());
                PiazzaFeedResponse feedResponse;
                for (int j = 0; j < feedFollowupArray.length(); j++) {
                    JSONObject feedFollowupObject = feedFollowupArray.getJSONObject(j);
                    feedResponse = new PiazzaFeedResponse();
                    if(feedFollowupObject.has(Utility.SUBJECT)) {
                        feedResponse.content = feedFollowupObject.get(Utility.SUBJECT).toString();
                    } else if(feedFollowupObject.has(Utility.HISTORY))    {
                        JSONArray jsonArrayForHistory = feedFollowupObject.getJSONArray(Utility.HISTORY);
                        JSONObject historyObject = jsonArrayForHistory.getJSONObject(jsonArrayForHistory.length()-1);
                        feedResponse.content = historyObject.get(Utility.CONTENT).toString();
                    }
                    if(feedFollowupObject.has(Utility.ANONYMOUS)) {
                        feedResponse.isAnonymous = Boolean.parseBoolean(feedFollowupObject.get(Utility.ANONYMOUS).toString());
                    }
                    if(feedFollowupObject.has(Utility.TYPE)) {
                        feedResponse.type = feedFollowupObject.get(Utility.TYPE).toString();
                    }
                    if(feedFollowupObject.has(Utility.NO_ANSWER)) {
                        feedResponse.numberOfAnswers = Integer.parseInt(feedFollowupObject.get(Utility.NO_ANSWER).toString());
                    }
                    if(feedFollowupObject.has(Utility.CREATED)) {
                        feedResponse.timeCreated = feedFollowupObject.get(Utility.CREATED).toString();
                    }
                    if(feedFollowupObject.has(Utility.UPDATED)) {
                        feedResponse.timeUpdated = feedFollowupObject.get(Utility.UPDATED).toString();
                    }
                    if (feedFollowupObject.has(Utility.TAG_ENDORSE))  {
                        JSONArray arr = feedFollowupObject.getJSONArray(Utility.TAG_ENDORSE);
                        feedResponse.upVotes = arr.length();
                    } else  {
                        feedResponse.upVotes = 0;
                    }

                    feedResponse.id = feedFollowupObject.get(Utility.ID).toString();

                    List<PiazzaFeedResponse> subFollowups = new ArrayList<PiazzaFeedResponse>();
                    if(feedResponse.numberOfAnswers > 0)    {
                        PiazzaFeedResponse response;
                        JSONArray feedbackArray = feedFollowupObject.getJSONArray(Utility.CHILDREN);
                        for (int k = 0; k < feedbackArray.length(); k++)    {
                            JSONObject feedbackObject = feedbackArray.getJSONObject(k);
                            response = new PiazzaFeedResponse();
                            response.isAnonymous = Boolean.parseBoolean(feedbackObject.get(Utility.ANONYMOUS).toString());
                            response.content = feedbackObject.get(Utility.SUBJECT).toString();
                            response.type = feedbackObject.get(Utility.TYPE).toString();
                            response.id = feedbackObject.get(Utility.ID).toString();
                            response.timeCreated = feedbackObject.get(Utility.CREATED).toString();
                            response.timeUpdated = feedbackObject.get(Utility.UPDATED).toString();
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
            String currentId = object.get(Utility.ID).toString();
            Boolean postExists = LUCENE_ENGINE.ifPostExists(currentId, "postId", 1);
            if(postExists)   {
                // Now that the post exists, we check for modified date
                String currentModifiedDate = object.get(Utility.MODIFIED).toString();
                List<String> output = LUCENE_ENGINE.searchIndexDefineReturnObjects(currentId, "postId", new ArrayList<String>(Arrays.asList(Utility.MODIFIED)), 1);
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
     * @param feedObject object
     * @return true/false
     */
    public boolean isPostQuestion(PiazzaFeedObject feedObject)    {
        if(feedObject.type.equals(Utility.POST_TYPE_QUESTION)
                && feedObject.numberOfAnswerFollowup <= 1
                && feedObject.tags.contains(Utility.UNANSWERED))  {
            return true;
        }
        return false;
    }

    /**
     * If a new object to be indexed needs to be answered, this method calls the AnswerPreprocess component
     * and extracts the answer to then be posted on Piazza using the PiazzaAPI
     * @param feedObject PiazzaFeedObject post object
     * @param postIds list of PostIds
     */
    public void answerQuestion(PiazzaFeedObject feedObject, List<String> postIds) {
        String rawQuery = feedObject.content;
        //TODO look at content and title of the post. Also, complex text preprocessing to extract the question component

        rawQuery = rawQuery.replace("?","").replace("<p>","").replace("</p>","");
        // Assumption: if content area is blank, we do not attempt to answer the question
        if(rawQuery.trim().length() < 1)    {
            return;
        }
        AnswerPreprocess answering = new AnswerPreprocess(SCRIPTS_PATH, postIds);
        StringBuilder answers = answering.getAnswer(rawQuery);
        if(answers == null || answers.toString() == "") {
            LOG.info("Tried to answer: " + rawQuery + "\t No response found.");
        }
        else    {
            PIAZZA_API.answerQuestion(feedObject.postId, feedObject.courseId, answers.toString());
        }
    }

    public void checkForAttachments(String strContent, PiazzaFeedObject feedObject) {
        // We only check for attachments to index if the post if posted by instructors and type is not "question"
        if(!feedObject.type.equals(Utility.POST_TYPE_QUESTION) && feedObject.tags.contains(Utility.INSTRUCTOR_NOTE)) {
            ArrayList<String> links = new ArrayList<String>();
            Pattern p = Pattern.compile("\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]", Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(strContent);
            while (m.find()) {
                String urlStr = m.group();
                if (urlStr.startsWith("(") && urlStr.endsWith(")")) {
                    urlStr = urlStr.substring(1, urlStr.length() - 1);
                }
                links.add(urlStr);
            }
            try {
                for (String link : links) {
                    if(link.endsWith(".pdf")) {
                        FileUtils.copyURLToFile(new URL(link), new File(SCRIPTS_PATH + "tempFile.pdf"));
                        String fileContent = new Utility().convertPdfToText(SCRIPTS_PATH + "tempFile.pdf");
                        LOG.info("indexing document at: " + link);
                        // Now that we have the file content, we index it
                        PiazzaAttachment piazzaAttachment = new PiazzaAttachment();
                        piazzaAttachment.content = fileContent;
                        piazzaAttachment.courseId = feedObject.courseId;
                        piazzaAttachment.modifiedDate = feedObject.modified;
                        piazzaAttachment.postId = feedObject.postId;
                        piazzaAttachment.upVotes = feedObject.upVotes;
                        LUCENE_ENGINE.addDocument(piazzaAttachment);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method gets the list of post Ids. We use this list to check if a post has been deleted before referring to
     * it for answering component
     * @param feedArray all feed array
     * @return list of post Ids
     */
    private List<String> getAllPostIds(JSONArray feedArray) {
        List<String> allPostIds = new ArrayList<String>();
        try {
            for (int i = 0; i < feedArray.length(); i++) {
                JSONObject object = feedArray.getJSONObject(i);
                allPostIds.add(object.getString(Utility.ID));
            }
        } catch (Exception e)   {
            e.printStackTrace();
        }
        return allPostIds;
    }
}
