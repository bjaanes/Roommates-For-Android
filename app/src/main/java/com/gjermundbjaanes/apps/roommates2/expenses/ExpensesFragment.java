package com.gjermundbjaanes.apps.roommates2.expenses;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.gjermundbjaanes.apps.roommates2.AddBehaviourFragment;
import com.gjermundbjaanes.apps.roommates2.R;
import com.gjermundbjaanes.apps.roommates2.RefreshableFragment;
import com.gjermundbjaanes.apps.roommates2.expenses.newexpense.NewExpenseActivity;
import com.gjermundbjaanes.apps.roommates2.expenses.viewexpense.ViewExpenseActivity;
import com.gjermundbjaanes.apps.roommates2.helpers.Constants;
import com.gjermundbjaanes.apps.roommates2.parsesubclasses.Expense;

public class ExpensesFragment extends Fragment implements RefreshableFragment, AddBehaviourFragment {
    private ExpenseAdapter adapter;

    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshFragment();
        }
    };

    public void refreshFragment() {
        adapter.loadObjects();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_expenses, container, false);

        setUpFragment(rootView);

        setUpBroadcastReceiver();

        return rootView;
    }

    private void setUpBroadcastReceiver() {
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getActivity());
        broadcastManager.registerReceiver(mMessageReceiver, new IntentFilter(Constants.NEED_TO_REFRESH));
    }

    private void setUpFragment(View rootView) {
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getActivity());
        broadcastManager.registerReceiver(new NeedToRefreshBroadcastReceiver(), new IntentFilter(Constants.EXPENSE_NEED_TO_REFRESH));

        ListView expenseListView = (ListView) rootView.findViewById(R.id.expenseListView);
        adapter = new ExpenseAdapter(getActivity());
        expenseListView.setAdapter(adapter);

        expenseListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Expense expense = (Expense) parent.getItemAtPosition(position);
                viewExpense(expense.getObjectId());
            }
        });
    }

    private void viewExpense(String objectID) {
        Intent intent = new Intent(getActivity(), ViewExpenseActivity.class);
        intent.putExtra(Constants.EXTRA_NAME_EXPENSE_ID, objectID);
        startActivity(intent);
    }

    private void createNewExpense() {
        Intent intent = new Intent(getActivity(), NewExpenseActivity.class);
        startActivity(intent);
    }

    @Override
    public void add() {
        createNewExpense();
    }

    private class NeedToRefreshBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshFragment();
        }
    }
}


