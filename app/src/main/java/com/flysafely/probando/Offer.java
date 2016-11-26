package com.flysafely.probando;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

/**
 * Created by matias on 16/11/16.
 */

public class Offer implements Serializable {

    private Double price;

    private String name;

    private Bitmap currentImage;

    public Offer(String name, Double price, Bitmap bitmap) {
        this.name = name;
        this.price = price;
        this.currentImage = bitmap;
    }

    public Double getPrice() {
        return price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Bitmap getBitmap() {
        return currentImage;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        currentImage.compress(Bitmap.CompressFormat.PNG, 100, stream);

        byte[] byteArray = stream.toByteArray();

        out.writeInt(byteArray.length);
        out.write(byteArray);

        currentImage = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {


        int bufferLength = in.readInt();

        byte[] byteArray = new byte[bufferLength];

        int pos = 0;
        do {
            int read = in.read(byteArray, pos, bufferLength - pos);

            if (read != -1) {
                pos += read;
            } else {
                break;
            }

        } while (pos < bufferLength);

        currentImage = BitmapFactory.decodeByteArray(byteArray, 0, bufferLength);

    }

}
