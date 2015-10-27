package EmailClient.src.main.java.inmind.qa.answeringComponent;

import EmailClient.src.main.java.inmind.qa.indexingComponent.Index;
import EmailClient.src.main.java.inmind.qa.indexingComponent.LuceneEngine;
import EmailClient.src.main.java.inmind.qa.models.PiazzaFeedObject;
import EmailClient.src.main.java.inmind.qa.models.PiazzaFeedResponse;
import EmailClient.src.main.java.inmind.qa.models.PiazzaAttachment;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;
/**
 * This class defines the operations for querying the index and returning the answers
 * @author suruchis (Suruchi Shah)
 * Date: 9/29/15
 */
public class AnswerPreprocess {
    private String PYTHON_SCRIPTS_PATH;
    private static String CONTENT_FILE = "content.txt";
    private static String QUERY_FILE = "query.txt";
    private String[] FIELDS = new String[] {"followups", "subject", "content"};
    private List<String> ALL_POST_IDS;
    private static String DISCLAIMER;

    public AnswerPreprocess(String scriptPath, List<String> postIds)   {
        PYTHON_SCRIPTS_PATH = scriptPath;
        ALL_POST_IDS = postIds;
        loadDisclaimerText(PYTHON_SCRIPTS_PATH);
    }

    public void loadDisclaimerText(String PYTHON_SCRIPTS_PATH)    {

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(PYTHON_SCRIPTS_PATH + "disclaimer.txt"));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            br.close();
            DISCLAIMER = sb.toString();
        } catch (Exception e)   {
            e.printStackTrace();
        }
    }

    /**
     * Primary method which gets the matching documents for the query and extracts the response.
     * @param rawQueryString raw query string
     * @return String with response to be posted
     */
    public StringBuilder getAnswer(String rawQueryString) {
        StringBuilder answers = new StringBuilder();
        try {
            LuceneEngine luceneEngine = new LuceneEngine();
            Query query = processRawQuery(rawQueryString);
            List<Object> documents = luceneEngine.searchIndexForMatchingDocuments(query, 10);
            List<String> responses = luceneEngine.searchIndex(query, rawQueryString, FIELDS, 10);

            for(String responseContent: responses)  {
                String[] responseParts = responseContent.split("~~");
                int docId = Integer.parseInt(responseParts[0]);
                String field = responseParts[1];
                // If the content originates from followups, we combine all the content from
                // followups and use that as the content string
                if(field.equals("followups"))   {
                    responseContent = "";
                    List<PiazzaFeedResponse> followups = ((PiazzaFeedObject)getDocument(docId, documents)).followups;
                    for(PiazzaFeedResponse followup: followups) {
                        responseContent = responseContent + followup.content + "\n";
                        for(PiazzaFeedResponse subResponse: followup.subResponses)  {
                            responseContent = responseContent + subResponse.content + "\n";
                        }
                    }
                }
                else {
                    responseContent = responseParts[2];
                }

                // Extract answering sentence from result

                String answeringSentence = extractAnswerSentence(responseContent, rawQueryString);
                if(answeringSentence != null && !answeringSentence.equals("") && !answeringSentence.equals("[]")) {

                    // Check if the response is from another question previously posted by Student.
                    Object originalPost = getDocument(docId, documents);
                    if(originalPost.getClass().equals(PiazzaFeedObject.class)) {
                        PiazzaFeedObject piazzaFeedObjectOriginalPost = (PiazzaFeedObject) originalPost;
                        if(piazzaFeedObjectOriginalPost.type.equals("question")) {
                            // Check if the post is answered, field is the content AND the post has NOT been deleted already
                            if(!piazzaFeedObjectOriginalPost.tags.contains("unanswered")
                                    && field.equals("content")
                                    && ALL_POST_IDS.contains(piazzaFeedObjectOriginalPost.postId)) {

                                answers.append("A similar question has been answered. See https://www.piazza.com/class/" + piazzaFeedObjectOriginalPost.courseId + "?cid=" + piazzaFeedObjectOriginalPost.nr + " for followup discussions\n\n");
                            }
                        }
                        else {
                            answers.append(answeringSentence.replace("[", "").replace("]", "") + "\n");
                            answers.append("Source: " + "https://www.piazza.com/class/" + piazzaFeedObjectOriginalPost.courseId + "?cid=" + piazzaFeedObjectOriginalPost.nr + "\n\n");
                        }
                    } else {
                        answers.append(answeringSentence.replace("[", "").replace("]", "") + "\n");
                        answers.append("Source: " + originalPost.getClass().getName() + "\n\n");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Adding disclaimer to the answer
        answers.append(DISCLAIMER);
        return answers;
    }

    /**
     * Processes a raw query string and converts to Query object
     * @param rawQuery raw string
     * @return Query object
     * @throws ParseException
     */
    public Query processRawQuery(String rawQuery) throws ParseException {
        MultiFieldQueryParser queryParser = new MultiFieldQueryParser(FIELDS, Index.INPUTANALYZER);
        queryParser.setDefaultOperator(QueryParser.OR_OPERATOR);
        Query query = queryParser.parse(QueryParser.escape(rawQuery));
        return query;
    }

    /**
     * Calls the python script to extract the sentence containing answer from the content
     * @param content content dump
     * @param query original query
     * @return Resulting sentence
     */
    private String extractAnswerSentence(String content, String query)    {
        StringBuilder responseSentences = new StringBuilder();
        String s;
        try {
            createTempFiles(content, query);
            String contentFile = PYTHON_SCRIPTS_PATH + CONTENT_FILE;
            String queryFile = PYTHON_SCRIPTS_PATH + QUERY_FILE;

            Process p = Runtime.getRuntime().exec(new String[]{"python", PYTHON_SCRIPTS_PATH + "extractMatchingSentences.py", queryFile, contentFile});

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

            // read any errors from the attempted command
            boolean errorExists = false;
            while ((s = stdError.readLine()) != null) {
                errorExists = true;
            }
            if(errorExists) {
                System.out.println("Error in the system: " + s);
                throw new Exception("Error in extracting sentences. Source: python script");
            }

            // read the output from the command
            while ((s = stdInput.readLine()) != null) {
                if(!s.equals(""))
                    responseSentences.append(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseSentences.toString();
    }

    /**
     * Gets the corresponding PiazzaFeedObject for a matched document to get more information about it
     * @param docId document Id that is indexed
     * @param documents all matching documents
     * @return PiazzaFeedObject
     */
    private Object getDocument(int docId, List<Object> documents) {
        for (Object obj: documents) {
            if(obj.getClass().equals(PiazzaFeedObject.class))   {
                PiazzaFeedObject piazzaFeedObject = (PiazzaFeedObject) obj;
                if(piazzaFeedObject.indexedDocId == docId)
                    return piazzaFeedObject;
            }
            else if (obj.getClass().equals(PiazzaAttachment.class))   {
                PiazzaAttachment piazzaAttachment = (PiazzaAttachment) obj;
                if(piazzaAttachment.indexedDocId == docId)
                    return piazzaAttachment;
            }
        }

        return null;
    }

    /**
     * Creates temporary txt files for content and query to be passed to the python script as arguments
     * @param content Content Dump
     * @param query Query Text
     */
    private void createTempFiles(String content, String query)  {
        try {
            String contentFile = PYTHON_SCRIPTS_PATH + CONTENT_FILE;
            String queryFile = PYTHON_SCRIPTS_PATH + QUERY_FILE;
            PrintWriter writer = new PrintWriter(contentFile, "UTF-8");
            writer.println(content);
            writer.close();
            writer = new PrintWriter(queryFile, "UTF-8");
            writer.println(query);
            writer.close();
        } catch (Exception e)   {
            e.printStackTrace();
        }
    }
}
