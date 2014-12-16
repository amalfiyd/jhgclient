/*     */ package masdar.jh;
/*     */ 
/*     */ import java.io.PrintStream;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.Random;
/*     */ import org.json.JSONArray;
/*     */ import org.json.JSONObject;
/*     */ 
/*     */ public class staticBot
/*     */ {
/*     */   private static final String TOKEN = "114963672895653325144-1417536702864";
/*     */   private static final String SERVER_HOST = "http://www.juniorhighgame.com";
/*  21 */   private static ArrayList<HashMap<String, Integer>> history = new ArrayList();
/*  22 */   private static ArrayList<HashMap<String, Double>> popularityHistory = new ArrayList();
/*  23 */   private static ArrayList<HashMap<String, Integer>> giveHistory = new ArrayList();
/*  25 */   private static JhBotApi api = new JhBotApi("http://www.juniorhighgame.com", "114963672895653325144-1417536702864");
/*  26 */   private static int staticToken = 0;
/*     */   
/*     */   public static void main(String[] args)
/*     */   {
/*  29 */     System.out.println("Program started.");
/*     */     
/*  31 */     JSONArray games = api.getGamesList();
/*     */     
/*  33 */     System.out.println("Listing available games\n---------------");
/*  34 */     for (int i = 0; i < games.length(); i++)
/*     */     {
/*  35 */       JSONObject game = games.getJSONObject(i);
/*  36 */       System.out.println(game.optString("name") + " id: " + game.optString("id"));
/*     */     }
/*  38 */     System.out.println("---------------");
/*  39 */     if (games.length() == 0)
/*     */     {
/*  40 */       System.out.println("No games available. Exiting");
/*  41 */       System.exit(0);
/*     */     }
/*  45 */     long gameId = games.optJSONObject(0).optLong("id");
/*  46 */     gameId = 911L;
/*     */     
/*  48 */     System.out.println("Joining the first game in the list (" + gameId + "). Was joining successful:" + 
/*  49 */       api.joinGame(gameId));
/*     */     
/*  51 */     JSONObject prevGameState = api.getGameState(gameId);
/*     */     
/*  53 */     boolean test = true;
/*  57 */     while (test)
/*     */     {
/*  58 */       JSONObject newGameState = api.getGameState(gameId);
/*     */       
/*  60 */       JSONArray users = newGameState.optJSONArray("users");
/*  65 */       if (newGameState == null)
/*     */       {
/*  66 */         System.out.println("State is null and that is not ok. Please check the code");
/*     */       }
/*     */       else
/*     */       {
/*  69 */         if (prevGameState != null)
/*     */         {
/*  70 */           if (newGameState.getInt("state") == 2)
/*     */           {
/*  71 */             System.out.println("Game has finished");
/*  72 */             break;
/*     */           }
/*  74 */           if (newGameState.getInt("state") == 1)
/*     */           {
/*  75 */             if (prevGameState.getInt("state") == 0) {
/*  76 */               System.out.println("Yahoo! The game has started!");
/*     */             }
/*  78 */             if (prevGameState.getInt("round") < newGameState.getInt("round"))
/*     */             {
/*  80 */               HashMap<String, Integer> transactions = new HashMap();
/*     */               boolean isMoveOk;
/*     */               try
/*     */               {
/*  83 */                 transactions = makeMove(newGameState);
/*     */               }
/*     */               catch (Exception ex)
/*     */               {
/*  92 */                 System.err.println("There was an error while invoking makeMove function.\nPlease check it.\n This will be accounted as you have kept all the tokens to yourself\nThe stack trace of the error is presented below");
/*  93 */                 ex.printStackTrace();
/*     */               }
/*     */               finally
/*     */               {
/*     */                 boolean isMoveOk;
/*  96 */                 System.out.println(transactions.toString());
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
/* 162 */     Random rand = new Random();
/* 163 */     int max = 2 * users.length();
/* 164 */     int min = -max;
/* 165 */     int randomNum = rand.nextInt(max - min + 1) + min;
/* 167 */     if (state.getInt("round") == 1) {
/* 167 */       staticToken = randomNum;
/*     */     }
/* 170 */     for (int i = 0; i < users.length(); i++)
/*     */     {
/* 172 */       JSONObject user = users.getJSONObject(i);
/* 173 */       if (!user.optBoolean("isCurrentPlayer")) {
/* 175 */         if (staticToken <= availableTokens)
/*     */         {
/* 177 */           transactionsToDo.put(user.optString("id"), Integer.valueOf(staticToken));
/* 178 */           availableTokens -= Math.abs(staticToken);
/*     */         }
/*     */       }
/*     */     }
/* 184 */     giveHistory.add(transactionsToDo);
/* 185 */     return transactionsToDo;
/*     */   }
/*     */ }


/* Location:           E:\Projects\NetBeansProjects\jhgclient\bin\
 * Qualified Name:     masdar.jh.staticBot
 * JD-Core Version:    0.7.0.1
 */