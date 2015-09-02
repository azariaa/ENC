package inmind.testing;

import inmind.email.EmailOperations;
import inmind.piazza.PiazzaApi;
import inmind.qa.QuestionAnsweringAgent;

import java.io.FileReader;
import java.util.Properties;

/**
 * Created by ziy on 8/5/15.
 */
public class Main {

  public static void main(String[] args) throws Exception {
    Properties prop = new Properties();
    prop.load(new FileReader("default.properties"));
    String username = prop.getProperty("username");
    String password = prop.getProperty("password");

    QuestionAnsweringAgent qa = new QuestionAnsweringAgent() {
      @Override
      public void addDocument(String document) {
        return;
      }

      @Override
      public String getAnswer(String question) {
        return "Test";
      }
    };

    EmailOperations emailOperations = new EmailOperations(username, password, username);
    //emailOperations.printLastEmails(2);
    // emailOperations.sendEmail("Testing Subject", "This is me,\n\n have a nice day!", "amos.azaria@gmail.com");

    String question = "";
    PiazzaApi piazzaApi = new PiazzaApi(username, password);
    //piazzaApi.answerQuestion("idc2auqeia5wc", qa.getAnswer(question));
     //piazzaApi.followup("icqi85vqvc375u","New Automatic followup");
     piazzaApi.askQuestion("ie348yjty84c7", "New Automatic question asked", "auto generated", "other");
  }

}
