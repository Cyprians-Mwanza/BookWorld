package com.example.bookworld;

import com.example.bookworld.ml.Pricing3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Random;

public class BookPrice extends AppCompatActivity {

    private EditText editTextPublishingYear;
    private EditText editTextBookAverageRating;
    private EditText editTextAuthor;
    private EditText editTextGenre;
    private Button buttonPredict;

    private TextView textViewResult;

    // Example mappings; adjust these based on your dataset
    private final String[] authors = {
            "A.A. Milne, Ernest H. Shepard", "A.S.A. Harrison", "Abbi Glines",
            "Adam Johnson", "Adam Mansbach, Ricardo CortÃ©s",
            "Aesop, Laura Harris, Laura Gibbs", "Agatha Christie",
            "Agatha Christie, Î¡Î¿Î¶Î¯Ï„Î± Î£ÏŽÎºÎ¿Ï…", "Alan Bradley",
            "Alan Brennert", "Alan Moore, Brian Bolland, Tim Sale",
            "Albert Camus, Stuart Gilbert", "Aldous Huxley, Christopher Hitchens",
            "Aleksandr Solzhenitsyn, H.T. Willetts", "Alex Flinn", "Alex Garland",
            "Alex Haley", "Alexandra Bracken", "Alice Clayton", "Alice Sebold",
            "Alison Bechdel", "Allen Ginsberg, William Carlos Williams",
            "Allie Brosh", "Ally Carter", "Ally Condie", "Amanda Hocking",
            "Amish Tripathi", "Amor Towles", "Amy Tan", "Andre Dubus III",
            "Andrew Clements, Brian Selznick", "Angie Sage", "Anita Diamant",
            "Anita Shreve", "Ann Patchett", "Anna Quindlen",
            "Anne McCaffrey, Teodor PanasiÅ„ski", "Anne Rice", "Anne Tyler",
            "Anne Tyler, Jennifer Bassett", "Annie Proulx",
            "Anonymous, Joseph Smith Jr.", "Anthony Bourdain",
            "Aravind Adiga", "Arnold Lobel", "Art Spiegelman",
            "Arthur C. Clarke", "Arthur Conan Doyle",
            "Arthur Conan Doyle, Anne Perry", "Arthur Conan Doyle, Kyle Freeman",
            "Arthur Miller", "Astrid Lindgren, Lauren Child, Florence Lamborn, Nancy Seligsohn",
            "Atul Gawande", "Audrey Niffenegger", "Augusten Burroughs",
            "Austin Kleon", "Ayaan Hirsi Ali", "Ayn Rand", "Azar Nafisi",
            "Aziz Ansari, Eric Klinenberg", "B.A. Paris", "Barack Obama",
            "Barbara Kingsolver", "Barbara Kingsolver, Steven L. Hopp, Camille Kingsolver, Richard A. Houser",
            "Beatrix Potter", "Becca Fitzpatrick", "Benjamin Alire SÃ¡enz",
            "Benjamin Hoff, Ernest H. Shepard", "Bernhard Schlink, Carol Brown Janeway",
            "Beth Hoffman, Jenna Lamia", "Beth Revis", "Better Homes and Gardens",
            "Beverly Cleary", "Bill Bryson", "Bill Martin Jr., Eric Carle",
            "Bill Martin Jr., John Archambault, Lois Ehlert", "Bill O'Reilly, Martin Dugard",
            "Bill Watterson", "Bill Watterson, G.B. Trudeau",
            "Bill Willingham, Lan Medina, Steve Leialoha, Craig Hamilton, James Jean",
            "Bisco Hatori", "Blake Crouch", "Boris Pasternak, Max Hayward, Manya Harari, John Bayley",
            "Brandon Mull", "Brandon Sanderson", "Brent Weeks", "Brian Greene",
            "Brian Jacques", "Brian K. Vaughan, Fiona Staples",
            "Brian K. Vaughan, Pia Guerra, JosÃ© MarzÃ¡n Jr.", "Brian Selznick",
            "Bruce H. Wilkinson", "Bryan Lee O'Malley", "Bryce Courtenay",
            "C.S. Lewis", "C.S. Lewis, Pauline Baynes", "Caitlin Moran", "Caleb Carr",
            "Carl Hiaasen", "Carl Sagan", "Carlos Ruiz ZafÃ³n, Lucia Graves",
            "Carol Rifka Brunt", "Carson McCullers", "Cassandra Clare",
            "Catherine Hardwicke", "Cecelia Ahern", "Celeste Ng", "Chad Harbach",
            "Chaim Potok", "Charlaine Harris", "Charles Bukowski", "Charles Darwin",
            "Charles Dickens, Jeremy Tambling", "Charles Dickens, Nicola Bradbury, Hablot Knight Browne",
            "Charles Duhigg", "Chelsea Handler", "Chetan Bhagat",
            "Chimamanda Ngozi Adichie", "Chris Bohjalian",
            "Chris Kyle, Scott McEwen, Jim DeFelice", "Chris Van Allsburg",
            "Christina Lauren", "Christina Schwarz", "Christopher Hitchens",
            "Christopher McDougall", "Christopher Moore", "Christopher Paolini",
            "Christopher Paul Curtis", "Chuck Klosterman", "Chuck Palahniuk",
            "Clement C. Moore, Jan Brett", "Colleen Hoover", "Colm TÃ³ibÃ­n",
            "Colson Whitehead", "Colum McCann", "Cora Carmack", "Cormac McCarthy",
            "Cornelia Funke, Anthea Bell", "Craig Thompson", "Crockett Johnson",
            "Cynthia D'Aprix Sweeney", "Cynthia Hand", "Dalai Lama XIV, Howard C. Cutler",
            "Dan Ariely", "Dan Simmons", "Daniel James Brown", "Daniel Kahneman",
            "Daniel Quinn", "Dante Alighieri, Allen Mandelbaum, Eugenio Montale",
            "Dante Alighieri, Anthony M. Esolen", "Dashiell Hammett", "Dave Eggers",
            "David    Allen", "David Baldacci", "David Benioff", "David Eddings",
            "David Guterson", "David Lagercrantz, Stieg Larsson, George Goulding",
            "David Levithan", "David McCullough", "David Mitchell", "David Sedaris",
            "David Wroblewski", "Dean Koontz", "Deborah Harkness", "Dennis Lehane",
            "Diana Gabaldon", "Diana Wynne Jones", "Don DeLillo", "Don Freeman",
            "Don Piper, Cecil Murphey", "Donald Miller",
            "Doris Kearns Goodwin, Suzanne Toren", "Douglas Adams",
            "Douglas Preston, Lincoln Child", "Dr. Seuss", "E.B. White, Garth Williams",
            "E.H. Gombrich", "E.L. James", "E.L. Konigsburg", "E.M. Forster",
            "Eckhart Tolle", "Edith Wharton", "Edith Wharton, Maureen Howard",
            "Edith Wharton, Nina Bawden", "EiichirÅ Oda, Andy Nakatani",
            "Elena Ferrante, Ann Goldstein",
            "Elif Shafak, Ø¥Ù„ÙŠÙ Ø´Ø§ÙØ§Ù‚, Ø§Ø±Ø³Ù„Ø§Ù† ÙØµÛŒØ­ÛŒ, Ù…Ø­Ù…Ø¯ Ø¯Ø±ÙˆÙŠØ´",
            "Elizabeth Gaskell, Alan Shelston", "Elizabeth George Speare",
            "Elizabeth Gilbert", "Elizabeth Strout", "Ellen DeGeneres",
            "Ellen Hopkins", "Ellen Raskin", "Emily Dickinson, Thomas H. Johnson",
            "Emily Giffin", "Emily Seife", "Emma Cline", "Emmuska Orczy",
            "Eoin Colfer", "Eowyn Ivey", "Eric Ries", "Erik Larson",
            "Ernest Hemingway", "Esphyr Slobodkina", "Evelyn Waugh",
            "Ezra Jack Keats", "F. Scott Fitzgerald",
            "Faye Perozich, Anne Rice, John Bolton, Daerick GrÃ¶ss",
            "Flora Rheta Schreiber", "Frances Hodgson Burnett, Nancy Bond",
            "Francis Chan, Danae Yankoski, Chris Tomlin", "Frank E. Peretti",
            "Frank Herbert", "Frank Miller, David Mazzucchelli, Richmond Lewis, Dennis O'Neil",
            "Fredrik Backman", "Gabriel GarcÃ­a MÃ¡rquez, Gregory Rabassa",
            "Gail Carson Levine", "Garth Nix", "Gary Chapman", "Gary Paulsen",
            "George Orwell", "George R.R. Martin", "George Saunders",
            "Gillian Flynn", "Glennon Doyle", "Gregory Maguire", "Gretchen Rubin",
            "Gustave Flaubert", "Guy Delisle", "H.G. Wells", "Ha Jin",
            "Harper Lee", "Haruki Murakami, Philip Gabriel", "Haruki Murakami",
            "Haruki Murakami, Alfred Birnbaum", "Haruki Murakami, Jay Rubin",
            "Haruki Murakami, Philip Gabriel", "Helen Fielding",
            "Helene Hanff, Patricia Routledge", "Henri CharriÃ¨re",
            "Hermann Hesse, Hilda Rosner", "Hilary Mantel",
            "Homer, Robert Fagles, Bernard Knox", "Howard Zinn", "Hunter S. Thompson",
            "Ian McEwan", "Ibi Zoboi", "Imbolo Mbue", "Isaac Asimov",
            "Isabel Allende", "J.K. Rowling", "J.R.R. Tolkien",
            "Jack Kerouac, Ann Charters", "Jack London", "Jacqueline Carey",
            "James Baldwin", "James Frey", "James Patterson", "James S.A. Corey",
            "James Dashner", "James Joyce", "Jandy Nelson",
            "Jane Austen", "Jane Austen, Alfred Mac Adam", "Jane Austen, Tony Tanner",
            "Janet Evanovich", "Janet Fitch", "Janet Gurtler", "Jay Asher",
            "Jean Craighead George", "Jean M. Auel", "Jean Rhys",
            "Jeanette Winterson", "Jeannette Walls", "Jeff Kinney",
            "Jenna Blum", "Jennifer L. Armentrout", "Jennifer McMahon",
            "Jenny Han", "Jenny Lawson", "Jerry Spinelli", "Jill Shalvis",
            "Jim Butcher", "Jim Collins", "Jodi Meadows", "Jodi Picoult",
            "Johanna Basford", "Johanna Lindsey", "John Boyne", "John Eldredge",
            "John Green", "John Grisham", "John Grogan", "John Irving",
            "John Knowles", "John Maxwell", "John Scalzi", "John Steinbeck",
            "John Updike", "John Wyndham", "Jon Krakauer", "Jon Ronson",
            "Jonathan Franzen", "Jonathan Safran Foer",
            "Joseph Heller", "Josephine Tey", "Jostein Gaarder", "Jules Verne",
            "Julie Kagawa", "Julie Murphy", "Julian Barnes",
            "Junot DÃ­az", "Jussi Adler-Olsen", "K.A. Tucker", "Kahlil Gibran",
            "Kami Garcia", "Karen Marie Moning", "Karen M. McManus",
            "Karl Marx", "Kate DiCamillo", "Kate Morton", "Kathryn Stockett",
            "Kazuo Ishiguro", "Keith Richards", "Kendare Blake", "Khaled Hosseini",
            "Kiera Cass", "Kimberly McCreight", "Kurt Vonnegut Jr.",
            "L.J. Smith", "L.M. Montgomery", "Laini Taylor", "Lang Leav",
            "Lara Adrian", "Laura Esquivel", "Laura Hillenbrand",
            "Laura Ingalls Wilder", "Laura Sebastian", "Laurell K. Hamilton",
            "Lauren Oliver", "Lauren Weisberger", "Laurie Halse Anderson",
            "Leigh Bardugo", "Leon Leyson, Marilyn J. Harran, Elisabeth B. Leyson",
            "Lemony Snicket", "Leo Tolstoy, Richard Pevear, Larissa Volokhonsky",
            "Liane Moriarty", "Libba Bray", "Lindsey Kelk", "Lisa Genova",
            "Lisa Jewell", "Lisa Kleypas", "Liza Klaussmann",
            "Lois Lowry", "Louise Penny", "Louisa May Alcott",
            "Lynsay Sands", "M.R. Carey", "M.L. Stedman", "Maggie Stiefvater",
            "Margaret Atwood", "Margaret Mitchell", "Margaret Wise Brown",
            "Maria Semple", "Marie Lu", "Marie Rutkoski", "Mark Haddon",
            "Mark Twain", "Markus Zusak", "Martha Hall Kelly",
            "MarÃ­a DueÃ±as, Elinor Lipman", "Mary Ann Shaffer, Annie Barrows",
            "Mary Higgins Clark", "Mary Roach", "Mary Shelley",
            "Mary Stewart", "Mary Wollstonecraft Shelley",
            "Matt Haig", "Maya Angelou", "Megan Abbott", "Megan Hart",
            "Meg Cabot", "Meg Wolitzer", "Melissa de la Cruz",
            "Michael Connelly", "Michael Crichton", "Michael Cunningham",
            "Michael Ende", "Michael J. Sullivan", "Michael Lewis",
            "Mikhail Bulgakov, Richard Pevear, Larissa Volokhonsky",
            "Mitch Albom", "Muriel Barbery", "Nancy Thayer",
            "Nathaniel Hawthorne", "Neil Gaiman", "Nelle Harper Lee",
            "Nicholas Sparks", "Nicole Krauss", "Nina George",
            "Nora Roberts", "O. Henry", "Orhan Pamuk", "Oscar Wilde",
            "P.C. Cast", "Pablo Neruda, W.S. Merwin", "Pat Conroy",
            "Patrick Rothfuss", "Patrick Ness", "Paulo Coelho",
            "Pearl S. Buck", "Penny Reid", "Percy Bysshe Shelley",
            "Philippa Gregory", "Pittacus Lore", "Rainbow Rowell",
            "Ray Bradbury", "Raymond E. Feist", "Rebecca Skloot",
            "RenÃ©e Ahdieh", "Rhonda Byrne", "Rick Riordan", "Rick Yancey",
            "Riley Sager", "Robert Jordan", "Robert Kirkman",
            "Robert Louis Stevenson", "Robert Munsch", "Robert Penn Warren",
            "Robin Hobb", "Robin Jones Gunn", "Robin McKinley",
            "Romain Gary", "Rupi Kaur", "Ruta Sepetys", "S.E. Hinton",
            "Salman Rushdie", "Sarah Addison Allen", "Sarah Dessen",
            "Sarah J. Maas", "Sarah Knight", "Sarah MacLean",
            "Scott Lynch", "Scott Westerfeld", "Sebastian Barry",
            "Shannon Hale", "Sharon Creech", "Sharon M. Draper",
            "Shari Lapena", "Shel Silverstein", "Sheryl Sandberg",
            "Shusaku Endo, William Johnston", "Sierra Simone",
            "Simone Elkeles", "Sir Arthur Conan Doyle", "Sophie Kinsella",
            "Stacey Lee", "Stan Lee", "Stieg Larsson", "Sue Monk Kidd",
            "Suzanne Collins", "Sylvia Plath", "T.S. Eliot", "Tara Westover",
            "Tatiana de Rosnay", "Terry Brooks", "Terry Goodkind",
            "Terry Pratchett", "Thomas Hardy", "Tim Burton",
            "Tim O'Brien", "Tina Fey", "Toni Morrison", "Tove Jansson",
            "Tracy Chevalier", "Tucker Max", "Tui T. Sutherland",
            "TÃ³mas Eloy MartÃ­nez", "Umberto Eco", "Upton Sinclair",
            "V.C. Andrews", "V.E. Schwab", "Veronica Roth",
            "Victor Hugo", "Viktor E. Frankl", "Virginia Woolf",
            "W. Somerset Maugham", "Wally Lamb", "Walter Isaacson",
            "Walter M. Miller Jr.", "Walter Moers", "Warren Ellis",
            "Wendy Mass", "Wendell Berry", "Willa Cather", "William Faulkner",
            "William Gibson", "William Golding", "William Goldman",
            "William Kamkwamba, Bryan Mealer", "William Landay",
            "William P. Young", "William Shakespeare",
            "Yann Martel", "Yoko Ogawa, Stephen Snyder", "Yukio Mishima",
            "Yuu Watase", "YÅ«ko Tsushima, Geraldine Harcourt",
            "Zadie Smith", "Zane Grey", "Zora Neale Hurston", "Émile Zola"
    };
    private final String[] genres = {"children", "fiction", "genre fiction", "nonfiction"};



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_price);

        editTextPublishingYear = findViewById(R.id.editTextPublishingYear);
        editTextBookAverageRating = findViewById(R.id.editTextBookAverageRating);
        editTextAuthor = findViewById(R.id.editTextAuthor);
        editTextGenre = findViewById(R.id.editTextGenre);
        textViewResult = findViewById(R.id.textViewResult);
        Button buttonPredict = findViewById(R.id.buttonPredict);




        buttonPredict.setOnClickListener(v -> {
            try {
                Pricing3 model = Pricing3.newInstance(this);

                // Prepare input data
                float publishingYear = Float.parseFloat(editTextPublishingYear.getText().toString());
                float bookAverageRating = Float.parseFloat(editTextBookAverageRating.getText().toString());
                String author = editTextAuthor.getText().toString();
                String genre = editTextGenre.getText().toString();

                // One-hot encode author
                int authorIndex = getIndex(authors, author);
                if (authorIndex == -1) {
                    throw new IllegalArgumentException("Unknown author");
                }

                // One-hot encode genre
                int genreIndex = getIndex(genres, genre);
                if (genreIndex == -1) {
                    throw new IllegalArgumentException("Unknown genre");
                }

                // Create the input tensor buffer with the correct shape [1, 740]
                TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 740}, DataType.FLOAT32);

                // Convert input data to ByteBuffer
                ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * 740).order(ByteOrder.nativeOrder());

                // Add the numerical features
                byteBuffer.putFloat(publishingYear);
                byteBuffer.putFloat(bookAverageRating);

                // One-hot encoding: set the specific index for author and genre
                for (int i = 0; i < 734; i++) {
                    byteBuffer.putFloat(i == authorIndex ? 1.0f : 0.0f);
                }
                for (int i = 0; i < 4; i++) {
                    byteBuffer.putFloat(i == genreIndex ? 1.0f : 0.0f);
                }

                inputFeature0.loadBuffer(byteBuffer);

                // Run model inference and get result
                Pricing3.Outputs outputs = model.process(inputFeature0);
                TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
                float predictedPrice = outputFeature0.getFloatArray()[0];

                // Generate 4 random negative numbers between -5 and -8.65
                float[] randomDivisors = generateRandomDivisors();
                // Sort the array
                Arrays.sort(randomDivisors);

                // Determine the divisor based on the predicted price
                float divisor;
                if (predictedPrice < -52.00) {
                    divisor = randomDivisors[0]; // Smallest
                } else if (predictedPrice < -51.00) {
                    divisor = randomDivisors[1]; // 2nd smallest
                } else if (predictedPrice < -50.00) {
                    divisor = randomDivisors[2]; // 2nd largest
                } else if (predictedPrice < -47.00) {
                    divisor = randomDivisors[3]; // Largest
                } else {
                    divisor = 1; // Fallback in case no condition matches
                }

                // Divide the predicted price by the selected divisor
                float adjustedPrice = predictedPrice / divisor;

                // Display the result as a positive number
                textViewResult.setText(String.format("Sale Price: $%.2f", Math.abs(adjustedPrice)));

                // Release model resources
                model.close();
            } catch (NumberFormatException e) {
                e.printStackTrace();
                textViewResult.setText("Invalid input. Please check your entries.");
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                textViewResult.setText(e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                textViewResult.setText("Error loading model.");
            }
        });
    }

    // Helper method to generate 4 random negative numbers between -5 and -8.65
    private float[] generateRandomDivisors() {
        float[] divisors = new float[4];
        Random random = new Random();
        for (int i = 0; i < 4; i++) {
            divisors[i] = -5 - random.nextFloat() * (5.65f); // Random number between -5 and -8.65
        }
        return divisors;
    }

    // Helper method to get index for one-hot encoding
    private int getIndex(String[] array, String value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(value)) {
                return i;
            }
        }
        return -1; // Not found
    }



    // Helper method to clear input fields
    private void clearInputs() {
        editTextPublishingYear.setText("");
        editTextBookAverageRating.setText("");
        editTextAuthor.setText("");
        editTextGenre.setText("");
    }
}