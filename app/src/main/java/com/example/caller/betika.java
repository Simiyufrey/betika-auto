package com.example.caller;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.caller.R;
import com.example.caller.databinding.FragmentBetikaBinding;
import com.example.caller.helpers.ApiService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class betika extends Fragment {



    public betika() {
        // Required empty public constructor
    }
    FragmentBetikaBinding binding;
    ProgressDialog dialog;

    Retrofit retrofit;
    ApiService apiService;
    ArrayList<HashMap<String,String>> logins;
    int index=0;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding =FragmentBetikaBinding.inflate(inflater,container,false);
       retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://api.betika.com")
                .build();

         apiService= retrofit.create(ApiService.class);

        binding.btnBulk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!binding.numbers.getText().equals("") && !binding.password1.getText().toString().equals("")){
//                    String mobile=binding.mobile1.getText().toString();
//                    String password=binding.password1.getText().toString();
                    index=0;

                    logins=new ArrayList<>();
                    processNumbers();
                    if(logins.size() >0){
                        makeRequests(index);
                    }
                    else{
                        Toast.makeText(getContext(),"No phone numbers found",Toast.LENGTH_SHORT).show();
                    }
                }
                else if(binding.amount.getText().toString().equals("")){
                    Toast.makeText(getContext(),"Bet Stake Amount required",Toast.LENGTH_SHORT).show();

                }
                else{
                    Toast.makeText(getContext(), "Provide all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return binding.getRoot();
    }

    private void makeRequests(int index){
        if(index < logins.size()){
            login(logins.get(index));
        }
    }
    private HashMap<String,String> login(HashMap<String,String> credentials){
        HashMap<String,String> accountDetails=new HashMap<>();
        try {

            JsonObject object = new JsonObject();
            object.addProperty("mobile", credentials.get("mobile"));
            object.addProperty("password", credentials.get("password"));
            object.addProperty("src", "MOBILE_WEB");
            object.addProperty("remember", true);
            binding.logs.append("Login Into betika account  "+object.get("mobile")+"\n");

            Map<String,String> headers=new HashMap<>();
            headers.put("Content-Type","application/json");

            Call<ResponseBody> call= apiService.loginAccount(object,headers);

           call.enqueue(new Callback<ResponseBody>() {
               @Override
               public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                   if(response.isSuccessful()){

                       String jsonResponse = null;
                       try {
                           jsonResponse = response.body().string();

                           JSONObject json = new JSONObject(jsonResponse);
                           JSONObject user=json.getJSONObject("data").getJSONObject("user");

                           String profile=user.get("id").toString();
                           String balance=user.get("balance").toString();
                           String bonus=user.get("bonus").toString();
                           String mobile=user.get("mobile").toString();
                           Log.d("LOG ME",json.toString());
                           String token=json.get("token").toString();
                           accountDetails.put("token",token);
                           accountDetails.put("balance",balance);
                           accountDetails.put("bonus",bonus);
                           accountDetails.put("profile",profile);
                           accountDetails.put("mobile",mobile);

                           logSuccess("Login successful");
//                           logSuccess("Logged in "+user.toString() +"\n");

                           HashMap<String,String> data=new HashMap<>();
                           data.put("token",accountDetails.get("token"));
                           data.put("amount",accountDetails.get("balance"));
                           data.put("app_name","MOBILE_WEB");
                           logSuccess("Balance: "+accountDetails.get("balance"));
                           if(Double.parseDouble(accountDetails.get("balance")) > 100){
//                               fetch_games(accountDetails);
//                               withdraw(data);
                           }
                           else{
                               logError("Failed to withdraw balance below 100 "+accountDetails.get("balance"));
                               index +=1;
                               makeRequests(index);
                           }

                       } catch (IOException e) {
                           logError(e.getMessage());
                       } catch (JSONException e) {
                           logError(e.getMessage());
                       }
                       Log.d("LOG ME", "Response: " + jsonResponse);
                   }
                   else{

                       try {
                           if(response.errorBody() !=null){
                               JSONObject error=new JSONObject(response.errorBody().string());
                               String message="Login Failed "+error.getJSONObject("error").get("message").toString();
                               logError(message);
                           }
                           else{
                               Toast.makeText(getContext(),"UnExpected Error Occurred!!!",Toast.LENGTH_SHORT).show();
                           }

                       } catch (IOException e) {
                           logError(e.getMessage().toString());
                       } catch (JSONException e) {
                           logError(e.getMessage());
                       }
                   }
               }
               @Override
               public void onFailure(Call<ResponseBody> call, Throwable t) {

                   Log.d("LOG ME", "Response: " + t.getMessage());

                   Toast.makeText(getContext(),"IS null"+t.getMessage(),Toast.LENGTH_SHORT).show();
               }
           });
        }
        catch (Exception e){

            Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
        }
        return accountDetails;
    }
    private void fetch_games(HashMap<String,String> user){
        try {
//            games url
            String  url2=String.format("https://api.betika.com/v1/uo/matches?page=1&limit=%s&tab=&sub_type_id=1,186,340&sport_id=14&tag_id=&sort_id=1&period_id=-1&esports=false",10);

            logSuccess("Fetching games");
            Call<ResponseBody> call=apiService.getGames("10");
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if(response.isSuccessful()){
                        String jsonresponse= null;

                        try {
                            jsonresponse = response.body().string();
                            JSONObject json=new JSONObject(jsonresponse);
                            JSONArray games=json.getJSONArray("data");
                            ArrayList<HashMap<String,Object>> betslip=new ArrayList<>();
                            int totalOdds=1;
                            for(int i=0; i<games.length(); i++){
                                HashMap<String,Object> bet=new HashMap<>();
                                JSONObject game=games.getJSONObject(i);
                                Log.d("LOG ME",game.toString());
                                String home_team=game.get("home_team").toString();
                                Log.d("LOG ME",home_team);
                                String away_team=game.get("away_team").toString();
                                String home_odd=game.get("home_odd").toString();
                                String away_odd=game.get("away_odd").toString();
                                String draw=game.get("neutral_odd").toString();
                                String sportID=game.get("sport_id").toString();
                                String game_id=game.get("game_id").toString();
                                String match_id=game.get("match_id").toString();
                                String startTime=game.get("start_time").toString();
                                String parentMatchId=game.get("parent_match_id").toString();

                                try {
                                    if(Double.parseDouble(home_odd) < 1.5 || Double.parseDouble(away_odd) <= 1.5) {
                                        String odd = "";
                                        String pick = "";
                                        String team = "";
                                        if (Double.parseDouble(home_odd) <= Double.parseDouble(away_odd)) {
                                            pick = "1";
                                            odd = home_odd;
                                        } else {
                                            pick = "3";
                                            odd = away_odd;
                                        }
                                        if(pick=="1"){
                                            team=home_team;
                                        } else if (pick=="3") {
                                            team=away_team;
                                        }
                                        totalOdds *=Double.parseDouble(odd);
                                        bet.put("sub_type_id", "1");
                                        bet.put("bet_pick", team);
                                        bet.put("odd_value", odd);
                                        bet.put("outcome_id", pick);
                                        bet.put("sport_id", sportID);
                                        bet.put("special_bet_value", "");
                                        bet.put("parent_match_id", parentMatchId);
                                        bet.put("bet_type", 7);

                                        logSuccess(String.format("%s vs %s",home_team,away_team));
                                        logWhite(String.format("%s to win",team));

                                        betslip.add(bet);
                                        break;
                                    }

                                }catch (Exception e){

                                    logError(e.getMessage());
                                }
                            }
                            String stake=binding.amount.getText().toString();
                            placeBet(betslip,stake,user.get("token"),String.valueOf(totalOdds), user.get("profile"),user.get("mobile"),user);
                            Log.d("LOG ME",games.toString());
                        } catch (JSONException e) {
                            logError(e.getMessage());
                            Log.d("LOG ME",e+"");
                        } catch (IOException e) {
                            logError(e.getMessage());
                            Log.d("LOG ME",e+"");
                        }
                    }
                }
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                    Log.e("Error ME",t.getMessage());
                }
            });

        }
        catch (Exception e){
            logError("Failed "+e.getMessage());
        }
    }

    private void placeBet(ArrayList<HashMap<String,Object>> betslip,String stake,String token,String total_odds,String profile,String mobile,HashMap<String,String> account){
        try {
            if(betslip.size() > 0){

                HashMap<String,Object> map=new HashMap<>();
                map.put("profile_id",profile);
                map.put("stake",stake);
                map.put("total_odd",total_odds);
                map.put("src","MOBILE_WEB");
                map.put("betslip",betslip);
                map.put("token",token);
                map.put("user_agent",getContext().getResources().getString(R.string.agent));
                map.put("app_version","6.0.0");
                map.put("affiliate",null);
                map.put("promo_id",null);
                map.put("fbpid",false);
                map.put("is_freebet",false);
                Call<ResponseBody> call2= apiService.placeBet(map);
                call2.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if(response.isSuccessful()){
                            try {
                                String jsonbody=response.body().string();
                                Log.d("LOG ME2",jsonbody.toString());
                                JSONObject json=new JSONObject(jsonbody);
                                JSONObject params=json.getJSONObject("params");

                                String betId=params.get("bet_id").toString();
                                HashMap<String,Object > cancelPayload=new HashMap<>();
                                cancelPayload.put("profile_id",profile);
                                cancelPayload.put("mobile",mobile);
                                cancelPayload.put("token",token);
                                cancelPayload.put("bet_id",betId);

                                cancelBet(cancelPayload,account);

                            } catch (IOException e) {
                                logError(e.getMessage());
                                Log.d("LOG ME",e.getMessage());
                            } catch (JSONException e) {
                                logError(e.getMessage());
                                Log.d("LOG ME",e.getMessage());
                            }

                            logSuccess("Bet placed successfully");
                        }
                        else{
                            try {
                                String jsonerror=response.errorBody().string();
                                JSONObject error=new JSONObject(jsonerror);
                                logError(error.get("message").toString());

                            } catch (IOException e) {
                                logError(e.getMessage());
                            } catch (JSONException e) {
                                logError(e.getMessage());
                                Log.d("LOG ME",e.getMessage());
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        logError(t.getMessage());
                        Log.d("LOG ME",t.getMessage());
                    }
                });

            }
            else{
                logError("No games in the betslip");
            }
        }
        catch (Exception e){
            logError(e.getMessage());
            Log.d("LOG ME",e.getMessage());
        }
    }

    private void cancelBet(HashMap<String,Object> data,HashMap<String,String> account){
        Call<ResponseBody> call3= apiService.cancelBet(data);
        call3.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()){
                    try {
                        String jsonresponse=response.body().string();

                        JSONObject json=new JSONObject(jsonresponse);
                        logSuccess(json.toString());
                        cancelBet(data,account);
                    } catch (IOException e) {
                        logError(e.getMessage());
                    } catch (JSONException e) {
                        logError(e.getMessage());
                    }
                }
                else{
                    try {
                        logError(response.errorBody().string());
                        HashMap<String,String> data=new HashMap<>();
                        data.put("token",account.get("token"));
                        data.put("amount",account.get("balance"));
                        data.put("app_name","MOBILE_WEB");
                        withdraw(data);

                    } catch (IOException e) {
                        logError(e.getMessage());
                    }
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

                Toast.makeText(getContext(),"Failed "+t.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void processNumbers(){

        String text=binding.numbers.getText().toString();
        String[]n=text.split("\n");
        StringBuffer bf=new StringBuffer();
        for(String t : n){
            if(t.length() > 9){
                Pattern pattern=Pattern.compile("\\d+");
                Matcher matcher=pattern.matcher(t);
                while (matcher.find()){
                    String number=matcher.group();
                    if(number.length() >7){
                        HashMap<String ,String> map=new HashMap<>();
                        map.put("mobile",number);
                        map.put("password",binding.password1.getText().toString());
                        logins.add(map);
                    }
                }
        }
    }}
private void withdraw(HashMap<String,String> payload){
        Call<ResponseBody> call4= apiService.withdraw(payload);
        call4.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                if(response.isSuccessful()){
                    try {
                        String jsonresponse=response.body().string();
                        JSONObject json=new JSONObject(jsonresponse);
                        logSuccess(json.getJSONObject("success").getString("message"));
                        index +=1;
                        makeRequests(index );
                    } catch (IOException e) {
                        logError(e.getMessage());
                        logError(e.getMessage());
                    } catch (JSONException e) {
                        logError(e.getMessage());
                    }
                }
                else{
                    try {
                        String jsonresponse=response.errorBody().string();
                        JSONObject error=new JSONObject(jsonresponse).getJSONObject("error");
                        logError(error.get("message").toString());
                    } catch (IOException e) {
                        logError(e.getMessage());
                    } catch (JSONException e) {
                        logError(e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

                logError(t.getMessage());
            }
        });

}
    private  void  logSuccess(String message){
        logMessage(message,getContext().getResources().getColor(R.color.green));
    }
    private void logWhite(String message){
        logMessage(message,Color.WHITE);
    }
    private  void  logError(String message){
        logMessage(message, Color.RED);
    }
    public void logMessage(String message, int color) {
        Spannable spannable = new SpannableString(message + "\n");
        spannable.setSpan(new ForegroundColorSpan(color), 0, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.logs.append(spannable);
        binding.scrollView.fullScroll(View.FOCUS_DOWN);
    }
}