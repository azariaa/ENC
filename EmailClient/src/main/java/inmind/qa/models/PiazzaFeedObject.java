package main.java.inmind.qa.models;

import main.java.inmind.qa.utils.Utility;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class defines the object which will store the data received from Piazza API
 * @author suruchis (Suruchi Shah)
 * @date 9/29/15
 */
public class PiazzaFeedObject {
    public String sourceType = Utility.SOURCE_TYPE_POST; //Source Type defines the type of document to be indexed
    public String postId;
    public String courseId;
    public List<String> folders; // Tags (Example HW1, Other, HW2..)
    public String nr; // same is cid (unique identifier of the created post
    public boolean isNew; // Is the post new or not. What defines new?
    public String m; // ??
    public String subject; // Subject of the post/note
    public int numberOfAnswerFollowup;
    public String type;
    public List<String> tags; //Example: "["hw1","instructor-note"]" "other" "student" "unanswered" ???
    public String content;
    public String status;
    public String modified;
    public String updated;
    public int upVotes;
    public int indexedDocId; // Used for retrieval
    public List<PiazzaFeedResponse> followups; //denoted by "log", size defines everything about the list

    public Object getFieldValue(String fieldName)   {
        switch(fieldName)   {
            case "sourceType":
                return sourceType;
            case "postId":
                return postId;
            case "courseId":
                return courseId;
            case "folders":
                return folders;
            case "nr":
                return nr;
            case "isNew":
                return isNew;
            case "m":
                return m;
            case "subject":
                return subject;
            case "numberOfAnswerFollowup":
                return numberOfAnswerFollowup;
            case "type":
                return type;
            case "tags":
                return tags;
            case "content":
                return content;
            case "status":
                return status;
            case "modified":
                return modified;
            case "updated":
                return updated;
            case "upVotes":
                return upVotes;
            case "indexedDocId":
                return indexedDocId;
            case "followups":
                return followups;
        }
        return null;
    }

    public void setFieldValue(String fieldName, Object value)   {
        switch(fieldName)   {
            case "postId":
                postId = value.toString();
                break;
            case "courseId":
                courseId = value.toString();
                break;
            case "folders":
                String[] foldersValue = value.toString().replace("[","").replace("]","").split(",");
                folders = Arrays.asList(foldersValue);
                break;
            case "nr":
                nr = value.toString();
                break;
            case "isNew":
                isNew = Boolean.parseBoolean(value.toString());
                break;
            case "m":
                m = value.toString();
                break;
            case "subject":
                subject = value.toString();
                break;
            case "numberOfAnswerFollowup":
                numberOfAnswerFollowup = Integer.parseInt(value.toString());
                break;
            case "type":
                type = value.toString();
                break;
            case "tags":
                String[] tagsValue = value.toString().replace("[","").replace("]","").split(",");
                tags = Arrays.asList(tagsValue);
                break;
            case "content":
                content = value.toString();
                break;
            case "status":
                status = value.toString();
                break;
            case "modified":
                modified = value.toString();
                break;
            case "upVotes":
                upVotes = Integer.parseInt(value.toString());
                break;
            case "indexedDocId":
                indexedDocId = Integer.parseInt(value.toString());
                break;
            case "updated":
                updated = value.toString();
                break;
            case "followups":
                String jsonValue = value.toString();
                if (!jsonValue.equals("[]"))    {
                    List<PiazzaFeedResponse> obj = new Gson().fromJson(jsonValue, new TypeToken<List<PiazzaFeedResponse>>() {}.getType());
                    followups = obj;
                }
                else {
                    followups = new ArrayList<PiazzaFeedResponse>();
                }
                break;
        }
    }
}