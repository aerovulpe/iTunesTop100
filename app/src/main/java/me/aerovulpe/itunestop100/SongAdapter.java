package me.aerovulpe.itunestop100;

import android.content.Context;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class SongAdapter extends ArrayAdapter<Song> {
	private Context context;
	private int layoutID;
	private ArrayList<Song> songs;

	// Holder object to be recycled.
	private PlaceHolder holder;

	SongAdapter(Context context, int layoutID) {
		super(context, layoutID);
		this.context = context;
		this.layoutID = layoutID;
		this.songs = new ArrayList<Song>();
	}

	@Override
	public Song getItem(int position) {
		return songs.get(position);
	}

	@Override
	public int getCount() {
		return songs.size();
	}

	public ArrayList<Song> getSongs() {
		return songs;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			// Get template element if element not yet created.
			convertView = LayoutInflater.from(context).inflate(layoutID,
					parent, false);
			holder = new PlaceHolder();

			// Subviews via Holder object.
			holder.nameView = (TextView) convertView
					.findViewById(R.id.name_text);
			holder.artistView = (TextView) convertView
					.findViewById(R.id.artist_text);
			holder.dateView = (TextView) convertView
					.findViewById(R.id.date_text);
			holder.genreView = (TextView) convertView
					.findViewById(R.id.genre_text);
			holder.thumbnailView = (ImageView) convertView
					.findViewById(R.id.thumbnail_image);

            holder.nameView.setMovementMethod(new ScrollingMovementMethod());
            holder.artistView.setMovementMethod(new ScrollingMovementMethod());

			// Set OnClickListener
			holder.thumbnailView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					int pos = (Integer) v.getTag();
					String popUp = songs.get(pos).getRights();
					Toast.makeText(context, popUp, Toast.LENGTH_SHORT).show();
				}
			});

			// Set tag to reference Holder object.
			convertView.setTag(holder);
		} else {
			// Get Holder object from existing element
			holder = (PlaceHolder) convertView.getTag();
		}

		// Add tag for position reference.
		holder.thumbnailView.setTag(position);

		// Data
		String nameText = songs.get(position).getName();
		String artistText = songs.get(position).getArtist();
		String dateText = songs.get(position).getDate();
		String genreText = songs.get(position).getGenre();
        String imageLink = songs.get(position).getImageLink();

		// Attach data to element subviews via Holder object.
		holder.nameView.setText(nameText);
		holder.artistView.setText(artistText);
		holder.dateView.setText(dateText);
		holder.genreView.setText(genreText);
        Picasso.with(context).load(imageLink)
                .error(R.drawable.placeholder)
                .placeholder(R.drawable.placeholder)
                .into(holder.thumbnailView);

		return convertView;
	}

	public void update() {
		notifyDataSetChanged();
	}

	static private class PlaceHolder {
		TextView nameView;
		TextView artistView;
		TextView dateView;
		TextView genreView;
		ImageView thumbnailView;
	}

}
