package com.codepath.apps.restclienttemplate;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.codepath.apps.restclienttemplate.adapters.TweetAdapter;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class TimelineActivity extends AppCompatActivity {
    public  static final String TAG = "TimelineActivity";
    TwitterClient client;
    TweetAdapter tweetAdapter;
    List<Tweet>tweets;
    RecyclerView rvTweet;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        
        client = TwitterApp.getRestClient(this);
        populatedHomeTimeLine();

        //find recycler view
        rvTweet = findViewById(R.id.rvTweets);
        // Init the list of tweets and adapter
        tweets = new ArrayList<>();
        tweetAdapter = new TweetAdapter(this,tweets);
        //Recycler view setup: layout manager and adapter
        rvTweet.setLayoutManager(new LinearLayoutManager(this));
        rvTweet.setAdapter(tweetAdapter);
        populatedHomeTimeLine();
    }

    private void populatedHomeTimeLine() {
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.d(TAG,"onSuccess! " + json.toString());
                JSONArray jsonArray = json.jsonArray;
                try {
                    Log.d(TAG,"Retrieved data successfully! " + jsonArray.toString());
                    tweets.addAll(Tweet.fromJsonArray(jsonArray));
                    //notify adapter
                    tweetAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    Log.d(TAG,"JSONException: ",e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.d(TAG,"onFailure",throwable);

            }
        });
    }
}