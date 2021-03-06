package masdar.jh;


import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class tft_popBotExample {
    // SET THIS VARIABLE
    private static final String TOKEN = "106611669683912797583-1417162822988";
    //////////////////////////////////////////////////////////////////////////////////////
    // Set the server host (most probably you do not need to change this, but it should be www.juniorhighgame.com)
    // Unless you are running your own jhg server
    private static final String SERVER_HOST = "http://www.juniorhighgame.com";
    //private static final String SERVER_HOST = "http://localhost:5000";
    //////////////////////////////////////////////////////////////////////////////////////

    private static ArrayList< HashMap<String, Integer> > history = new ArrayList<HashMap<String, Integer>>();

    private static JhBotApi api = new JhBotApi(SERVER_HOST, TOKEN);

    public static void main(String[] args) {
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

    private static HashMap<String ,Integer> recordHistory(JSONObject state) {
        HashMap<String ,Integer> receivedTokens = new HashMap<String, Integer>();
        try {
            JSONArray users = state.optJSONArray("users");
            // Here is how you access it
            for(int i=0; i<users.length(); i++) {
                JSONObject user = users.optJSONObject(i);
                if (!user.optBoolean("isCurrentPlayer")){
                    receivedTokens.put(user.optString("id"), user.optInt("received"));
                }
            }
            // Lets record the history. You might need it in future
            history.add(receivedTokens);
        }
        catch (Exception ex) {
            System.out.println("Sadly, there was an exception while recording hte history. Here is the stack trace");
            ex.printStackTrace();
        }
        return receivedTokens;
    }

    /**
     * You need to fill out this function to make move. This function will be called each time a new round starts.
     * @param state current state of the game will be passed
     * @return the transaction hashmap
     * @throws Exception
     */
    private static HashMap<String, Integer> makeMove(JSONObject state) throws Exception {
        // Fill out this variable to make transactions
        HashMap<String, Integer> transactionsToDo = new HashMap<String, Integer>();

        // This array contains the users and the amount of tokens received from them on last round
        JSONArray users = state.optJSONArray("users");
 
        // This variable will contain available tokens for this round
        int availableTokens = state.optInt("availableToks");
     
        HashMap<String ,Integer> receivedTokens = recordHistory(state);
        // receivedTokens HashMap contains received tokens (maps user id to tokens got from that user)
        // this.history variable is an array of this kind of objects, representing the history during the game.
        /////////////// START YOUR CODE FROM HERE /////////////////////////////
        
        transactionsToDo = tftPopMove(users, availableTokens, receivedTokens);

        /////////////// END YOUR CODE HERE /////////////////////////////
        // By this point transactionsToDo must contain all transactions you want to do during this move
        return transactionsToDo;
    }
    
    private static HashMap<String, Integer> tftPopMove(JSONArray users, int availableTokens, HashMap<String, Integer> receivedTokens) throws Exception
    {
    	 // Wael-21/11/2014  Tit-for-Tat
        HashMap<String,Double> popularity = new HashMap<String,Double>();
        HashMap<String, Integer> transactionsToDo = new HashMap<String, Integer>();
        // Get the popularity of agents
        for(int i=0; i<users.length(); i++) {
            JSONObject user = users.optJSONObject(i);
            if (!user.optBoolean("isCurrentPlayer")){
            	popularity.put(user.optString("id"), user.optDouble("popularity"));
            }
        }
                
               
        ValueComparator bvc =  new ValueComparator(popularity);
        TreeMap<String,Double> sortedPopularity = new TreeMap<String,Double>(bvc);
        
        sortedPopularity.putAll(popularity);
        
        System.out.println(sortedPopularity.toString());
        
        for(String userId : sortedPopularity.keySet())
        {
        	if (availableTokens - Math.abs(receivedTokens.get(userId)) >= 0)
        	{        		        	      	
        		
        		transactionsToDo.put(userId, receivedTokens.get(userId));
        		availableTokens -= Math.abs(receivedTokens.get(userId));
        		
        	}
        	else
        	{
        		
        		if (receivedTokens.get(userId) > 0)
        			transactionsToDo.put(userId, availableTokens);
        		else 
        			transactionsToDo.put(userId, -availableTokens);	
        	}
        	
        }
        
        return transactionsToDo;
    }
}

//28/11/2014 Wael-E this class is for sorting the HashMap objects by values
class ValueComparator implements Comparator<String> {

 HashMap<String, Double> base;
 public ValueComparator(HashMap<String, Double> base) {
     this.base = base;
 }

 // Note: this comparator imposes orderings that are inconsistent with equals.    
 public int compare(String a, String b) {
     if (base.get(a) >= base.get(b)) {
         return -1;
     } else {
         return 1;
     } // returning 0 would merge keys
 }
}