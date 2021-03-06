package com.codepath.apps.restclienttemplate;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.adapters.TweetAdapter;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class TimelineActivity extends AppCompatActivity {
    public  static final String TAG = "TimelineActivity";
    private final int REQUEST_CODE = 20;
    TwitterClient client;
    TweetAdapter tweetAdapter;
    List<Tweet>tweets;
    RecyclerView rvTweet;
    SwipeRefreshLayout swipeRefreshLayout;
    EndlessRecyclerViewScrollListener scrollListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        //init swipe refresh container
        swipeRefreshLayout = findViewById(R.id.swipeContainer);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(TAG,"onRefresh! Fetching new data ");
                populatedHomeTimeLine();
            }
        });
        // Configure the refreshing colors
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        //init client
        client = TwitterApp.getRestClient(this);

        //find recycler view
        rvTweet = findViewById(R.id.rvTweets);
        // Init the list of tweets and adapter
        tweets = new ArrayList<>();
        tweetAdapter = new TweetAdapter(this,tweets);
        //Recycler view setup: layout manager and adapter
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvTweet.setLayoutManager(layoutManager);
        rvTweet.setAdapter(tweetAdapter);

        //init endless scroll listener
        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Log.d(TAG,"onLoadMore... page: "+ page);
                loadMoreData();
            }
        };
        // Adds the scroll listener to RecyclerView
        rvTweet.addOnScrollListener(scrollListener);

        populatedHomeTimeLine();

    }

    private void loadMoreData() {
        // 1. Send an API request to retrieve appropriate paginated data
        client.getNextPageOfTweets(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.d(TAG,"onLoadMore... onSuccess! ");
                // 2. Deserialize and construct new model objects from the API response
                JSONArray jsonArray = json.jsonArray;
                // 3. Append the new data objects to the existing set of items inside the array of items
                try {
                    List<Tweet> tweets = Tweet.fromJsonArray(jsonArray);
                    // 4. Notify the adapter of the new items made with `notifyItemRangeInserted()
                    tweetAdapter.addAll(tweets);
                }
                catch (JSONException e)
                {
                    Log.d(TAG,"onLoadMore... JSONException! ",e);

                }

            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.d(TAG,"onLoadMore... onFailure! ",throwable);
            }
        }, tweets.get(tweets.size()-1).id);

    }

    private void populatedHomeTimeLine() {
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.d(TAG,"onSuccess! " + json.toString());
                JSONArray jsonArray = json.jsonArray;
                try {
                    Log.d(TAG,"Retrieved data successfully! " + jsonArray.toString());
                    // Remember to CLEAR OUT old items before appending in the new ones
                    tweetAdapter.clear();
                    // ...the data has come back, add new items to your adapter...
                    tweetAdapter.addAll(Tweet.fromJsonArray(jsonArray));
                    // Now we call setRefreshing(false) to signal refresh has finished
                    swipeRefreshLayout.setRefreshing(false);

                } catch (JSONException e) {
                    Log.d(TAG,"JSONException: ",e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.d(TAG,"onFailure for populatedHomeTimeLine",throwable);

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true; // must return true to have the menu display

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()== R.id.compose)
        {
            Intent intent = new Intent(this, ComposeActivity.class);
            startActivityForResult(intent,REQUEST_CODE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK)
        {
            //get data from intent
            Tweet tweet = Parcels.unwrap(data.getParcelableExtra("tweet"));
            // update recycler view with new tweet
            tweets.add(0,tweet);
            tweetAdapter.notifyItemInserted(0);
            rvTweet.smoothScrollToPosition(0);
        }

    }
}