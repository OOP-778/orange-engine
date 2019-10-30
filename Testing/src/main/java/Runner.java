import com.oop.orangeengine.database.ODatabase;
import com.oop.orangeengine.database.object.DataController;
import com.oop.orangeengine.database.types.SqlLiteDatabase;

import java.io.File;

public class Runner {
    public static void main(String[] args) {
        File dab = new File("dab");
        if (!dab.exists())
            dab.mkdirs();

        System.out.println(dab.getPath());
        ODatabase database = new SqlLiteDatabase(dab, "data");
        Controller controller = new Controller(database);
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
