package com.tzidis.android.inventory.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class InventoryContract {

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.tzidis.android.inventory";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     */
    public static final String PATH_PRODUCTS = "products";

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private InventoryContract() {}

    /* Inner class that defines the table contents */
    public static class ProductsEntry implements BaseColumns {

        // Table name
        public static final String TABLE_NAME = "products";
        // The unique id
        public static final String _ID = BaseColumns._ID;
        // Name Column
        public static final String COLUMN_PRODUCT_NAME = "name";
        // Description Column
        public static final String COLUMN_PRODUCT_DESCRIPTION = "description";
        // Price Column
        public static final String COLUMN_PRODUCT_PRICE = "price";
        // Supplier Column
        public static final String COLUMN_PRODUCT_SUPPLIER = "supplier";
        // Quantity Column
        public static final String COLUMN_PRODUCT_QUANTITY = "quantity";
        // Image Column
        public static final String COLUMN_PRODUCT_IMAGE = "image";
        //Content Uri
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of products.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single product.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

    }
}
