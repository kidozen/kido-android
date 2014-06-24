package kidozen.client.internal;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.StringReader;
import java.lang.reflect.Type;

/**
 * Created by christian on 5/12/14.
 */
public class KidoEntitySerializer {
    private static KidoEntitySerializer INSTANCE;
    private Gson mGson;

    // Private constructor suppresses
    private KidoEntitySerializer(){
        mGson = new Gson();
    }

    private static void createInstance() {
        if (INSTANCE == null) {
            // synchronized to avoid possible  multi-thread issues
            synchronized(KidoEntitySerializer.class) {
                // must check for null again
                if (INSTANCE == null) {
                    INSTANCE = new KidoEntitySerializer();
                }
            }
        }
    }

    public static KidoEntitySerializer getInstance() {
        createInstance();
        return INSTANCE;
    }

    public String toJson(Object obj) {
        return mGson.toJson(obj);
    }

    @SuppressWarnings("unchecked")
    public <T> T fromJson(String json, Type typeOfT) throws JsonSyntaxException {
        if (json == null) {
            return null;
        }
        StringReader reader = new StringReader(json);
        T target = (T) mGson.fromJson(reader, typeOfT);
        return target;
    }

}
