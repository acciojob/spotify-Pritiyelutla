package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap; 
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap;
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
        artistAlbumMap.put(u,new ArrayList<Album>());
        return u;
    }

    public Album createAlbum(String title, String artistName) {
        
        for(Artist a: artists)
        {
            if(a.getName().equals(artistName))
            { 
                Album album = new Album(title);
                albums.add(album);
                artistAlbumMap.get(a).add(album);
                albumSongMap.put(album,new ArrayList<Song>());
                return album;
            }
        }
        Artist a = createArtist(artistName);
        Album album = new Album(title);
        albums.add(album);
        artistAlbumMap.get(a).add(album);
        albumSongMap.put(album,new ArrayList<Song>());
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
                songs.add(song);
                return song;
            }
        }
        throw new Exception("Album does not exist");
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        User userX = null;
        for(User user: users)
        {
            if(user.getMobile().equals(mobile))
            {
                
               userX = user;
               break;

            }
        }
        if(userX == null)
            throw new Exception("User does not exist");

        Playlist playlist = new Playlist(title);
        playlists.add(playlist);
        playlistListenerMap.put(playlist,new ArrayList<User>());
        playlistListenerMap.get(playlist).add(userX);
        creatorPlaylistMap.put(userX,playlist);
        List<Playlist> userPlaylist = userPlaylistMap.get(userX);
        userPlaylist.add(playlist);

        List<Song> playlistSongs = new ArrayList<Song>();

        // Get all songs of length
        for(Song song:songs){
            if(song.getLength() == length){
                playlistSongs.add(song);
            }
        }
        playlistSongMap.put(playlist,playlistSongs);
        return playlist;

    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        User userX = null;
        for(User user: users)
        {
            if(user.getMobile().equals(mobile))
            {
                userX = user;
                break;

            }
        }

        Playlist playlist = new Playlist(title);
        playlists.add(playlist);
        playlistListenerMap.put(playlist,new ArrayList<User>());
        playlistListenerMap.get(playlist).add(userX);
        creatorPlaylistMap.put(userX,playlist);
        List<Playlist> userPlaylist = userPlaylistMap.get(userX);
        userPlaylist.add(playlist);

        List<Song> playlistSongs = new ArrayList<Song>();

        // Get all songs of length
        for(Song song:songs){
            if(songTitles.contains(song.getTitle())){
                playlistSongs.add(song);
            }
        }
        playlistSongMap.put(playlist,playlistSongs);
        return playlist;
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {

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
            throw new Exception("Playlist does not exist");

        User userX = null;
        for(User user: users)
        {
            if(user.getMobile().equals(mobile))
            {
              userX = user;
              break;
            }
        }
        if(userX == null)
            throw new Exception("User does not exist");

        // Check if he is listening
        int index = playlistListenerMap.get(playlistX).indexOf(userX);
        if(index != -1)
            return playlistX;
        
        // Check if he is creator
        if(creatorPlaylistMap.containsKey(userX))
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
        if(userX == null)
            throw new Exception("User does not exist");

        Song songX = null;
        for(Song song: songs)
        {
            if(song.getTitle().equals(songTitle))
            {
              songX = song;
              break;
            }
        }
        if(songX == null)
            throw new Exception("Song does not exist");

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
                        keyArtist.setLikes(keyArtist.getLikes() + 1);
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
            if(maxCount < artist.getLikes()){
                artistName = artist.getName();
                maxCount = artist.getLikes();
            }
        }
        return artistName;
    }

    public String mostPopularSong() {
        /**This API returns the song with the maximum number of likes. */
        String songName = null;
        int maxCount = -1;
        for(Song song: songs){
            if(maxCount < song.getLikes()){
                songName = song.getTitle();
                maxCount = song.getLikes();
            }
        }
        return songName;
    }
}
