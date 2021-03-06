package tk.wheresoft.wheresapp.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnCloseListener;
import android.widget.SearchView.OnQueryTextListener;

import tk.wheresoft.wheresapp.R;

import tk.wheresoft.wheresapp.activity.ContactDataActivity;
import tk.wheresoft.wheresapp.adapter.CallAdapter;
import tk.wheresoft.wheresapp.bussiness.contacts.factory.ASContactsFactory;
import tk.wheresoft.wheresapp.loader.CallListLoader;
import tk.wheresoft.wheresapp.model.Call;
import tk.wheresoft.wheresapp.model.Contact;

import java.util.List;


public class CallListFragment extends ListFragment
        implements OnQueryTextListener, OnCloseListener, LoaderManager.LoaderCallbacks<List<Call>> {

    private static String COLUMN_ID = ContactsContract.RawContacts.SYNC1;
    // This is the Adapter being used to display the list's data.
    CallAdapter mAdapter;
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
            else if (tab == 1)
                this.recent = true;
        }


    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEmptyText("No recent calls");

        //Ahora se hace asi porque aún no hay un Fragment para cada lista
        Intent i = getActivity().getIntent();

        // We have a menu item to show in action bar.
        setHasOptionsMenu(true);

        mAdapter = new CallAdapter(getActivity());
        setListAdapter(mAdapter);

        setListShown(false);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (recent)
            inflater.inflate(R.menu.menu_list_call, menu);
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
        Intent intent = new Intent(getActivity(), ContactDataActivity.class);
        Bundle bundle = new Bundle();
        Call call = mAdapter.getItem(position);
        Contact contact = new Contact();
        contact.setServerid(Long.toString(mAdapter.getItemId(position)));
        contact = ASContactsFactory.getInstance().getInstanceASContacts(getActivity()).getContact(contact);
        bundle.putSerializable("USER", contact);
        intent.putExtra("USER", bundle);
        startActivity(intent);
    }

    @Override
    public Loader<List<Call>> onCreateLoader(int id, Bundle args) {
        return new CallListLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<Call>> loader, List<Call> data) {
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
    public void onLoaderReset(Loader<List<Call>> loader) {
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
