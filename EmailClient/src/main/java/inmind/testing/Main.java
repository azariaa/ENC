package EmailClient.src.main.java.inmind.testing;


import java.io.FileReader;
import java.util.Date;
import java.util.Properties;

import EmailClient.src.main.java.inmind.email.EmailOperations;
import EmailClient.src.main.java.inmind.piazza.PiazzaApi;
import EmailClient.src.main.java.inmind.qa.answeringComponent.AnswerPreprocess;
import EmailClient.src.main.java.inmind.qa.indexingComponent.Index;
import EmailClient.src.main.java.inmind.qa.indexingComponent.IndexingOperations;

/**
 * @author suruchis (Suruchi Shah)
 */
public class Main {

    public static void main(String[] args) throws Exception {
      Properties prop = new Properties();
      prop.load(new FileReader("default.properties"));
      String username = prop.getProperty("username");
      String password = prop.getProperty("password");
      String pythonScriptPaths = prop.getProperty("pythonScriptsPath");
      String course_id = prop.getProperty("course_id");

      IndexingOperations indexingOperations = new IndexingOperations(pythonScriptPaths, username, password);
      PiazzaApi piazzaApi = new PiazzaApi(username, password);
      Index.initialize(prop.getProperty("index_location"));
      boolean answerQuestions = Boolean.parseBoolean(prop.getProperty("answer_questions"));
      indexingOperations.addPiazzaPosts(piazzaApi.getFeed(course_id), piazzaApi, course_id, answerQuestions);
  }

}
