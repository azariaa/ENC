package EmailClient.src.main.java.inmind.qa;

import EmailClient.src.main.java.inmind.email.EmailConstants;
import com.google.gson.JsonObject;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;

/**
 * This class contains the logic for all indexing operations
 * @author suruchis (Suruchi Shah)
 * @date 9/26/15
 */
public class IndexingOperations {

    private static String indexPath;
    private static LuceneEngine luceneEngine;

    public IndexingOperations(String indexpath) {
        setIndexPath(indexpath);
        try {
            luceneEngine = new LuceneEngine(getIndexPath());
        } catch (Exception e)   {
            e.printStackTrace();
        }
    }

    public String getIndexPath() {
        return indexPath;
    }

    public void setIndexPath(String indexPath) {
        this.indexPath = indexPath;
    }

    /**
     * Adds all the recently extracted emails to the index
     * Condition: only if email type = PIAZZA and type is NOT question
     * @param emailsArray
     */
    public static void addEmailDocuments(JSONArray emailsArray)  {
        IndexDocument document;
        try {
            for (int i = 0; i < emailsArray.length(); i++) {
                JSONObject object = emailsArray.getJSONObject(i);
                if (object.get(EmailConstants.SENDERSOURCE.toString()).equals("PIAZZA") && !object.get(EmailConstants.TYPE.toString()).equals("QUESTION"))    {
                    document = new IndexDocument();
                    document.setSubject(object.get(EmailConstants.SUBJECT.toString()).toString());
                    document.setContent(object.get(EmailConstants.CONTENT.toString()).toString());
                    document.setSource("PIAZZA EMAIL");
                    document.setDate(new Date());
                    luceneEngine.addDocument(document);
                }
            }
            luceneEngine.LE_IndexWriter.close();
        } catch (Exception e)   {
            e.printStackTrace();
        }
        System.out.println("All recent emails have been indexed at " + indexPath);
    }

    public static void addPiazzaPosts(JSONArray feedArray)    {

        try {
            for (int i = 0; i < feedArray.length(); i++) {
                JSONObject object = feedArray.getJSONObject(i);

            }
        } catch (Exception e)   {

        }
    }
}
