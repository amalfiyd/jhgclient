package masdar.jh;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONObject;

public class RationalLearnerBot
{
	private static final String TOKEN = "114963672895653325144-1417544730491";
	private static final String SERVER_HOST = "http://www.juniorhighgame.com";
	private static ArrayList<HashMap<String, Integer>> history = new ArrayList();
	private static ArrayList<HashMap<String, Double>> popularityHistory = new ArrayList();
	private static ArrayList<HashMap<String, Integer>> giveHistory = new ArrayList();
	private static JhBotApi api = new JhBotApi("http://www.juniorhighgame.com", "114963672895653325144-1417544730491");

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
		gameId = 947L;
		System.out.println("Joining the first game in the list (" + gameId + "). Was joining successful:" + 
		api.joinGame(gameId));

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
				popularities.put(user.optString("id"), Double.valueOf(user.optDouble("popularity")));
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
		JSONObject myUser = null;
		int currentRound = state.getInt("round");
	  	for (int i = 0; i < users.length(); i++)
	  	{
			  JSONObject user = users.getJSONObject(i);
			  if (user.optBoolean("isCurrentPlayer"))
			  {
				  myUser = user;
				  break;
			  }
		  }
	  		  
  		  transactionsToDo = rationalLearnerMove(users, availableTokens, currentRound, myUser);
	
  		  giveHistory.add(transactionsToDo);
  		  ((HashMap)giveHistory.get(giveHistory.size() - 1)).put(myUser.optString("id"), Integer.valueOf(availableTokens));
  		  return transactionsToDo;
	}

	public static double countLikelihood(int currentRound, String opponentId, String myId, int daClass)
	{
		double likelihood = 0.0D;
		double zeroRate = 0.0D;
		if (daClass == 0)
		{
			if (((Integer)((HashMap)giveHistory.get(giveHistory.size() - 1 - 1)).get(opponentId)).intValue() < ((Integer)((HashMap)history.get(history.size() - 1)).get(opponentId)).intValue()) {
				likelihood = 1.0D;
			} else {
				likelihood = zeroRate;
			}
		}
		else if (daClass == 1)
		{
			if (((Integer)((HashMap)giveHistory.get(giveHistory.size() - 1 - 1)).get(opponentId)).intValue() > ((Integer)((HashMap)history.get(history.size() - 1)).get(opponentId)).intValue()) {
				likelihood = 1.0D;
			} else {
				likelihood = zeroRate;
			}
		}
		else if (daClass == 2)
		{
			if (((HashMap)giveHistory.get(giveHistory.size() - 1 - 1)).get(opponentId) == ((HashMap)history.get(history.size() - 1)).get(opponentId)) {
				likelihood = 1.0D;
			} else {
				likelihood = zeroRate;
			}
		}
		else if (daClass == 3)
		{
			boolean isSame = true;
			int token = ((Integer)((HashMap)history.get(0)).get(opponentId)).intValue();
			for (int x = 1; x < history.size(); x++) {
				if (token != ((Integer)((HashMap)history.get(x)).get(opponentId)).intValue())
				{
					isSame = false;
					break;
				}
			}
			if (isSame) {
				likelihood = 1.0D;
			} else {
				likelihood = zeroRate;
			}
		}
		else
		{
			likelihood = Math.pow(0.0D, history.size() - 1);
		}
		return likelihood;
	}
	
	public static double countExpectedUtility(int token, int staticToken, double posterior, double myPopularity, double opponentPopularity, int daClass)
	{
		double utility = 0.0D;
		if (daClass == 0) {
			utility = posterior * (myPopularity * Math.abs(token) + opponentPopularity * (token + 1));
		} else if (daClass == 1) {
			utility = posterior * (myPopularity * Math.abs(token) + opponentPopularity * (token - 1));
		} else if (daClass == 2) {
			utility = posterior * (myPopularity * Math.abs(token) + opponentPopularity * token);
		} else if (daClass == 3) {
			utility = posterior * (myPopularity * Math.abs(token) + opponentPopularity * staticToken);
		} else {
			utility = posterior * (myPopularity * Math.abs(token) + opponentPopularity * 0.0D);
		}
		return utility;
	}

  	private static HashMap<String, Integer> rationalLearnerMove(JSONArray users, int availableTokens, int currentRound, JSONObject myUser) throws Exception
  	{
  		HashMap<String, Integer> transactionsToDo = new HashMap<String, Integer>();
  		int numberOfClass = 5;
  		double classPrior = 1 / numberOfClass;
  		if ((currentRound == 1) || (currentRound == 2))
  		{
  			for (int i = 0; i < users.length(); i++)
  			{
  				JSONObject user = users.getJSONObject(i);
  				if ((availableTokens > 0) && (!user.optBoolean("isCurrentPlayer"))) {
  					transactionsToDo.put(user.optString("id"), Integer.valueOf(1));
  				}
  				availableTokens--;
  			}
  		}
  		else
  		{
  			HashMap<String, Integer> utilityCount = new HashMap();
  			int normalizer2 = 0;
  			for (int i = 0; i < users.length(); i++)
  			{
  				double likelihood = 0.0D;
  				double normalizer = 0.0D;
  				double[] numerators = new double[numberOfClass];
  				double[] posteriors = new double[numberOfClass];
  		
  				JSONObject user = users.getJSONObject(i);
  				if (!user.optBoolean("isCurrentPlayer"))
  				{
  					for (int j = 0; j < numberOfClass; j++)
  					{
  						likelihood = countLikelihood(currentRound, user.optString("id"), myUser.optString("id"), j);
  						numerators[j] = (likelihood * classPrior);
  						normalizer += numerators[j];
  					}
  					for (int j = 0; j < numberOfClass; j++) {
  						numerators[j] /= normalizer;
  					}
  					int tokens = 2 * users.length();
  					int tempVal = -tokens;
  					double[] expectedUtility = new double[tokens + 1];
  					for (int j = 0; j < expectedUtility.length; j++)
  					{
  						for (int k = 0; k < numberOfClass; k++) {
  							expectedUtility[j] += countExpectedUtility(tempVal, ((Integer)((HashMap)history.get(history.size() - 1)).get(user.optString("id"))).intValue(), posteriors[k], myUser.optDouble("popularity"), user.optDouble("popularity"), k);
  						}
  						tempVal++;
  					}
  					double max = expectedUtility[0];
  					int maxIndex = 0;
  					for (int j = 1; j <= tokens; j++) {
  						if (expectedUtility[j] > max)
  						{
  							max = expectedUtility[j];
  							maxIndex = j;
  						}
  					}
  					utilityCount.put(user.optString("id"), Integer.valueOf(maxIndex - tokens / 2));
  					normalizer2 += Math.abs(maxIndex - tokens / 2);
  				}
  			}
  			for (int i = 0; i < users.length(); i++)
  			{
  				JSONObject user = users.getJSONObject(i);
  				if (!user.optBoolean("isCurrentPlayer"))
  				{
  					int totalToken = 2 * users.length();
  					int token = ((Integer)utilityCount.get(user.optString("id"))).intValue() / normalizer2 * totalToken;
  					transactionsToDo.put(user.optString("id"), Integer.valueOf(token));
  				}
  			}
		}
  		
  		return transactionsToDo;
  	}

}


/* Location:           E:\Projects\NetBeansProjects\jhgclient\bin\
 * Qualified Name:     masdar.jh.RationalLearnerBot
 * JD-Core Version:    0.7.0.1
 */