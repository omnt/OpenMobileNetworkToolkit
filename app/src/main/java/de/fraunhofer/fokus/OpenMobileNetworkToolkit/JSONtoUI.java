package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.content.Context;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.Gravity;

import androidx.cardview.widget.CardView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;

import android.graphics.Color;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.Information;
public class JSONtoUI {
    private static final String TAG = "JSONtoUI";
    private static final int CARD_RADIUS = 9;
    private static final int CARD_ELEVATION = 9;
    private static final int TEXT_SIZE_LARGE = 20;
    private static final int TEXT_SIZE_MEDIUM = 16;
    private static final int TEXT_SIZE_SMALL = 14;
    private static final String DEFAULT_VALUE = "N/A";

    private LinearLayout createRow(Context context) {
        LinearLayout rowLayout = new LinearLayout(context);
        rowLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        rowLayout.setOrientation(LinearLayout.HORIZONTAL);
        return rowLayout;
    }

    public int getColor(JSONObject min, JSONObject max, float value) {
        // Ensure value is within min and max
        int minValue = min.optInt("value");
        int maxValue = max.optInt("value");
        if (value < minValue) value = minValue;
        if (value > maxValue) value = maxValue;

        // Normalize the value to be within [0, 1]
        float normalizedValue = (value - minValue) / (maxValue - minValue);

        // Get colors from JSON
        int minColor = Color.parseColor(min.optString("color"));
        int maxColor = Color.parseColor(max.optString("color"));

        // Calculate the red, green, and blue components based on the normalized value
        int red = (int) ((1 - normalizedValue) * Color.red(minColor) + normalizedValue * Color.red(maxColor));
        int green = (int) ((1 - normalizedValue) * Color.green(minColor) + normalizedValue * Color.green(maxColor));
        int blue = (int) ((1 - normalizedValue) * Color.blue(minColor) + normalizedValue * Color.blue(maxColor));

        return Color.rgb(red, green, blue);
    }

    private CardView createCard(Context context, String title, String value, JSONObject min, JSONObject max) {
        CardView card = new CardView(context);
        card.setLayoutParams(createCardLayoutParams());
        card.setRadius(CARD_RADIUS);
        card.setCardElevation(CARD_ELEVATION);
        card.setMaxCardElevation(CARD_ELEVATION);
        card.setUseCompatPadding(true);
        card.setPreventCornerOverlap(true);

        LinearLayout cardContent = new LinearLayout(context);
        cardContent.setOrientation(LinearLayout.VERTICAL);

        cardContent.addView(createTextView(context, title, TEXT_SIZE_LARGE, Gravity.CENTER));
        cardContent.addView(createTextView(context, formatValue(value), determineTextSize(value), Gravity.CENTER));

        if(formatValue(value).equals(DEFAULT_VALUE) || min == null|| max == null) {
            card.setCardBackgroundColor(Color.DKGRAY);
        } else {
            card.setCardBackgroundColor(getColor(min, max, Float.parseFloat(value)));
        }

        card.addView(cardContent);
        return card;
    }

    private LinearLayout.LayoutParams createCardLayoutParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        params.weight = 1;
        return params;
    }

    private TextView createTextView(Context context, String text, int textSize, int gravity) {
        TextView textView = new TextView(context);
        textView.setText(text);
        textView.setTextSize(textSize);
        textView.setPadding(10, 10, 10, 10);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = gravity;
        textView.setLayoutParams(params);
        return textView;
    }

    private String formatValue(String value) {
        if (value.isEmpty() || value.equals(String.valueOf(Integer.MAX_VALUE))) {
            return DEFAULT_VALUE;
        }
        return value;
    }

    private int determineTextSize(String value) {
        if(value.length() < 5) return TEXT_SIZE_LARGE;
        if(value.length() < 8) return TEXT_SIZE_MEDIUM;
        return TEXT_SIZE_SMALL;
    }

    public LinearLayout createUIFromJSON(Context context, JSONObject jsonObj, Information data) {
        LinearLayout mainLayout = new LinearLayout(context);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        JSONObject table = jsonObj.optJSONObject("table");
        if (table == null) return mainLayout;

        HashMap<String, String> informationMap = data.getInformation();
        for (Iterator<String> it = table.keys(); it.hasNext(); ) {
            String key = it.next();
            JSONObject row = table.optJSONObject(key);
            if (row == null) continue;

            LinearLayout rowLayout = createRow(context);
            processRow(context, row, informationMap, rowLayout);
            mainLayout.addView(rowLayout);
        }

        return mainLayout;
    }

    private void processRow(Context context, JSONObject row, HashMap<String, String> informationMap, LinearLayout rowLayout) {
        for (Iterator<String> iter = row.keys(); iter.hasNext(); ) {
            String columnKey = iter.next();
            JSONArray valueArray = row.optJSONArray(columnKey);
            if (valueArray == null) continue;

            for (int i = 0; i < valueArray.length(); i++) {
                JSONObject valueObject = valueArray.optJSONObject(i);
                if (valueObject == null) continue;

                String valKey = valueObject.optString("key");
                String valValue = extractValues(valueObject.optString("parameter"), informationMap);
                JSONObject range = valueObject.optJSONObject("range");
                JSONObject min = null;
                JSONObject max = null;
                if (range != null) {
                    min = range.optJSONObject("min");
                    max = range.optJSONObject("max");
                }
                rowLayout.addView(createCard(context, valKey, valValue, min, max));

            }
        }
    }

    private String extractValues(String parameterKey, HashMap<String, String> informationMap) {
        if (parameterKey == null) return DEFAULT_VALUE;

        StringBuilder valValue = new StringBuilder();
        for (String key : parameterKey.split(",")) {
            String valueString = informationMap.get(key);
            if (valueString != null) {
                valValue.append(valueString);
            }
        }
        return valValue.length() > 0 ? valValue.toString() : DEFAULT_VALUE;
    }

    public JSONObject loadJsonFromAsset(Context context, String path) {
        try (InputStream is = context.getAssets().open(path)) {
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            String jsonString = new String(buffer, StandardCharsets.UTF_8);
            return new JSONObject(jsonString);
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Failed to load JSON from asset: " + path, e);
            return null;
        }
    }
}
