package org.readium.sdklauncher_android;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.readium.sdklauncher_android.model.OpenPageRequest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.readium.model.epub3.Container;
import com.readium.model.epub3.Package;
import com.readium.model.epub3.components.navigation.NavigationElement;
import com.readium.model.epub3.components.navigation.NavigationPoint;
import com.readium.model.epub3.components.navigation.NavigationTable;

public abstract class NavigationTableActivity extends Activity {
	
    private static final String TAG = "NavigationTableActivity";
	private Context context;
    private Button back;
    protected Package pckg;
	protected int containerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.table_of_contents);

        context = this;
        back = (Button) findViewById(R.id.backToBookView6);
        Intent intent = getIntent();
        if (intent.getFlags() == Intent.FLAG_ACTIVITY_NEW_TASK) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                String value = extras.getString(Constants.BOOK_NAME);
                back.setText(value);
                containerId = extras.getInt(Constants.CONTAINER_ID);
                Container container = ContainerHolder.getInstance().get(containerId);
                pckg = container.getDefaultPackage();
            }
        }

        final ListView items = (ListView) findViewById(R.id.tableOfContents);

        this.setListViewContent(items, getNavigationTable());

        initListener();
    }

	protected abstract NavigationTable getNavigationTable();

    protected void setListViewContent(ListView view, final NavigationTable navigationTable) {
    	List<String> list = flatNavigationTable(navigationTable, new ArrayList<String>(), "");
    	final List<NavigationElement> navigationElements = flatNavigationTable(navigationTable, new ArrayList<NavigationElement>());
        BookListAdapter bookListAdapter = new BookListAdapter(this, list);
        view.setAdapter(bookListAdapter);
        view.setOnItemClickListener(new ListView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
            	NavigationElement navigation = navigationElements.get(arg2);
            	if (navigation instanceof NavigationPoint) {
            		NavigationPoint point = (NavigationPoint) navigation;
            		Log.i(TAG, "Open webview at : "+point.getContent());
            		Intent intent = new Intent(NavigationTableActivity.this, WebViewActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            		intent.putExtra(Constants.CONTAINER_ID, containerId);
            		OpenPageRequest openPageRequest = OpenPageRequest.fromContentUrl(point.getContent(), navigationTable.getSourceHref());
            		try {
						intent.putExtra(Constants.OPEN_PAGE_REQUEST_DATA, openPageRequest.toJSON().toString());
	            		startActivity(intent);
					} catch (JSONException e) {
						Log.e(TAG, ""+e.getMessage(), e);
					}
            	}
                Toast.makeText(context, "this is item " + navigation.getTitle(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<String> flatNavigationTable(NavigationElement parent,
			List<String> list, String shift) {
    	String newShift = shift + "   ";
        for (NavigationElement ne : parent.getChildren()) {
            list.add(shift + ne.getTitle()+" ("+ne.getChildren().size()+")");
            flatNavigationTable(ne, list, newShift);
		}
		return list;
	}

    private List<NavigationElement> flatNavigationTable(NavigationElement parent,
			List<NavigationElement> list) {
        for (NavigationElement ne : parent.getChildren()) {
            list.add(ne);
            flatNavigationTable(ne, list);
		}
		return list;
	}

	private void initListener() {
        back.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}

