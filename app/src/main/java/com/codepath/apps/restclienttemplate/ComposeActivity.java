package com.codepath.apps.restclienttemplate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.parceler.Parcels;

import okhttp3.Headers;

public class ComposeActivity extends AppCompatActivity {

    public static final int MAX_TWEET_LENGTH = 280;
    EditText etCompose;
    Button btnTweet;
    TwitterClient twitterClient;
    public static final String TAG = "ComposeActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        twitterClient = TwitterApp.getRestClient(this);

        setContentView(R.layout.activity_compose);
        etCompose = findViewById(R.id.etCompose);
        btnTweet = findViewById(R.id.btnTweet);

        //set onclick listener for button
        btnTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String content = etCompose.getText().toString();
                if (content.isEmpty())
                {
                    Toast.makeText(ComposeActivity.this,"Sorry! Your Tweet is empty",Toast.LENGTH_LONG).show();
                    return;
                }
                else if (content.length()>MAX_TWEET_LENGTH)
                {
                    Toast.makeText(ComposeActivity.this,"Sorry! Your Tweet is too long",Toast.LENGTH_LONG).show();
                    return;
                }
                else {
                    //Make API call
                    twitterClient.publishTweet(content, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.i(TAG,"publishTweet: onSuccess!..."+ content);
                            try {
                                Tweet tweet = Tweet.fromJson(json.jsonObject);
                                Log.i(TAG,"publishTweet says: "+ tweet.body);
                                Intent intent = new Intent();
                                intent.putExtra("tweet", Parcels.wrap(tweet));
                                setResult(RESULT_OK,intent);
                                finish(); // close activity
                            } catch (JSONException e) {
                                Log.e(TAG, "publishTweet: Retrieve tweet fail!",e);
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.e(TAG, "publishTweet: onFailure!",throwable);
                        }
                    });

                }

            }
        });
    }
}