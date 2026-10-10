package com.example.qlsv;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.qlsv.adapter.SinhVienAdapter;
import com.example.qlsv.api.ApiClient;
import com.example.qlsv.api.ApiService;
import com.example.qlsv.databinding.ActivityMainBinding;
import com.example.qlsv.databinding.DialogSinhVienBinding;
import com.example.qlsv.model.SinhVien;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements SinhVienAdapter.OnSinhVienActionListener {

    private ActivityMainBinding binding;
    private SinhVienAdapter adapter;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        apiService = ApiClient.getApiService();

        setupRecyclerView();
        setupListeners();

        loadData();
    }

    private void setupRecyclerView() {
        adapter = new SinhVienAdapter(this);
        binding.rvSinhVien.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSinhVien.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.swipeRefreshLayout.setOnRefreshListener(this::loadData);
        binding.fabAdd.setOnClickListener(v -> showAddOrEditDialog(null));
    }

    private void loadData() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.tvEmpty.setVisibility(View.GONE);

        apiService.getAllSinhVien().enqueue(new Callback<List<SinhVien>>() {
            @Override
            public void onResponse(@NonNull Call<List<SinhVien>> call, @NonNull Response<List<SinhVien>> response) {
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<SinhVien> list = response.body();
                    adapter.setList(list);
                    if (list.isEmpty()) {
                        binding.tvEmpty.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Lỗi tải dữ liệu: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<SinhVien>> call, @NonNull Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(MainActivity.this, "Kết nối thất bại: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onEdit(SinhVien sinhVien) {
        showAddOrEditDialog(sinhVien);
    }

    @Override
    public void onDelete(SinhVien sinhVien) {
        showDeleteConfirmDialog(sinhVien);
    }

    private void showAddOrEditDialog(SinhVien existingSv) {
        DialogSinhVienBinding dialogBinding = DialogSinhVienBinding.inflate(LayoutInflater.from(this));
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogBinding.getRoot());

        AlertDialog dialog = builder.create();

        boolean isEdit = existingSv != null;

        if (isEdit) {
            dialogBinding.tvDialogTitle.setText("Cập Nhật Sinh Viên");
            dialogBinding.etMaSV.setText(existingSv.getMaSV());
            dialogBinding.etMaSV.setEnabled(false); // Mã SV là ID không cho chỉnh sửa
            dialogBinding.etHoTen.setText(existingSv.getHoTen());
            dialogBinding.etNamSinh.setText(String.valueOf(existingSv.getNamSinh()));
            dialogBinding.etLop.setText(existingSv.getLop());
            dialogBinding.etDiaChi.setText(existingSv.getDiaChi());
            dialogBinding.etAnhURI.setText(existingSv.getAnhURI());
        } else {
            dialogBinding.tvDialogTitle.setText("Thêm Sinh Viên Mới");
        }

        dialogBinding.btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialogBinding.btnSave.setOnClickListener(v -> {
            String maSV = dialogBinding.etMaSV.getText() != null ? dialogBinding.etMaSV.getText().toString().trim() : "";
            String hoTen = dialogBinding.etHoTen.getText() != null ? dialogBinding.etHoTen.getText().toString().trim() : "";
            String namSinhStr = dialogBinding.etNamSinh.getText() != null ? dialogBinding.etNamSinh.getText().toString().trim() : "";
            String lop = dialogBinding.etLop.getText() != null ? dialogBinding.etLop.getText().toString().trim() : "";
            String diaChi = dialogBinding.etDiaChi.getText() != null ? dialogBinding.etDiaChi.getText().toString().trim() : "";
            String anhURI = dialogBinding.etAnhURI.getText() != null ? dialogBinding.etAnhURI.getText().toString().trim() : "";

            if (TextUtils.isEmpty(maSV)) {
                dialogBinding.tilMaSV.setError("Vui lòng nhập Mã SV");
                return;
            }
            dialogBinding.tilMaSV.setError(null);

            int namSinh = 0;
            if (!TextUtils.isEmpty(namSinhStr)) {
                try {
                    namSinh = Integer.parseInt(namSinhStr);
                } catch (NumberFormatException ignored) {
                }
            }

            SinhVien sv = new SinhVien(maSV, hoTen, namSinh, diaChi, lop, anhURI);

            if (isEdit) {
                updateSinhVienApi(sv, dialog);
            } else {
                addSinhVienApi(sv, dialog);
            }
        });

        dialog.show();
    }

    private void addSinhVienApi(SinhVien sv, AlertDialog dialog) {
        apiService.addSinhVien(sv).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Thêm sinh viên thành công!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    loadData();
                } else {
                    Toast.makeText(MainActivity.this, "Thêm thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Toast.makeText(MainActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSinhVienApi(SinhVien sv, AlertDialog dialog) {
        apiService.updateSinhVien(sv.getMaSV(), sv).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Cập nhật sinh viên thành công!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    loadData();
                } else {
                    Toast.makeText(MainActivity.this, "Cập nhật thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Toast.makeText(MainActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmDialog(SinhVien sv) {
        new AlertDialog.Builder(this)
                .setTitle("Xác Nhận Xóa")
                .setMessage("Bạn có chắc chắn muốn xóa sinh viên: " + (sv.getHoTen() != null ? sv.getHoTen() : sv.getMaSV()) + "?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteSinhVienApi(sv.getMaSV()))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteSinhVienApi(String maSV) {
        apiService.deleteSinhVien(maSV).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Xóa sinh viên thành công!", Toast.LENGTH_SHORT).show();
                    loadData();
                } else {
                    Toast.makeText(MainActivity.this, "Xóa thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Toast.makeText(MainActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
