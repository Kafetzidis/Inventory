package com.tzidis.android.inventory;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.tzidis.android.inventory.data.InventoryContract.ProductsEntry;



public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    ProductCursorAdapter productAdapter;
    private static final int PRODUCT_LOADER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open DetailActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, DetailActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the products data
        ListView listView = (ListView) findViewById(R.id.list_view_products);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        listView.setEmptyView(emptyView);

        // Setup cursor adapter using cursor from last step
        productAdapter = new ProductCursorAdapter(this, null);
        // Attach cursor adapter to the ListView
        listView.setAdapter(productAdapter);

        //Set onClickListener for the FAB button

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //Create new intent when a list item is clicked
                Intent itemIntent = new Intent(CatalogActivity.this, DetailActivity.class);
                //Append the id of the item to the Uri to be passed to the DetailActivity
                Uri currentProductUri = ContentUris.withAppendedId(ProductsEntry.CONTENT_URI, id);
                //Set the Uri to the intent
                itemIntent.setData(currentProductUri);
                //Launch the intent
                startActivity(itemIntent);
            }
        });

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    //Helper method to insert dummy data
    private void insertData(){

        Uri mImageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + getResources().getResourcePackageName(R.drawable.no_image)
                + '/' + getResources().getResourceTypeName(R.drawable.no_image) + '/' +
                getResources().getResourceEntryName(R.drawable.no_image));
        String imageString = mImageUri.toString();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(ProductsEntry.COLUMN_PRODUCT_NAME, getString(R.string.dummy_name));
        values.put(ProductsEntry.COLUMN_PRODUCT_DESCRIPTION, getString(R.string.dummy_desc));
        values.put(ProductsEntry.COLUMN_PRODUCT_PRICE, 1.99);
        values.put(ProductsEntry.COLUMN_PRODUCT_SUPPLIER, getString(R.string.dummy_supplier));
        values.put(ProductsEntry.COLUMN_PRODUCT_QUANTITY, 5);
        values.put(ProductsEntry.COLUMN_PRODUCT_IMAGE, imageString);

        // Insert a new row for the dummy data into the provider using the ContentResolver.
        // Use the {@link ProductsEntry#CONTENT_URI} to indicate that we want to insert
        // into the products database table.
        // Receive the new content URI that will allow us to access the dummy's data in the future.
        Uri mNewUri = getContentResolver().insert(
                ProductsEntry.CONTENT_URI,          //The content uri
                values                              //The values to insert
        );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertData();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                ProductsEntry._ID,
                ProductsEntry.COLUMN_PRODUCT_NAME,
                ProductsEntry.COLUMN_PRODUCT_PRICE,
                ProductsEntry.COLUMN_PRODUCT_QUANTITY
        };
        return new CursorLoader(this,
                ProductsEntry.CONTENT_URI,                  // The Content Uri
                projection,                                 // The columns to return
                null,                                       // Selection Criteria
                null,                                       // Selection Criteria
                null                                        // The sort order
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        //Update ProductsCursorAdapter with this new one containing updated product data
        productAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        //Callback called when the data needs to be deleted
        productAdapter.swapCursor(null);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the products.
                deleteAllProducts();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the product in the database.
     */
    private void deleteAllProducts() {
        // Call the ContentResolver to delete all the products.

        int rowsDeleted = getContentResolver().delete(ProductsEntry.CONTENT_URI, null, null);

        // Show a toast message depending on whether or not the delete was successful.
        if (rowsDeleted == 0) {
            // If no rows were deleted, then there was an error with the delete.
            Toast.makeText(this, getString(R.string.editor_delete_all_products_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the delete was successful and we can display a toast.
            Toast.makeText(this, getString(R.string.editor_delete_all_products_successful),
                    Toast.LENGTH_SHORT).show();
        }

    }
}
