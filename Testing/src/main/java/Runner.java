import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.oop.orangeengine.database.ODatabase;
import com.oop.orangeengine.database.gson.MapFactory;
import com.oop.orangeengine.database.gson.RuntimeClassFactory;
import com.oop.orangeengine.database.types.MySqlDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Runner {
    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        final Gson gson = new GsonBuilder()
                .serializeNulls()
                .registerTypeAdapterFactory(RuntimeClassFactory.of(Object.class))
                .registerTypeAdapterFactory(new MapFactory())
                .setPrettyPrinting()
                .create();
        Map<Integer, Testinbo> hello = new HashMap<>();
        hello.put(252, new Testinbo());

        String json = gson.toJson(hello);
        System.out.println(json);

        hello = gson.fromJson(json, hello.getClass());
        System.out.println(hello);
    }

    public static class Testinbo {
        private int i = 252525;
        private long l = 2525252525L;
        private float f = 252525F;
    }

}
