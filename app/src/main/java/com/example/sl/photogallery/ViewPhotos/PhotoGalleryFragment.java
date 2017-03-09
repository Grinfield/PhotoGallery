package com.example.sl.photogallery.ViewPhotos;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SearchView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sl.photogallery.BaseFragment;
import com.example.sl.photogallery.ImageCache.ImageUtil;
import com.example.sl.photogallery.ImageCache.ThumbnailDownloader;
import com.example.sl.photogallery.LoginIn.LoginFragment;
import com.example.sl.photogallery.R;
import com.example.sl.photogallery.Service.PollService;
import com.example.sl.photogallery.Model.FlickrFetcher;
import com.example.sl.photogallery.Model.GalleryItem;
import com.googlecode.flickrjandroid.oauth.OAuth;

import java.io.File;
import java.util.ArrayList;

import static com.example.sl.photogallery.LoginIn.LoginFragment.OAUTH_KEY;

/**
 * Created by sl on 2016/11/10.
 */

public class PhotoGalleryFragment extends BaseFragment {
    private static final String TAG = "PhotoGalleryFragment";

    private GridView mGridView;
    private ProgressBar mLoadingProgressBar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FloatingActionButton mFloatingActionButton;

    ThumbnailDownloader mThumbnailDownloader;
    ArrayList<GalleryItem> mItems;
    SparseBooleanArray mSparseBooleanArray;
    private ArrayAdapter<GalleryItem> mImageAdapter;

    private int current_page = 1;
    private int fetched_page = 0;
    private int scrollPosition = 0;
    private boolean mIsGridViewIdle = true;
    private int mImageWidth;
    private int mImageHeight;
    private boolean mCanGetBitmapFromNetWork = false;
    public boolean mIsPersonal = false;
    private OAuth mOAuth, backupOAuth;

    public static PhotoGalleryFragment getInstance(OAuth oauth){
        PhotoGalleryFragment fragment = new PhotoGalleryFragment();
        if (oauth != null){
            Bundle args = new Bundle();
            args.putSerializable(OAUTH_KEY, oauth);
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onReceiveOauth(OAuth oauth) {
        Log.i(TAG, "receive broadcast: " + oauth);
        if (oauth != null
                && oauth.getUser() != null
                && oauth.getUser().getId() != null){
            String userName = oauth.getUser().getUsername();
            if (userName != null){
                backupOAuth = mOAuth = oauth;
            }
        }else {
            backupOAuth = mOAuth = null;
        }
        updateUI();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate called");
        setRetainInstance(true);
        setHasOptionsMenu(true);

        if (getArguments() != null){
            mOAuth = (OAuth) getArguments().getSerializable(LoginFragment.OAUTH_KEY);
            if (mOAuth != null && mOAuth.getUser().getId() != null){
                backupOAuth = mOAuth;
                mIsPersonal = true;
            }
        }

        mThumbnailDownloader = new ThumbnailDownloader(getActivity());

        DisplayMetrics dm = getResources().getDisplayMetrics();
        mImageWidth = dm.widthPixels / 3;
        mImageHeight = (int) ImageUtil.dp2px(getActivity(), 120);

        if (isWifi(getActivity())){
            mCanGetBitmapFromNetWork = true;
            updateItems();
        }else{
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("You are not in a wifi state, are you sure to connect?");
            builder.setTitle("Notice:");
            builder.setIcon(android.R.drawable.ic_dialog_alert);
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mCanGetBitmapFromNetWork = true;

                    updateItems();
                }
            });
            builder.setNegativeButton("No", null);
            builder.show();
        }
    }

    public boolean isWifi(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo != null
                && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }

    public void updateItems(){
        if (mItems != null){
            mItems.clear();
        }
        current_page = 1;
        fetched_page = 0;
        if (mLoadingProgressBar != null){
            if (mSwipeRefreshLayout == null
                    || (mSwipeRefreshLayout != null && !mSwipeRefreshLayout.isRefreshing())){
                mLoadingProgressBar.setVisibility(View.VISIBLE);
            }
        }
        Log.i(TAG, "update items called");
        Log.i(TAG, "current fragment id: " + PhotoGalleryFragment.this.hashCode());
        new FetchItemsTask().execute(current_page);
    }

    public void updateUI(){
        if (mFloatingActionButton != null){
            if ( backupOAuth == null){
                mFloatingActionButton.hide();
            }else {
                mFloatingActionButton.show();
                if (mIsPersonal){
                    mFloatingActionButton.setImageResource(R.drawable.earth_64px);
                }else
                    mFloatingActionButton.setImageResource(R.drawable.personal_icon);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView called");
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        mItems = new ArrayList<GalleryItem>();
        mSwipeRefreshLayout = (SwipeRefreshLayout)v.findViewById(R.id.swipe_refresh);
        mGridView = (GridView)v.findViewById(R.id.gridView);
        mLoadingProgressBar = (ProgressBar)v.findViewById(R.id.fetching_photos_bar);
        mFloatingActionButton = (FloatingActionButton)v.findViewById(R.id.floatingActionButton);
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (backupOAuth != null){
                    if (mIsPersonal){
                        mOAuth = null;
                        mIsPersonal = false;
                    }else {
                        mOAuth = backupOAuth;
                        mIsPersonal = true;
                    }
                }
                updateItems();
            }
        });

        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_green_light,
                                android.R.color.holo_blue_light,
                                android.R.color.holo_red_light,
                                android.R.color.holo_orange_light);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mLoadingProgressBar.setVisibility(View.INVISIBLE);
                updateItems();
            }
        });

        mSwipeRefreshLayout.setOnChildScrollUpCallback(new SwipeRefreshLayout.OnChildScrollUpCallback() {
            @Override
            public boolean canChildScrollUp(SwipeRefreshLayout parent, @Nullable View child) {
                if (child instanceof ViewGroup){
                    ViewGroup viewGroup = (ViewGroup)child;
                    AdapterView gridView = (AdapterView)viewGroup.getChildAt(0);
                    if (gridView != null && gridView.getChildCount() > 0){
                        return gridView.getFirstVisiblePosition() > 0 ||
                                gridView.getChildAt(0).getTop() < gridView.getPaddingTop();
                    }
                }
                return false;
            }
        });


        mGridView.setEmptyView(mLoadingProgressBar);
        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    mIsGridViewIdle = true;
                    mImageAdapter.notifyDataSetChanged();
                } else {
                    mIsGridViewIdle = false;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem + visibleItemCount == totalItemCount
                        && current_page == fetched_page
                        && visibleItemCount < totalItemCount){
                    scrollPosition = firstVisibleItem + 3;
                    new FetchItemsTask().execute(++current_page);
                }
            }
        });

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GalleryItem item = mItems.get(position);
                Uri photoPageUri = Uri.parse(item.getPhotoPageUrl());
                Intent i = new Intent(getActivity(), PhotoPageActivity.class);
                i.setData(photoPageUri);
                startActivity(i);
            }
        });

        mGridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
        mGridView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            View customActionBar;
            TextView selectedPhotoCount;
            Intent mIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            ArrayList<Uri> mImageUris = new ArrayList<Uri>();

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                GalleryItem item = mItems.get(position);
                if (mImageAdapter != null){
                    mSparseBooleanArray = mGridView.getCheckedItemPositions();
                    selectedPhotoCount.setText(getString(R.string.photo_selected_number,
                            "" + mGridView.getCheckedItemCount()));
                    mode.invalidate();
                    mImageAdapter.notifyDataSetChanged();
                    Log.i(TAG, "position:" + position);
                }
                if (checked){
                    if (mSparseBooleanArray.size() > 5 ){
                        Toast.makeText(getActivity(),
                                "Notice:no more than 5 items, please cancel one and reselect.",
                                Toast.LENGTH_SHORT).show();
                        mGridView.setItemChecked(position, false);
                    }
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.photo_share, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                if (customActionBar == null){
                    customActionBar = getActivity().getLayoutInflater().
                            inflate(R.layout.actionbar_custom, null);
                    selectedPhotoCount = (TextView)customActionBar.findViewById(R.id.selected_count);
                }
                mode.setCustomView(customActionBar);
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, final MenuItem item) {
                switch (item.getItemId()){
                    case R.id.menu_share_photos:
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                for (int i = 0; i < mSparseBooleanArray.size(); i++){
                                    if (mSparseBooleanArray.valueAt(i)){
                                        mThumbnailDownloader.downloadBitmapToExternalDirectoryFromUrl
                                                (mItems.get(i).getUrl(), mImageUris);
                                    }
                                }
                            }
                        });
                        thread.start();

                        ShareActionProvider mShareAction = new ShareActionProvider(getActivity());
                        //ShareActionProvider mShareAction = (ShareActionProvider) item.getActionProvider();
                        item.setActionProvider(mShareAction);
                        if (mShareAction != null){
                            mIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, mImageUris);
                            mIntent.setType("image/*");
                            mShareAction.setShareIntent(mIntent);
                        }

                        return true;
                    case R.id.menu_deselect_photos:
                        mSparseBooleanArray.clear();
                        mImageAdapter.notifyDataSetChanged();
                        mode.finish();

                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }
        });

        setupAdapter();
        updateUI();
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_photo_gallery, menu);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            //pull out the SearchView
            MenuItem searchItem = menu.findItem(R.id.menu_item_search);
            SearchView searchView = (SearchView)searchItem.getActionView();

            //get the data from our searchable.xml as a searchableInfo
            SearchManager searchManager = (SearchManager)getActivity().
                    getSystemService(Context.SEARCH_SERVICE);
            ComponentName name = getActivity().getComponentName();
            SearchableInfo searchableInfo = searchManager.getSearchableInfo(name);

            searchView.setSearchableInfo(searchableInfo);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.menu_item_search:
                if (mIsPersonal){
                    mIsPersonal = false;
                }
                getActivity().onSearchRequested();
                return true;
            case R.id.menu_item_clear:
                PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .edit().putString(FlickrFetcher.PREF_SEARCH_QUERY, null)
                        .commit();
                updateItems();
                mLoadingProgressBar.setVisibility(View.VISIBLE);
                return true;
            case R.id.menu_item_toggle_polling:
                boolean shouldStartAlarm = !PollService.isServiceAlarmOn(getActivity());
                PollService.setServiceAlarm(getActivity(), shouldStartAlarm);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem toggleItem = menu.findItem(R.id.menu_item_toggle_polling);
        if (PollService.isServiceAlarmOn(getActivity())){
            toggleItem.setTitle(R.string.stop_polling);
        }else {
            toggleItem.setTitle(R.string.start_polling);
        }
        Log.i(TAG, "Service is " + PollService.isServiceAlarmOn(getActivity()));
    }

    void setupAdapter(){
        if (getActivity() == null || mGridView == null) return;

        if (mItems != null){
            //mGridView.setAdapter(new ArrayAdapter<GalleryItem>(getActivity(), android.R.layout.simple_gallery_item, mItems));
            mImageAdapter = new GalleryItemAdapter(mItems);
            mGridView.setAdapter(mImageAdapter);
            mGridView.setSelection(scrollPosition);
        }else {
            mGridView.setAdapter(null);
        }
    }

    private class GalleryItemAdapter extends ArrayAdapter<GalleryItem>{
        SparseBooleanArray mSparseBooleanArray;

        public GalleryItemAdapter(ArrayList<GalleryItem> items){
            super(getActivity(), 0, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            GridView mGridView = (GridView)parent;
            mSparseBooleanArray = mGridView.getCheckedItemPositions();

            ViewHolder holder = null;
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.gallery_item, parent, false);
                holder = new ViewHolder();
                holder.imageView = (ImageView) convertView.findViewById(R.id.gallery_item_imageView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            ImageView imageView = holder.imageView;
            final GalleryItem tag = (GalleryItem) imageView.getTag();
            final GalleryItem item = getItem(position);

            if (!item.equals(tag)) {
                imageView.setBackgroundResource(R.drawable.image_default);
            }
            //loading image as gridView is not scrolling
            if (mIsGridViewIdle && mCanGetBitmapFromNetWork) {
                imageView.setTag(item);
                mThumbnailDownloader.bindBitmap(item.getUrl(), imageView, mImageWidth, mImageHeight);
                if (mSparseBooleanArray.get(position)){
                    imageView.setImageResource(R.drawable.border);
                }else {
                    imageView.setImageResource(android.R.color.transparent);
                }
            }
            return convertView;
        }
    }

    private static class ViewHolder {
        public ImageView imageView;
    }

    private class FetchItemsTask extends AsyncTask<Integer, Void, ArrayList<GalleryItem>>{

        @Override
        protected ArrayList<GalleryItem> doInBackground(Integer... params) {
            Activity activity = getActivity();

            if (activity == null){
                return new ArrayList<GalleryItem>();
            }

            String query = PreferenceManager.getDefaultSharedPreferences(activity).getString(FlickrFetcher.PREF_SEARCH_QUERY, null);

            if (mOAuth != null
                    && mOAuth.getUser().getId() != null
                    && mIsPersonal){
                Log.i(TAG, "received oauth: " + mOAuth);
                return new FlickrFetcher().fetchUserPhotos(mOAuth, params[0]);
            }else if (query != null){
                 Log.i(TAG, "received a query: " + query);
                 return new FlickrFetcher().search(query, params [0]);
             }else{
                Log.i(TAG, "no extra params: universal search");
                Log.i(TAG, "current page" + current_page);
                 return new FlickrFetcher().fetchItems(params [0]);
             }
        }

        @Override
        protected void onPostExecute(ArrayList<GalleryItem> galleryItems) {
            if (mItems != null){
                mItems.addAll(galleryItems);
            }else {
                mItems = galleryItems;
            }

            updateUI();
            mLoadingProgressBar.setVisibility(View.INVISIBLE);
            mSwipeRefreshLayout.setRefreshing(false);
            setupAdapter();
            fetched_page++;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i(TAG, "onDestroyView called");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy called");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.i(TAG, "onDetach called");
    }
}
