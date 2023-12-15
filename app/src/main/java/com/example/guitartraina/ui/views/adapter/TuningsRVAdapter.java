package com.example.guitartraina.ui.views.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.guitartraina.R;
import com.example.guitartraina.activities.tuner.Tuning;

import java.util.Arrays;
import java.util.List;

public class TuningsRVAdapter extends RecyclerView.Adapter<TuningsRVAdapter.TuningViewHolder> {

    private View.OnClickListener onChangeTuningClickListener;
    private View.OnClickListener onDeleteTuningClickListener;

    public void setOnDeleteTuningClickListener(View.OnClickListener onClickListener) {
        this.onDeleteTuningClickListener = onClickListener;
    }

    public static class TuningViewHolder extends RecyclerView.ViewHolder {
        final CardView cardView;
        final TextView tvTitle;
        final TextView tvNoteNames;

        TuningViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.item);
            tvTitle = itemView.findViewById(R.id.tuning_title);
            tvNoteNames = itemView.findViewById(R.id.tuning_notes);
        }
    }

    public List<Tuning> getTuningList() {
        return tuningList;
    }

    public int getItem() {
        return item;
    }

    private final List<Tuning> tuningList;
    private final boolean deletable;
    private int item;

    public TuningsRVAdapter(List<Tuning> tuningList, boolean deletable) {
        this.deletable = deletable;
        this.tuningList = tuningList;
    }

    public void setOnChangeTuningClickListener(View.OnClickListener onChangeTuningClickListener) {
        this.onChangeTuningClickListener = onChangeTuningClickListener;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @NonNull
    @Override
    public TuningViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        TuningViewHolder tuningViewHolder = new TuningViewHolder(view);
        tuningViewHolder.itemView.setOnClickListener(view1 -> {
            if (onChangeTuningClickListener == null) {
                return;
            }
            int adapterPos = tuningViewHolder.getAdapterPosition();
            if (adapterPos != RecyclerView.NO_POSITION) {
                item = adapterPos;
                this.onChangeTuningClickListener.onClick(view1);
            }
        });
        if (deletable) {
            ImageButton btnDelete = tuningViewHolder.itemView.findViewById(R.id.tuning_delete);
            btnDelete.setVisibility(View.VISIBLE);
            btnDelete.setOnClickListener(view12 -> {
                if (onChangeTuningClickListener == null) {
                    return;
                }
                int adapterPos = tuningViewHolder.getAdapterPosition();
                if (tuningList.get(adapterPos).getId() < 0) {
                    btnDelete.setAlpha(0.5f);
                }
                if (adapterPos != RecyclerView.NO_POSITION) {
                    item = adapterPos;
                    onDeleteTuningClickListener.onClick(view);
                }
            });
        }
        return tuningViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull TuningViewHolder holder, int position) {
        Tuning tuning = tuningList.get(position);
        holder.tvTitle.setText(tuning.getTitle());
        double[] frequencies = tuning.getFrequencies();
        holder.tvNoteNames.setText(String.format("%s=%sHz", Arrays.toString(tuning.getNoteNames()).replace("[", "").replace("]", ""), frequencies[frequencies.length - 1]));
    }

    @Override
    public int getItemCount() {
        return tuningList.size();
    }
}
