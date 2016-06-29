package com.nicholasgot.clientapp.autocomplete;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

import java.util.HashMap;

/**
 * custom autocomplete textview class for pickup location
 */
public class CustomAutocompleteTextView extends AutoCompleteTextView {
    private static final String DESCRIPTION = "description";

    public CustomAutocompleteTextView(Context context, AttributeSet attrs) {
        super(context,attrs);
    }

    protected CharSequence convertSelectionToString(Object selectedItem) {
        HashMap<String,String> hm = (HashMap<String, String>) selectedItem;
        return hm.get(DESCRIPTION);
    }
}
