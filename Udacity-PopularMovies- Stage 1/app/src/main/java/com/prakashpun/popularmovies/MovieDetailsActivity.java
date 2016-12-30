package com.prakashpun.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class MovieDetailsActivity extends AppCompatActivity {

    Movies movieDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View rootView = (View) getLayoutInflater().inflate(R.layout.activity_movie_details, null);
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("movieDetails")) {

            movieDetail = (Movies) intent.getParcelableExtra("movieDetails");
            DisplayInfo(rootView);
        }
        setContentView(rootView);
    }

    private void DisplayInfo(View v) {
        TextView movieName = (TextView) v.findViewById(R.id.movieName);
        ImageView thumbnail = (ImageView) v.findViewById(R.id.detailedMoviePoster);
        TextView releaseDate = (TextView) v.findViewById(R.id.releaseDate);
        TextView ratings = (TextView) v.findViewById(R.id.ratingsView);
        TextView overview = (TextView) v.findViewById(R.id.synopsisView);

        movieName.setText(movieDetail.getTitle());
        Picasso.with(getApplicationContext()).load(movieDetail.getPoster()).into(thumbnail);
        releaseDate.setText("Year: " + movieDetail.getReleaseDate());
        ratings.setText("Ratings: " + movieDetail.getVoteAverage() + "/10");
        overview.setText(movieDetail.getOverview());
    }
}
