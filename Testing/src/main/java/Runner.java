import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import t.a.ExampleObject;

public class Runner {

    final static Gson gson = new GsonBuilder()
            .registerTypeAdapterFactory(new UpdateableAdapterFactory())
            .create();

    public static void main(String[] args) {
        String serialized = gson.toJson(new ExampleObject());
        System.out.println(serialized);

        ExampleObject exampleObject = gson.fromJson(serialized, ExampleObject.class);
        System.out.println(exampleObject);

    }
}
