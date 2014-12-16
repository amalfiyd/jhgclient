/*     */ package masdar.jh;
/*     */ 
/*     */ import java.io.PrintStream;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.Random;
/*     */ import org.json.JSONArray;
/*     */ import org.json.JSONObject;
/*     */ 
/*     */ public class gullibleBot
/*     */ {
/*     */   private static final String TOKEN = "106586887905360722018-1417587110062";
/*     */   private static final String SERVER_HOST = "http://www.juniorhighgame.com";
/*  23 */   private static ArrayList<HashMap<String, Integer>> history = new ArrayList();
/*  24 */   private static ArrayList<HashMap<String, Double>> popularityHistory = new ArrayList();
/*  25 */   private static ArrayList<HashMap<String, Integer>> giveHistory = new ArrayList();
/*  27 */   private static JhBotApi api = new JhBotApi("http://www.juniorhighgame.com", "106586887905360722018-1417587110062");
/*     */   
/*     */   public static void main(String[] args)
/*     */   {
/*  30 */     System.out.println("Program started.");
/*     */     
/*  32 */     JSONArray games = api.getGamesList();
/*     */     
/*  34 */     System.out.println("Listing available games\n---------------");
/*  35 */     for (int i = 0; i < games.length(); i++)
/*     */     {
/*  36 */       JSONObject game = games.getJSONObject(i);
/*  37 */       System.out.println(game.optString("name") + " id: " + game.optString("id"));
/*     */     }
/*  39 */     System.out.println("---------------");
/*  40 */     if (games.length() == 0)
/*     */     {
/*  41 */       System.out.println("No games available. Exiting");
/*  42 */       System.exit(0);
/*     */     }
/*  46 */     long gameId = games.optJSONObject(0).optLong("id");
/*  47 */     gameId = 947L;
/*     */     
/*  49 */     System.out.println("Joining the first game in the list (" + gameId + "). Was joining successful:" + 
/*  50 */       api.joinGame(gameId));
/*     */     
/*  52 */     JSONObject prevGameState = api.getGameState(gameId);
/*     */     
/*  54 */     boolean test = true;
/*  58 */     while (test)
/*     */     {
/*  59 */       JSONObject newGameState = api.getGameState(gameId);
/*     */       
/*  61 */       JSONArray users = newGameState.optJSONArray("users");
/*  66 */       if (newGameState == null)
/*     */       {
/*  67 */         System.out.println("State is null and that is not ok. Please check the code");
/*     */       }
/*     */       else
/*     */       {
/*  70 */         if (prevGameState != null)
/*     */         {
/*  71 */           if (newGameState.getInt("state") == 2)
/*     */           {
/*  72 */             System.out.println("Game has finished");
/*  73 */             break;
/*     */           }
/*  75 */           if (newGameState.getInt("state") == 1)
/*     */           {
/*  76 */             if (prevGameState.getInt("state") == 0) {
/*  77 */               System.out.println("Yahoo! The game has started!");
/*     */             }
/*  79 */             if (prevGameState.getInt("round") < newGameState.getInt("round"))
/*     */             {
/*  81 */               HashMap<String, Integer> transactions = new HashMap();
/*     */               boolean isMoveOk;
/*     */               try
/*     */               {
/*  84 */                 transactions = makeMove(newGameState);
/*     */               }
/*     */               catch (Exception ex)
/*     */               {
/*  93 */                 System.err.println("There was an error while invoking makeMove function.\nPlease check it.\n This will be accounted as you have kept all the tokens to yourself\nThe stack trace of the error is presented below");
/*  94 */                 ex.printStackTrace();
/*     */               }
/*     */               finally
/*     */               {
/*     */                 boolean isMoveOk;
/*  97 */                 boolean isMoveOk = api.makeMove(gameId, transactions);
/*  98 */                 if (isMoveOk) {
/*  99 */                   System.out.println("Last move was successful");
/*     */                 } else {
/* 101 */                   System.out.println("Last move failed! Check previous logs for more info on the error");
/*     */                 }
/*     */               }
/*     */             }
/*     */           }
/*     */         }
/* 106 */         prevGameState = newGameState;
/*     */         try
/*     */         {
/* 108 */           Thread.sleep(3000L);
/*     */         }
/*     */         catch (InterruptedException e)
/*     */         {
/* 110 */           e.printStackTrace();
/*     */         }
/*     */       }
/*     */     }
/* 114 */     System.out.println("Program finished. So long and thank you for all the fish!");
/*     */   }
/*     */   
/*     */   private static HashMap<String, Integer> recordHistory(JSONObject state)
/*     */   {
/* 118 */     HashMap<String, Integer> receivedTokens = new HashMap();
/* 119 */     HashMap<String, Double> popularities = new HashMap();
/*     */     try
/*     */     {
/* 121 */       JSONArray users = state.optJSONArray("users");
/* 123 */       for (int i = 0; i < users.length(); i++)
/*     */       {
/* 124 */         JSONObject user = users.optJSONObject(i);
/* 125 */         if (!user.optBoolean("isCurrentPlayer")) {
/* 126 */           receivedTokens.put(user.optString("id"), Integer.valueOf(user.optInt("received")));
/*     */         }
/* 128 */         popularities.put(user.getString("id"), Double.valueOf(user.optDouble("popularity")));
/*     */       }
/* 131 */       history.add(receivedTokens);
/* 132 */       popularityHistory.add(popularities);
/*     */     }
/*     */     catch (Exception ex)
/*     */     {
/* 135 */       System.out.println("Sadly, there was an exception while recording hte history. Here is the stack trace");
/* 136 */       ex.printStackTrace();
/*     */     }
/* 138 */     return receivedTokens;
/*     */   }
/*     */   
/*     */   private static HashMap<String, Integer> makeMove(JSONObject state)
/*     */     throws Exception
/*     */   {
/* 149 */     HashMap<String, Integer> transactionsToDo = new HashMap();
/*     */     
/*     */ 
/* 152 */     JSONArray users = state.optJSONArray("users");
/*     */     
/*     */ 
/* 155 */     int availableTokens = state.optInt("availableToks");
/*     */     
/* 157 */     HashMap<String, Integer> receivedTokens = recordHistory(state);
/*     */     
/*     */ 
/*     */ 
/*     */ 
/* 162 */     int currentRound = state.getInt("round");
/* 163 */     for (int i = 0; i < users.length(); i++)
/*     */     {
/* 165 */       JSONObject user = users.getJSONObject(i);
/* 166 */       if (!user.optBoolean("isCurrentPlayer")) {
/* 171 */         if (currentRound == 1)
/*     */         {
/* 173 */           transactionsToDo.put(user.optString("id"), Integer.valueOf(1));
/* 174 */           availableTokens--;
/*     */         }
/*     */         else
/*     */         {
/* 178 */           int lastGivenToken = ((Integer)((HashMap)history.get(history.size() - 1)).get(user.optString("id"))).intValue();
/* 179 */           int max = 2 * users.length();
/* 181 */           if (lastGivenToken >= max - 1)
/*     */           {
/* 183 */             if (availableTokens > 0)
/*     */             {
/* 185 */               while (availableTokens < max) {
/* 187 */                 max--;
/*     */               }
/* 189 */               transactionsToDo.put(user.optString("id"), Integer.valueOf(max));
/* 190 */               availableTokens -= Math.abs(max);
/*     */             }
/*     */           }
/*     */           else
/*     */           {
/* 196 */             int min = lastGivenToken + 1;
/* 197 */             Random rand = new Random();
/* 198 */             int toGive = rand.nextInt(max - min + 1) + min;
/* 199 */             if (availableTokens > 0)
/*     */             {
/* 201 */               while (availableTokens < toGive) {
/* 203 */                 toGive--;
/*     */               }
/* 205 */               transactionsToDo.put(user.optString("id"), Integer.valueOf(toGive));
/* 206 */               availableTokens -= Math.abs(toGive);
/*     */             }
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/* 214 */     giveHistory.add(transactionsToDo);
/* 215 */     return transactionsToDo;
/*     */   }
/*     */ }


/* Location:           E:\Projects\NetBeansProjects\jhgclient\bin\
 * Qualified Name:     masdar.jh.gullibleBot
 * JD-Core Version:    0.7.0.1
 */