package me.aerovulpe.itunestop100;

import java.io.InputStream;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

public class Song {

	private String name, artist, date, genre, fileLink, webLink, rights;

	private Bitmap thumbnail;

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

	public Bitmap getThumbnail() {
		return thumbnail;
	}

	public void setImageLink(final String imageLink) {

		Runnable downloadImage = new Runnable() {

			@Override
			public void run() {
				Bitmap mbitmap = null;
				try {
					InputStream in = new java.net.URL(imageLink).openStream();
					mbitmap = BitmapFactory.decodeStream(in);
				} catch (Exception e) {
					Log.e("Error", e.getMessage());
					e.printStackTrace();
				}
				thumbnail = mbitmap;
			}

		};

		Thread downloadImageThread = new Thread(downloadImage);
		downloadImageThread.start();
	}

	public void setDate(String releaseDate) {
		int separator = releaseDate.indexOf("T");
		date = releaseDate.substring(0, separator);
	}

	public void goTo(Context context) {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(webLink));
		PackageManager packageManager = context.getPackageManager();
		int size = packageManager.queryIntentActivities(intent, 0).size();

		if (size > 0)
			context.startActivity(intent);
	}
}
