package me.aerovulpe.itunestop100;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {

	private String xmlData;
	private SongAdapter adapter;

	private static MediaPlayer player;

	private static final String COUNT_KEY = "me.aerovulpe.itunestop100.MainActivity.INT_KEY";
	private static final String XML_FILE = "me.aerovulpe.itunestop100.MainActivity.XML_FILE";

	private static final String TOP_100_URL = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topsongs/limit=100/xml";

	ListView songList;

	private int count;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		songList = (ListView) findViewById(R.id.songs_list);
        songList.setItemsCanFocus(true);
		adapter = (SongAdapter) getLastNonConfigurationInstance();

		if (adapter == null) {
			adapter = new SongAdapter(MainActivity.this,
					R.layout.song_row);
			player = new MediaPlayer();
		}

		songList.setAdapter(adapter);
		songList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Song song = adapter.getItem(position);
				play(song);
                song.select(adapter.getSongs());
                adapter.update();
			}
		});
		songList.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				adapter.getItem(position).goTo(view.getContext());
				return true;
			}
		});

		player.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				mp.reset();
			}
		});

		SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
		count = sharedPreferences.getInt(COUNT_KEY, 0);
		if (count == 0) {
			new DownloadTask().execute(TOP_100_URL);
		} else {
			update();
		}
		count++;
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putInt(COUNT_KEY, count);
		editor.commit();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (isFinishing() && player != null)
			player.release();
	}

	@Override
	protected void onResume() {
		super.onResume();
		xmlData = getXMLFile();
		update();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (xmlData != null)
			saveXMLFile(xmlData);
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		// Save the SongAdapter instance, because we know the Activity will be
		// immediately recreated.
		SongAdapter adapt = adapter;
		adapter = null; // Prevent onDestroy() from releasing adapter instance
		return adapt;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("Refresh").setOnMenuItemClickListener(
				new OnMenuItemClickListener() {

					@Override
					public boolean onMenuItemClick(MenuItem item) {
						new DownloadTask().execute(TOP_100_URL);
						return true;
					}

				});
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_stop) {
			if (player.isPlaying()) {
				player.stop();
				player.reset();
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void play(Song song) {
		AudioManager audiomanager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		if (audiomanager.isMusicActive())
			return;

		try {
			player.setAudioStreamType(AudioManager.STREAM_MUSIC);
			player.setDataSource(song.getFileLink());
			player.prepare();
			player.start();
		} catch (Exception e) {
			Toast.makeText(this, "Could not play song preview",
					Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
	}

	private void update() {
		if (adapter == null)
			return;

		if (SongParser.process(this, adapter.getSongs(), xmlData)) {
			adapter.update();
		}

	}

	// Reading and Writing

	public String getXMLFile() {
		String content = null;
		FileInputStream fileInputStream = null;

		try {
			fileInputStream = openFileInput(XML_FILE);
			int size = fileInputStream.available();
			byte[] bytes = new byte[size];
			fileInputStream.read(bytes);
			fileInputStream.close();
			content = new String(bytes, "UTF-8");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return content;

	}

	public void saveXMLFile(String content) {

		FileOutputStream fileOutputStream = null;

		try {
			fileOutputStream = openFileOutput(XML_FILE, Context.MODE_PRIVATE);
			byte[] bytes = content.getBytes();
			fileOutputStream.write(bytes);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fileOutputStream != null) {
				try {
					fileOutputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	private class DownloadTask extends AsyncTask<String, Void, String> {

		private static final String DOWNLOAD_FAILED = "Unable to download Top 10 Apps file.";
		private boolean downloadSuccessful = true;

		String mXMLData;
		ProgressDialog progDialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progDialog = new ProgressDialog(MainActivity.this);
			progDialog.setMessage("Loading...");
			progDialog.setIndeterminate(false);
			progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progDialog.setCancelable(true);
			progDialog.show();
		}

		@Override
		protected String doInBackground(String... urls) {
			try {
				mXMLData = downloadXML(urls[0]);
			} catch (IOException e) {
				e.printStackTrace();
				downloadSuccessful = false;
				return DOWNLOAD_FAILED;
			}

			return mXMLData;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			if (progDialog != null) {
				progDialog.dismiss();
			}

			if (downloadSuccessful) {
				xmlData = result;
				update();
			} else {
				Toast.makeText(MainActivity.this, DOWNLOAD_FAILED,
						Toast.LENGTH_SHORT).show();
			}
		}

		private String downloadXML(String urlString) throws IOException {
			final int BUFFER_SIZE = 2000;
			InputStream inputStream = null;

			String xmlContents = "";

			try {
				URL url = new URL(urlString);
				HttpURLConnection connection = (HttpURLConnection) url
						.openConnection();
				connection.setReadTimeout(10000);
				connection.setConnectTimeout(15000);
				connection.setRequestMethod("GET");
				connection.setDoInput(true);
				int responseCode = connection.getResponseCode();
				Log.d("DOWNLOADTASK RESPONSE", String.valueOf(responseCode));
				inputStream = connection.getInputStream();

				InputStreamReader inputreader = new InputStreamReader(
						inputStream);

				int charRead;

				char[] inputBuffer = new char[BUFFER_SIZE];

				try {
					while ((charRead = inputreader.read(inputBuffer)) > 0) {
						String readString = String.copyValueOf(inputBuffer, 0,
								charRead);
						xmlContents += readString;
						inputBuffer = new char[BUFFER_SIZE];
					}

					return xmlContents;
				} catch (IOException e) {
					e.printStackTrace();
					Toast.makeText(MainActivity.this, DOWNLOAD_FAILED,
							Toast.LENGTH_LONG).show();
					downloadSuccessful = false;
					return DOWNLOAD_FAILED;
				}

			} finally {
				if (inputStream != null)
					inputStream.close();
			}
		}

	}
}
