package com.example.android.inventory;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventory.data.InventoryContract.InventoryEntry;

import java.io.FileDescriptor;
import java.io.IOException;

/**
 * Created by wenshuo on 2016/10/11.
 */

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = EditorActivity.class.getSimpleName();

    private TextView mNameTextView;
    private TextView mQuantityTextView;
    private TextView mPriceTextView;
    private ImageView mImageView;

    private Uri mCurrentUri;
    private static final int INVENTORY_LOADER = 0;
    private int mCurrentQuantity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        mNameTextView = (TextView) findViewById(R.id.edit_inventory_name);
        mQuantityTextView = (TextView) findViewById(R.id.edit_inventory_quantity);
        mPriceTextView = (TextView) findViewById(R.id.edit_inventory_price);
        mImageView = (ImageView) findViewById(R.id.image);

        Intent intent = getIntent();
        mCurrentUri = intent.getData();
            getSupportLoaderManager().initLoader(INVENTORY_LOADER, null, this);
    }



    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_INVENTORY_NAME,
                InventoryEntry.COLUMN_INVENTORY_QUANTITY,
                InventoryEntry.COLUMN_INVENTORY_PRICE,
                InventoryEntry.COLUMN_INVENTORY_IMAGE

        };

        return new CursorLoader(this,
                mCurrentUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (cursor.moveToFirst()) {
            mNameTextView.setText(cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_NAME)));
            mCurrentQuantity = cursor.getInt(cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_QUANTITY));
            mQuantityTextView.setText(String.valueOf(mCurrentQuantity));
            mPriceTextView.setText(Integer.toString(cursor.getInt(cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_PRICE))));
            String uri_string = cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_IMAGE));
            if (!TextUtils.isEmpty(uri_string)) {
                Uri imageUri = Uri.parse(uri_string);
                mImageView.setImageBitmap(getBitmapFromUri(imageUri));
            }
            else
                mImageView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mNameTextView.setText("");
        mQuantityTextView.setText("");
        mPriceTextView.setText("");
    }


    public void showDeleteConfirmationDialog(View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                deleteInventory();
            }
        });
        builder.setNegativeButton(R.string.cancel, null);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteInventory() {

        int rowDeleted = getContentResolver().delete(mCurrentUri,null,null);
        if (rowDeleted == 0)
            Toast.makeText(this, getString(R.string.editor_delete_product_failed), Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, getString(R.string.editor_delete_product_successful), Toast.LENGTH_SHORT).show();

        finish();
    }

    public void quantityPlus(View view) {

        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_INVENTORY_QUANTITY, mCurrentQuantity + 1);
        int rowUpdated = getContentResolver().update(mCurrentUri, values, null, null);
        if (rowUpdated == 0)
            Toast.makeText(this, "Update Error", Toast.LENGTH_SHORT).show();
    }

    public void quantityMinus(View view) {

        if (mCurrentQuantity > 0) {
            ContentValues values = new ContentValues();
            values.put(InventoryEntry.COLUMN_INVENTORY_QUANTITY, mCurrentQuantity - 1);
            int rowUpdated = getContentResolver().update(mCurrentUri, values, null, null);
            if (rowUpdated == 0)
                Toast.makeText(this, "Update Error", Toast.LENGTH_SHORT).show();
        }
    }

    public void orderMore(View view) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_SUBJECT, "Need more " + mNameTextView.getText().toString());
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
        else
            Toast.makeText(this, getString(R.string.email_app_needed), Toast.LENGTH_SHORT).show();
    }

    private Bitmap getBitmapFromUri(Uri uri) {
        ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            parcelFileDescriptor =
                    getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            parcelFileDescriptor.close();
            return image;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                if (parcelFileDescriptor != null) {
                    parcelFileDescriptor.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Error closing ParcelFile Descriptor");
            }
        }
    }
}