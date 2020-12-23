package com.androidsolution.searchpix.viewmodels;

import android.content.Intent;
import androidx.databinding.BaseObservable;
import androidx.databinding.BindingAdapter;
import android.view.View;
import android.widget.ImageView;

import com.androidsolution.searchpix.DetailActivity;
import com.androidsolution.searchpix.R;
import com.androidsolution.searchpix.models.PixabayImage;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;


public class PixabayImageViewModel extends BaseObservable {
    private PixabayImage pixabayImage;

    public PixabayImageViewModel(PixabayImage pixabayImage) {
        this.pixabayImage = pixabayImage;
    }

    public String getImageUrl() {
        return pixabayImage.getPreviewURL();
    }

    public String getHighResImageUrl() {
        return pixabayImage.getWebformatURL();
    }

    @BindingAdapter({"bind:imageUrl"})
    public static void loadImage(ImageView view, String imageUrl) {     //loads requested image using Glide
        Glide.with(view.getContext())
                .load(imageUrl)
                .placeholder(R.drawable.ic_image_placeholder)
                .into(view);
    }

    public View.OnClickListener openDetails() {         //handles when clicked on an image, loads high resolution image
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(), DetailActivity.class);
                String serialized = new Gson().toJson(pixabayImage);
                i.putExtra(DetailActivity.PIXABAY_IMAGE, serialized);
                v.getContext().startActivity(i);
            }
        };
    }
}
