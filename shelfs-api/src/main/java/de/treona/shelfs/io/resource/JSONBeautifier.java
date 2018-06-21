package de.treona.shelfs.io.resource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.json.JSONObject;

public class JSONBeautifier {

    public static String beautifyJSONObject(JSONObject sourceObject) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser jsonParser = new JsonParser();
        JsonElement gsonObject = jsonParser.parse(sourceObject.toString());

        return gson.toJson(gsonObject);
    }

    public static JSONObject beautifyJSONObjectToObject(JSONObject sourceObject) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser jsonParser = new JsonParser();
        JsonElement gsonObject = jsonParser.parse(sourceObject.toString());

        return new JSONObject(gson.toJson(gsonObject));
    }
}
