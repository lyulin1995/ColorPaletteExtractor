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
        paletteDetail.put("color_1", "");
        paletteDetail.put("color_2", "");
        paletteDetail.put("color_3", "");
        paletteDetail.put("color_4", "");
        this.paletteDetail = paletteDetail;
    }

    public void setPaletteDetail( Map<String, Object> paletteDetail) {
        this.paletteDetail = paletteDetail;
    }

    public void setColorOne(String vibrantColor) {
        paletteDetail.put("color_1", vibrantColor);
    }

    public void setColorTwo(String lightVibrant) {
        paletteDetail.put("color_2", lightVibrant);
    }

    public void setColorThree(String dominantColor) {
        paletteDetail.put("color_3", dominantColor);
    }

    public void setColorFour(String darkMuted) {
        paletteDetail.put("color_4", darkMuted);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }
}
