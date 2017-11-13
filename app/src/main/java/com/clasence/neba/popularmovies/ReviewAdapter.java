package com.clasence.neba.popularmovies;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.clasence.neba.popularmovies.models.ReviewHelper;

import java.util.ArrayList;

/**
 * Created by Neba.
 */

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.MyViewHolder> {

    private Activity context;
    private ArrayList<ReviewHelper> reviewList;

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        private TextView tvUser,tvReview;
        public MyViewHolder(View itemView) {
            super(itemView);
            tvUser = (TextView) itemView.findViewById(R.id.user);
            tvReview = (TextView) itemView.findViewById(R.id.review);
        }
    }

    public ReviewAdapter(Activity context, ArrayList<ReviewHelper> reviewHelpers){
        this.context=context;
        this.reviewList=reviewHelpers;

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.review_helper, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final ReviewHelper reviewHelper = reviewList.get(position);

        holder.tvUser.setText(reviewHelper.getAuthor());
        holder.tvReview.setText(reviewHelper.getComment());

    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }


}
