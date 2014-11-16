package masdar.jh;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;


public class HttpRequestHelper {

    String authToken = null;
    String basePath;

    public Response doRequest(String method, String targetEndpoint, String data) {
        HttpURLConnection connection = null;
        try {
            URL url;

            if (data == null) {
                data = "";
            }
            url = new URL(basePath+targetEndpoint);
            connection = (HttpURLConnection)url.openConnection();

            connection.setRequestMethod(method);

            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Content-Language", "en-US");

            if (!"GET".equalsIgnoreCase(method)) {
                connection.setRequestProperty("Content-Length", Integer.toString(data.getBytes().length));
            }

            connection.setRequestProperty("authorization", "Bearer "+authToken);

            connection.setUseCaches(false);
            if (!"GET".equalsIgnoreCase(method) && !"DELETE".equalsIgnoreCase(method)) {
                connection.setDoInput(true);
            }

            //Send request
            if (!"GET".equalsIgnoreCase(method) && !"DELETE".equalsIgnoreCase(method)) {
                connection.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                wr.writeBytes(data);
                wr.flush();
                wr.close();
            }
            //Get Response
            StringBuilder response = new StringBuilder();
            if (connection.getResponseCode() < 300) {
                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String line;
                while((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }
                rd.close();
            }
            JSONObject retJson =  new JSONObject();
            try {
                retJson = new JSONObject(response.toString());
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
            return new Response(connection.getResponseCode(), retJson);

        } catch (Exception e) {
            e.printStackTrace();
            return new Response(0, null);

        } finally {
            if(connection != null) {
                connection.disconnect();
            }
        }
    }

    public HttpRequestHelper(String basePath, String authToken) {
        this.authToken = authToken;
        this.basePath = basePath;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }
}
