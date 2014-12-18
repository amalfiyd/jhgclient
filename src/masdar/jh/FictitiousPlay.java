package masdar.jh;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

public class FictitiousPlay
{
  private static final String TOKEN = "114963672895653325144-1417536702864";
  private static final String SERVER_HOST = "http://www.juniorhighgame.com";
/*  21 */   private static ArrayList<HashMap<String, Integer>> history = new ArrayList();
/*  22 */   private static ArrayList<HashMap<String, Double>> popularityHistory = new ArrayList();
/*  23 */   private static ArrayList<HashMap<String, Integer>> giveHistory = new ArrayList();
/*  25 */   private static JhBotApi api = new JhBotApi("http://www.juniorhighgame.com", "114963672895653325144-1417536702864");
/*  26 */   private static int staticToken = 0;
  
  public static void main(String[] args)
  {
	  System.out.println("Program started.");
    
	  JSONArray games = api.getGamesList();
    
	  System.out.println("Listing available games\n---------------");
	  for (int i = 0; i < games.length(); i++)
	  {
		  JSONObject game = games.getJSONObject(i);
		  System.out.println(game.optString("name") + " id: " + game.optString("id"));
	  }
	  System.out.println("---------------");
	  if (games.length() == 0)
	  {
		  System.out.println("No games available. Exiting");
		  System.exit(0);
	  }
	  long gameId = games.optJSONObject(0).optLong("id");
	  gameId = 911L;
    
	  System.out.println("Joining the first game in the list (" + gameId + "). Was joining successful:" + api.joinGame(gameId));
	  JSONObject prevGameState = api.getGameState(gameId);
    
	  boolean test = true;
	  while (test)
	  {
		  JSONObject newGameState = api.getGameState(gameId);
      
		  JSONArray users = newGameState.optJSONArray("users");
		  if (newGameState == null)
		  {
			  System.out.println("State is null and that is not ok. Please check the code");
		  }
		  else
		  {
			  if (prevGameState != null)
			  {
				  if (newGameState.getInt("state") == 2)
				  {
					  System.out.println("Game has finished");
					  break;
				  }
				  if (newGameState.getInt("state") == 1)
				  {
					  if (prevGameState.getInt("state") == 0) {
						  System.out.println("Yahoo! The game has started!");
					  }
					  if (prevGameState.getInt("round") < newGameState.getInt("round"))
					  {
						  HashMap<String, Integer> transactions = new HashMap();
						  boolean isMoveOk;
						  try
						  {
							  transactions = makeMove(newGameState);
						  }
						  catch (Exception ex)
						  {
							  System.err.println("There was an error while invoking makeMove function.\nPlease check it.\n This will be accounted as you have kept all the tokens to yourself\nThe stack trace of the error is presented below");
							  ex.printStackTrace();
						  }
						  finally
						  {
							  isMoveOk = api.makeMove(gameId, transactions);
							  if (isMoveOk) {
								  System.out.println("Last move was successful");
							  } else {
								  System.out.println("Last move failed! Check previous logs for more info on the error");
							  }
						  }
					  }
				  }
			  }
			  prevGameState = newGameState;
			  try
			  {
				  Thread.sleep(3000L);
			  }
			  catch (InterruptedException e)
			  {
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
	  
	  transactionsToDo = fictitiousPlayMove(users, availableTokens, receivedTokens);
	  
	  giveHistory.add(transactionsToDo);
	  return transactionsToDo;
  	}
  
  	public static HashMap<String, Integer> fictitiousPlayMove(JSONArray users, int availableTokens, HashMap<String, Integer> receivedTokens) throws Exception
  	{
  		HashMap<String, HashMap<Integer, Integer>> fictitiousData = new HashMap<String, HashMap<Integer, Integer>>();
  		HashMap<String, HashMap<Integer, Double>> posteriors = new HashMap<String, HashMap<Integer, Double>>();
  	  
  		HashMap<String, Integer> toGiveValues = new HashMap<String, Integer>();
  	  
	  	  // Get my user
	  	  JSONObject myuser = null;
	  	  for(int i = 0; i < users.length(); i++)
	  	  {
	  		  JSONObject user = users.getJSONObject(i);
	  		  if(myuser == null && user.optBoolean("isCurrentPlayer"))
	  		  {
	  			  myuser = user;
	  		  }
	  		  
	  		  // instanced the fictitiousData
	  		  HashMap<Integer, Integer> temp = new HashMap<Integer, Integer>();	// represents propability of each actions
	  		  HashMap<Integer, Double> temp2 = new HashMap<Integer, Double>();	// represents posteriors of each actions
	  		  for(int j = -(2*users.length()); j <= (2*users.length()); j++)
	  		  {
	  			  if(j > 0)	temp.put(j, 1);
	  			  else	temp.put(j, 0);
	  			  
	  			  temp2.put(j, 0.0);
	  		  }
	  		  fictitiousData.put(user.optString("id"), temp);
	  		  posteriors.put(user.optString("id"), temp2);
	  	  }
	  	  
	  	  // Get total received values until T
	  	  int finalNormalizer = 0;
	  	  for(int i = 0; i < users.length(); i++)
	  	  {
	  		  JSONObject user = users.getJSONObject(i);
	  		  int normalizer = 0;
	  		  
	  		  if(!user.optBoolean("isCurrentPlayer")) 
	  		  {
	  			  // Update the fictitiousData
	  			  for(int x = 0; x < history.size(); x++)
	  			  {
	  				  int action = history.get(x).get(user.optString("id"));
	  				  int nextData = fictitiousData.get(user.optString("id")).get(action) + 1;
	  				  fictitiousData.get(user.optString("id")).replace(action, nextData);
	  			  }
	  			  
	  			  // Count normalizer
	  			  for(int x = -(2*users.length()); x <= (2*users.length()); x++)
	  			  {
	  				  normalizer += fictitiousData.get(user.opt("id")).get(x);
	  			  }
	  			  
	  			  // Count posterior
	  			  double maxPost = 0.0;
	  			  int maxAction = -99;
	  			  for(int x = -(2*users.length()); x <= (2*users.length()); x++)
	  			  {
	  				  double postTemp = fictitiousData.get(user.optString("id")).get(x) / normalizer;
	  				  posteriors.get(user.optString("id")).replace(x, postTemp);
	  				  
	  				  if(postTemp > maxPost)
	  				  {
	  					  maxPost = postTemp;
	  					  maxAction = x;
	  				  }
	  			  }
	  			  
	  			  
	  			  double value = maxAction * popularityHistory.get(popularityHistory.size()-1).get(user.optString("id"));  
	  			  double toGive = value / (popularityHistory.get(popularityHistory.size()-1).get(myuser.optString("id")));
	  			  
	  			  int toGiveInt = (int)Math.floor(toGive);
	  			  toGiveValues.put(user.optString("id"), toGiveInt);
	  			  
	  			  finalNormalizer += toGiveInt;
	  		  }
	  	  }
	  	  
	  	  // Token giving
	  	  HashMap<String, Integer> transactionsTodo = new HashMap<String, Integer>();
	  	  for(int i = 0; i < users.length(); i++)
	  	  {
	  		  JSONObject user = users.getJSONObject(i);
	  		  if(!user.optBoolean("isCurrentPlayer"))
	  		  {
	  			  int toGive = toGiveValues.get(user.optString("id"));
	  			  int finalGive = (int)Math.floor((double)toGive / finalNormalizer * (2*users.length()));
	  			  
	  			  if(availableTokens >= finalGive)
	  			  {
	  				  transactionsTodo.put(user.optString("id"), finalGive);
	  			  }
	  			  availableTokens -= Math.abs(finalGive);
	  		  }
	  	  }
	  		
	  	  return transactionsTodo;
  	}
  
}




/* Location:           E:\Projects\NetBeansProjects\jhgclient\bin\
 * Qualified Name:     masdar.jh.staticBot
 * JD-Core Version:    0.7.0.1
 */