package main.java.inmind.qa.models;


import java.util.Date;

/**
 * This class defines the fields for indexing.
 * Adding attributes here will automatically be reflected in addDocument method
 * @author suruchis (Suruchi Shah)
 * @date 9/26/15
 */
public class IndexDocument {
    private String source;
    private String content;
    private Date date;
    private String subject;

    public Object getFieldValue(String fieldName)   {
        switch(fieldName)   {
            case "source":
                return getSource();
            case "content":
                return getContent();
            case "date":
                return getDate();
            case "subject":
                return getSubject();
        }
        return null;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}
