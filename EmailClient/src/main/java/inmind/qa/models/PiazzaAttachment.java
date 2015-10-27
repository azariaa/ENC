package EmailClient.src.main.java.inmind.qa.models;

/**
 * This class defines the object for attachments content to be indexed
 * @author suruchis (Suruchi Shah)
 * Date: 10/19/2015
 */
public class PiazzaAttachment {
    public String sourceType = "attachment"; //Source Type defines the type of document to be indexed
    public String postId;
    public String courseId;
    public int upVotes;
    public String content;
    public String modifiedDate;
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
            case "modifiedDate":
                return modifiedDate;
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
            case "modifiedDate":
                modifiedDate = value.toString();
                break;
            case "indexedDocId":
                indexedDocId = Integer.parseInt(value.toString());
                break;
        }
    }
}
