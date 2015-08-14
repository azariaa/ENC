package inmind.piazza;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Amos Azaria on 30-Jul-15.
 */
public class PiazzaApi
{
    private String userEmail = "inmindenc@gmail.com";
    private String password = "CNEdnimni";
    static final String browser = "Mozilla/5.0";

    private String cookie = null;

    public PiazzaApi(String userEmail, String password)
    {
        this.userEmail = userEmail;
        this.password = password;
        loginGetCookie();
    }

    private enum PiazzaAction
    {
        answer, followup, question
    }

    public void answerQuestion(String cid, String answer)
    {
        performAction(PiazzaAction.answer, cid, "", "", answer, "");
    }

    public void followup(String cid, String question)
    {
        performAction(PiazzaAction.followup, cid, "", question, "", "");
    }

    public void askQuestion(String nid, String subject, String question, String folder)
    {
        performAction(PiazzaAction.question, "", nid, subject, question, folder);
    }

    private void performAction(PiazzaAction action, String cid, String nid, String subject, String content, String folder)
    {
        String urlEnd = null;
        String type = null;
        if (action == PiazzaAction.answer)
        {
            if (cid.isEmpty() || content.isEmpty())
            {
                System.out.println("Error, don't have enough information");
                return;
            }
            urlEnd = "content.answer";
            type = "s_answer"; //using student answer, for instructor answer use "i_answer"

        }
        else if (action == PiazzaAction.followup)
        {
            if (cid.isEmpty() || subject.isEmpty())
            {
                System.out.println("Error, don't have enough information");
                return;
            }
            urlEnd = "content.create";
            type = "followup";
        }
        else if (action == PiazzaAction.question)
        {
            if (nid.isEmpty() || content.isEmpty() || subject.isEmpty())
            {
                System.out.println("Error, don't have enough information");
                return;
            }
            urlEnd = "content.create";
            type = "question";
        }
        if (cookie == null) //shouldn't really happen because is called in constructor
        {
            loginGetCookie();
        }
        try
        {
            String postUrl = "https://piazza.com/logic/api?" + urlEnd;//"https://piazza.com/logic/api?content.create";

            URL obj = new URL(postUrl);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            //using post
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", browser);
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            con.setRequestProperty("Cookie", cookie);

            //TODO: if any additional functionality is required, all this should be done properly using JSon.
            String parameters = "{\"method\":\"" + urlEnd + "\",\"params\":{\"type\":\""+type+"\",\"anonymous\":\"no\"," +
                    "\"subject\":\"" + subject + "\", \"content\":\"" + content + "\"," +
                    "\"cid\":\"" + cid + "\", \"nid\":\"" + nid + "\"," +
                    "\"folders\""+":[\""+ folder + "\"]," + "\"revision\":0}}";

            // Send post request
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(parameters);
            wr.flush();
            wr.close();
            int responseCode = con.getResponseCode();
            if (responseCode == 200)
            {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null)
                {
                    System.out.println(inputLine + "\n");
                }
            }
            else
            {
                System.out.println("S: error. (response code is: " + responseCode + ")");
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    /**
     * logs in to the system and get a new cookie.
     * login and cookie handling may be easier using Apache client
     */
    private void loginGetCookie()
    {
        try
        {

            String url = "https://piazza.com/logic/api?method=user.login";

            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            //using post
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", browser);
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

            String parameters = "{\"method\":\"user.login\",\"params\":{\"email\":\"" + userEmail + "\",\"pass\":\"" + password + "\"}}";

            // Send post request
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(parameters);
            wr.flush();
            wr.close();
            int responseCode = con.getResponseCode();
            cookie = con.getHeaderField("Set-Cookie");
            if (responseCode == 200)
            {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null)
                {
                    System.out.println(inputLine + "\n");
                }
            }
            else
            {
                System.out.println("S: error. (response code is: " + responseCode + ")");
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

}
