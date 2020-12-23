package com.androidsolution.searchpix.models;

public class PixabayImage {

    private String previewURL;
    private String webformatURL;

    public PixabayImage(String previewURL,String webformatURL) {

        this.previewURL = previewURL;
        this.webformatURL = webformatURL;
    }

    // thumbnail url
    public String getPreviewURL() {
        return previewURL;
    }
    // actual url high quality
    public String getWebformatURL() {
        return webformatURL;
    }

}