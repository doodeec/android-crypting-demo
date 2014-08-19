package com.doodeec.cryptodemo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;


public class MyActivity extends Activity {

    static final String TAG = "SymmetricAlgorithmAES";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_my);

        // Set up secret key spec for 128-bit AES encryption and decryption
        SecretKeySpec sks = null;
        try {
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            sr.setSeed("any data used as random seed".getBytes());
            KeyGenerator kg = KeyGenerator.getInstance("AES");
            kg.init(128, sr);
            sks = new SecretKeySpec((kg.generateKey()).getEncoded(), "AES");
        } catch (Exception e) {
            Log.e(TAG, "AES secret key spec error");
        }

        Cipher encryptC = null;
        Cipher decryptC = null;
        try {
            encryptC = Cipher.getInstance("AES");
            encryptC.init(Cipher.ENCRYPT_MODE, sks);
            decryptC = Cipher.getInstance("AES");
            decryptC.init(Cipher.DECRYPT_MODE, sks);
        } catch (Exception e) {
            Log.e(TAG, "AES encryption error");
        }

        File folder = new File(Environment.getExternalStorageDirectory(), "CRYPTOTEST");
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                Log.d("Crypting", "Error creating folder");
            }
        }
        String mPath = String.format("%s/encryptedImage.png", folder.getAbsolutePath());



        Log.d("[ENCRYPTION]:", "Start");

        // Original text
        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.owl);
        ImageView orig = (ImageView) findViewById(R.id.image);
        orig.setImageBitmap(originalBitmap);


        ByteArrayOutputStream blob = new ByteArrayOutputStream();
        originalBitmap.compress(Bitmap.CompressFormat.PNG, 50, blob);
        byte[] bitmapdata = blob.toByteArray();
        Log.d("[COMPRESSED]:", "");





        // Encode the original data with AES
        byte[] encodedBytes = null;
        try {
            encodedBytes = encryptC.doFinal(bitmapdata);

            FileOutputStream fos = new FileOutputStream(new File(mPath));
            CipherOutputStream cos = new CipherOutputStream(fos, encryptC);
            cos.write(encodedBytes);
            cos.close();
        } catch (Exception e) {
            Log.e(TAG, "AES encryption error");
        }

        Log.i("[ENCODED]", "");

        Bitmap encodedImage = BitmapFactory.decodeByteArray(encodedBytes , 0, encodedBytes.length);
        ImageView encoded = (ImageView) findViewById(R.id.encryptedImage);
        encoded.setImageBitmap(encodedImage);





        // Decode the encoded data with AES
        byte[] decodedBytes = null;
        try {
            FileInputStream fis = new FileInputStream(mPath);
            CipherInputStream cis = new CipherInputStream(fis, decryptC);
            decodedBytes = IOUtils.toByteArray(cis);
            decodedBytes = decryptC.doFinal(IOUtils.toByteArray(cis));
        } catch (Exception e) {
            Log.e(TAG, "AES decryption error");
        }

        Bitmap decodedImage = BitmapFactory.decodeByteArray(decodedBytes , 0, decodedBytes.length);
        Log.d("[DECODED]:", "");

        ImageView decoded = (ImageView) findViewById(R.id.decryptedImage);
        decoded.setImageBitmap(decodedImage);
    }
}