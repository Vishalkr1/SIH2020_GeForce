package com.example.hack2020;

import android.media.Image;

public class AddCareRecievers {
    private Image image;
    private String email;

    public AddCareRecievers(Image image, String email) {
        this.image = image;
        this.email = email;
    }

    public AddCareRecievers(){

    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
