package com.androidsolution.searchpix.models;

import java.util.List;


public class PixabayImageList {

    private List<PixabayImage> hits;

    public PixabayImageList(List<PixabayImage> hits) {
        this.hits = hits;
    }

    public List<PixabayImage> getHits() {
        return hits;
    }           //return list of Image urls

}