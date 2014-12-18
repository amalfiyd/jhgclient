package masdar.jh;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;
 
public class staticBot
{
	private static final String TOKEN = "114963672895653325144-1417536702864";
	private static final String SERVER_HOST = "http://www.juniorhighgame.com";
	private static ArrayList<HashMap<String, Integer>> history = new ArrayList();
	private static ArrayList<HashMap<String, Double>> popularityHistory = new ArrayList();
	private static ArrayList<HashMap<String, Integer>> giveHistory = new ArrayList();
	private static JhBotApi api = new JhBotApi("http://www.juniorhighgame.com", "114963672895653325144-1417536702864");
	private static int staticToken = 0;
 
	public static void main(String[] args) 
	{
	    System.out.println("Program started.");
	
	    JSONArray games = api.getGamesList();
	
	    System.out.println("Listing available games\n---------------");
	    for(int i=0; i<games.length(); i++) {
	        JSONObject game = games.getJSONObject(i);
	        System.out.println(game.optString("name") + " id: " + game.optString("id"));
	    }
	    System.out.println("---------------");
	    if(games.length() == 0 ) {
	        System.out.println("No games available. Exiting");
	        System.exit(0);
	    }
	
	    // GAME JOINING PART IS HERE
	    long gameId = 2776; //games.optJSONObject(0).optLong("id");
	
	    System.out.println("Joining the first game in the list ("+gameId+"). Was joining successful:"
	            + api.joinGame(gameId));
	    // END GAME JOINING APRT
	    JSONObject prevGameState = api.getGameState(gameId);
	    
	    boolean test = true;
	    
	    while(test) {
	        JSONObject newGameState = api.getGameState(gameId);
	        
	        if (newGameState == null) {
	            System.out.println("State is null and that is not ok. Please check the code");
	        }
	        else {
	            if(prevGameState != null) {
	                if (newGameState.getInt("state") == JhBotApi.STATE_FINISHED) {
	                    System.out.println("Game has finished");
	                    break;
	                }
	                if (newGameState.getInt("state") == JhBotApi.STATE_IN_PROGRESS) {
	                    if (prevGameState.getInt("state") == JhBotApi.STATE_NOT_STARTED) {
	                        System.out.println("Yahoo! The game has started!");
	                    }
	                    if(prevGameState.getInt("round") < newGameState.getInt("round")) {
	                        // New round has started
	                        HashMap<String, Integer> transactions = new HashMap<String, Integer>();
	                        try {
	                            // Lets try to run your function
	                        	
	                        	 
	                        	transactions = makeMove(newGameState);
	                        }
	                        catch (Exception ex) {
	                            // If execution reached this part then you did something wrong in makeMove method!
	                            // Try harder, debug and try again!
	                            // DO NOT instantly mail me or the TA. Think and google before that.
	                            // Anyway, if after an hour of debugging you still have no idea why your code does not work
	                            // feel free to send me an email with a PayPal payment of 1$ (attaching this whole file)
	                            // to hrafael@masdar.ac.ae
	                            System.err.println("There was an error while invoking makeMove function.\nPlease check it.\n This will be accounted as you have kept all the tokens to yourself\nThe stack trace of the error is presented below");
	                            ex.printStackTrace();
	                        }
	                        finally {
	                            boolean isMoveOk = api.makeMove(gameId, transactions);
	                            if(isMoveOk)
	                                System.out.println("Last move was successful");
	                            else
	                                System.out.println("Last move failed! Check previous logs for more info on the error");
	                        }
	                    }
	                }
	            }
	            prevGameState = newGameState;
	            try {
	                Thread.sleep(3000);
	            } catch (InterruptedException e) {
	                e.printStackTrace();
	            }
	        }
	    }
	    System.out.println("Program finished. So long and thank you for all the fish!");
	}

	private static HashMap<String, Integer> recordHistory(JSONObject state)
	{
		HashMap<String, Integer> receivedTokens = new HashMap();
		HashMap<String, Double> popularities = new HashMap();
		try
		{
			JSONArray users = state.optJSONArray("users");
			for (int i = 0; i < users.length(); i++)
			{
				JSONObject user = users.optJSONObject(i);
				if (!user.optBoolean("isCurrentPlayer")) {
					receivedTokens.put(user.optString("id"), Integer.valueOf(user.optInt("received")));
				}
				popularities.put(user.getString("id"), Double.valueOf(user.optDouble("popularity")));
			}
			history.add(receivedTokens);
			popularityHistory.add(popularities);
		}
		catch (Exception ex)
		{
			System.out.println("Sadly, there was an exception while recording hte history. Here is the stack trace");
			ex.printStackTrace();
		}
		return receivedTokens;
	}

	private static HashMap<String, Integer> makeMove(JSONObject state) throws Exception
	{
			HashMap<String, Integer> transactionsToDo = new HashMap();
			JSONArray users = state.optJSONArray("users");
			int availableTokens = state.optInt("availableToks");
			HashMap<String, Integer> receivedTokens = recordHistory(state);
	
			  transactionsToDo = staticMaMove(users, availableTokens, state);

			  giveHistory.add(transactionsToDo);
			  return transactionsToDo;
	}
		
		private static HashMap<String, Integer> staticMaMove(JSONArray users, int availableTokens, JSONObject state) throws Exception
		{
			HashMap<String, Integer> transactionsToDo = new HashMap<String, Integer>();
			Random rand = new Random();
			int max = 2 * users.length();
			int min = -max;
			int randomNum = rand.nextInt(max - min + 1) + min;
			if (state.getInt("round") == 1) {
				staticToken = randomNum;
			}
			for (int i = 0; i < users.length(); i++)
			{
				JSONObject user = users.getJSONObject(i);
				if (!user.optBoolean("isCurrentPlayer")) {
					if (staticToken <= availableTokens)
					{
						transactionsToDo.put(user.optString("id"), Integer.valueOf(staticToken));
						availableTokens -= Math.abs(staticToken);
					}
				}
			}
			
			return transactionsToDo;
		}
}


/* Location:           E:\Projects\NetBeansProjects\jhgclient\bin\
 * Qualified Name:     masdar.jh.staticBot
 * JD-Core Version:    0.7.0.1
 */