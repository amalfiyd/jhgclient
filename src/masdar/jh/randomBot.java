/*     */ package masdar.jh;
/*     */ 
/*     */ import java.io.PrintStream;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.Random;
/*     */ import org.json.JSONArray;
/*     */ import org.json.JSONObject;
/*     */ 
/*     */ public class randomBot
/*     */ {
/*     */   private static final String TOKEN = "114397137729536594852-1417587405217";
/*     */   private static final String SERVER_HOST = "http://www.juniorhighgame.com";
/*  21 */   private static ArrayList<HashMap<String, Integer>> history = new ArrayList();
/*  22 */   private static ArrayList<HashMap<String, Double>> popularityHistory = new ArrayList();
/*  23 */   private static ArrayList<HashMap<String, Integer>> giveHistory = new ArrayList();
/*  25 */   private static JhBotApi api = new JhBotApi("http://www.juniorhighgame.com", "114397137729536594852-1417587405217");
/*     */   
/*     */   public static void main(String[] args)
/*     */   {
/*  28 */     System.out.println("Program started.");
/*     */     
/*  30 */     JSONArray games = api.getGamesList();
/*     */     
/*  32 */     System.out.println("Listing available games\n---------------");
/*  33 */     for (int i = 0; i < games.length(); i++)
/*     */     {
/*  34 */       JSONObject game = games.getJSONObject(i);
/*  35 */       System.out.println(game.optString("name") + " id: " + game.optString("id"));
/*     */     }
/*  37 */     System.out.println("---------------");
/*  38 */     if (games.length() == 0)
/*     */     {
/*  39 */       System.out.println("No games available. Exiting");
/*  40 */       System.exit(0);
/*     */     }
/*  44 */     long gameId = games.optJSONObject(0).optLong("id");
/*  45 */     gameId = 947L;
/*     */     
/*  47 */     System.out.println("Joining the first game in the list (" + gameId + "). Was joining successful:" + 
/*  48 */       api.joinGame(gameId));
/*     */     
/*  50 */     JSONObject prevGameState = api.getGameState(gameId);
/*     */     
/*  52 */     boolean test = true;
/*  56 */     while (test)
/*     */     {
/*  57 */       JSONObject newGameState = api.getGameState(gameId);
/*     */       
/*  59 */       JSONArray users = newGameState.optJSONArray("users");
/*  64 */       if (newGameState == null)
/*     */       {
/*  65 */         System.out.println("State is null and that is not ok. Please check the code");
/*     */       }
/*     */       else
/*     */       {
/*  68 */         if (prevGameState != null)
/*     */         {
/*  69 */           if (newGameState.getInt("state") == 2)
/*     */           {
/*  70 */             System.out.println("Game has finished");
/*  71 */             break;
/*     */           }
/*  73 */           if (newGameState.getInt("state") == 1)
/*     */           {
/*  74 */             if (prevGameState.getInt("state") == 0) {
/*  75 */               System.out.println("Yahoo! The game has started!");
/*     */             }
/*  77 */             if (prevGameState.getInt("round") < newGameState.getInt("round"))
/*     */             {
/*  79 */               HashMap<String, Integer> transactions = new HashMap();
/*     */               boolean isMoveOk;
/*     */               try
/*     */               {
/*  82 */                 transactions = makeMove(newGameState);
/*     */               }
/*     */               catch (Exception ex)
/*     */               {
/*  91 */                 System.err.println("There was an error while invoking makeMove function.\nPlease check it.\n This will be accounted as you have kept all the tokens to yourself\nThe stack trace of the error is presented below");
/*  92 */                 ex.printStackTrace();
/*     */               }
/*     */               finally
/*     */               {
/*     */                 boolean isMoveOk;
/*  95 */                 System.out.println(transactions.toString());
/*  96 */                 boolean isMoveOk = api.makeMove(gameId, transactions);
/*  97 */                 if (isMoveOk) {
/*  98 */                   System.out.println("Last move was successful");
/*     */                 } else {
/* 100 */                   System.out.println("Last move failed! Check previous logs for more info on the error");
/*     */                 }
/*     */               }
/*     */             }
/*     */           }
/*     */         }
/* 105 */         prevGameState = newGameState;
/*     */         try
/*     */         {
/* 107 */           Thread.sleep(3000L);
/*     */         }
/*     */         catch (InterruptedException e)
/*     */         {
/* 109 */           e.printStackTrace();
/*     */         }
/*     */       }
/*     */     }
/* 113 */     System.out.println("Program finished. So long and thank you for all the fish!");
/*     */   }
/*     */   
/*     */   private static HashMap<String, Integer> recordHistory(JSONObject state)
/*     */   {
/* 117 */     HashMap<String, Integer> receivedTokens = new HashMap();
/* 118 */     HashMap<String, Double> popularities = new HashMap();
/*     */     try
/*     */     {
/* 120 */       JSONArray users = state.optJSONArray("users");
/* 122 */       for (int i = 0; i < users.length(); i++)
/*     */       {
/* 123 */         JSONObject user = users.optJSONObject(i);
/* 124 */         if (!user.optBoolean("isCurrentPlayer")) {
/* 125 */           receivedTokens.put(user.optString("id"), Integer.valueOf(user.optInt("received")));
/*     */         }
/* 127 */         popularities.put(user.getString("id"), Double.valueOf(user.optDouble("popularity")));
/*     */       }
/* 130 */       history.add(receivedTokens);
/* 131 */       popularityHistory.add(popularities);
/*     */     }
/*     */     catch (Exception ex)
/*     */     {
/* 134 */       System.out.println("Sadly, there was an exception while recording hte history. Here is the stack trace");
/* 135 */       ex.printStackTrace();
/*     */     }
/* 137 */     return receivedTokens;
/*     */   }
/*     */   
/*     */   private static HashMap<String, Integer> makeMove(JSONObject state)
/*     */     throws Exception
/*     */   {
/* 148 */     HashMap<String, Integer> transactionsToDo = new HashMap();
/*     */     
/*     */ 
/* 151 */     JSONArray users = state.optJSONArray("users");
/*     */     
/*     */ 
/* 154 */     int availableTokens = state.optInt("availableToks");
/*     */     
/* 156 */     HashMap<String, Integer> receivedTokens = recordHistory(state);
/* 162 */     for (int i = 0; i < users.length(); i++)
/*     */     {
/* 164 */       JSONObject user = users.getJSONObject(i);
/* 165 */       if (!user.optBoolean("isCurrentPlayer"))
/*     */       {
/* 167 */         Random rand = new Random();
/* 168 */         int max = 2 * users.length();
/* 169 */         int min = -max;
/* 170 */         int randomNum = rand.nextInt(max - min + 1) + min;
/* 172 */         if (randomNum <= availableTokens)
/*     */         {
/* 174 */           transactionsToDo.put(user.optString("id"), Integer.valueOf(randomNum));
/* 175 */           availableTokens -= Math.abs(randomNum);
/*     */         }
/*     */       }
/*     */     }
/* 181 */     giveHistory.add(transactionsToDo);
/* 182 */     return transactionsToDo;
/*     */   }
/*     */ }


/* Location:           E:\Projects\NetBeansProjects\jhgclient\bin\
 * Qualified Name:     masdar.jh.randomBot
 * JD-Core Version:    0.7.0.1
 */