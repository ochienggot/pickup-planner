package com.nicholasgot.citypulse.androidapp.autocompletetext;

import java.util.HashMap;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

public class CustomAutoCompleteTextView extends AutoCompleteTextView{

	public CustomAutoCompleteTextView(Context context,AttributeSet attrs) {
		super(context,attrs);
		// TODO Auto-generated constructor stub
	}
	
	
	
	protected CharSequence convertSelectionToString(Object selectedItem)
	{
		HashMap<String,String> hm = (HashMap<String, String>) selectedItem;
		return hm.get("description");
	}

}
