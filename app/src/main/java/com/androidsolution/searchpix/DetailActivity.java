package com.androidsolution.searchpix;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;

import com.androidsolution.searchpix.databinding.ActivityDetailsBinding;
import com.androidsolution.searchpix.models.PixabayImage;
import com.androidsolution.searchpix.viewmodels.PixabayImageViewModel;
import com.google.gson.Gson;

public class DetailActivity extends AppCompatActivity {

    ActivityDetailsBinding activityDetailsBinding;
    public final static String PIXABAY_IMAGE = "PIXABAY_IMAGE";
    private PixabayImage image;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {      //binding layout to UI component
        super.onCreate(savedInstanceState);
        activityDetailsBinding = DataBindingUtil.setContentView(this, R.layout.activity_details);
        initImage();
        activityDetailsBinding.setViewmodel(new PixabayImageViewModel(image));
    }

    private void initImage() {          //convering Json into java object
        image = new Gson().fromJson(getIntent().getStringExtra(PIXABAY_IMAGE), PixabayImage.class);
    }
}
