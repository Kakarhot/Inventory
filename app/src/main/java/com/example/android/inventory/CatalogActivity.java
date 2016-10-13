package com.example.android.inventory;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;

import com.example.android.inventory.data.InventoryContract.InventoryEntry;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    public static final String LOG_TAG = CatalogActivity.class.getSimpleName();

    private static final int INVENTORY_LOADER = 0;

    private InventoryCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);


        mCursorAdapter = new InventoryCursorAdapter(this,null);
        ListView listView = (ListView)findViewById(R.id.list_view_pet);
        View emptyView = findViewById(R.id.empty_view);
        listView.setEmptyView(emptyView);
        listView.setAdapter(mCursorAdapter);

        getSupportLoaderManager().initLoader(INVENTORY_LOADER, null, CatalogActivity.this);
    }


    public void addItem(View view) {
        Intent intent = new Intent(CatalogActivity.this, AddItemActivity.class);
        startActivity(intent);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_INVENTORY_NAME,
                InventoryEntry.COLUMN_INVENTORY_QUANTITY,
                InventoryEntry.COLUMN_INVENTORY_SOLD,
                InventoryEntry.COLUMN_INVENTORY_PRICE
        };

        return new CursorLoader(this,
                InventoryEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}
