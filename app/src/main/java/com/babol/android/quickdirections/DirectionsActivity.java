package com.babol.android.quickdirections;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import ListView.*;

public class DirectionsActivity  extends Activity{
    private Integer[] imgDir = {R.drawable.left2, R.drawable.left1, R.drawable.up2, R.drawable.right1, R.drawable.right2,
            R.drawable.round, R.drawable.merge, R.drawable.north, R.drawable.east, R.drawable.south, R.drawable.west};
    private ListView listView;
    private List<DirectionRow> dirItems;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directions);

        Intent intent = getIntent();
        ArrayList<String> list = intent.getStringArrayListExtra("key");

        // initialize rowItems array
        dirItems = new ArrayList<DirectionRow>();

        // populate the rowItems array
        for(int i = 0;i < list.size(); i++){
            String nav = list.get(i);
            DirectionRow row = new DirectionRow(imgDir[getDirection(nav)], nav);
            dirItems.add(row);
        }
        listView = (ListView) findViewById(R.id.listV_directions);

        final DirectionListAdapter adapter = new DirectionListAdapter(this, R.layout.list_directions, dirItems);           // data set: a List of RowItem objects

        listView.setAdapter(adapter);
    }

    // Get direction arrow image depending on the context of the direction
    int getDirection(String nav) {
        if(nav.contains("Keep left") || nav.contains("keep left"))
            return 0;
        else if(nav.contains("Turn left")||nav.contains("turn left"))
            return 1;
        else if(nav.contains("Turn right")||nav.contains("turn right"))
            return 3;
        else if(nav.contains("Keep right")||nav.contains("keep right"))
            return 4;
        else if(nav.contains("Roundabout")||nav.contains("roundabout"))
            return 5;
        else if(nav.contains("Merge") || nav.contains("merge"))
            return 6;
        else if(nav.contains("Head North") || nav.contains("head north"))
            return 7;
        else if(nav.contains("Head east") || nav.contains("head east"))
            return 8;
        else if(nav.contains("Head south") || nav.contains("head south"))
            return 9;
        else if(nav.contains("Head West") || nav.contains("head west"))
            return 10;
        else
            return 2;
    }

    // finish the activity and go back to maps
    public void onBack(View view){
        finish();
    }
}
