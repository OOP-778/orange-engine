import com.oop.orangeengine.database.annotations.DatabaseTable;
import com.oop.orangeengine.database.annotations.DatabaseValue;
import com.oop.orangeengine.database.object.DatabaseObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@DatabaseTable(tableName = "a")
public class A extends DatabaseObject implements Serializable {

    @DatabaseValue(columnName = "test")
    private List<String> something = new ArrayList<>();

    @DatabaseValue(columnName = "wagaw")
    private transient List<String> somethingTransient = new ArrayList<>();

    @DatabaseValue(columnName = "tes")
    private B b = new B();

    protected A() {}


}
