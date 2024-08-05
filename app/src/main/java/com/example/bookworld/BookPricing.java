package com.example.bookworld;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;

public class BookPricing extends AppCompatActivity {

    private ModelHandler modelHandler;
    private EditText editPublishingYear, editBookAverageRating, editAuthor, editGenre;
    private TextView textResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_pricing);

        // Initialize UI elements
        editPublishingYear = findViewById(R.id.editPublishingYear);
        editBookAverageRating = findViewById(R.id.editBookAverageRating);
        editAuthor = findViewById(R.id.editAuthor);
        editGenre = findViewById(R.id.editGenre);
        textResult = findViewById(R.id.textResult);
        Button btnPredict = findViewById(R.id.btnPredict);

        // Initialize ModelHandler
        try {
            modelHandler = new ModelHandler(this);
        } catch (IOException e) {
            e.printStackTrace();
            textResult.setText("Model initialization failed.");
            return;
        }

        // Set up button click listener
        btnPredict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // Get input values
                    String publishingYearStr = editPublishingYear.getText().toString();
                    String bookAverageRatingStr = editBookAverageRating.getText().toString();
                    String author = editAuthor.getText().toString();
                    String genre = editGenre.getText().toString();

                    // Validate inputs
                    if (publishingYearStr.isEmpty() || bookAverageRatingStr.isEmpty() || author.isEmpty() || genre.isEmpty()) {
                        textResult.setText("Please fill all fields.");
                        return;
                    }

                    float publishingYear = Float.parseFloat(publishingYearStr);
                    float bookAverageRating = Float.parseFloat(bookAverageRatingStr);

                    // Make prediction
                    float prediction = modelHandler.predict(publishingYear, bookAverageRating, author, genre);
                    textResult.setText(String.format("Predicted Sale Price: %.2f", prediction));

                } catch (NumberFormatException e) {
                    textResult.setText("Invalid input format.");
                } catch (Exception e) {
                    e.printStackTrace();
                    textResult.setText("Prediction failed.");
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (modelHandler != null) {
            modelHandler.close();
        }
    }
}
