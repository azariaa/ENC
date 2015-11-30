package main.java.inmind.qa.models;


import main.java.inmind.qa.utils.Utility;

/**
 * This class defines the object for attachments content to be indexed
 * @author suruchis (Suruchi Shah)
 * Date: 10/19/2015
 */
public class PiazzaAttachment {
    public String sourceType = Utility.SOURCE_TYPE_ATTACHMENT; //Source Type defines the type of document to be indexed
    public String postId;
    public String courseId;
    public int upVotes;
    public String content;
    public String modified;
    public int indexedDocId; // Used for retrieval

    public Object getFieldValue(String fieldName)   {
        switch (fieldName) {
            case "postId":
                return postId;
            case "sourceType":
                return sourceType;
            case "courseId":
                return courseId;
            case "upVotes":
                return upVotes;
            case "content":
                return content;
            case "modified":
                return modified;
            case "indexedDocId":
                return indexedDocId;
        }
        return null;
    }

    public void setFieldValue(String fieldName, Object value) {
        switch (fieldName) {
            case "postId":
                postId = value.toString();
                break;
            case "courseId":
                courseId = value.toString();
                break;
            case "upVotes":
                upVotes = Integer.parseInt(value.toString());
                break;
            case "content":
                content = value.toString();
                break;
            case "modified":
                modified = value.toString();
                break;
            case "indexedDocId":
                indexedDocId = Integer.parseInt(value.toString());
                break;
        }
    }
}
