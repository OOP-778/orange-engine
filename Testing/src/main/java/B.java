import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class B implements Serializable {

    private transient List<String> staty =  new ArrayList<String>(){{
        add("wagawgag");
    }};

}
