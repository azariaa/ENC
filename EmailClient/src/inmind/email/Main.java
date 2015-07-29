package inmind.email;

/**
 * Created by Amos Azaria on 15-Jul-15.
 */
public class Main
{
    static final String username = "inmindenc@gmail.com";
    static final String password = "CNEdnimni";

    public static void main(String[] args) throws Exception
    {
        EmailOperations emailOperations = new EmailOperations(username,password,username);
        emailOperations.printLastEmails(2);
        //emailOperations.sendEmail("Testing Subject", "This is me,\n\n have a nice day!", "amos.azaria@gmail.com");
    }
}
