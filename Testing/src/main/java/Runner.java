import com.oop.orangeengine.database.ODatabase;
import com.oop.orangeengine.database.types.MySqlDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Runner {
    public static void main(String[] args) throws SQLException {
        MySqlDatabase database = new MySqlDatabase(
                new MySqlDatabase.MySqlProperties()
                        .database("customer_103989_test")
                        .user("customer_103989_test")
                        .password("W6OD~ZbtxNjPoIcB#TlT")
                        .url("eu01-sql.pebblehost.com")
        );
        System.out.println(database.getTables());
    }
}
