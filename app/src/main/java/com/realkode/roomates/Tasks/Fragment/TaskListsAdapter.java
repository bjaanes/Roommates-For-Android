package com.realkode.roomates.Tasks.Fragment;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.realkode.roomates.Helpers.AdapterItems.EntryItemForTaskList;
import com.realkode.roomates.Helpers.AdapterItems.Item;
import com.realkode.roomates.Helpers.AdapterItems.SectionItem;
import com.realkode.roomates.Helpers.Utils;
import com.realkode.roomates.ParseSubclassses.TaskList;
import com.realkode.roomates.ParseSubclassses.User;
import com.realkode.roomates.R;

import java.util.ArrayList;
import java.util.List;

public class TaskListsAdapter extends BaseAdapter {
    private final Context context;

    private final ArrayList<Item> items = new ArrayList<Item>();
    private ArrayList<TaskList> taskLists = new ArrayList<TaskList>();


    public TaskListsAdapter(Context context) {
        this.context = context;
        loadObjects();
    }

    void reloadElements() {
        if (taskLists != null) {
            items.clear();

            ArrayList<TaskList> unfinishedElements = new ArrayList<TaskList>(taskLists);
            ArrayList<TaskList> finishedElements = new ArrayList<TaskList>();

            for (TaskList taskList : unfinishedElements) {
                if (taskList.getDone()) {
                    finishedElements.add(taskList);
                }
            }
            unfinishedElements.removeAll(finishedElements);


            items.add(new SectionItem(context.getString(R.string.tasks_section_item_title_todo)));
            for (TaskList taskList : unfinishedElements) {
                items.add(new EntryItemForTaskList(taskList.getListName(),
                        context.getString(R.string.tasks_item_created_by) + " " + taskList.getCreatedBy().getDisplayName(),
                        taskList));
            }

            items.add(new SectionItem(context.getString(R.string.tasks_section_item_title_finished)));
            for (TaskList taskList : finishedElements) {
                items.add(new EntryItemForTaskList(taskList.getListName(),
                        context.getString(R.string.tasks_item_created_by) + " " + taskList.getCreatedBy().getDisplayName(),
                        taskList));
            }


            notifyDataSetChanged();
        }

    }

    public void loadObjects() {
        ParseQuery<TaskList> taskListParseQuery = ParseQuery.getQuery(TaskList.class);
        taskListParseQuery.include("createdBy");
        taskListParseQuery.orderByAscending("createdAt");
        taskListParseQuery.whereEqualTo("household", User.getCurrentUser().getActiveHousehold());

        Utils.setSafeQueryCaching(taskListParseQuery);


        taskListParseQuery.findInBackground(new FindCallback<TaskList>() {
            @Override
            public void done(List<TaskList> incomingTaskLists, ParseException e) {
                if (e == null) {
                    taskLists = new ArrayList<TaskList>(incomingTaskLists);
                    reloadElements();
                }
            }
        });


    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int i) {
        Item item = items.get(i);
        if (!item.isSection()) {
            EntryItemForTaskList entryItemForTaskList = (EntryItemForTaskList) item;
            System.out.println(entryItemForTaskList.getTaskList().getListName());
            return entryItemForTaskList.getTaskList();
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        final Item item = items.get(position);

        if (item != null) {
            if (item.isSection()) {
                SectionItem sectionItem = (SectionItem) item;
                view = View.inflate(context, R.layout.list_element_section, null);
                view.setBackgroundColor(Color.LTGRAY);
                view.setOnClickListener(null);
                view.setOnLongClickListener(null);
                view.setLongClickable(false);
                TextView title = (TextView) view.findViewById(R.id.sectionTitleTextView);

                title.setText(sectionItem.getTitle());
            } else {
                EntryItemForTaskList entryItemForTaskList = (EntryItemForTaskList) item;
                view = View.inflate(context, R.layout.list_task_element_layout, null);
                TextView title = (TextView) view.findViewById(R.id.textViewList);
                TextView subTitle = (TextView) view.findViewById(R.id.textViewCreatedBy);

                title.setText(entryItemForTaskList.getTitle());
                subTitle.setText(entryItemForTaskList.getSubtitle());
            }
        }

        return view;
    }

}


