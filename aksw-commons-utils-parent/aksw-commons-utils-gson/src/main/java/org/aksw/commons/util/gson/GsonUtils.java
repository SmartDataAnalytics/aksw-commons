package org.aksw.commons.util.gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class GsonUtils {

    /** Set a string at a given path. Creates intermediate json objects as needed. A null value creates the path but removes the last key. */
    public static JsonObject setString(JsonObject obj, List<String> keys, String value) {
        return setElement(obj, keys, value == null ? null : new JsonPrimitive(value));
    }

    public static JsonObject setNumber(JsonObject obj, List<String> keys, Number number) {
        return setElement(obj, keys, number == null ? null : new JsonPrimitive(number));
    }

    /** Set an element at a given path. Creates intermediate json objects as needed. A null value creates the path but removes the last key. */
    public static JsonObject setElement(JsonObject obj, List<String> keys, JsonElement value) {
        int l = keys.size();
        JsonObject tgt = l == 0 ? obj : makePath(obj, keys.subList(0, l - 1));
        String key = keys.get(l - 1);
        if (value == null) {
            tgt.remove(key);
        } else {
            tgt.add(key, value);
        }
        return obj;
    }

    /** Creates fresh empty JSON objects if a key has no value */
    public static JsonObject makePath(JsonObject obj, String ...keys) {
        return makePath(obj, Arrays.asList(keys));
    }

    public static JsonObject makePath(JsonObject obj, List<String> keys) {
        for (String key : keys) {
            JsonElement member = obj.get(key);

            if (member == null) {
                member = new JsonObject();
                obj.add(key, member);
            } else if (!(member instanceof JsonObject)) {
                throw new RuntimeException("Encountered unexpected existing non-object member.");
            }

            obj = (JsonObject)member;
        }

        return obj;
    }

    /** This method is a stub. Currently it only splits by '.'. It should later
     * also support array access syntax such as foo.bar["baz"] */
    public static List<String> parsePathSegments(String str) {
        return new ArrayList<>(Arrays.asList(str.split("\\.")));
    }

    public static JsonElement merge(JsonElement a, JsonElement b) {
        JsonElement result;
        if (a == null) {
            result = b == null ? null : b.deepCopy();
        } else if (b == null) {
            result = a;
        } else if (a.isJsonArray() && b.isJsonArray()) {
            result = merge(a.getAsJsonArray(), b.getAsJsonArray());
        } else if (a.isJsonObject() && b.isJsonObject()) {
            result = merge(a.getAsJsonObject(), b.getAsJsonObject());
        } else {
            result = b;
        }
        return result;
    }

    public static JsonElement merge(JsonObject a, JsonObject b) {
        for (Entry<String, JsonElement> e : b.entrySet()) {
            String k = e.getKey();
            JsonElement x = a.get(k);
            JsonElement y = e.getValue();
            JsonElement elt = merge(x, y);
            a.add(k, elt);
        }
        return a;
    }

    public static JsonArray merge(JsonArray a, JsonArray b) {
        Set<JsonElement> set = new HashSet<>(a.size());
        a.forEach(set::add);
        for (Iterator<JsonElement> it = b.iterator(); it.hasNext();) {
            JsonElement item = it.next();
            if (!set.contains(item)) {
                set.add(item);
                // Ensure the merged data is independent of b - therefore deep copy!
                a.add(item.deepCopy());
            }
        }
        return a;
    }
}