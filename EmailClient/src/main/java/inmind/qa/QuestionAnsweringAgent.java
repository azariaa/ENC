package EmailClient.src.main.java.inmind.qa;

/**
 * Created by ziy on 8/5/15.
 */
public interface QuestionAnsweringAgent {
  void addDocument(String document, string source);
  String getAnswer(String question, String source);

}
