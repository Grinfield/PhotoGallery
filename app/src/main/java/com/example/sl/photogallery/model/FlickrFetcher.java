package com.example.sl.photogallery.Model;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.googlecode.flickrjandroid.oauth.OAuth;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by sl on 2016/11/10.
 */

public class FlickrFetcher {
    public static final String TAG = "FlickrFetcher";
    private Context mContext;

    //shared preference for saving params
    public static final String PREF_SEARCH_QUERY = "searchQuery";
    public static final String PREF_LAST_RESULT_ID = "lastResultId";

    public static final String API_KEY = "77bd723be1c9570c9df9cb84109eebbe";
    public static final String SECRET_KEY = "724af57ae7331f98";

    //used for search or fetch photo items.
    private static final String ENDPOINT = "https://api.flickr.com/services/rest/";
    private static final String METHOD_GET_RECENT = "flickr.photos.getRecent";
    private static final String METHOD_SEARCH = "flickr.photos.search";
    private static final String METHOD_GET_PHOTOS = "flickr.people.getPhotos";
    private static final String PARAM_EXTRAS = "extras";
    private static final String PARAM_TEXT = "text";
    private static final String PAGE = "page";
    private static final String EXTRA_SMALL_URL = "url_s";
    private static final String XML_PHOTO = "photo";

    public FlickrFetcher(Context context){
        mContext = context;
    }
    public FlickrFetcher(){

    }

    byte [] getUrlBytes(String urlSpec) throws IOException{
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();

        try{
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                return null;
            int bytesRead = 0;
            byte [] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0){
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        }
        finally {
            connection.disconnect();
        }
    }

    public String getUrl(String urlSpec) throws IOException{
        String response = new String(getUrlBytes(urlSpec));
        Log.i(TAG, "response string: " + response);
        return response;
    }

    public ArrayList<GalleryItem> downloadGalleryItems(String url){
        ArrayList<GalleryItem> items = new ArrayList<GalleryItem>();
        try{
            String xmlString = getUrl(url);
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(xmlString));
            Log.i(TAG, "Received xml: " + xmlString);

            parseItems(items, parser);
        }catch (IOException e){
            Log.e(TAG, "Failed to fetch items" + e);
        }catch (XmlPullParserException xppe){
            Log.e(TAG, "Failed to parse items: " + xppe);
        }
        return items;
    }

    public ArrayList<GalleryItem> fetchItems(Integer page){
        String url = Uri.parse(ENDPOINT).buildUpon()
                    .appendQueryParameter("method", METHOD_GET_RECENT)
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter(PARAM_EXTRAS, EXTRA_SMALL_URL)
                    .appendQueryParameter(PAGE, page.toString())
                    .build().toString();
        Log.i(TAG, "fetch items url: " + url);
        return downloadGalleryItems(url);
    }

    public ArrayList<GalleryItem> fetchUserPhotos(OAuth oAuth, Integer page){
        if (oAuth != null && oAuth.getUser().getId() != null){
            String id = oAuth.getUser().getId();
            String url = Uri.parse(ENDPOINT).buildUpon()
                    .appendQueryParameter("method", METHOD_GET_PHOTOS)
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter("user_id", id)
                    .appendQueryParameter("privacy_filter", "completely private photos")
                    .appendQueryParameter(PARAM_EXTRAS, EXTRA_SMALL_URL)
                    .appendQueryParameter(PAGE, page.toString())
                    .build().toString();
            Log.i(TAG, "fetch user items url: " + url);
            return downloadGalleryItems(url);
        }
        return null;
    }

    public ArrayList<GalleryItem> search(String query, Integer page){
        String url = Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("method", METHOD_SEARCH)
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter(PARAM_EXTRAS, EXTRA_SMALL_URL)
                .appendQueryParameter(PARAM_TEXT, query)
                .appendQueryParameter(PAGE, page.toString())
                .build().toString();
        Log.i(TAG, "search items url: " + url);
        return downloadGalleryItems(url);
    }

    public void parseItems(ArrayList<GalleryItem> items, XmlPullParser parser) throws IOException, XmlPullParserException{
        int eventType = parser.next();

        while (eventType != XmlPullParser.END_DOCUMENT){
            if (eventType == XmlPullParser.START_TAG && XML_PHOTO.equals(parser.getName())){
                String id = parser.getAttributeValue(null, "id");
                String caption = parser.getAttributeValue(null, "title");
                String smallUrl = parser.getAttributeValue(null, EXTRA_SMALL_URL);
                String owner = parser.getAttributeValue(null, "owner");
                GalleryItem item = new GalleryItem();
                item.setCaption(caption);
                item.setId(id);
                item.setUrl(smallUrl);
                item.setOwner(owner);
                items.add(item);
            }
            eventType = parser.next();
        }
    }
}
