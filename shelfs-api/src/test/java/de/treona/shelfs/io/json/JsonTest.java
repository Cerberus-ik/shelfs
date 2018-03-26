package de.treona.shelfs.io.json;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;


public class JsonTest {

    @Test
    public void tryParse() {
        try {
            JSONObject jsonObject = new JSONObject("{\"data\": \"hello\"}");
        } catch (JSONException e) {
            fail(e);
        }
        try {
            JSONObject invalideObject = new JSONObject("not valid json");
            fail("Not valid json, no exception.");
        } catch (JSONException ignore) {
        }
    }
}
