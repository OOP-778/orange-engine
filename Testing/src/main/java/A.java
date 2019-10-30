import com.oop.orangeengine.database.annotations.DatabaseTable;
import com.oop.orangeengine.database.annotations.DatabaseValue;
import com.oop.orangeengine.database.object.DatabaseObject;

import java.util.ArrayList;
import java.util.List;

@DatabaseTable(tableName = "a")
public class A extends DatabaseObject {

    @DatabaseValue(columnName = "test")
    private List<String> something = new ArrayList<>();

}
