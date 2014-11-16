package masdar.jh;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class JhBotApi {
    public static final int STATE_NOT_STARTED   = 0;
    public static final int STATE_IN_PROGRESS   = 1;
    public static final int STATE_FINISHED      = 2;
    public static final int STATE_DELETED       = 3;

    private String authToken;
    private String jhApiUrl;
    private int round = 0;
    private int state;
    private JSONObject latestGameState = null;
    private HttpRequestHelper requestHelper;



    public JhBotApi(String jhApiUrl, String authToken) {
        this.authToken = authToken;
        this.jhApiUrl = jhApiUrl;
        requestHelper = new HttpRequestHelper(this.jhApiUrl, this.authToken);
    }

    public JSONArray getGamesList () {
        Response resp = requestHelper.doRequest("GET", "/json/games", null);
        if(resp.getStatusCode() != 200 || resp.getData().optString("res").equalsIgnoreCase("NOK")) {
            System.out.println("Auth token is incorrect. Acquire a new one");
            System.out.println(resp.toString());
            return null;
        }
        return resp.getData().optJSONArray("data");
    }

    public boolean joinGame (long gameId) {
        Response resp = requestHelper.doRequest("POST", "/json/games/"+gameId+"/join", null);
        if(resp.getStatusCode() != 200 || resp.getData().optString("res").equalsIgnoreCase("NOK")) {
            System.out.println("Auth token is incorrect. Acquire a new one");
            return false;
        }
        return true;
    }

    public JSONObject getGameState(long gameId) {
        Response resp = requestHelper.doRequest("GET", "/json/games/"+gameId+"/state/"+round, null);
        if(resp.getStatusCode() != 200 || resp.getData().optString("res").equalsIgnoreCase("NOK")) {
            System.out.println("Auth token is incorrect. Acquire a new one");
            return null;
        }
        JSONObject gameState = resp.getData().optJSONObject("data");
        round = gameState.optInt("round");
        state = gameState.optInt("state");
        latestGameState = gameState;
        return gameState;
    }

    public boolean makeMove(long gameId, HashMap<String, Integer> transactions) {
        JSONArray jsonTrans = new JSONArray();
        if (transactions != null) {
            for (String userId : transactions.keySet()){
                JSONObject trans = new JSONObject();
                trans.put("dst", userId);
                trans.put("amount", transactions.get(userId));
                jsonTrans.put(trans);
            }
        }
        Response resp = requestHelper.doRequest("POST", "/json/games/"+gameId+"/transactions/"+round, jsonTrans.toString());
        if(resp.getStatusCode() != 200 || resp.getData().optString("res").equalsIgnoreCase("NOK")) {
            System.out.println("Something is wrong. Server responded with "+resp.toString());
            return false;
        }
        return true;
    }


    public JSONObject getLatestGameState() {
        return latestGameState;
    }
}
