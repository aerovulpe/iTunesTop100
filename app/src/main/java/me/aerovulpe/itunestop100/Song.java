package me.aerovulpe.itunestop100;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import java.util.List;

public class Song {

	private String name, artist, date, genre, fileLink, imageLink, webLink, rights;
    private boolean selected = false;


	public String getFileLink() {
		return fileLink;
	}

	public void setWebLink(String webLink) {
		this.webLink = webLink;
	}

	public void setFileLink(String fileLink) {
		this.fileLink = fileLink;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getDate() {
		return date;
	}

	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	public String getRights() {
		return rights;
	}

	public void setRights(String rights) {
		this.rights = rights;
	}

	public String getImageLink() {
		return imageLink;
	}

	public void setImageLink(final String imageLink) {
        this.imageLink = imageLink;
	}

	public void setDate(String releaseDate) {
		int separator = releaseDate.indexOf("T");
		date = releaseDate.substring(0, separator);
	}

    public boolean isSelected() {
        return selected;
    }

    public void deSelect(){
        selected = false;
    }

    public void goTo(Context context) {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(webLink));
		PackageManager packageManager = context.getPackageManager();
		int size = packageManager.queryIntentActivities(intent, 0).size();

		if (size > 0)
			context.startActivity(intent);
	}

    public void select(List<Song> songs){
        for (Song song : songs){
            song.deSelect();
        }
        selected = true;
    }

}
