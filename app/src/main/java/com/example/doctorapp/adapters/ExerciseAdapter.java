package com.example.doctorapp.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.doctorapp.IOnLoadMore;
import com.example.doctorapp.R;
import com.example.doctorapp.model.ExerciseModel;
import com.squareup.picasso.Picasso;

import java.util.LinkedList;
import java.util.List;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseHolder>{

    public static final int MODE_DEL = 645;
    public static final int MODE_ADD = 48;
    private int mode = MODE_ADD;

    private Context mContext;
    private List<ExerciseModel> mData;
    private IOnLoadMore loadMore;
    private AdapterInteraction mListener;
    private boolean delMode = false;
    private boolean addMode = false;
    private List<ExerciseModel> mDataToDel;
    private AdapterAction actionListener;

    public ExerciseAdapter(Context mContext, List<ExerciseModel> mData, IOnLoadMore loadMore) {
        this.mContext = mContext;
        this.mData = mData;
        this.loadMore = loadMore;
        mDataToDel = new LinkedList<>();
    }

    public void addData(List<ExerciseModel> models){
        mData.addAll(models);
        notifyItemInserted(mData.size());
    }

    public void setAdapterMode(int mode){
        switch (mode){
            case MODE_DEL:
                this.mode = MODE_DEL;
                break;
            case MODE_ADD:
                this.mode = MODE_ADD;
                break;
        }
    }

    @NonNull
    @Override
    public ExerciseHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.item_exercise, viewGroup,false);
        return new ExerciseHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseHolder exerciseHolder, int i) {
        exerciseHolder.onBind(mData.get(i));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    class ExerciseHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        ImageView imageView;
        CardView cardView;
        TextView textView;
        ConstraintLayout constraintLayout;
        ExerciseModel model;
        ImageButton actionBtn;
        TextView category;

        ExerciseHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_video);
            cardView = itemView.findViewById(R.id.cardView);
            cardView.setOnClickListener(this);
            textView = itemView.findViewById(R.id.exercise_desc);
            constraintLayout = itemView.findViewById(R.id.constraint_exercise_cell);
            actionBtn = itemView.findViewById(R.id.btn_action_exercise);
            category = itemView.findViewById(R.id.categoryExercise);
            actionBtn.setOnClickListener(this::actionClicked);
        }

        private void actionClicked(View view) {
            if (actionListener!=null){
                if (mode == MODE_DEL)
                    actionListener.onDelClicked(model);
                else actionListener.onAddClicked(model);
            }
        }


        void onBind(ExerciseModel model){
            this.model = model;
            if (model.getType() == ExerciseModel.TYPE_HEADER){
                cardView.setVisibility(View.GONE);
                textView.setVisibility(View.GONE);
                category.setText(model.getCategory());
                category.setVisibility(View.VISIBLE);
                return;
            }else{
                cardView.setVisibility(View.VISIBLE);
                textView.setVisibility(View.VISIBLE);
                category.setVisibility(View.GONE);
            }

            Picasso.with(mContext)
                    .load(model.getUrlImage())
                    .error(R.drawable.ic_close_gray)
                    .into(imageView);
            textView.setText(model.getName());
            if (mode == MODE_DEL)
                actionBtn.setBackgroundResource(R.drawable.ic_delete_black_24dp);
            else actionBtn.setBackgroundResource(R.drawable.ic_add_black_24dp);
            if (model.isSelected())
                constraintLayout.setBackgroundColor(mContext.getResources().getColor(R.color.color_selected));
            else constraintLayout.setBackgroundColor(mContext.getResources().getColor(R.color.color_white));
        }

        @Override
        public void onClick(View v) {
            if (actionListener != null && model.getType()!= ExerciseModel.TYPE_HEADER)
                actionListener.onClicked(model);
            //loadMore.loadMore(0);
            if (delMode) {
                if (!model.isSelected()) {
                    cardView.setCardBackgroundColor(mContext.getResources().getColor(R.color.color_selected));
                    model.setSelected(true);
                    mDataToDel.add(model);
                } else {
                    cardView.setCardBackgroundColor(mContext.getResources().getColor(R.color.color_white));
                    model.setSelected(false);
                    mDataToDel.remove(model);
                }
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (mListener!=null) {
                mListener.onDelModeActive();
                delMode = true;
                model.setSelected(true);
                mDataToDel.add(model);
                model.setSelected(true);
                constraintLayout.setBackgroundColor(mContext.getResources().getColor(R.color.color_selected));
            }
            return true;
        }


    }

    public void setListner(AdapterInteraction listener){
        this.mListener = listener;
    }

    public void setListner(AdapterAction listner){
        this.actionListener = listner;
    }


    public interface AdapterAction{
        void onDelClicked(ExerciseModel model);
        void onAddClicked(ExerciseModel model);
        void onClicked(ExerciseModel model);
    }

    public interface AdapterInteraction{
        void onDelModeActive();
        void onDelModeDisabled();
        void onAddModeActive();
        void onAddModeDisabled();
    }
}
