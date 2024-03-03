package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap; 
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap; // why this i am not getting below we already have list
    public HashMap<User, List<Playlist>> userPlaylistMap;
    public HashMap<Song, List<User>> songLikeMap;

    public List<User> users;
    public List<Song> songs;
    public List<Playlist> playlists;
    public List<Album> albums;
    public List<Artist> artists;

    public SpotifyRepository(){ 
        //To avoid hitting apis multiple times, initialize all the hashmaps here with some dummy data
        artistAlbumMap = new HashMap<>();
        albumSongMap = new HashMap<>();
        playlistSongMap = new HashMap<>();
        playlistListenerMap = new HashMap<>();
        creatorPlaylistMap = new HashMap<>();
        userPlaylistMap = new HashMap<>();
        songLikeMap = new HashMap<>();

        users = new ArrayList<>();
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();
    }

    public User createUser(String name, String mobile) {
        User u = new User(name,mobile);
        users.add(u);
        userPlaylistMap.put(u,new ArrayList<Playlist>());
        return u;
    }

    public Artist createArtist(String name) {
        Artist u = new Artist(name);
        artists.add(u);
        artistAlbumMap.put(u,new List<Album>());
        return u;
    }

    public Album createAlbum(String title, String artistName) {
        
        for(Artist a: artists)
        {
            if(a.getName().equals(artistName))
            {
                Album album = new Album(title, artistName);
                albums.add(album);
                artistAlbumMap.get(a).add(album);
                albumSongMap.put(album,new List<Song>());
                return album;
            }
        }
        Artist a = createArtist(artistName);
        Album album = new Album(title, artistName);
        albums.add(album);
        artistAlbumMap.get(a).add(album);
        albumSongMap.put(album,new List<Song>());
        return album;
    }

    public Song createSong(String title, String albumName, int length) throws Exception{
        for(Album alb: albums)
        {
            if(alb.getTitle().equals(albumName))
            {
                Song song = new Song(title,length);
                albumSongMap.get(alb).add(song);
                songLikeMap.put(song,new ArrayList<User>());
                return song;
            }
        }
        throw new Exception("Album not found");
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        for(User user: users)
        {
            if(user.getMobile().equals(mobile))
            {
                
                Playlist playlist = new Playlist(title);
                playlists.add(playlist);
                List<Playlist> userPlaylist = userPlaylistMap.get(user);
                userPlaylist.add(playlist);

                List<Song> playlistSongs = new ArrayList<Song>();

                // Get all songs of length
                for(Song song:songs){
                    if(song.length == length){
                        playlistSongs.add(song);
                    }
                }
                playlistSongMap.put(playlist,playlistSongs);

            }
        }
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        // Do this same as above ok
        for(User user: users)
        {
            if(user.getMobile().equals(mobile))
            {
                Playlist playlist = new Playlist(title);
                playlists.add(playlist);
                List<Playlist> userPlaylist = userPlaylistMap.get(user);
                userPlaylist.add(playlist);
                // start from here below \/
                List<Song> playlistSongs = new ArrayList<Song>();

                // Get all songs of length
                for(Song song:songs){
                    if(songTitles.contains(song.title)){ 
                        playlistSongs.add(song);
                    }
                }
                playlistSongMap.put(playlist,playlistSongs);

            }
        }
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        /*Find playlist: This API finds a playlist with a given title and adds the given user as a listener to that playlist. If the user is the creator or already a listener, it does nothing. */
        /// 1. find playlist object

        Playlist playlistX = null;
        for(Playlist playlist: playlists)
        {
            if(playlist.getTitle().equals(playlistTitle))
            {
              playlistX = playlist;
              break;
            }
        }
        if(playlistX == null)
            throw new Exception("Playlist not found");

        User userX = null;
        for(User user: users)
        {
            if(user.getMobile().equals(mobile))
            {
              userX = user;
              break;
            }
        }

        // Check if he is listening
        int index = playlistListenerMap.get(playlistX).indexOf(userX);
        if(index != -1)
            return playlistX;
        
        // Check if he creator
        if(creatorPlaylistMap.contains(userX))
            return playlistX;
        
        // add as listener
        playlistListenerMap.get(playlistX).add(userX);
        return playlistX;
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        /*This API allows a user to like a given song, which also auto-likes the corresponding artist. If the user has already liked the song, it does nothing. */

        User userX = null;
        for(User user: users)
        {
            if(user.getMobile().equals(mobile))
            {
              userX = user;
              break;
            }
        }

        Song songX = null;
        for(Song song: songs)
        {
            if(song.getTitle().equals(songTitle))
            {
              songX = song;
              break;
            }
        }

        // Check if already liked
        int index = songLikeMap.get(songX).indexOf(userX);
        if(index == -1){
            songLikeMap.get(songX).add(userX);
            songX.setLikes(songLikeMap.get(songX).size());
            likeArtist(songX);
        }

        return songX;


    }

    public void likeArtist(Song song){
        for (Map.Entry<Album, List<Song>> entry : albumSongMap.entrySet()) {
            Album keyAlb = entry.getKey();
            int index = entry.getValue().indexOf(song);
            if(index != -1){
                for (Map.Entry<Artist, List<Album>> entryX : artistAlbumMap.entrySet()) {
                    Artist keyArtist = entryX.getKey();
                    int indexX = entryX.getValue().indexOf(keyAlb);
                    if(indexX != -1){
                        keyArtist.setLikes(.getLikes() + 1);
                        break;
                    }                    
                }
                break;
            }
        }
    }

    public String mostPopularArtist() {
        String artistName = null;
        int maxCount = -1;
        for(Artist artist: artists){
            if(maxCount < entry.getValue().size()){
                artistName = artist.getName();
                maxCount = artist.getLikes();
            }
        }
        return songName;
    }

    public String mostPopularSong() {
        /**This API returns the song with the maximum number of likes. */
        String songName = null;
        int maxCount = -1;
        for(Song song: songs){
            if(maxCount < entry.getValue().size()){
                songName = song.getTitle();
                maxCount = song.getLikes();
            }
        }
        return songName;
    }
}
