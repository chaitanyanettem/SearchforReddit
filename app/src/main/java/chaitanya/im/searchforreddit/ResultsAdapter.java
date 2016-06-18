package chaitanya.im.searchforreddit;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import chaitanya.im.searchforreddit.DataModel.RecyclerViewItem;

//basic adapter
public class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.ViewHolder> {

    private List<RecyclerViewItem> resultList;

    public ResultsAdapter(List<RecyclerViewItem> results) {
        resultList = results;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            titleTextView = (TextView) itemView.findViewById(R.id.post_title);
        }
    }

    // Inflate layout and return viewholder
    @Override
    public ResultsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View resultView = inflater.inflate(R.layout.result_card, parent, false);

        ViewHolder viewHolder = new ViewHolder(resultView);
        return viewHolder;
    }

    //populate data in each itemview in the recyclerview
    @Override
    public void onBindViewHolder(ResultsAdapter.ViewHolder viewHolder, int position) {
        RecyclerViewItem result = resultList.get(position);

        TextView textView = viewHolder.titleTextView;
        textView.setText(result.getTitle());
    }

    //return count of items
    @Override
    public int getItemCount() {
        return resultList.size();
    }

}


















