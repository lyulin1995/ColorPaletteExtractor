package com.lys.testapplication;

import java.util.HashMap;
import java.util.Map;

public class PaletteObj {
    public String imagePath;
    public String title;
    public Map<String, Object> paletteDetail;

    public PaletteObj (){
        // Empty needed for firebase
    }

    public PaletteObj(String title, String imagePath) {
        this.title = title;
        this.imagePath = imagePath;
        Map<String, Object> paletteDetail = new HashMap<>();
        paletteDetail.put("vibrantColor", "");
        paletteDetail.put("lightVibrant", "");
        paletteDetail.put("dominantColor", "");
        paletteDetail.put("darkMuted", "");
        this.paletteDetail = paletteDetail;
    }

    public void setPaletteDetail( Map<String, Object> paletteDetail) {
        this.paletteDetail = paletteDetail;
    }

    public void setVibrantColor(String vibrantColor) {
        paletteDetail.put("vibrantColor", vibrantColor);
    }

    public void setLightVibrantColor(String lightVibrant) {
        paletteDetail.put("lightVibrant", lightVibrant);
    }

    public void setDominantColor(String dominantColor) {
        paletteDetail.put("dominantColor", dominantColor);
    }

    public void setDarkMutedColor(String darkMuted) {
        paletteDetail.put("darkMuted", darkMuted);
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
