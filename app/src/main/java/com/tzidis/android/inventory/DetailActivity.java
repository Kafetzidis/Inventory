package com.tzidis.android.inventory;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.tzidis.android.inventory.data.InventoryContract.ProductsEntry;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static com.tzidis.android.inventory.data.InventoryProvider.LOG_TAG;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;

/**
 * Allows user to create a new product or edit an existing one.
 */
public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * The request code to store image from the gallery
     */
    private static final int PICK_IMAGE_REQUEST = 0;

    /**
     * Identifier for the product loader
     */
    private static final int EXISTING_PRODUCT_LOADER = 1;

    /**
     * EditText field to enter the product's name
     */
    private EditText mNameEditText;

    /**
     * EditText field to enter the product's description
     */
    private EditText mDescriptionEditText;

    /**
     * EditText field to enter the product's price
     */
    private EditText mPriceEditText;

    /**
     * EditText field to enter the product's supplier
     */
    private EditText mSupplierEditText;

    /**
     * EditText field to enter the product's quantity
     */
    private EditText mQuantityEditText;

    /**
     * ImageView field to enter the product's image
     */
    private ImageView mImageView;

    /**
     * Button to decrease the product's quantity
     */
    private Button mMinusButton;

    /**
     * Button to increase the product's quantity
     */
    private Button mPlusButton;

    /**
     * Button to order the product
     */
    private Button mOrderButton;

    /**
     * Button to delete the product
     */
    private Button mDeleteButton;

    /**
     * Uri from intent
     */
    private Uri mCurrentProductUri;

    /**
     * Product image Uri
     */
    private Uri mImageUri;

    /**
     * Boolean flag that keeps track of whether the product has been edited (true) or not (false)
     */
    private boolean mProductHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, so we set the mProductHasChanged to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mOrderButton = (Button) findViewById(R.id.order_button);
        mDeleteButton = (Button) findViewById(R.id.delete_button);

        //Examine the intent to figure out if we are inserting a new product or editing
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        //If the intent does not contain a Uri we know we are creating a new product
        if (mCurrentProductUri == null) {
            setTitle(getString(R.string.add_product));
            // Hide the "Delete" and "Order" buttons.
            // (It doesn't make sense to delete a product or order one that hasn't been created yet.)
            mOrderButton.setVisibility(View.GONE);
            mDeleteButton.setVisibility(View.GONE);
        } else {
            setTitle(getString(R.string.edit_product));
            //Kick off loader
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mDescriptionEditText = (EditText) findViewById(R.id.edit_product_description);
        mPriceEditText = (EditText) findViewById(R.id.edit_product_price);
        mSupplierEditText = (EditText) findViewById(R.id.edit_product_supplier);
        mQuantityEditText = (EditText) findViewById(R.id.edit_product_quantity);
        mImageView = (ImageView) findViewById(R.id.edit_product_image);
        mMinusButton = (Button) findViewById(R.id.button_minus);
        mPlusButton = (Button) findViewById(R.id.button_plus);

        mNameEditText.setOnTouchListener(mTouchListener);
        mDescriptionEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mSupplierEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mImageView.setOnTouchListener(mTouchListener);
        mMinusButton.setOnTouchListener(mTouchListener);
        mPlusButton.setOnTouchListener(mTouchListener);

        // Set a clickListener on minus button
        mMinusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(mQuantityEditText.getText().toString())) {
                    int quantity = parseInt(mQuantityEditText.getText().toString().trim());
                    if (quantity == 0) {
                        quantity = 0;
                        Toast.makeText(view.getContext(), getString(R.string.negative_quantity_error),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        quantity--;
                    }
                    mQuantityEditText.setText(Integer.toString(quantity));
                }
            }
        });

        // Set a clickListener on plus button
        mPlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity;
                if (!TextUtils.isEmpty(mQuantityEditText.getText().toString())) {
                    quantity = parseInt(mQuantityEditText.getText().toString().trim());
                    quantity++;
                } else {
                    quantity = 1;
                }
                mQuantityEditText.setText(Integer.toString(quantity));
            }
        });

        // Set a clickListener on email button
        mOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:" + mSupplierEditText.getText().toString().trim()));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

        // Set a clickListener on delete button
        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
            }
        });

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImageSelector();
            }
        });
    }

    @Override
    public void onBackPressed() {
        // If the product hasn't changed, continue with handling back button press
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    //Gets user input data from editor and inserts them into the database
    private boolean saveProduct() {

        //Find the relevant editTexts and get user input
        String nameString = mNameEditText.getText().toString().trim();
        String descriptionString = mDescriptionEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String supplierString = mSupplierEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();


        //Check if the fields are blank before saving
        if (TextUtils.isEmpty(nameString) && TextUtils.isEmpty(descriptionString) &&
                TextUtils.isEmpty(priceString) && TextUtils.isEmpty(supplierString) &&
                TextUtils.isEmpty(quantityString)){
            return false;
        }else if(TextUtils.isEmpty(nameString)){
            Toast.makeText(this, R.string.no_name_error,
                    Toast.LENGTH_SHORT).show();
            return false;
        }else if(TextUtils.isEmpty(priceString)) {
            Toast.makeText(this, R.string.no_price_error,
                    Toast.LENGTH_SHORT).show();
            return false;
        }else if(TextUtils.isEmpty(quantityString)) {
            Toast.makeText(this, R.string.no_quantity_error,
                    Toast.LENGTH_SHORT).show();
            return false;
        }else if (mImageUri == null) {
            Toast.makeText(this, R.string.no_image_error,
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        // Parse the price string into Double

        Double price ;
        if (!TextUtils.isEmpty(priceString)) {
            price = parseDouble(priceString);
        }else{return false;}

        // Parse the quantity string into int

        int quantity ;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = parseInt(quantityString);
        }else{return false;}


        if (mImageUri == null) {
            mImageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                    "://" + getResources().getResourcePackageName(R.drawable.no_image)
                    + '/' + getResources().getResourceTypeName(R.drawable.no_image) + '/' +
                    getResources().getResourceEntryName(R.drawable.no_image));
        }

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(ProductsEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(ProductsEntry.COLUMN_PRODUCT_DESCRIPTION, descriptionString);
        values.put(ProductsEntry.COLUMN_PRODUCT_PRICE, price);
        values.put(ProductsEntry.COLUMN_PRODUCT_SUPPLIER, supplierString);
        values.put(ProductsEntry.COLUMN_PRODUCT_QUANTITY, quantity);
        values.put(ProductsEntry.COLUMN_PRODUCT_IMAGE, mImageUri.toString());

        //If the intent does not contain a Uri we know we are creating a new product
        if (mCurrentProductUri == null) {
            // Insert the new row, returning the primary key value of the new row
            Uri mNewUri = getContentResolver().insert(
                    ProductsEntry.CONTENT_URI,          //The content uri
                    values                              //The values to insert
            );

            //Show a toast message depending on whether or not the insertion was successful
            if (mNewUri != null) {
                Toast.makeText(this, R.string.insert_product_toast_error, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.insert_product_toast_success), Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(
                    mCurrentProductUri,                  //The content uri
                    values,                              //The values to insert
                    null,
                    null
            );

            //Show a toast message depending on whether or not the insertion was successful
            if (rowsAffected != 0) {
                Toast.makeText(this, R.string.insert_product_toast_error, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.insert_product_toast_success), Toast.LENGTH_SHORT).show();
            }
        }

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_detail.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save product to the database and exit
                if(saveProduct())
                finish();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the product hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(DetailActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(DetailActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
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
                ProductsEntry.COLUMN_PRODUCT_DESCRIPTION,
                ProductsEntry.COLUMN_PRODUCT_PRICE,
                ProductsEntry.COLUMN_PRODUCT_SUPPLIER,
                ProductsEntry.COLUMN_PRODUCT_QUANTITY,
                ProductsEntry.COLUMN_PRODUCT_IMAGE
        };
        return new CursorLoader(this,
                mCurrentProductUri,                         // The Content Uri
                projection,                                 // The columns to return
                null,                                       // Selection Criteria
                null,                                       // Selection Criteria
                null                                        // The sort order
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of product attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(ProductsEntry.COLUMN_PRODUCT_NAME);
            int descriptionColumnIndex = cursor.getColumnIndex(ProductsEntry.COLUMN_PRODUCT_DESCRIPTION);
            int priceColumnIndex = cursor.getColumnIndex(ProductsEntry.COLUMN_PRODUCT_PRICE);
            int supplierColumnIndex = cursor.getColumnIndex(ProductsEntry.COLUMN_PRODUCT_SUPPLIER);
            int quantityColumnIndex = cursor.getColumnIndex(ProductsEntry.COLUMN_PRODUCT_QUANTITY);
            int imageColumnIndex = cursor.getColumnIndex(ProductsEntry.COLUMN_PRODUCT_IMAGE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String description = cursor.getString(descriptionColumnIndex);
            Double price = cursor.getDouble(priceColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String image = cursor.getString(imageColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mDescriptionEditText.setText(description);
            mPriceEditText.setText(Double.toString(price));
            mSupplierEditText.setText(supplier);
            mQuantityEditText.setText(Integer.toString(quantity));
            mImageUri = Uri.parse(image);
            mImageView.setImageBitmap(getBitmapFromUri(mImageUri));
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText(null);
        mDescriptionEditText.setText(null);
        mPriceEditText.setText(null);
        mSupplierEditText.setText(null);
        mQuantityEditText.setText(null);
        mImageUri = null;
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
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
    private void deleteProduct() {
        // Only perform the delete if this is an existing product.
        if (mCurrentProductUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentProductUri
            // content URI already identifies the product that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }

    //Method to import image from the gallery
    public void openImageSelector() {
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
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.  Pull that uri using "resultData.getData()"

            if (resultData != null) {
                mImageUri = resultData.getData();
                Log.i(LOG_TAG, "Uri: " + mImageUri.toString());

                mImageView.setImageBitmap(getBitmapFromUri(mImageUri));
            }
        }
    }

    public Bitmap getBitmapFromUri(Uri uri) {

        if (uri == null || uri.toString().isEmpty())
            return null;

        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;

            input = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, "Failed to load image.", fne);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {

            }
        }
    }
}