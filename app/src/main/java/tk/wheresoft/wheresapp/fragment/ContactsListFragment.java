package tk.wheresoft.wheresapp.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.LocationManager;
import android.support.v4.app.ListFragment; //android.app.ListFragment;
import android.support.v4.app.LoaderManager; //android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.Loader; //android.content.Loader;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView; //android.support.v7.widget.SearchView
import android.widget.SearchView.OnCloseListener;
import android.widget.SearchView.OnQueryTextListener; //android.support.v7.widget.SearchView.OnQueryTextListener

import tk.wheresoft.wheresapp.R;

import tk.wheresoft.wheresapp.activity.ContactDataActivity;
import tk.wheresoft.wheresapp.activity.MapActivity;
import tk.wheresoft.wheresapp.adapter.ContactAdapter;
import tk.wheresoft.wheresapp.loader.ContactListLoader;
import tk.wheresoft.wheresapp.model.Contact;

import java.util.List;


public class ContactsListFragment extends ListFragment
        implements OnQueryTextListener, OnCloseListener, LoaderManager.LoaderCallbacks<List<Contact>> {

    private static String COLUMN_ID = ContactsContract.RawContacts.SYNC1;
    // This is the Adapter being used to display the list's data.
    ContactAdapter mAdapter;
    // The SearchView for doing filtering.
    SearchView mSearchView;
    // If non-null, this is the current filter the user has provided.
    String mCurFilter;
    private Boolean favourite = false;
    private Boolean recent = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // handle fragment arguments
        Bundle arguments = getArguments();
        setHasOptionsMenu(true);
        if (arguments.containsKey("TAB")) {
            Integer tab = arguments.getInt("TAB");
            if (tab == 2)
                this.favourite = true;
        }


    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (favourite)
            setEmptyText("No favourite contacts");
        else if (recent)
            setEmptyText("No recent calls");
        else
            setEmptyText("No contacts");

        //Ahora se hace asi porque aún no hay un Fragment para cada lista
        Intent i = getActivity().getIntent();

        // We have a menu item to show in action bar.
        setHasOptionsMenu(true);

        mAdapter = new ContactAdapter(getActivity());
        setListAdapter(mAdapter);

        setListShown(false);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (recent)
            inflater.inflate(R.menu.menu_list_call, menu);
        if (!recent && !favourite)
            inflater.inflate(R.menu.menu_list_contact, menu);
        // Place an action bar item for searching.
        MenuItem item = menu.add("Search");
        item.setIcon(android.R.drawable.ic_menu_search);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
                | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        mSearchView = new MySearchView(getActivity());
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setOnCloseListener(this);
        mSearchView.setIconifiedByDefault(true);
        item.setActionView(mSearchView);
    }

    public boolean onQueryTextChange(String newText) {
        // Called when the action bar search text has changed.  Since this
        // is a simple array adapter, we can just have it do the filtering.
        mCurFilter = !TextUtils.isEmpty(newText) ? newText : null;
        mAdapter.getFilter().filter(mCurFilter);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        // Don't care about this.
        return true;
    }

    @Override
    public boolean onClose() {
        if (!TextUtils.isEmpty(mSearchView.getQuery())) {
            mSearchView.setQuery(null, true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // When the user clicks REFRESH
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // Insert desired behavior here.
        Log.i("LoaderCustom", "Item clicked: " + id);
        Contact contact = mAdapter.getItem(position);
        if (contact.getServerid().equals("0")) {
            if (checkGPS()) {
                Intent intent = new Intent(getActivity(), MapActivity.class);
                intent.putExtra("TESTCALL", "TEST");
                startActivity(intent);
            }
        } else {
            Intent intent = new Intent(getActivity(), ContactDataActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("USER", contact);
            intent.putExtra("USER", bundle);
            startActivity(intent);
        }
    }

    private boolean checkGPS() {
        LocationManager mlocManager = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);
        boolean enabled = mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (enabled) {
            return enabled;
        } else {
            showGPSDisabledAlertToUser();
            return false;
        }
    }

    private void showGPSDisabledAlertToUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setMessage("Tienes que activar el GPS para poder realizar la llamada.").setCancelable(false).setPositiveButton("Ir a configuración para activar el GPS", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(callGPSSettingIntent);
            }
        });
        alertDialogBuilder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    @Override
    public Loader<List<Contact>> onCreateLoader(int id, Bundle args) {
        return new ContactListLoader(getActivity(), favourite, recent);
    }

    @Override
    public void onLoadFinished(Loader<List<Contact>> loader, List<Contact> data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mAdapter.setData(data);

        // The list should now be shown.
        if (isResumed()) {
            setListShown(true);
        } else {
            setListShownNoAnimation(true);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Contact>> loader) {
        // Clear the data in the adapter.
        mAdapter.setData(null);
    }

    public static class MySearchView extends SearchView {
        public MySearchView(Context context) {
            super(context);
        }

        // The normal SearchView doesn't clear its search text when
        // collapsed, so we will do this for it.
        @Override
        public void onActionViewCollapsed() {
            setQuery("", false);
            super.onActionViewCollapsed();
        }
    }

}
