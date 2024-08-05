package com.example.bookworld;

import android.content.Context;
import android.util.Log;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class ModelHandler {

    private Interpreter interpreter;
    private Map<String, Integer> authorMap;
    private Map<String, Integer> genreMap;
    private final int numAuthors = 3; // Update based on your actual number of authors
    private final int numGenres = 3; // Update based on your actual number of genres

    public ModelHandler(Context context) throws IOException {
        try {
            // Load TensorFlow Lite model
            ByteBuffer modelBuffer = loadModelFile(context, "ml/BookPricing.tflite");
            interpreter = new Interpreter(modelBuffer);
            Log.d("ModelHandler", "Model initialized successfully.");
        } catch (IOException e) {
            Log.e("ModelHandler", "Model initialization failed", e);
            throw e;
        }

        // Initialize encoding maps
        initializeEncodingMaps();
    }

    private ByteBuffer loadModelFile(Context context, String modelFilename) throws IOException {
        try (InputStream is = context.getAssets().open(modelFilename)) {
            byte[] modelData = new byte[is.available()];
            is.read(modelData);
            ByteBuffer byteBuffer = ByteBuffer.wrap(modelData);
            Log.d("ModelHandler", "Model file loaded successfully.");
            return byteBuffer;
        } catch (IOException e) {
            Log.e("ModelHandler", "Error reading model file", e);
            throw e;
        }
    }

    private void initializeEncodingMaps() {
        // Initialize encoding maps for authors
        authorMap = new HashMap<>();
        authorMap.put("Author1", 0);
        authorMap.put("Author2", 1);
        authorMap.put("Author3", 2);
        // Add more authors if needed

        // Initialize encoding maps for genres
        genreMap = new HashMap<>();
        genreMap.put("Fiction", 0);
        genreMap.put("History", 1);
        genreMap.put("Science", 2);
        // Add more genres if needed
    }

    private float[] oneHotEncode(int index, int size) {
        float[] encoding = new float[size];
        if (index >= 0 && index < size) {
            encoding[index] = 1.0f;
        }
        return encoding;
    }

    private float[] encodeAuthor(String author) {
        int index = authorMap.getOrDefault(author, -1); // Use -1 for unknown authors
        return oneHotEncode(index, numAuthors);
    }

    private float[] encodeGenre(String genre) {
        int index = genreMap.getOrDefault(genre, -1); // Use -1 for unknown genres
        return oneHotEncode(index, numGenres);
    }

    public float predict(float publishingYear, float bookAverageRating, String author, String genre) {
        // Prepare input tensor
        TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 8}, DataType.FLOAT32);

        // Fill the input tensor with your data
        float[] inputValues = new float[8];
        inputValues[0] = publishingYear;
        inputValues[1] = bookAverageRating;

        // Add one-hot encoded author and genre
        float[] authorEncoding = encodeAuthor(author);
        float[] genreEncoding = encodeGenre(genre);

        // Combine all features into the inputValues array
        System.arraycopy(authorEncoding, 0, inputValues, 2, numAuthors);
        System.arraycopy(genreEncoding, 0, inputValues, 2 + numAuthors, numGenres);

        inputFeature0.loadArray(inputValues);

        // Run model inference
        TensorBuffer outputFeature0 = TensorBuffer.createFixedSize(new int[]{1}, DataType.FLOAT32);
        interpreter.run(inputFeature0.getBuffer(), outputFeature0.getBuffer().rewind());

        // Return prediction result
        return outputFeature0.getFloatValue(0);
    }

    public void close() {
        if (interpreter != null) {
            interpreter.close();
        }
    }
}
