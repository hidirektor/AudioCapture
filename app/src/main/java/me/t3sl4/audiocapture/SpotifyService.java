package me.t3sl4.audiocapture;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

interface SpotifyService {
    @GET("v1/me/player/currently-playing")
    Call<CurrentlyPlaying> getCurrentPlayingTrack(@Header("Authorization") String authToken);
}
