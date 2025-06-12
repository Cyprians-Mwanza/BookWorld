import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class FetchAllUsers {
    public static void main(String[] args) {

            // Step 1: Process the result set
            System.out.println("ID\tName\tEmail");
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String email = resultSet.getString("email");

                System.out.println(id + "\t" + name + "\t" + email);
            }

            // Step 2: Clean up
            resultSet.close();
            statement.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
