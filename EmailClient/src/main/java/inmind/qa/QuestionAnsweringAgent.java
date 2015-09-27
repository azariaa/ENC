package EmailClient.src.main.java.inmind.qa;

import javax.jdo.annotations.Index;

/**
 * Created by ziy on 8/5/15.
 */
public interface QuestionAnsweringAgent {
  void addDocument(String content, String source);
  String getAnswer(String question, String source);

}
