package com.example.doctorapp.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.doctorapp.R;
import com.example.doctorapp.model.PatientModel;

import java.util.LinkedList;
import java.util.List;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.PatientHolder> {

    private Context mContext;
    private List<PatientModel> mData;
    private ItemInteraction mListener;
    private boolean delMode = false;
    private List<PatientModel> mDataToDel;

    public PatientAdapter(Context mContext, List<PatientModel> mData) {
        this.mContext = mContext;
        this.mData = mData;
        mDataToDel = new LinkedList<>();
    }

    @NonNull
    @Override
    public PatientHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.item_patient, viewGroup,false);
        return new PatientHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientHolder patientHolder, int i) {
        patientHolder.onBind(mData.get(i));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setCounterForPatient(String id, long aLong) {
        for (PatientModel i:mData) {
            if (i.getPatientID().equals(id)){
                i.setUnreadMessages(aLong);
                break;
            }

        }
        notifyDataSetChanged();
    }

    class PatientHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private CardView card;
        private PatientModel model;
        private TextView nameSurname;
        private TextView conclusion;
        private TextView unreadMessages;
        private boolean isSelected = false;
        PatientHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.patient_chat_card);
            card.setOnClickListener(this);
            card.setOnLongClickListener(this);
            nameSurname = itemView.findViewById(R.id.textView);
            conclusion = itemView.findViewById(R.id.textView2);
            unreadMessages= itemView.findViewById(R.id.textView7);
        }

        void onBind(PatientModel patientModel) {
            this.model = patientModel;
            if (patientModel.isSelected())
                card.setCardBackgroundColor(mContext.getResources().getColor(R.color.color_selected));
            else card.setCardBackgroundColor(mContext.getResources().getColor(R.color.color_white));
            nameSurname.setText(String.format("%s %s", model.getName(), model.getSecondName()));
            conclusion.setText(model.getDescription());
            if (patientModel.getUnreadMessages()==0){
                unreadMessages.setVisibility(View.GONE);
            }else {
                unreadMessages.setText(String.valueOf(model.getUnreadMessages()));
                unreadMessages.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onClick(View v) {
            if (mListener != null && !delMode)
                mListener.onItemClicked(model);
            if (delMode){
                if (!isSelected) {
                    card.setCardBackgroundColor(mContext.getResources().getColor(R.color.color_selected));
                    isSelected = true;
                    model.setSelected(true);
                    mDataToDel.add(model);
                }
                else {
                    card.setCardBackgroundColor(mContext.getResources().getColor(R.color.color_white));
                    isSelected = false;
                    model.setSelected(false);
                    mDataToDel.remove(model);
                }
                checkSelected();
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (mListener!=null) {
                mListener.onDelModeActive();
                delMode = true;
                isSelected = true;
                mDataToDel.add(model);
                model.setSelected(true);
                card.setCardBackgroundColor(mContext.getResources().getColor(R.color.color_selected));
            }
            return true;
        }
    }

    private void checkSelected(){
        for (PatientModel i:mData)
            if (i.isSelected())
                return;
        delMode = false;
        mListener.onDelModeDisabled();
    }

    public void deleteSelected(){
        mData.removeAll(mDataToDel);
        notifyDataSetChanged();
    }

    public void setmListener(ItemInteraction listener){
        this.mListener = listener;
    }

    public void desableDelMode(){
        delMode = false;
        for (PatientModel i:mData) {
            if (i.isSelected())
                i.setSelected(false);
        }
        notifyDataSetChanged();
    }

    public interface ItemInteraction{
        void onItemClicked(PatientModel model);
        void onDelModeActive();
        void onDelModeDisabled();
    }
}
