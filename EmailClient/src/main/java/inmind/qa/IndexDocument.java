package EmailClient.src.main.java.inmind.qa;


import java.util.Date;

/**
 * This class defines the fields for indexing.
 * Adding attributes here will automatically be reflected in addDocument method
 * @author suruchis (Suruchi Shah)
 * @date 9/26/15
 */
public class IndexDocument {
    public String source;
    public String content;
    public Date date;
    public String subject;

    public Object getFieldValue(String fieldName)   {
        switch(fieldName)   {
            case "source":
                System.out.print("here");
                return source;
            case "content":
                System.out.print("here1");
                return content;
            case "date":
                System.out.print("here2");
                return date;
            case "subject":
                System.out.print("here3");
                return subject;
        }
        return null;
    }
}
