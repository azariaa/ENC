package main.java.inmind.qa.piazza;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Amos Azaria on 15-Jul-15.
 * @modifiedBy suruchis (Suruchi Shah) on 13-Sept-15
 */
public class PiazzaApi
{
    private String userEmail;
    private String password;
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

    /**
     * Works only if question wasn't already answered (Agent shouldn't edit already answered questions)
     * @param cid
     * @param answer
     */
    public void answerQuestion(String cid, String nid, String answer)
    {
        performAction(PiazzaAction.answer, cid, nid, "", answer, "");
    }

    public void followup(String cid, String nid, String question)
    {
        performAction(PiazzaAction.followup, cid, nid, question, "", "");
    }

    public void askQuestion(String nid, String subject, String question, String folder)
    {
        performAction(PiazzaAction.question, "", nid, subject, question, folder);
    }

    public JSONArray getFeed(String nid) {
        String urlEnd = "network.get_my_feed";
        if (cookie == null) //shouldn't really happen because is called in constructor
        {
            loginGetCookie();
        }
        try
        {
            String postUrl = "https://piazza.com/logic/api?" + urlEnd;

            URL obj = new URL(postUrl);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            //using post
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", browser);
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            con.setRequestProperty("Cookie", cookie);

            String parameters = "{\"method\":\"" + urlEnd + "\",\"params\":{\"nid\":\"" + nid + "\"}}";

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
                StringBuilder allFeed = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null)
                {
                    allFeed.append(inputLine);
                    //System.out.println(inputLine + "\n");
                }
                JSONObject jsonObject = new JSONObject(allFeed.toString());
                JSONArray jsonArray = jsonObject.getJSONObject("result").getJSONArray("feed");
                return jsonArray;
            }
            else
            {
                System.out.println("S: error. (response code is: " + responseCode + ")");
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return null;
    }

    public JSONArray getFeedFollowup(String nid, String cid)    {
        String urlEnd = "content.get";
        if (cookie == null) //shouldn't really happen because is called in constructor
        {
            loginGetCookie();
        }
        try
        {
            String postUrl = "https://piazza.com/logic/api?" + urlEnd;

            URL obj = new URL(postUrl);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            //using post
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", browser);
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            con.setRequestProperty("Cookie", cookie);

            String parameters = "{\"method\":\"" + urlEnd + "\",\"params\":{\"nid\":\"" + nid + "\", \"cid\":\"" + cid + "\"}}";

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
                StringBuilder allFeed = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null)
                {
                    allFeed.append(inputLine);
                    //System.out.println(inputLine + "\n");
                }
                JSONObject jsonObject = new JSONObject(allFeed.toString());
                JSONArray jsonArray = jsonObject.getJSONObject("result").getJSONArray("children");
                return jsonArray;
            }
            else
            {
                System.out.println("S: error. (response code is: " + responseCode + ")");
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return null;
    }

    public String getFeedFullContent(String nid, String cid)    {

        String fullContent = "";
        String urlEnd = "content.get";
        if (cookie == null) //shouldn't really happen because is called in constructor
        {
            loginGetCookie();
        }
        try
        {
            String postUrl = "https://piazza.com/logic/api?" + urlEnd;
            URL obj = new URL(postUrl);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            //using post
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", browser);
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            con.setRequestProperty("Cookie", cookie);

            String parameters = "{\"method\":\"" + urlEnd + "\",\"params\":{\"nid\":\"" + nid + "\", \"cid\":\"" + cid + "\"}}";

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
                StringBuilder allFeed = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null)
                {
                    allFeed.append(inputLine);
                    //System.out.println(inputLine + "\n");
                }
                JSONObject jsonObject = new JSONObject(allFeed.toString()).getJSONObject("result");
                if (jsonObject.has("history")) {
                    JSONArray jsonArrayForHistory = jsonObject.getJSONArray("history");
                    JSONObject historyObject = jsonArrayForHistory.getJSONObject(jsonArrayForHistory.length()-1);
                    fullContent = historyObject.get("content").toString();
                    return fullContent;
                }
                return fullContent;
            }
            else
            {
                System.out.println("S: error. (response code is: " + responseCode + ")");
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return null;
    }

    public JSONObject getFullPost(String nid, String cid)    {
        String fullContent = "";
        String urlEnd = "content.get";
        if (cookie == null) //shouldn't really happen because is called in constructor
        {
            loginGetCookie();
        }
        try
        {
            String postUrl = "https://piazza.com/logic/api?" + urlEnd;
            URL obj = new URL(postUrl);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            //using post
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", browser);
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            con.setRequestProperty("Cookie", cookie);

            String parameters = "{\"method\":\"" + urlEnd + "\",\"params\":{\"nid\":\"" + nid + "\", \"cid\":\"" + cid + "\"}}";

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
                StringBuilder allFeed = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null)
                {
                    allFeed.append(inputLine);
                    //System.out.println(inputLine + "\n");
                }
                JSONObject jsonObject = new JSONObject(allFeed.toString()).getJSONObject("result");
                return jsonObject;
            }
            else
            {
                System.out.println("S: error. (response code is: " + responseCode + ")");
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return null;
    }

    private void performAction(PiazzaAction action, String cid, String nid, String subject, String content, String folder) {
        String urlEnd = null;
        String type = null;
        if (action == PiazzaAction.answer)
        {
            if (cid.isEmpty() || content.isEmpty() || nid.isEmpty())
            {
                System.out.println("Error, don't have enough information");
                return;
            }
            content = content.replace("\"","\\\"");
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


    public File getSyllabus(String nid)
    {
        try
        {
            //ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            File tempFile = File.createTempFile("syllabus", ".pdf");
            FileUtils.copyURLToFile(new URL("https://piazza.com/class_profile/syllabus/" + nid), tempFile);
            // Delete temp file when program exits.
            tempFile.deleteOnExit();
            return tempFile;

        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return null;
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
