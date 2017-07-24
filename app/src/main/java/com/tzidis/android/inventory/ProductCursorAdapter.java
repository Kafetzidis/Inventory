package com.tzidis.android.inventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import java.text.NumberFormat;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.tzidis.android.inventory.data.InventoryContract.ProductsEntry;

import java.util.Locale;

/**
 * {@link ProductCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of product data as its data source. This adapter knows
 * how to create list items for each row of product data in the {@link Cursor}.
 */

public class ProductCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link ProductCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the product data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current product can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        // Find fields to populate in inflated template
        TextView nameTextView = (TextView) view.findViewById(R.id.product_name);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);

        // Find the columns of product attributes that we're interested in
        final int id = cursor.getInt(cursor.getColumnIndex(ProductsEntry._ID));
        int nameColumnIndex = cursor.getColumnIndex(ProductsEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ProductsEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ProductsEntry.COLUMN_PRODUCT_QUANTITY);

        // Extract properties from cursor
        String productName = cursor.getString(nameColumnIndex);

        //Format the price
        Double productPrice = cursor.getDouble(priceColumnIndex);
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.getDefault());
        String productPriceFormatted = formatter.format(productPrice).toString();

        final int productQuantity = cursor.getInt(quantityColumnIndex);

        // Populate fields with extracted properties
        nameTextView.setText(productName);
        priceTextView.setText(productPriceFormatted);
        quantityTextView.setText(Integer.toString(productQuantity));

        //Find the sale button
        Button button = (Button) view.findViewById(R.id.sale_button);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                Uri currentProductUri = ContentUris.withAppendedId(ProductsEntry.CONTENT_URI, id);
                reduceProductQuantity(context, view, productQuantity, currentProductUri);
            }

        });
    }

    private void reduceProductQuantity(Context context, View view, int quantity, Uri currentProductUri) {

        if (quantity > 0) {
            quantity--;

            ContentValues values = new ContentValues();
            values.put(ProductsEntry.COLUMN_PRODUCT_QUANTITY, quantity);
            context.getContentResolver().update(currentProductUri, values, null, null);
        }
        else {
            Toast.makeText(view.getContext(), "This product has no stock", Toast.LENGTH_SHORT).show();
        }
    }
}