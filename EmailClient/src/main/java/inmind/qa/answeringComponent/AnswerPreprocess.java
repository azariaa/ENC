package main.java.inmind.qa.answeringComponent;


import main.java.inmind.qa.indexingComponent.Index;
import main.java.inmind.qa.indexingComponent.LuceneEngine;
import main.java.inmind.qa.models.PiazzaAttachment;
import main.java.inmind.qa.models.PiazzaFeedObject;
import main.java.inmind.qa.models.PiazzaFeedResponse;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
/**
 * This class defines the operations for querying the index and returning the answers
 * @author suruchis (Suruchi Shah)
 * Date: 9/29/15
 */
public class AnswerPreprocess {
    private String PYTHON_SCRIPTS_PATH;
    private static String TEMP_CONTENT_FOLDER = "tempContent/";
    private static String GAZETEER = "gazeteer/";
    private static String CONTENT_FILE = "content.txt";
    private static String QUERY_FILE = "query.txt";
    private static String QUESTION_SEPARATOR = "~~~";
    private String[] FIELDS = new String[] {"followups", "subject", "content"};
    private List<String> ALL_POST_IDS;
    private static String DISCLAIMER;

    public AnswerPreprocess(String scriptPath, List<String> postIds)   {
        PYTHON_SCRIPTS_PATH = scriptPath;
        ALL_POST_IDS = postIds;
        loadDisclaimerText(PYTHON_SCRIPTS_PATH);
    }

    /**
     * Loads the disclaimer text in memory
     * @param PYTHON_SCRIPTS_PATH path to python scripts
     */
    public void loadDisclaimerText(String PYTHON_SCRIPTS_PATH)    {

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(PYTHON_SCRIPTS_PATH + GAZETEER + "disclaimer.txt"));
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
        boolean answerFound = false;
        boolean similarQuestionFound = false;
        try {
            LuceneEngine luceneEngine = new LuceneEngine();

            // Let's process the question post first and extract the questions
            // Next, we go through each question and try to extract an answer
            List<String> allQuestionsToBeAnswered = extractQuestionSentences(rawQueryString);

            for(String returnedSentence: allQuestionsToBeAnswered)  {
                // TODO Look at multi sentence questions
                String questionSentence = returnedSentence.split(QUESTION_SEPARATOR)[0];
                answers.append("\nQuestion: " + questionSentence + "\n");
                String rawQuery = questionSentence.replace("?"," ").replace("<p>","").replace("</p>","");
                // TODO Keyword Extraction from questions
                String keyWords = returnedSentence.split(QUESTION_SEPARATOR)[1];
//                getKeywords(rawQuery, Utility.CONTENT);

                Query query = processRawQuery(keyWords);
                List<Object> documents = luceneEngine.searchIndexForMatchingDocuments(query, 10);
                List<String> responses = luceneEngine.searchIndex(query, rawQuery, FIELDS, 10);

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

                    String answeringSentence = extractAnswerSentence(responseContent, questionSentence);
                    if(answeringSentence != null && !answeringSentence.equals("") && !answeringSentence.equals("[]")) {

                        // Check if the response is from another question previously posted by Student.
                        Object originalPost = getDocument(docId, documents);
                        if(originalPost!=null && originalPost.getClass().equals(PiazzaFeedObject.class)) {
                            PiazzaFeedObject piazzaFeedObjectOriginalPost = (PiazzaFeedObject) originalPost;
                            if(piazzaFeedObjectOriginalPost.type.equals("question")) {
                                // Check if the post is answered, field is the content AND the post has NOT been deleted already
                                if(!piazzaFeedObjectOriginalPost.tags.contains("unanswered")
                                        && field.equals("content")
                                        && (ALL_POST_IDS.contains(piazzaFeedObjectOriginalPost.postId) || ALL_POST_IDS == null)
                                        && !similarQuestionFound) {
                                    answerFound = true;
                                    similarQuestionFound = true;
                                    answers.append("A similar question has been answered. See <a href=\"https://www.piazza.com/class/" + piazzaFeedObjectOriginalPost.courseId + "?cid=" + piazzaFeedObjectOriginalPost.nr + "\">Click here to see the post</a> for followup discussions\n");
                                }
                            }
                            else {
                                answerFound = true;
                                answers.append("\"..." + answeringSentence.replace("[", "").replace("]", "") + "..\" ");
                                answers.append("<a href=\"https://www.piazza.com/class/" + piazzaFeedObjectOriginalPost.courseId + "?cid=" + piazzaFeedObjectOriginalPost.nr + "\">[Source]</a>\n");
                            }
                        } else {
                            if(originalPost!=null) {
                                answerFound = true;
                                answers.append(answeringSentence.replace("[", "").replace("]", ""));
                                answers.append("[Source: " + originalPost.getClass().getName() + "]\n");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Adding disclaimer to the answer only if we do find an answer
        if (answerFound==true && answers.toString().trim().length() > 1)
            answers.append("\n" + DISCLAIMER);
        else
            answers = new StringBuilder();
        return answers;
    }

    /**
     * Used in Keyword Extraction to compute the IDF
     * @param questionSentence the word we are looking to find the document frequency of
     * @param field "content", "subject"
     */
    public void getKeywords(String questionSentence, String field) {
        // STEP 0: Remove stop words from the question sentence
        String[] words = questionSentence.split(" ");
        for(String word: words) {
            String termText = word;
            Term termInstance = new Term(field, word);
            try {
                long termFreq = Index.getINDEXREADER().totalTermFreq(termInstance);
                long docCount = Index.getINDEXREADER().docFreq(termInstance);
                long totalDocs = Index.getINDEXREADER().numDocs();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
            String contentFile = PYTHON_SCRIPTS_PATH + TEMP_CONTENT_FOLDER + CONTENT_FILE;
            String queryFile = PYTHON_SCRIPTS_PATH + TEMP_CONTENT_FOLDER + QUERY_FILE;

            Process p = Runtime.getRuntime().exec(new String[]{"python", PYTHON_SCRIPTS_PATH + "extractMatchingSentences.py", queryFile, contentFile});

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

            // read any errors from the attempted command
            boolean errorExists = false;
            String errorMessage = "";
            while ((s = stdError.readLine()) != null) {
                errorExists = true;
                errorMessage = errorMessage + s + "\n";
            }
            if(errorExists) {
                System.out.println("Error in the system: " + errorMessage);
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
    //TODO LOG the exceptions instead of throwing exceptions

    /**
     * Takes the entire raw question post and breaks it into questions
     * @param rawQueryContent
     * @return a list of distinct questions
     */
    private List<String> extractQuestionSentences(String rawQueryContent)   {
        List<String> questionSentences = new ArrayList<String>();
        String s;
        try {
            createTempFiles("", rawQueryContent);
            String queryFile = PYTHON_SCRIPTS_PATH + TEMP_CONTENT_FOLDER + QUERY_FILE;

            Process p = Runtime.getRuntime().exec(new String[]{"python", PYTHON_SCRIPTS_PATH + "extractQuerySentences.py", queryFile, PYTHON_SCRIPTS_PATH + GAZETEER + "stopwords.txt"});

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

            // read any errors from the attempted command
            boolean errorExists = false;
            String errorMessage = "";
            while ((s = stdError.readLine()) != null) {
                errorExists = true;
                errorMessage = errorMessage + s + "\n";
            }
            if(errorExists) {
                System.out.println("Error in the system: " + errorMessage);
                throw new Exception("Error in extracting sentences. Source: python script");
            }

            //read the output from the command
            while((s = stdInput.readLine()) != null)    {
                if(!s.trim().equals(""))
                    questionSentences.add(s);
            }
        } catch (Exception e)   {
            e.printStackTrace();
        }
        return questionSentences;
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
            String contentFile = PYTHON_SCRIPTS_PATH + TEMP_CONTENT_FOLDER + CONTENT_FILE;
            String queryFile = PYTHON_SCRIPTS_PATH + TEMP_CONTENT_FOLDER + QUERY_FILE;
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
