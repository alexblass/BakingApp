package net.alexblass.bakingapp;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import net.alexblass.bakingapp.data.RecipesContract.RecipeEntry;
import net.alexblass.bakingapp.widget.BakingWidgetProvider;
import net.alexblass.bakingapp.widget.WidgetService;

import java.util.ArrayList;
import java.util.List;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID;
import static net.alexblass.bakingapp.data.constants.Keys.PREFS_KEY;
import static net.alexblass.bakingapp.data.constants.Keys.RECIPE_NAME_KEY;
import static net.alexblass.bakingapp.data.constants.Keys.WIDGET_ID_KEY;

/**
 * An Activity that allows users to configure their widget settings to choose a Recipe.
 */

public class ConfigurationActivity extends Activity {

    // A list of the Recipe names
    private List<String> mRecipesNamesList;
    // A list of the Recipe IDs
    private List<Integer> mRecipeIdsList;
    // The spinner of Recipe options
    private Spinner mRecipeOptions;
    // Recipe ID to be saved in SharedPreferences
    private int mSelectedRecipeId;
    // The widget ID
    private int mAppWidgetId;
    // The shared preferences to store our recipe
    SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);
        setResult(RESULT_CANCELED);

        mPrefs = getApplicationContext().getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE);

        initListViews();
    }

    public void initListViews() {

        mRecipeOptions = (Spinner) findViewById(R.id.recipe_spinner);
        mRecipesNamesList = new ArrayList<>();
        mRecipeIdsList = new ArrayList<>();

        // Populate the Spinner with the Recipe data
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(this, R.layout.spinner_item, mRecipesNamesList){
                    // Override the ArrayAdapter to hide the default value/hint "Select"
                    @Override
                    public View getDropDownView(int position, View convertView, ViewGroup parent) {

                        View v = null;

                        if (position == 0) {
                            TextView tv = new TextView(getContext());
                            tv.setHeight(0);
                            tv.setVisibility(View.GONE);
                            v = tv;
                        }
                        else {

                            v = super.getDropDownView(position, null, parent);
                        }

                        parent.setVerticalScrollBarEnabled(false);
                        return v;
                    }
                };

        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        // Add default starting value so Spinner does not show up empty
        adapter.add(getString(R.string.make_selection));

        // Get the Recipes in our Provider
        String[] projection = {
                RecipeEntry._ID,
                RecipeEntry.COLUMN_NAME
        };

        // Check if the recipe exists in the database
        Cursor cursor = this.getContentResolver().query(
                RecipeEntry.CONTENT_URI,
                projection,
                null,
                null,
                null,
                null
        );

        if (cursor != null) {
            if (cursor.moveToFirst()) {

                String name;
                int recipeId;

                recipeId = cursor.getInt(cursor.getColumnIndex(RecipeEntry._ID));
                name = cursor.getString(cursor.getColumnIndex(RecipeEntry.COLUMN_NAME));

                mRecipesNamesList.add(name);
                mRecipeIdsList.add(recipeId);

                while (cursor.moveToNext()) {
                    recipeId = cursor.getInt(cursor.getColumnIndex(RecipeEntry._ID));
                    name = cursor.getString(cursor.getColumnIndex(RecipeEntry.COLUMN_NAME));

                    mRecipesNamesList.add(name);
                    mRecipeIdsList.add(recipeId);
                }
            }
            cursor.close();
        }

        mRecipeOptions.setAdapter(adapter);

        if (getIntent().hasExtra(RECIPE_NAME_KEY)){
            int position = adapter.getPosition(getIntent().getStringExtra(RECIPE_NAME_KEY));
            mRecipeOptions.setSelection(position);
        }

        Button okButton = (Button) findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mRecipeOptions.getSelectedItemPosition() > 0) {
                    handleOkButton();
                } else {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.make_selection_prompt), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void handleOkButton() {
        // Get the ID.  It should be the same position as the list of names in
        // the spinner, except offset by 1 for the index 0 = "Select"
        mSelectedRecipeId = mRecipeIdsList.get(mRecipeOptions.getSelectedItemPosition() - 1);
        showAppWidget();
    }

    private void showAppWidget() {
        SharedPreferences.Editor prefsEditor = mPrefs.edit();

        if (getIntent().hasExtra(WIDGET_ID_KEY)){
            mAppWidgetId = getIntent().getIntExtra(WIDGET_ID_KEY, INVALID_APPWIDGET_ID);

            prefsEditor.putInt("widget" + mAppWidgetId, mSelectedRecipeId);
            prefsEditor.commit();

            Intent intent = new Intent(this, BakingWidgetProvider.class);
            intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
            int ids[] = AppWidgetManager.getInstance(
                    getApplication()).getAppWidgetIds(new ComponentName(getApplication(), BakingWidgetProvider.class));
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,ids);
            sendBroadcast(intent);

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            int appWidgetIds[] = appWidgetManager.getAppWidgetIds(
                    new ComponentName(this, BakingWidgetProvider.class));
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_ingredients_list);

            finish();
        } else {
            mAppWidgetId = INVALID_APPWIDGET_ID;
            Intent intent = getIntent();
            Bundle extras = intent.getExtras();
            if (extras != null) {
                mAppWidgetId = extras.getInt(EXTRA_APPWIDGET_ID,
                        INVALID_APPWIDGET_ID);

                AppWidgetProviderInfo providerInfo = AppWidgetManager.getInstance(
                        getBaseContext()).getAppWidgetInfo(mAppWidgetId);
                String appWidgetLabel = providerInfo.label;

                prefsEditor.putInt("widget" + mAppWidgetId, mSelectedRecipeId);
                prefsEditor.commit();

                Intent startService = new Intent(ConfigurationActivity.this,
                        WidgetService.class);
                startService.putExtra(EXTRA_APPWIDGET_ID, mAppWidgetId);
                startService.setAction("FROM CONFIGURATION ACTIVITY");
                setResult(RESULT_OK, startService);
                startService(startService);

                finish();
            }
            if (mAppWidgetId == INVALID_APPWIDGET_ID) {
                Log.i(ConfigurationActivity.class.getSimpleName(), "Invalid app widget ID");
                finish();
            }
        }
    }
}
