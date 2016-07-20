package chaitanya.im.searchforreddit;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import chaitanya.im.searchforreddit.DataModel.RecyclerViewItem;

//basic adapter
public class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.ViewHolder> {

    private static List<RecyclerViewItem> resultList;
    private static AppCompatActivity context;
    String point;
    String comment;
    Typeface fontawesome;

    public ResultsAdapter(List<RecyclerViewItem> results, AppCompatActivity context) {
        resultList = results;
        this.context = context;
        comment = context.getString(R.string.comment);
        point = context.getString(R.string.upvote);
        fontawesome = Typeface.createFromAsset(context.getAssets(), "fontawesome-webfont.ttf");
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView titleTextView;
        public TextView topTextView;
        public TextView bottomTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            titleTextView = (TextView) itemView.findViewById(R.id.post_title);
            topTextView = (TextView) itemView.findViewById(R.id.top_text_view);
            bottomTextView = (TextView) itemView.findViewById(R.id.bottom_text_view);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getLayoutPosition(); // gets item position
            String url = resultList.get(position).getPermalink();
            UtilMethods.resultClicked(context, url);
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
        TextView title = viewHolder.titleTextView;
        TextView top = viewHolder.topTextView;
        TextView bottom = viewHolder.bottomTextView;

        String topText = result.getAuthor() +
                " in <font color=#E91E63>" + result.getSubreddit() +
                "</font>";

        String bottomText = comment + result.getNumComments() + "&nbsp;&nbsp;\u2022&nbsp;&nbsp;" +
                result.getTimeString() + "&nbsp;&nbsp;\u2022&nbsp;" + "<font color=#FF9800> " +
                point + result.getScore() + "</font>";

        bottom.setTypeface(fontawesome);
        title.setText(result.getTitle());
        top.setText(Html.fromHtml(topText));
        bottom.setText(Html.fromHtml(bottomText));
    }

    //return count of items
    @Override
    public int getItemCount() {
        return resultList.size();
    }

}


















