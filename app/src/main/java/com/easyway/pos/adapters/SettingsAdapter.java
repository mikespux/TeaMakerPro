package com.easyway.pos.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.easyway.pos.R;
import com.easyway.pos.data.SettingsItem;

import java.util.List;


public class SettingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public LayoutInflater inflater;
    private final Activity mActivity;
    List<SettingsItem> settingsItems;

    public SettingsAdapter(Activity mActivity, List<SettingsItem> settingsItems) {
        this.mActivity = mActivity;
        inflater = LayoutInflater.from(mActivity);
        this.settingsItems = settingsItems;

    }

    @Override
    public int getItemViewType(int position) {

        return position;

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.settings_item, parent, false);
        ItemHolder holder = new ItemHolder(view);
        return holder;
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ItemHolder itemHolder = (ItemHolder) holder;
        SettingsItem settingsItem = settingsItems.get(position);
        itemHolder.title.setText(settingsItem.title);
        itemHolder.icon.setImageResource(settingsItem.iconId);
    }

    @Override
    public int getItemCount() {

        if (settingsItems != null) {
            return settingsItems.size();
        } else {
            return 1;
        }
    }

    class ItemHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView icon;

        public ItemHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.listText);
            icon = itemView.findViewById(R.id.listIcon);

        }
    }

}
