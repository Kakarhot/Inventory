package com.example.android.inventory;

/**
 * Created by wenshuo on 2016/10/11.
 */

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventory.data.InventoryContract;

public class InventoryCursorAdapter extends CursorAdapter {

    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item,parent,false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        final TextView nameView = (TextView)view.findViewById(R.id.name);
        final TextView quantityView = (TextView)view.findViewById(R.id.quantity);
        final TextView priceView = (TextView)view.findViewById(R.id.price);
        final TextView soldView = (TextView)view.findViewById(R.id.sold);
        Button saleButton  = (Button)view.findViewById(R.id.sale_button);
        LinearLayout parentView = (LinearLayout)view.findViewById(R.id.list_product) ;

        final int rowId = cursor.getInt(cursor.getColumnIndex(InventoryContract.InventoryEntry._ID));

        parentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context,EditorActivity.class);
                intent.setData(ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI,rowId));
                context.startActivity(intent);
            }
        });

        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int soldQuantity = Integer.parseInt(soldView.getText().toString());
                int quantity = Integer.parseInt(quantityView.getText().toString());
                if(quantity > 0) {
                    quantity = quantity - 1;
                    soldQuantity = soldQuantity + 1;
                }
                ContentValues values = new ContentValues();
                values.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_QUANTITY,quantity);
                values.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_SOLD,soldQuantity);

                Uri uri = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI,rowId);
                int rowUpdated = context.getContentResolver().update(uri,values,null,null);
                if (rowUpdated == 0)
                    Toast.makeText(context,"Update Error", Toast.LENGTH_SHORT).show();
            }
        });

        String name = cursor.getString(cursor.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_INVENTORY_NAME));
        String quantity = cursor.getString(cursor.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_INVENTORY_QUANTITY));
        String sold = cursor.getString(cursor.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_INVENTORY_SOLD));
        String price = cursor.getString(cursor.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_INVENTORY_PRICE));

        nameView.setText(name);
        quantityView.setText(quantity);
        soldView.setText(sold);
        priceView.setText(price);
    }
}
