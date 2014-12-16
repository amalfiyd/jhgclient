/*     */ package masdar.jh;
/*     */ 
/*     */ import java.io.PrintStream;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import org.json.JSONArray;
/*     */ import org.json.JSONObject;
/*     */ 
/*     */ public class RationalLearnerBot
/*     */ {
/*     */   private static final String TOKEN = "114963672895653325144-1417544730491";
/*     */   private static final String SERVER_HOST = "http://www.juniorhighgame.com";
/*  20 */   private static ArrayList<HashMap<String, Integer>> history = new ArrayList();
/*  21 */   private static ArrayList<HashMap<String, Double>> popularityHistory = new ArrayList();
/*  22 */   private static ArrayList<HashMap<String, Integer>> giveHistory = new ArrayList();
/*  24 */   private static JhBotApi api = new JhBotApi("http://www.juniorhighgame.com", "114963672895653325144-1417544730491");
/*     */   
/*     */   public static void main(String[] args)
/*     */   {
/*  27 */     System.out.println("Program started.");
/*     */     
/*  29 */     JSONArray games = api.getGamesList();
/*     */     
/*  31 */     System.out.println("Listing available games\n---------------");
/*  32 */     for (int i = 0; i < games.length(); i++)
/*     */     {
/*  33 */       JSONObject game = games.getJSONObject(i);
/*  34 */       System.out.println(game.optString("name") + " id: " + game.optString("id"));
/*     */     }
/*  36 */     System.out.println("---------------");
/*  37 */     if (games.length() == 0)
/*     */     {
/*  38 */       System.out.println("No games available. Exiting");
/*  39 */       System.exit(0);
/*     */     }
/*  43 */     long gameId = games.optJSONObject(0).optLong("id");
/*  44 */     gameId = 947L;
/*     */     
/*  46 */     System.out.println("Joining the first game in the list (" + gameId + "). Was joining successful:" + 
/*  47 */       api.joinGame(gameId));
/*     */     
/*  49 */     JSONObject prevGameState = api.getGameState(gameId);
/*     */     
/*  51 */     boolean test = true;
/*  53 */     while (test)
/*     */     {
/*  54 */       JSONObject newGameState = api.getGameState(gameId);
/*     */       
/*  56 */       JSONArray users = newGameState.optJSONArray("users");
/*  58 */       if (newGameState == null)
/*     */       {
/*  59 */         System.out.println("State is null and that is not ok. Please check the code");
/*     */       }
/*     */       else
/*     */       {
/*  62 */         if (prevGameState != null)
/*     */         {
/*  63 */           if (newGameState.getInt("state") == 2)
/*     */           {
/*  64 */             System.out.println("Game has finished");
/*  65 */             break;
/*     */           }
/*  67 */           if (newGameState.getInt("state") == 1)
/*     */           {
/*  68 */             if (prevGameState.getInt("state") == 0) {
/*  69 */               System.out.println("Yahoo! The game has started!");
/*     */             }
/*  71 */             if (prevGameState.getInt("round") < newGameState.getInt("round"))
/*     */             {
/*  73 */               HashMap<String, Integer> transactions = new HashMap();
/*     */               boolean isMoveOk;
/*     */               try
/*     */               {
/*  76 */                 transactions = makeMove(newGameState);
/*     */               }
/*     */               catch (Exception ex)
/*     */               {
/*  85 */                 System.err.println("There was an error while invoking makeMove function.\nPlease check it.\n This will be accounted as you have kept all the tokens to yourself\nThe stack trace of the error is presented below");
/*  86 */                 ex.printStackTrace();
/*     */               }
/*     */               finally
/*     */               {
/*     */                 boolean isMoveOk;
/*  89 */                 boolean isMoveOk = api.makeMove(gameId, transactions);
/*  90 */                 if (isMoveOk) {
/*  91 */                   System.out.println("Last move was successful");
/*     */                 } else {
/*  93 */                   System.out.println("Last move failed! Check previous logs for more info on the error");
/*     */                 }
/*     */               }
/*     */             }
/*     */           }
/*     */         }
/*  98 */         prevGameState = newGameState;
/*     */         try
/*     */         {
/* 100 */           Thread.sleep(3000L);
/*     */         }
/*     */         catch (InterruptedException e)
/*     */         {
/* 102 */           e.printStackTrace();
/*     */         }
/*     */       }
/*     */     }
/* 106 */     System.out.println("Program finished. So long and thank you for all the fish!");
/*     */   }
/*     */   
/*     */   private static HashMap<String, Integer> recordHistory(JSONObject state)
/*     */   {
/* 110 */     HashMap<String, Integer> receivedTokens = new HashMap();
/* 111 */     HashMap<String, Double> popularities = new HashMap();
/*     */     try
/*     */     {
/* 113 */       JSONArray users = state.optJSONArray("users");
/* 115 */       for (int i = 0; i < users.length(); i++)
/*     */       {
/* 116 */         JSONObject user = users.optJSONObject(i);
/* 117 */         if (!user.optBoolean("isCurrentPlayer")) {
/* 118 */           receivedTokens.put(user.optString("id"), Integer.valueOf(user.optInt("received")));
/*     */         }
/* 120 */         popularities.put(user.optString("id"), Double.valueOf(user.optDouble("popularity")));
/*     */       }
/* 124 */       history.add(receivedTokens);
/* 125 */       popularityHistory.add(popularities);
/*     */     }
/*     */     catch (Exception ex)
/*     */     {
/* 128 */       System.out.println("Sadly, there was an exception while recording hte history. Here is the stack trace");
/* 129 */       ex.printStackTrace();
/*     */     }
/* 131 */     return receivedTokens;
/*     */   }
/*     */   
/*     */   private static HashMap<String, Integer> makeMove(JSONObject state)
/*     */     throws Exception
/*     */   {
/* 142 */     HashMap<String, Integer> transactionsToDo = new HashMap();
/*     */     
/*     */ 
/* 145 */     JSONArray users = state.optJSONArray("users");
/*     */     
/*     */ 
/* 148 */     JSONObject myUser = null;
/* 149 */     for (int i = 0; i < users.length(); i++)
/*     */     {
/* 151 */       JSONObject user = users.getJSONObject(i);
/* 152 */       if (user.optBoolean("isCurrentPlayer"))
/*     */       {
/* 154 */         myUser = user;
/* 155 */         break;
/*     */       }
/*     */     }
/* 160 */     int availableTokens = state.optInt("availableToks");
/*     */     
/* 162 */     HashMap<String, Integer> receivedTokens = recordHistory(state);
/*     */     
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/* 168 */     int numberOfClass = 5;
/* 169 */     int currentRound = state.getInt("round");
/* 170 */     double classPrior = 1 / numberOfClass;
/* 173 */     if ((currentRound == 1) || (currentRound == 2))
/*     */     {
/* 176 */       for (int i = 0; i < users.length(); i++)
/*     */       {
/* 178 */         JSONObject user = users.getJSONObject(i);
/* 180 */         if ((availableTokens > 0) && 
/* 181 */           (!user.optBoolean("isCurrentPlayer"))) {
/* 183 */           transactionsToDo.put(user.optString("id"), Integer.valueOf(1));
/*     */         }
/* 185 */         availableTokens--;
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 191 */       HashMap<String, Integer> utilityCount = new HashMap();
/* 192 */       int normalizer2 = 0;
/* 193 */       for (int i = 0; i < users.length(); i++)
/*     */       {
/* 195 */         double likelihood = 0.0D;
/* 196 */         double normalizer = 0.0D;
/* 197 */         double[] numerators = new double[numberOfClass];
/* 198 */         double[] posteriors = new double[numberOfClass];
/*     */         
/* 200 */         JSONObject user = users.getJSONObject(i);
/* 201 */         if (!user.optBoolean("isCurrentPlayer"))
/*     */         {
/* 204 */           for (int j = 0; j < numberOfClass; j++)
/*     */           {
/* 206 */             likelihood = countLikelihood(currentRound, user.optString("id"), myUser.optString("id"), j);
/* 207 */             numerators[j] = (likelihood * classPrior);
/* 208 */             normalizer += numerators[j];
/*     */           }
/* 212 */           for (int j = 0; j < numberOfClass; j++) {
/* 214 */             numerators[j] /= normalizer;
/*     */           }
/* 218 */           int tokens = 2 * users.length();
/* 219 */           int tempVal = -tokens;
/* 220 */           double[] expectedUtility = new double[tokens + 1];
/* 221 */           for (int j = 0; j < expectedUtility.length; j++)
/*     */           {
/* 223 */             for (int k = 0; k < numberOfClass; k++) {
/* 225 */               expectedUtility[j] += countExpectedUtility(tempVal, ((Integer)((HashMap)history.get(history.size() - 1)).get(user.optString("id"))).intValue(), posteriors[k], myUser.optDouble("popularity"), user.optDouble("popularity"), k);
/*     */             }
/* 227 */             tempVal++;
/*     */           }
/* 231 */           double max = expectedUtility[0];
/* 232 */           int maxIndex = 0;
/* 233 */           for (int j = 1; j <= tokens; j++) {
/* 235 */             if (expectedUtility[j] > max)
/*     */             {
/* 237 */               max = expectedUtility[j];
/* 238 */               maxIndex = j;
/*     */             }
/*     */           }
/* 242 */           utilityCount.put(user.optString("id"), Integer.valueOf(maxIndex - tokens / 2));
/* 243 */           normalizer2 += Math.abs(maxIndex - tokens / 2);
/*     */         }
/*     */       }
/* 247 */       for (int i = 0; i < users.length(); i++)
/*     */       {
/* 249 */         JSONObject user = users.getJSONObject(i);
/* 250 */         if (!user.optBoolean("isCurrentPlayer"))
/*     */         {
/* 252 */           int totalToken = 2 * users.length();
/* 253 */           int token = ((Integer)utilityCount.get(user.optString("id"))).intValue() / normalizer2 * totalToken;
/* 254 */           transactionsToDo.put(user.optString("id"), Integer.valueOf(token));
/*     */         }
/*     */       }
/*     */     }
/* 260 */     giveHistory.add(transactionsToDo);
/* 261 */     ((HashMap)giveHistory.get(giveHistory.size() - 1)).put(myUser.optString("id"), Integer.valueOf(availableTokens));
/* 262 */     return transactionsToDo;
/*     */   }
/*     */   
/*     */   public static double countLikelihood(int currentRound, String opponentId, String myId, int daClass)
/*     */   {
/* 268 */     double likelihood = 0.0D;
/* 269 */     double zeroRate = 0.0D;
/* 270 */     if (daClass == 0)
/*     */     {
/* 272 */       if (((Integer)((HashMap)giveHistory.get(giveHistory.size() - 1 - 1)).get(opponentId)).intValue() < ((Integer)((HashMap)history.get(history.size() - 1)).get(opponentId)).intValue()) {
/* 274 */         likelihood = 1.0D;
/*     */       } else {
/* 276 */         likelihood = zeroRate;
/*     */       }
/*     */     }
/* 278 */     else if (daClass == 1)
/*     */     {
/* 280 */       if (((Integer)((HashMap)giveHistory.get(giveHistory.size() - 1 - 1)).get(opponentId)).intValue() > ((Integer)((HashMap)history.get(history.size() - 1)).get(opponentId)).intValue()) {
/* 282 */         likelihood = 1.0D;
/*     */       } else {
/* 284 */         likelihood = zeroRate;
/*     */       }
/*     */     }
/* 286 */     else if (daClass == 2)
/*     */     {
/* 288 */       if (((HashMap)giveHistory.get(giveHistory.size() - 1 - 1)).get(opponentId) == ((HashMap)history.get(history.size() - 1)).get(opponentId)) {
/* 290 */         likelihood = 1.0D;
/*     */       } else {
/* 292 */         likelihood = zeroRate;
/*     */       }
/*     */     }
/* 294 */     else if (daClass == 3)
/*     */     {
/* 296 */       boolean isSame = true;
/* 297 */       int token = ((Integer)((HashMap)history.get(0)).get(opponentId)).intValue();
/* 298 */       for (int x = 1; x < history.size(); x++) {
/* 300 */         if (token != ((Integer)((HashMap)history.get(x)).get(opponentId)).intValue())
/*     */         {
/* 302 */           isSame = false;
/* 303 */           break;
/*     */         }
/*     */       }
/* 307 */       if (isSame) {
/* 307 */         likelihood = 1.0D;
/*     */       } else {
/* 308 */         likelihood = zeroRate;
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 312 */       likelihood = Math.pow(0.0D, history.size() - 1);
/*     */     }
/* 315 */     return likelihood;
/*     */   }
/*     */   
/*     */   public static double countExpectedUtility(int token, int staticToken, double posterior, double myPopularity, double opponentPopularity, int daClass)
/*     */   {
/* 321 */     double utility = 0.0D;
/* 322 */     if (daClass == 0) {
/* 324 */       utility = posterior * (myPopularity * Math.abs(token) + opponentPopularity * (token + 1));
/* 326 */     } else if (daClass == 1) {
/* 328 */       utility = posterior * (myPopularity * Math.abs(token) + opponentPopularity * (token - 1));
/* 330 */     } else if (daClass == 2) {
/* 332 */       utility = posterior * (myPopularity * Math.abs(token) + opponentPopularity * token);
/* 334 */     } else if (daClass == 3) {
/* 336 */       utility = posterior * (myPopularity * Math.abs(token) + opponentPopularity * staticToken);
/*     */     } else {
/* 340 */       utility = posterior * (myPopularity * Math.abs(token) + opponentPopularity * 0.0D);
/*     */     }
/* 343 */     return utility;
/*     */   }
/*     */ }


/* Location:           E:\Projects\NetBeansProjects\jhgclient\bin\
 * Qualified Name:     masdar.jh.RationalLearnerBot
 * JD-Core Version:    0.7.0.1
 */