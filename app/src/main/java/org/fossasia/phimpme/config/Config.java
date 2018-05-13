package org.fossasia.phimpme.config;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by roma on 27.04.2017.
 */

public class Config {

    private LruCache<String, Bitmap> mMemoryCache;

    private String name;

    private String logoUrl;
    private String iconUrl;
    private String backgroundUrl;

    private Integer primaryDarkColor;
    private Integer primaryColor;
    private Integer accentColor;

    private Integer cameraColor;
    private Integer galleryColor;
    private Integer settingsColor;
    private Integer accountsColor;
    private Integer shareColor;
    private Integer aboutColor;

    private String menuLayout;

    private List<CustomStickerPack> customStickerPackList;
    private boolean showDefaultStickers;

    Map<String, String> iconsMap;

    private void manageCache() {
        Log.w("cache", "init");
        mMemoryCache = new LruCache<String, Bitmap>(20 * 1000 * 1000) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };
    }

    private static volatile Config INSTANCE = null;
    private Config() {
    }

    public static Config get(){
        if(INSTANCE == null){
            synchronized (Config.class){
                if(INSTANCE == null){
                    INSTANCE = new Config();
                }
            }
        }
        return INSTANCE;
    }

    public void init(Context context) {
        manageCache();
        try {
            JSONObject settings = new JSONObject(loadSettings(context));
            logoUrl = settings.getString("logo");
            iconUrl = settings.getString("icon");
            backgroundUrl = settings.getString("background");


            JSONObject themeColors = settings.getJSONObject("themeColors");
            primaryDarkColor = readColor(themeColors, "colorPrimaryDark");
            primaryColor = readColor(themeColors, "colorPrimary");
            accentColor = readColor(themeColors, "colorAccent");

            cameraColor = readColor(settings, "cameraColor");
            galleryColor = readColor(settings, "galleryColor");
            settingsColor = readColor(settings, "settingsColor");
            accountsColor = readColor(settings, "accountsColor");
            shareColor = readColor(settings, "shareColor");
            aboutColor = readColor(settings, "aboutColor");

            menuLayout = settings.getJSONObject("mainScreen").getString("menuLayout");

            customStickerPackList = new ArrayList<>();
            JSONArray stickersArray = settings.getJSONArray("customStickerAlbums");
            for (int i=0; i < stickersArray.length(); i++){
                CustomStickerPack customStickerPack = new CustomStickerPack();
                JSONObject jsonSticker = stickersArray.getJSONObject(i);
                customStickerPack.setName(jsonSticker.getString("stickerAlbumName"));
                customStickerPack.setIconUrl(jsonSticker.getString("stickerIcon"));

                JSONArray stickerImagesArray = jsonSticker.getJSONArray("stickerAlbum");
                List<String> stickerImages = new ArrayList<>();
                for (int j=0; j < stickerImagesArray.length(); j++){
                    stickerImages.add(stickerImagesArray.getJSONObject(j).getString("path"));
                }
                customStickerPack.setImageUrlList(stickerImages);
                customStickerPackList.add(customStickerPack);
            }
            showDefaultStickers = settings.getBoolean("defaultStickers");

            iconsMap = new HashMap<>();
            fillIconsMap(context, settings.getJSONObject("menuButtonSettings").getString("buttonAppearance"));
            fillIconsMap(context, settings.getJSONObject("editorButtonSettings").getString("editorButtonAppearance"));

        } catch (JSONException e) {
            Log.e("Config", "Json parse error: " + e.getMessage());
        } catch (IOException e) {
            Log.e("Config", "Json read error: " + e.getMessage());
        }
    }
    //------------------------------------------------------
    private void fillIconsMap(Context context, String path){
        try {
            String[] files = context.getAssets().list(path);
            for (String name : files) {
                iconsMap.put(name.replaceFirst("[.][^.]+$", ""), path + File.separator + name);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //------------------------------------------------------
    public Drawable createDrawable(Context context, String link) {
        if (!link.equals("")) {
            try {
                Bitmap b = mMemoryCache.get(link);
                if (b == null) {
                    b = BitmapFactory.decodeStream(context.getAssets().open(link));
                    b.setDensity(Bitmap.DENSITY_NONE);
                    mMemoryCache.put(link, b);
                }
                return new BitmapDrawable(context.getResources(), b);
            } catch (FileNotFoundException e) {
                Log.d("Config", "Image " + link + " not found");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public Bitmap readBitmap(Context context, String link) {
        if (!link.equals("")) {
            try {
                Bitmap b = mMemoryCache.get(link);
                if (b == null) {
                    b = BitmapFactory.decodeStream(context.getAssets().open(link));
                    b.setDensity(Bitmap.DENSITY_NONE);
                    mMemoryCache.put(link, b);
                }
                return b;
            } catch (FileNotFoundException e) {
                Log.d("Config", "Image " + link + " not found");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private Integer readColor(JSONObject jsonTheme, String name) throws JSONException {
        String color = jsonTheme.getString(name);
        if (color == null || color.equals("")) {
            return null;
        }
        if (!color.startsWith("#")) {
            color = "#" + color;
        }
        return Color.parseColor(color);
    }


    public String loadSettings(Context context) throws IOException {
        String json = null;
        try {
            InputStream is = context.getAssets().open("settings.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public void changeIcon(ImageView imageView, String key){
        if(iconsMap.containsKey(key)){
            imageView.setColorFilter(0xFFFFFFFF, PorterDuff.Mode.MULTIPLY);
            imageView.setImageDrawable(createDrawable(imageView.getContext(), iconsMap.get(key)));
        }
    }
    //------------------------------------------------------

    public String getBackgroundUrl() {
        return backgroundUrl;
    }

    public String getName() {
        return name;
    }

    public String getMenuLayout() {
        return menuLayout;
    }

    public Integer getCameraColor() {
        return cameraColor;
    }

    public Integer getGalleryColor() {
        return galleryColor;
    }

    public Integer getSettingsColor() {
        return settingsColor;
    }

    public Integer getAccountsColor() {
        return accountsColor;
    }

    public Integer getShareColor() {
        return shareColor;
    }

    public Integer getAboutColor() {
        return aboutColor;
    }

    public Integer getPrimaryDarkColor() {
        return primaryDarkColor;
    }

    public Integer getPrimaryColor() {
        return primaryColor;
    }

    public Integer getAccentColor() {
        return accentColor;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public List<CustomStickerPack> getCustomStickerPackList() {
        return customStickerPackList;
    }

    public boolean isShowDefaultStickers() {
        return showDefaultStickers;
    }

    public Map<String, String> getIconsMap() {
        return iconsMap;
    }
}
