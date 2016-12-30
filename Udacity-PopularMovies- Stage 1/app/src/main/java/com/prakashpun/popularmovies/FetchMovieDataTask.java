package com.prakashpun.popularmovies;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by prakash_pun on 12/7/2016.
 * <p>
 * Async class for fetching the movie details
 * <p>
 * To separate the Async class from the activity, created an interface to implement a callback
 * Followed information found at:
 * http://www.jameselsey.co.uk/blogs/techblog/extracting-out-your-asynctasks-into-separate-classes-makes-your-code-cleaner/
 */


public class FetchMovieDataTask extends AsyncTask<String, Void, List<Movies>> {

    private final String LOG_TAG = FetchMovieDataTask.class.getSimpleName();
    private final String API_KEY = "";
    private final String MOVIE_POSTER_URL = "http://image.tmdb.org/t/p/";
    private final String MOVIE_POSTER_SIZE = "w185";

    private Context context;
    private AsyncTaskCompleteListener<List<Movies>> listener;


    public FetchMovieDataTask(Context context, AsyncTaskCompleteListener<List<Movies>> listener) {
        this.context = context;
        this.listener = listener;
    }

    //Get only year from the Year of Movie release
    private String getYear(String date) {
        final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        final Calendar cal = Calendar.getInstance();
        try {
            cal.setTime(df.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return Integer.toString(cal.get(Calendar.YEAR));
    }

    //Extract movie details from JSON array and create Movies Objects
    private List<Movies> extractMovieData(String moviesJsonStr) throws JSONException {

        final String MOVIES_RESULT = "results";
        final String MOVIE_TITLE = "original_title";
        final String MOVIE_POSTER_PATH = "poster_path";
        final String MOVIE_OVERVIEW = "overview";
        final String MOVIE_VOTE_AVERAGE = "vote_average";
        final String MOVIE_RELEASE_DATE = "release_date";

        JSONObject moviesJson = new JSONObject(moviesJsonStr);
        JSONArray moviesArray = moviesJson.getJSONArray(MOVIES_RESULT);
        int moviesLength = moviesArray.length();
        List<Movies> movies = new ArrayList<Movies>();

        for (int i = 0; i < moviesLength; ++i) {

            JSONObject movie = moviesArray.getJSONObject(i);
            String title = movie.getString(MOVIE_TITLE);
            String poster = MOVIE_POSTER_URL + MOVIE_POSTER_SIZE + movie.getString(MOVIE_POSTER_PATH);
            String overview = movie.getString(MOVIE_OVERVIEW);
            String voteAverage = movie.getString(MOVIE_VOTE_AVERAGE);
            String releaseDate = getYear(movie.getString(MOVIE_RELEASE_DATE));

            movies.add(new Movies(title, poster, overview, voteAverage, releaseDate));

        }

        return movies;

    }

    //Do JSON parsing and HTTP calls on background thread
    @Override
    protected List<Movies> doInBackground(String... params) {
        if (params.length == 0) {
            return null;
        }

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String moviesJsonStr = null;

        try {

            final String BASE_URL = "http://api.themoviedb.org/3/movie/";
            final String KEY = "api_key";
            String sortBy = params[0];

            Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                    .appendPath(sortBy)
                    .appendQueryParameter(KEY, API_KEY)
                    .build();

            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                return null;
            }
            moviesJsonStr = buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        try {
            return extractMovieData(moviesJsonStr);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(List<Movies> movies) {
        super.onPostExecute(movies);
        listener.onTaskComplete(movies);
    }
}
