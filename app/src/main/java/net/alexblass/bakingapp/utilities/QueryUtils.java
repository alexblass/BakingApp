package net.alexblass.bakingapp.utilities;

import android.text.TextUtils;
import android.util.Log;

import net.alexblass.bakingapp.models.Recipe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Helper methods to request and receive Recipe data.
 */

public class QueryUtils {

    // Log tag for error messages
    private static final String LOG_TAG = QueryUtils.class.getSimpleName();

    // Empty private constructor because no QueryUtils
    // object should be initialized
    private QueryUtils(){}

    // Query the JSON url and return a list of Recipes
    public static Recipe[] fetchRecipes(String requestUrl){
        URL url = createUrl(requestUrl);

        // perform the HTTP request to the URL to receive a JSON response
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request", e);
        }

        // Extract the data and create a list of Recipes
        Recipe[] recipes = extractFeatureFromJson(jsonResponse);
        return recipes;
    }

    // Returns the new URL object from the given String url
    private static URL createUrl(String stringUrl){
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL", e);
        }
        return url;
    }

    // Make an HTTP request to the JSON url and return a String as the response
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        if (url == null){
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(1000);
            urlConnection.setConnectTimeout(1500);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful, then read the input stream
            // and parse the response
            if (urlConnection.getResponseCode() == 200){
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the movie JSON results.", e);
        } finally {
            if (urlConnection != null){
                urlConnection.disconnect();
            }
            if (inputStream != null){
                inputStream.close();
            }
        }

        return jsonResponse;
    }

    // Convert the InputStream into a String that contains the whole JSON file
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();

        if (inputStream != null) {
            InputStreamReader inputStreamReader =
                    new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }

        return output.toString();
    }

    // Returns a list of Recipes built from parsing the JSON response
    private static Recipe[] extractFeatureFromJson(String jsonResponse){
        if (TextUtils.isEmpty(jsonResponse)){
            return null;
        }

        Recipe[] recipes = new Recipe[0];

        // Try to parse the JSON response  If there's a formatting problem,
        // an exception will be thrown
        try {
            JSONArray baseJsonResponse = new JSONArray(jsonResponse);
            recipes = new Recipe[baseJsonResponse.length()];

            for (int i = 0; i < baseJsonResponse.length(); i++){
                JSONObject currentRecipe = baseJsonResponse.getJSONObject(i);

                String recipeName = currentRecipe.getString("name");
                int recipeServings = currentRecipe.getInt("servings");

                // TODO: Fetch other data from JSON

                Recipe newRecipe = new Recipe(recipeName, recipeServings);
                recipes[i] = newRecipe;
            }
        } catch (JSONException e){
            Log.e(LOG_TAG, "Problem parsing the JSON response.");
        }
        return recipes;
    }
}