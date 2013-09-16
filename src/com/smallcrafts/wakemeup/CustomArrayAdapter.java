package com.smallcrafts.wakemeup;

import java.util.List;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;

public class CustomArrayAdapter<T> extends ArrayAdapter<T> {
	
	private Filter filter;
	public List<T> items;
	
	@Override
	public Filter getFilter(){
		if (filter == null){
			filter = new NoFilter();
		}
		return filter;
	}

	public CustomArrayAdapter(Context context, int textViewResourceId,
            List<T> objects) {
        super(context, textViewResourceId, objects);
        items = objects;
    }
	
	public CustomArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }
	
	private class NoFilter extends Filter{

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			return new FilterResults();
		}

		@Override
		protected void publishResults(CharSequence constraint,
				FilterResults results) {
		}
	}
}
