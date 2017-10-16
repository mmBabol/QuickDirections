package ListView;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.babol.android.quickdirections.R;

import java.util.List;

/**
 * Created by Mat on 12/03/2016.
 *
 * Custom list adapter for directions
 */

public class DirectionListAdapter extends ArrayAdapter<DirectionRow> {

private Context context;

    public DirectionListAdapter(Context context, int resource, List<DirectionRow> items) {
        super(context, resource, items);
        this.context = context;
    }

    private class ViewHolder{
        ImageView list_image;
        TextView txtDirection;
    }

    public View getView(int position, View convertView, ViewGroup parent){
        ViewHolder holder = null;

        DirectionRow row = getItem(position);

        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if(convertView == null){
            convertView = mInflater.inflate(R.layout.list_directions, null);

            holder = new ViewHolder();

            holder.list_image = (ImageView) convertView.findViewById(R.id.list_image_dir);
            holder.txtDirection = (TextView) convertView.findViewById(R.id.txtDirection);

            convertView.setTag(holder);
        }
        else
            holder = (ViewHolder) convertView.getTag();

        holder.txtDirection.setText(row.toString());
        holder.list_image.setImageResource(row.getImageID());

        return convertView;
    }

}
