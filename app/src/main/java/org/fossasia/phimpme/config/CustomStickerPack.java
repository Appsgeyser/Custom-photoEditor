package org.fossasia.phimpme.config;

import java.util.List;

/**
 * Created by roma on 11.05.2018.
 */

public class CustomStickerPack {

    private String name;
    private String iconUrl;
    private List<String> imageUrlList;

    public CustomStickerPack() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public List<String> getImageUrlList() {
        return imageUrlList;
    }

    public void setImageUrlList(List<String> imageUrlList) {
        this.imageUrlList = imageUrlList;
    }
}
