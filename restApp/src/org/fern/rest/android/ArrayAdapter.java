package org.fern.rest.android;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

/**
 * <p>
 * This method is a wrapper for the @see android.widget.ArrayAdapter class. It
 * was written to add some extra functionality to the class, for easier list
 * item manipulation.
 * </p>
 * <p>
 * It was chosen not to support <code>T[]</code> type arguments, because they
 * are more difficult to manipulate later on. <code>List&lt;T&gt;</code> objects
 * are very easily manipulated, however.
 * </p>
 * 
 * @author Sergio Enriquez
 * @author Andrew Hays
 * @param <T> The type of Array that the adapter will store.
 */
public class ArrayAdapter<T> extends android.widget.ArrayAdapter<T> {
    private List<T> mItems;
    
    /**
     * This constructor is used for creating an ArrayAdapter with a preset list.
     * 
     * @param context A context provider for accessing the application.
     * @param textViewResourceId The resource to use for adding items to the @see
     *            android.view.View.
     * @param objects The objects that are going to be added to the
     *            ArrayAdapter.
     */
    public ArrayAdapter(Context context, int textViewResourceId, List<T> objects) {
        super(context, textViewResourceId, objects);
        this.mItems = objects;
    }

    /**
     * This method is for using a blank list to start out. This should be
     * preferred for the use of filling a list afterwards.
     * 
     * @param context A context provider for accessing the application.
     * @param textViewResourceId The resource to use for adding items to the @see
     *            android.view.View.
     */
    public ArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId, new ArrayList<T>());
        this.mItems = new ArrayList<T>();
    }

    /**
     * Adds an entire list of items to the collection. Unfortunately, this must
     * go through each item individually to add it, since addAll wasn't
     * introduced to the Android framework until SDK 11
     * 
     * @param items The list of items to add to the collection.
     */
    public void addAll(List<T> items) {
        for (T item: items) {
            this.add(item);
        }
        mItems.addAll(items);
    }
 
    /**
     * Resets the list to the list of given items.
     * 
     * @param items The list of items to reset to.
     */
    public void setList(List<T> items) {
        this.clear();
        this.addAll(items);
    }
    
    @Override
    public void clear() {
        super.clear();
        mItems.clear();
    }
    
    public List<T> getItems() {
        return mItems;
    }

    /**
     * Original method used to refresh to this. @#setList(List) should be used
     * instead.
     * 
     * @param items The items to reset the list to.
     */
    @Deprecated
    public void refreshList(List<T> items) {
        this.setList(items);
    }

}
