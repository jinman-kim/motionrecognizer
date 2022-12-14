package com.google.mediapipe.examples.hands;

import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethod;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class SearchYoutube extends AppCompatActivity {

    SearchView SearchView;
    AsyncTask<?, ?, ?> searchTask;

    ArrayList<SearchData> sdata = new ArrayList<SearchData>();

    final String serverKey="AIzaSyCHMwPBRIkEaY6y03aieWMWBh27qGZnb2k";

    String result1;
    final int PERMISSION = 1;
    RecyclerView recyclerview;

    UtubeAdapter utubeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.youtube_search);
        if(Build.VERSION.SDK_INT >= 23){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.INTERNET,
                    Manifest.permission.RECORD_AUDIO},PERMISSION);
        }
        SearchView = (SearchView) findViewById(R.id.SearchView);



        SearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                result1 = query;
                searchTask = new searchTask().execute();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });




        recyclerview= findViewById(R.id.recyclerview);


        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
        recyclerview.setLayoutManager(mLinearLayoutManager);




    }

    private class searchTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                paringJsonData(getUtube());
            } catch (JSONException | IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {



            utubeAdapter = new UtubeAdapter(SearchYoutube.this, sdata);
            recyclerview.setAdapter(utubeAdapter);
            utubeAdapter.notifyDataSetChanged();
        }
    }

    //????????? url??? ???????????? ????????? ???????????? json ????????? ???????????????
    public JSONObject getUtube() throws IOException {

        String originUrl = "https://www.googleapis.com/youtube/v3/search?"
                + "part=snippet&q=" + result1
                + "&key="+ serverKey + "&maxResults=50";

        String myUrl = String.format(originUrl);

        URL url = new URL(myUrl);

        HttpURLConnection connection =(HttpURLConnection)url.openConnection();
        connection.setRequestMethod("GET");
        connection.setReadTimeout(10000);
        connection.setConnectTimeout(15000);
        connection.connect();

        String line;
        String result="";
        InputStream inputStream=connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuffer response = new StringBuffer();

        while ((line = reader.readLine())!=null){
            response.append(line);
        }
        System.out.println("????????????"+ response);
        result=response.toString();


        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = new JSONObject(result);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return jsonObject;
    }

    //json ????????? ????????? ?????? ????????? ???????????? ????????????.
    //????????? ?????? ???????????? ?????? ?????? ??? ????????? ????????? ????????? ??????????????? ??????????????? ?????????.
    private void paringJsonData(JSONObject jsonObject) throws JSONException {
        //??????????????? ??????????????? ???????????? ???????????? ?????? ???????????? ????????? ????????????.
        sdata.clear();

        JSONArray contacts = jsonObject.getJSONArray("items");

        for (int i = 0; i < contacts.length(); i++) {
            JSONObject c = contacts.getJSONObject(i);
            String kind =  c.getJSONObject("id").getString("kind"); // ????????? ???????????? playlist??? ??????
            if(kind.equals("youtube#video")){
                vodid = c.getJSONObject("id").getString("videoId");
            }else{
                vodid = c.getJSONObject("id").getString("playlistId"); // ?????????
            }

            String title = c.getJSONObject("snippet").getString("title"); //????????? ????????? ???????????????
            String changString = stringToHtmlSign(title);


            String date = c.getJSONObject("snippet").getString("publishedAt") //????????????
                    .substring(0, 10);
            String imgUrl = c.getJSONObject("snippet").getJSONObject("thumbnails")
                    .getJSONObject("default").getString("url");  //????????? ????????? URL???

            //JSON?????? ????????? ???????????? ????????? ????????? ???????????? ????????????.
            sdata.add(new SearchData(vodid, changString, imgUrl, date));
        }
    }

    String vodid = "";


    //?????? ????????? ???????????? " ' ????????? ????????? ???????????? ????????? ?????? ????????? ?????? ????????? ?????? ???????????? ?????????
    private String stringToHtmlSign(String str) {

        return str.replaceAll("&amp;", "[&]")

                .replaceAll("[<]", "&lt;")

                .replaceAll("[>]", "&gt;")

                .replaceAll("&quot;", "'")

                .replaceAll("&#39;", "'");
    }


}

