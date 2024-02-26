package me.t3sl4.audiocapture;

public class CurrentlyPlaying {
    // Bu model sınıfı, Spotify'dan dönen JSON yapılarına göre düzenlenmelidir.
    // Örnek olarak basit bir yapı kullanılmıştır.
    public Track item;

    public static class Track {
        public String name;
        // Diğer alanlar eklenebilir
    }
}
