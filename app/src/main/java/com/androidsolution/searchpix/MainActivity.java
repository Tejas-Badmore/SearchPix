package com.androidsolution.searchpix;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.Set;

import com.androidsolution.searchpix.adapters.PixabayImageListAdapter;
import com.androidsolution.searchpix.api_services.InternetCheck;
import com.androidsolution.searchpix.api_services.PixabayService;
import com.androidsolution.searchpix.listeners.InfiniteScrollListener;
import com.androidsolution.searchpix.models.*;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.mancj.materialsearchbar.MaterialSearchBar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements MaterialSearchBar.OnSearchActionListener, NavigationView.OnNavigationItemSelectedListener{

    private List<PixabayImage> pixabayImageList;
    private PixabayImageListAdapter pixabayImageListAdapter;
    private InfiniteScrollListener infiniteScrollListener;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private TextView noResults,welcomeText;
    private String currentQuery = "";
    private NavigationView navigationView;
    private MaterialSearchBar searchBar;
    private RelativeLayout lowerRL;
    private GridLayoutManager mLayoutManager;
    SharedPreferences sharedPreferences;
    private DrawerLayout drawer;
    private static final int TIME_INTERVAL = 1000;
    private long mBackPressed;
    private Set<String> searchSet;
    private  int itemId=0;
    private Queue<String> q_search = new LinkedList<String>();
    private final int REQ_CODE_SPEECH_INPUT = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();                    //initialise all the views in this activity
        initRecyclerView(2);        //setting grid layout Column
        setupSharedPreference();        //getting shared preferences from memory
        addSearchSetToNavMenu();        //adding search history to navigation menu

        if (!InternetCheck.isInternetAvailable(this))           //checking internet connection
            initSnackbar(R.string.no_internet);
    }

    private void initViews() {
        lowerRL = (RelativeLayout) findViewById(R.id.lowerRL);

        //navigation view initialising
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        drawer = findViewById(R.id.drawer_layout);

        //search bar initialising
        searchBar = (MaterialSearchBar) findViewById(R.id.searchBar);
        searchBar.setOnSearchActionListener(this);
        setupSearchMenuBar();               // setting up three dots in search bar and listner

        recyclerView = (RecyclerView) findViewById(R.id.activity_main_list);
        progressBar = (ProgressBar) findViewById(R.id.activity_main_progress);
        setProgressBar(false);

        //display text
        noResults = (TextView) findViewById(R.id.no_results_text);
        welcomeText = (TextView) findViewById(R.id.welcome_text);
    }

    private void initRecyclerView(int col) {            //setting grid layout Column and recycler view

        recyclerView.setHasFixedSize(true);
        mLayoutManager = new GridLayoutManager(this, col);
        recyclerView.setLayoutManager(mLayoutManager);
        pixabayImageList = new ArrayList<>();
        pixabayImageListAdapter = new PixabayImageListAdapter(pixabayImageList);
        recyclerView.setAdapter(pixabayImageListAdapter);
        initInfiniteScrollListener(mLayoutManager);     //adding infinite scroller Listner to recycler view layout

    }

    private void setupSharedPreference() {          //getting search history and last query fired

        sharedPreferences = getSharedPreferences("SEARCH_HISTORY", MODE_PRIVATE);

        //retrieving the values
        String lastQuery = sharedPreferences.getString("LastQuery", "");
        searchSet = sharedPreferences.getStringSet("SearchSet", null);


        if(searchSet!=null)
            q_search.addAll(searchSet);         //populating Queue with search set
        else
            searchSet = new HashSet<String>();  //if search set is null:-> initialising new searchSet

        if(!lastQuery.equals("")) {             //executing last fired query
            setProgressBar(true);
            loadImages(1, lastQuery);
            welcomeText.setVisibility(View.GONE);
        }
        else
            welcomeText.setVisibility(View.VISIBLE);        //display Welcome Text for the first time

    }

    private void addSearchSetToNavMenu() {          //adding Search History queries into Navigation Menu
        Menu menu = navigationView.getMenu();
        if(!q_search.isEmpty()){
            for(String str: q_search){              //adding all the queries into Navigation Menu
                menu.add(0,itemId,1,str);
                MenuItem item = menu.getItem(itemId);
                SpannableString s = new SpannableString(item.getTitle());
                s.setSpan(new RelativeSizeSpan(1.5f),0,s.length(),0);
                item.setTitle(s);
                itemId++;
            }
        }
        else        //if nothing is in Search History, display msg
        {
            menu.add(10,itemId,1,"Nothing to display");
            MenuItem item = menu.getItem(0);
            SpannableString s = new SpannableString(item.getTitle()); //get text from our menu item.
            s.setSpan(new RelativeSizeSpan(1.2f),0,s.length(),0); //here is where we are actually setting the size with a float (proportion).
            item.setTitle(s);
        }
    }

    private void loadImages(int page, String query) {   //loading images of query

        //Accessing PixabayServices using API key
        PixabayService.createPixabayService().getImageResults(getString(R.string.PIXABAY_API_KEY),
                query, page, 20).enqueue(new Callback<PixabayImageList>() {
            @Override
            public void onResponse(Call<PixabayImageList> call, Response<PixabayImageList> response) {
                if (response.isSuccessful())
                    addImagesToList(response.body());       //if successful in getting image, add to image list
                else
                    setProgressBar(false);
            }

            @Override
            public void onFailure(Call<PixabayImageList> call, Throwable t) {
                initSnackbar(R.string.error);   // on failure in getting images, display msg
            }
        });
    }

    private void addImagesToList(PixabayImageList response) {
        setProgressBar(false);
        int position = pixabayImageList.size();
        pixabayImageList.addAll(response.getHits());
        pixabayImageListAdapter.notifyItemRangeInserted(position, position + 20);   //appending new images

        if (pixabayImageList.isEmpty())                 //if list is empty, display msg "No result"
            noResults.setVisibility(View.VISIBLE);
        else
            noResults.setVisibility(View.GONE);
    }

    @Override
    public void onSearchConfirmed(CharSequence text) {      //on firing query

        searchBar.closeSearch();                        //closing search bar
        currentQuery = text.toString();                 //getting fired query

        processQuery(currentQuery);         //processing the fired query
    }

    private void processQuery(String currentQuery) {     //get query fired images
        welcomeText.setVisibility(View.GONE);
        resetImageList();                               //reset recycler view
        setProgressBar(true);                           //starting progress bar
        noResults.setVisibility(View.GONE);

        navMenuAddingSystem(currentQuery);          //adding query to Search History

        enterLastQuery(currentQuery);                           //storing query as a last query
        loadImages(1, currentQuery);          //loading images of fired query
    }

    private void navMenuAddingSystem(String currentQuery) {
        if(!searchSet.contains(currentQuery)){
            Menu menu = navigationView.getMenu();
            if(searchSet.size()==0)
                menu.removeGroup(10);
            if(searchSet.size()==10){
                String tempStr = q_search.poll();
                searchSet.remove(tempStr);
                menu.removeItem(itemId-10);
            }
            searchSet.add(currentQuery);
            q_search.add(currentQuery);
            menu.add(0,itemId,1,currentQuery);
            MenuItem item = menu.getItem(q_search.size()-1);
            SpannableString s = new SpannableString(item.getTitle());
            s.setSpan(new RelativeSizeSpan(1.5f),0,s.length(),0);
            item.setTitle(s);
            itemId++;
        }
    }

    @Override
    public void onButtonClicked(int buttonCode) {
        switch (buttonCode){
            case MaterialSearchBar.BUTTON_NAVIGATION:   //show Navigation Menu
                setDrawerView(true);
                break;
            case MaterialSearchBar.BUTTON_SPEECH:       //starting Speech Recognition
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Say something" );
                try {
                    startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
                } catch (ActivityNotFoundException a) {
                    Toast.makeText(getApplicationContext(),"Speech not supported",Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {   //getting query from Search History
        if(item.getGroupId()==1)            // ignore selecting display msg in navigation menu
            return true;

//        int id = item.getItemId();          //get selected menu item id
        currentQuery = item.getTitle().toString();
        processQuery(currentQuery);

        setDrawerView(false);
        return true;
    }

    @Override
    public void onSearchStateChanged(boolean enabled) {     //change color of the background
        if(enabled)
            lowerRL.setBackgroundColor(getResources().getColor(R.color.transparentBlack));
        else
            lowerRL.setBackgroundColor(getResources().getColor(R.color.originalWhite));
    }

    private void initSnackbar(int messageId) {
        setProgressBar(false);
        Snackbar snackbar = Snackbar.make(recyclerView, messageId, Snackbar.LENGTH_INDEFINITE).setAction(R.string.retry, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (InternetCheck.isInternetAvailable(v.getContext())) {
                    resetImageList();
                    setProgressBar(true);
                    loadImages(1, currentQuery);
                } else initSnackbar(R.string.no_internet);
            }
        });
        snackbar.show();
    }

    private void setupSearchMenuBar() {                 //setting up three dots in search bar
        searchBar.inflateMenu(R.menu.grid_size_menu);
        searchBar.getMenu().setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) { //setting grid layout column
                switch (item.getItemId()){
                    case R.id.grid_size2:
                        mLayoutManager.setSpanCount(2);
                        break;
                    case R.id.grid_size3:
                        mLayoutManager.setSpanCount(3);
                        break;
                    case R.id.grid_size4:
                        mLayoutManager.setSpanCount(4);
                }
                recyclerView.setLayoutManager(mLayoutManager);
                return true;
            }
        });
    }

    private void initInfiniteScrollListener(LinearLayoutManager mLayoutManager) {   //infinite scroller listener
        infiniteScrollListener = new InfiniteScrollListener(mLayoutManager) {
            @Override
            public void onLoadMore(int page) {
                setProgressBar(true);
                loadImages(page, currentQuery);
            }
        };
        recyclerView.addOnScrollListener(infiniteScrollListener);
    }

    private void resetImageList() {     //clearing Layout for new images
        pixabayImageList.clear();
        infiniteScrollListener.resetCurrentPage();
        pixabayImageListAdapter.notifyDataSetChanged();
    }

    private void enterLastQuery(String currentQuery) {             // for storing last Query fired
        final SharedPreferences.Editor myEdit = sharedPreferences.edit();
        myEdit.putString("LastQuery", currentQuery);
        myEdit.commit();
    }

    private void enterLastSearchSet() {         //storing search history when application ends
        final SharedPreferences.Editor myEdit = sharedPreferences.edit();
        myEdit.putStringSet("SearchSet", searchSet);
        myEdit.commit();
    }

    public void setProgressBar(Boolean progress){       //starting-stoping progress bar
        if(progress){
            progressBar.setVisibility(View.VISIBLE);
        }else{
            progressBar.setVisibility(View.GONE);
        }
    }

    private void setDrawerView(Boolean progress){           //opening-closing Navigation Menu
        if(progress){
            drawer.openDrawer(GravityCompat.START);
        }else{
            drawer.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    public void onBackPressed() {       //performing actions on back button pressed
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            setDrawerView(false);
        } else {        //exiting application when pressed 2 times
            if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
                enterLastSearchSet();
                super.onBackPressed();
            } else {
                Toast.makeText(getBaseContext(), "Press Two times to exit!",Toast.LENGTH_SHORT).show();
            }
            mBackPressed = System.currentTimeMillis();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);  //receiving speech input as a text
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT:         //getting specific speech given with this ID
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    currentQuery = result.get(0);
                    processQuery(currentQuery);
                }
                else
                    Toast.makeText(this,"Problem with Speech",Toast.LENGTH_SHORT).show();
        }
    }
}
