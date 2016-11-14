package views;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;

import com.suntrans.smartfire.R;

import java.util.LinkedHashMap;
import java.util.Map;

public class SectionListAdapter extends BaseAdapter {

	 public final Map<String, Adapter> sections = new LinkedHashMap<String, Adapter>();    //内容区
	    public final ArrayAdapter<String> headers;      //标题区，只有字符串显示
	    public final static int TYPE_SECTION_HEADER = 0;  
	  
	    public SectionListAdapter(Context context) {  
	        headers = new ArrayAdapter<String>(context, R.layout.list_header);  
	    }  
	  
	    public void addSection(String section, Adapter adapter) {  
	        this.headers.add(section);  
	        this.sections.put(section, adapter);  
	    }  
	  
	@Override
	public int getCount() {
		// total together all sections, plus one for each section header  
        int total = 0; 
        for (Adapter adapter : this.sections.values())  
            total += adapter.getCount() + 1;  
        return total;
	}

	public int getViewTypeCount() {  
        // assume that headers count as one, then total all sections  
        int total = 1;  
        for (Adapter adapter : this.sections.values())  
            total += adapter.getViewTypeCount();  
        return total;  
    }  
	
	@Override
	public Object getItem(int position) {
		for (Object section : this.sections.keySet()) {  
            Adapter adapter = sections.get(section);  
            int size = adapter.getCount() + 1;  
  
            // check if position inside this section  
            if (position == 0)  
                return section;  
            if (position < size)  
                return adapter.getItem(position - 1);  
  
            // otherwise jump into next section  
            position -= size;  
        }  
        return null; 
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	
	public int getItemViewType(int position) {  
        int type = 1;  
        for (Object section : this.sections.keySet()) {  
            Adapter adapter = sections.get(section);  
            int size = adapter.getCount() + 1;  
  
            // check if position inside this section  
            if (position == 0)  
                return TYPE_SECTION_HEADER;  
            if (position < size)  
                return type + adapter.getItemViewType(position - 1);  
  
            // otherwise jump into next section  
            position -= size;  
            type += adapter.getViewTypeCount();  
        }  
        return -1;  
    }  
	
	public boolean areAllItemsSelectable() {  
        return false;  
    }  
  
    public boolean isEnabled(int position) {  
        return (getItemViewType(position) != TYPE_SECTION_HEADER);  
    }  
    
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		int sectionnum = 0;  
        for (Object section : this.sections.keySet()) {  
            Adapter adapter = sections.get(section);  
            int size = adapter.getCount() + 1;  
  
            // 检查是否还在这个区中check if position inside this section  
            if (position == 0)  
                return headers.getView(sectionnum, convertView, parent);  
            if (position < size)  
                return adapter.getView(position - 1, convertView, parent);  
  
            // 否则跳到下一个区otherwise jump into next section  
            position -= size;  
            sectionnum++;  
        }  
        return null; 
	}

}
