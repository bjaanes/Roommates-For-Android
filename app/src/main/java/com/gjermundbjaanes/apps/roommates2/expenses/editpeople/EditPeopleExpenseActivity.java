package com.gjermundbjaanes.apps.roommates2.expenses.editpeople;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.gjermundbjaanes.apps.roommates2.R;
import com.gjermundbjaanes.apps.roommates2.helpers.Constants;
import com.gjermundbjaanes.apps.roommates2.helpers.ToastMaker;
import com.gjermundbjaanes.apps.roommates2.parsesubclasses.Expense;
import com.gjermundbjaanes.apps.roommates2.parsesubclasses.User;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;

public class EditPeopleExpenseActivity extends Activity {
    private Expense activeExpense;
    private ArrayList<User> paidList;
    private ArrayList<User> notPaidList;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_expense_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == findViewById(R.id.action_save).getId()) {
            saveExpense();
        }

        return true;
    }


    private void saveExpense() {
        activeExpense.setPaidUp(paidList);
        activeExpense.setNotPaidUp(notPaidList);
        final ProgressDialog resetProgress = ProgressDialog
                .show(EditPeopleExpenseActivity.this, getString(R.string.saving), getString(R.string.please_wait),
                        true);
        activeExpense.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                resetProgress.dismiss();
                Intent intent = new Intent(Constants.EXPENSE_NEED_TO_REFRESH);
                LocalBroadcastManager.getInstance(EditPeopleExpenseActivity.this).sendBroadcast(intent);
                finish();
            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        queryMemberList();
    }

    private void queryMemberList() {
        final String objectID = getIntent().getExtras().getString(Constants.EXTRA_NAME_EXPENSE_ID);

        final ProgressDialog resetProgress = ProgressDialog
                .show(EditPeopleExpenseActivity.this, getString(R.string.loading), getString(R.string.please_wait),
                        true);

        ParseQuery<Expense> query = new ParseQuery<Expense>("Expense");
        query.include("owed");
        query.include("notPaidUp");
        query.include("paidUp");

        query.getInBackground(objectID, new GetCallback<Expense>() {
            @Override
            public void done(Expense expense, ParseException e) {
                if (e == null) {
                    resetProgress.dismiss();
                    setUpExpense(expense);
                } else {
                    ToastMaker.makeLongToast(R.string.could_not_fetch_roommates, EditPeopleExpenseActivity.this);
                    EditPeopleExpenseActivity.this.finish();
                }

            }
        });
    }

    private void setUpExpense(Expense expense) {
        activeExpense = expense;

        setContentView(R.layout.activity_edit_people_expense);
        final ListView list = (ListView) findViewById(R.id.edit_people_listview);
        ArrayList<User> userList = expense.getNotPaidUp();
        userList.addAll(expense.getPaidUp());
        ArrayList<String> objectIDs = new ArrayList<String>();

        for (User users : userList) {
            objectIDs.add(users.getObjectId());
        }

        HouseholdMembersAdapterEditExpense membersListViewAdapter =
                new HouseholdMembersAdapterEditExpense(getApplicationContext(), objectIDs);

        paidList = expense.getPaidUp();
        notPaidList = expense.getNotPaidUp();

        list.setAdapter(membersListViewAdapter);
        list.setOnItemClickListener(new PersonItemOnClickListener(list, paidList, notPaidList));
    }
}
