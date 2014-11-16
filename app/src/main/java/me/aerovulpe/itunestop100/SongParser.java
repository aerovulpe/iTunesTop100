package me.aerovulpe.itunestop100;

import java.io.StringReader;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.widget.Toast;

public class SongParser {

	public static boolean process(Context context, ArrayList<Song> songs, String xmlData) {
		boolean wasProcessed = true;
		boolean inEntry = false;
		Song current = null;
		String textValue = "";

		if (xmlData == null) {
			wasProcessed = false;
			return wasProcessed;
		}

		songs.clear();

		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser xpp = factory.newPullParser();

			xpp.setInput(new StringReader(xmlData));
			int eventType = xpp.getEventType();

			while (eventType != XmlPullParser.END_DOCUMENT) {
				String tagName = xpp.getName();
				String link = "";

				if (eventType == XmlPullParser.START_DOCUMENT) {
					// Do nothing.
				} else if (eventType == XmlPullParser.END_DOCUMENT) {
					// Do nothing.
				} else if (eventType == XmlPullParser.START_TAG) {
					if (tagName.equalsIgnoreCase("entry")) {
						inEntry = true;
						current = new Song();
					}

					if (inEntry && tagName.equalsIgnoreCase("link")) {
						link = xpp.getAttributeValue(null, "href");
						int attributeCount = xpp.getAttributeCount();
						if (attributeCount == 5) {
							current.setFileLink(link);
						} else if (attributeCount == 3) {
							current.setWebLink(link);
						}
					}
				} else if (eventType == XmlPullParser.TEXT) {
					textValue = xpp.getText();
				} else if (eventType == XmlPullParser.END_TAG) {
					if (inEntry) {
						if (tagName.equalsIgnoreCase("entry")) {
							songs.add(current);
							inEntry = false;
						}

						if (tagName.equalsIgnoreCase("title")) {
							current.setName(textValue);
						} else if (tagName.equalsIgnoreCase("name")) {
							current.setArtist(textValue);
						} else if (tagName.equalsIgnoreCase("releaseDate")) {
							current.setDate(textValue);
						} else if (tagName.equalsIgnoreCase("category")) {
							current.setGenre(xpp.getAttributeValue(null,
									"label"));
						}else if (tagName.equalsIgnoreCase("image")){
							current.setImageLink(textValue);
						}else if (tagName.equalsIgnoreCase("rights")){
							current.setRights(textValue);
						}
					}
				}
				eventType = xpp.next();
			}

		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(context, "Could not update list", Toast.LENGTH_LONG).show();
			wasProcessed = false;
		}

		return wasProcessed;
	}
}
