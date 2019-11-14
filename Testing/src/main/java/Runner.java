import com.oop.orangeengine.database.ODatabase;
import com.oop.orangeengine.database.object.DataController;
import com.oop.orangeengine.database.types.SqlLiteDatabase;

import java.io.File;

public class Runner {
    public static void main(String[] args) {

        String testing = "awhawhawh awhawhawh awhawhwaa";
        String[] split = testing.split(" ");

        StringBuffer buffer = new StringBuffer();
        for (String spaced : split)
            buffer.append(spaced).append(" ");

        System.out.println(buffer);
    }

    public static class Controller extends DataController {

        public Controller(ODatabase database) {
            super(database);

            registerClass(A.class);

            load();
            setAutoSave(true);
        }
    }

}
