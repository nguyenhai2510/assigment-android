package com.example.qlsv.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.qlsv.R;
import com.example.qlsv.databinding.ItemSinhVienBinding;
import com.example.qlsv.model.SinhVien;

import java.util.ArrayList;
import java.util.List;

public class SinhVienAdapter extends RecyclerView.Adapter<SinhVienAdapter.SinhVienViewHolder> {

    public interface OnSinhVienActionListener {
        void onEdit(SinhVien sinhVien);
        void onDelete(SinhVien sinhVien);
    }

    private List<SinhVien> list = new ArrayList<>();
    private final OnSinhVienActionListener listener;

    public SinhVienAdapter(OnSinhVienActionListener listener) {
        this.listener = listener;
    }

    public void setList(List<SinhVien> newList) {
        this.list = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SinhVienViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSinhVienBinding binding = ItemSinhVienBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new SinhVienViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SinhVienViewHolder holder, int position) {
        SinhVien sv = list.get(position);
        holder.bind(sv);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class SinhVienViewHolder extends RecyclerView.ViewHolder {
        private final ItemSinhVienBinding binding;

        public SinhVienViewHolder(@NonNull ItemSinhVienBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(SinhVien sv) {
            binding.tvHoTen.setText(sv.getHoTen() != null ? sv.getHoTen() : "N/A");
            binding.tvMaSV.setText("Mã SV: " + (sv.getMaSV() != null ? sv.getMaSV() : "N/A"));
            binding.tvLop.setText("Lớp: " + (sv.getLop() != null ? sv.getLop() : "N/A"));
            binding.tvNamSinh.setText("Năm sinh: " + (sv.getNamSinh() > 0 ? sv.getNamSinh() : "N/A"));
            binding.tvDiaChi.setText("Địa chỉ: " + (sv.getDiaChi() != null ? sv.getDiaChi() : "N/A"));

            // Load Avatar image via Glide
            if (sv.getAnhURI() != null && !sv.getAnhURI().trim().isEmpty()) {
                Glide.with(binding.getRoot().getContext())
                        .load(sv.getAnhURI())
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(binding.imgAvatar);
            } else {
                binding.imgAvatar.setImageResource(R.drawable.ic_launcher_foreground);
            }

            binding.btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEdit(sv);
            });

            binding.btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(sv);
            });
        }
    }
}
