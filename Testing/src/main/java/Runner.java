import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.Duration;
import java.time.Instant;

public class Runner {
    public static void main(String[] args) {
        System.out.println("== GSON ==");
        Gson gson = new GsonBuilder().serializeNulls().create();

        Instant now = Instant.now();
        String serializedGson = gson.toJson(new A());
        System.out.println(serializedGson);

        System.out.println("Serialization Took: " + Duration.between(now, Instant.now()).toMillis());
        now = Instant.now();

        gson.fromJson(serializedGson, A.class);
        System.out.println("Deserialization Took: " + Duration.between(now, Instant.now()).toMillis());

    }

}
