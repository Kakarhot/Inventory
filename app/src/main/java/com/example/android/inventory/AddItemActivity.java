package com.example.android.inventory;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventory.data.InventoryContract;

import java.io.FileDescriptor;
import java.io.IOException;

/**
 * Created by wenshuo on 2016/10/12.
 */

public class AddItemActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 0;
    private static final String LOG_TAG = AddItemActivity.class.getSimpleName();

    private EditText mNameEditText;
    private EditText mQuantityEditText;
    private EditText mPriceEditText;
    private ImageView mImageView;
    private Uri mUri = null;

    private boolean mInventoryHasChanged = false;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mInventoryHasChanged = true;
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        mNameEditText = (EditText) findViewById(R.id.edit_inventory_name);
        mQuantityEditText = (EditText) findViewById(R.id.edit_inventory_quantity);
        mPriceEditText = (EditText) findViewById(R.id.edit_inventory_price);
        mImageView = (ImageView) findViewById(R.id.image);

        mNameEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
    }


    public void saveInventory(View view){

        if (TextUtils.isEmpty(mNameEditText.getText()) && TextUtils.isEmpty(mQuantityEditText.getText()) && TextUtils.isEmpty(mPriceEditText.getText()) && mUri == null)
            return;

        String nameString;
        if (!TextUtils.isEmpty(mNameEditText.getText()))
            nameString = mNameEditText.getText().toString().trim();
        else {
            Toast.makeText(this, getString(R.string.name_required_error), Toast.LENGTH_SHORT).show();
            return;
        }

        int quantity = 0;
        if (!TextUtils.isEmpty(mQuantityEditText.getText()))
            quantity = Integer.parseInt(mQuantityEditText.getText().toString().trim());

        int price;
        if (!TextUtils.isEmpty(mPriceEditText.getText()))
            price = Integer.parseInt(mPriceEditText.getText().toString().trim());
        else {
            Toast.makeText(this, getString(R.string.price_required_error), Toast.LENGTH_SHORT).show();
            return;
        }

        String imageUri = "";
        if (mUri != null)
            imageUri = mUri.toString();

        ContentValues values = new ContentValues();
        values.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_NAME,nameString);
        values.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_QUANTITY,quantity);
        values.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_PRICE,price);
        values.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_IMAGE,imageUri);

        Uri uri = getContentResolver().insert(InventoryContract.InventoryEntry.CONTENT_URI, values);
            if (uri == null)
                Toast.makeText(this, getString(R.string.inventory_saved_error), Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, getString(R.string.inventory_saved), Toast.LENGTH_SHORT).show();

        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (!mInventoryHasChanged) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        NavUtils.navigateUpFromSameTask(AddItemActivity.this);
                    }
                };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, null);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (!mInventoryHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        };

        showUnsavedChangesDialog(discardButtonClickListener);
    }

    public void openImageSelector(View view) {
        Intent intent;

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                mUri = resultData.getData();
                Log.i(LOG_TAG, "Uri: " + mUri.toString());
                mImageView.setImageBitmap(getBitmapFromUri(mUri));
            }
        }
    }

    private Bitmap getBitmapFromUri(Uri uri) {
        ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            parcelFileDescriptor = getContentResolver().openFileDescriptor(uri, "r");
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
