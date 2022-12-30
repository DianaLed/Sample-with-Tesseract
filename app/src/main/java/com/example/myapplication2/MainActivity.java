package com.example.myapplication2;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends Activity {

    // Объявление константы для запроса на выбор изображения
    private static final int PICK_IMAGE_REQUEST = 1;

    // Объявление переменной для хранения выбранного изображения
    private Bitmap selectedImage;

    // Кнопка для выбора изображения
    Button chooseImageButton;

    // Изображение, отображаемое на экране
    ImageView imageView;

    // Текстовое поле для отображения текста с изображения
    TextView textView;

    // Объект Tesseract OCR
    TessBaseAPI tessBaseAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Находим кнопку, и Инициализируем Tesseract OCR
        tessBaseAPI = new TessBaseAPI();
        String datapath = getFilesDir() + "/tesseract/";
        File dir = new File(datapath + "tessdata/");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String lang = "rus";
        String lang1 = "eng";
        File file = new File(datapath + "tessdata/" + lang + lang1 + ".traineddata");
        if (!file.exists()) {
            try {
                InputStream in = getAssets().open("tessdata/" + lang + ".traineddata");
                OutputStream out = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                in.close();
                out.flush();
                out.close();
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
            }
        }
        tessBaseAPI.init(datapath, lang+lang1);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Освобождаем ресурсы Tesseract OCR
        tessBaseAPI.end();
    }

    // Метод для выбора изображения
    public void onbtn(View view) {
        // Создаем новый интент для выбора изображения из галереи
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Выберите изображение"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        textView = findViewById(R.id.TextView);
        // Проверяем, что результат пришел от нашего запроса на выбор изображения
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // Получаем Uri выбранного изображения
            Uri imageUri = data.getData();

            try {
                selectedImage = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
// Отображаем изображение на экране
                // Преобразуем Bitmap в массив байтов
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                selectedImage.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] imageBytes = byteArrayOutputStream.toByteArray();

                // Передаем массив байтов Tesseract OCR для распознавания текста
                File imageFile = new File(getFilesDir(), "myFile.txt");
                FileOutputStream fos = new FileOutputStream(imageFile);
                fos.write(imageBytes);

                tessBaseAPI.setImage(imageFile);
                String recognizedText = tessBaseAPI.getUTF8Text();
                textView.setText(recognizedText);
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}