package com.prakashpun.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MoviesGridFragment extends Fragment {

    private final String STORED_MOVIES = "stored_movies";
    private MovieAdapter movieAdapter;

    private SharedPreferences prefs;
    String sortOrder;

    List<Movies> movies = new ArrayList<Movies>();

    public MoviesGridFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sortOrder = prefs.getString(getString(R.string.preferences_sort_order_key),
                getString(R.string.preferences_sort_default_value));


        if (savedInstanceState != null) {
            ArrayList<Movies> storedMovies = new ArrayList<Movies>();
            storedMovies = savedInstanceState.<Movies>getParcelableArrayList(STORED_MOVIES);
            movies.clear();
            movies.addAll(storedMovies);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<Movies> storedMovies = new ArrayList<Movies>();
        storedMovies.addAll(movies);
        outState.putParcelableArrayList(STORED_MOVIES, storedMovies);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        movieAdapter = new MovieAdapter(
                getActivity(),
                R.layout.grid_item_poster,
                R.id.grid_item_poster_imageview,
                new ArrayList<String>()
        );

        View rootView = inflater.inflate(R.layout.movies_main_fragment, container, false);
        GridView gridView = (GridView) rootView.findViewById(R.id.movies_grid);


        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = rootView.findViewById(R.id.empty_view);
        gridView.setEmptyView(emptyView);

        gridView.setAdapter(movieAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Movies detail = movies.get(position);
                Intent startDetailActivity = new Intent(getActivity(), MovieDetailsActivity.class)
                        .putExtra("movieDetails", detail);
                startActivity(startDetailActivity);
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        // get sort order to see if it has recently changed
        String prefSortOrder = prefs.getString(getString(R.string.preferences_sort_order_key),
                getString(R.string.preferences_sort_default_value));

        if (movies.size() > 0 && prefSortOrder.equals(sortOrder)) {
            updatePosterAdapter();
        } else {
            sortOrder = prefSortOrder;
            if(isNetworkAvailable())
                getMovies();
            else
                Toast.makeText(getContext(), "Application is offline. Please retry", Toast.LENGTH_SHORT).show();
        }
    }

    private void getMovies() {
            FetchMovieDataTask fetchMovieDataTask = new FetchMovieDataTask(getContext(), new AsyncTaskCompleteListener<List<Movies>>() {
                @Override
                public void onTaskComplete(List<Movies> result) {
                    movies.clear();
                    movies.addAll(result);
                    updatePosterAdapter();
                }
            });
            fetchMovieDataTask.execute(sortOrder);

    }

    // updates the ArrayAdapter of poster images
    private void updatePosterAdapter() {
        movieAdapter.clear();
        for (Movies movie : movies) {
            movieAdapter.add(movie.getPoster());
        }
    }

    //To check if network is available
    public boolean isNetworkAvailable() {

        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }
}
