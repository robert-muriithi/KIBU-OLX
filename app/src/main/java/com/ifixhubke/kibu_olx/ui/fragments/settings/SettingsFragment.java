package com.ifixhubke.kibu_olx.ui.fragments.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ifixhubke.kibu_olx.R;
import com.ifixhubke.kibu_olx.adapters.SettingsAdapter;
import com.ifixhubke.kibu_olx.data.Item;
import com.ifixhubke.kibu_olx.databinding.FragmentSettingsBinding;
import com.ifixhubke.kibu_olx.others.ItemClickListener;
import com.ifixhubke.kibu_olx.viewmodels.MainViewModel;

import java.util.List;
import java.util.Objects;

import timber.log.Timber;

public class SettingsFragment extends Fragment implements ItemClickListener, Toolbar.OnMenuItemClickListener {
    FragmentSettingsBinding binding;
    SettingsAdapter adapter;
    private DatabaseReference databaseReference;
    String userid, firstName, lastName, phoneNum, email;

    FirebaseUser user;
    MainViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        binding.toolbar3.setOnMenuItemClickListener(this);

        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        userid = user.getUid();

        getUserDetails();

        viewModel = new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication()).create(MainViewModel.class);

        viewModel.allItems().observe(requireActivity(), items -> {
            binding.progressBar.setVisibility(View.INVISIBLE);
            initAdapter(items);
            binding.yourPostRecyclerview.setAdapter(adapter);
        });

        binding.editTextView.setOnClickListener(v -> setViews());

        binding.saveTextView.setOnClickListener(v -> {

            updateData();
            binding.editUserName1.setVisibility(View.INVISIBLE);
            binding.editUserName2.setVisibility(View.INVISIBLE);
            binding.saveTextView.setVisibility(View.INVISIBLE);
            binding.editTextView.setVisibility(View.VISIBLE);
            binding.userName.setVisibility(View.VISIBLE);
            binding.profileImage.setVisibility(View.VISIBLE);
            binding.userEmail.setVisibility(View.VISIBLE);
            binding.phoneNum.setVisibility(View.VISIBLE);
            binding.editPhoneNum.setVisibility(View.INVISIBLE);
        });

        return view;
    }

    private void getUserDetails() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        reference.child(userid).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                firstName = Objects.requireNonNull(snapshot.child("f_Name").getValue()).toString();
                lastName = Objects.requireNonNull(snapshot.child("l_Name").getValue()).toString();
                email = Objects.requireNonNull(snapshot.child("e_Mail").getValue()).toString();
                phoneNum = Objects.requireNonNull(snapshot.child("phone_No").getValue()).toString();

                binding.userName.setText(firstName + " " + lastName);
                binding.userEmail.setText(email);
                binding.phoneNum.setText(phoneNum);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void initAdapter(List<Item> items) {
        adapter = new SettingsAdapter(items, this);
    }

    @SuppressLint("SetTextI18n")
    public void updateData() {
        String fName1 = binding.editUserName1.getText().toString();
        String fName2 = binding.editUserName2.getText().toString();
        String phone = binding.editPhoneNum.getText().toString();

        if (TextUtils.isEmpty(fName1) && TextUtils.isEmpty(fName2) && TextUtils.isEmpty(phone)) {
            Toast.makeText(requireContext(), "Cannot be blank", Toast.LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(fName1) || TextUtils.isEmpty(fName2) || TextUtils.isEmpty(phone)) {

            databaseReference.child(userid).child("f_Name").setValue(fName1);
            binding.userName.setText(fName1 + " " + lastName);
            databaseReference.child(userid).child("l_Name").setValue(fName2);
            binding.userName.setText(firstName + " " + fName2);
            databaseReference.child(userid).child("phone_No").setValue(phone);
            binding.phoneNum.setText(phone);
            binding.userName.setVisibility(View.VISIBLE);
        } else if ((!TextUtils.isEmpty(fName1) && !fName1.equals(firstName)) ||
                (!TextUtils.isEmpty(fName2) && !fName2.equals(lastName)) ||
                (!TextUtils.isEmpty(phoneNum) && !phone.equals(phoneNum))) {
            databaseReference.child(userid).child("f_Name").setValue(fName1);
            databaseReference.child(userid).child("l_Name").setValue(fName2);
            databaseReference.child(userid).child("phone_No").setValue(phone);
            binding.userName.setText(fName1 + " " + fName2);
            binding.phoneNum.setText(phone);
            binding.userName.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(requireContext(), "Unable to edit name", Toast.LENGTH_SHORT).show();
        }
    }

    public void setViews() {
        binding.userName.setVisibility(View.INVISIBLE);
        binding.editUserName1.setVisibility(View.VISIBLE);
        binding.editUserName2.setVisibility(View.VISIBLE);
        binding.editTextView.setVisibility(View.INVISIBLE);
        binding.saveTextView.setVisibility(View.VISIBLE);
        binding.profileImage.setVisibility(View.INVISIBLE);
        binding.userEmail.setVisibility(View.INVISIBLE);
        binding.phoneNum.setVisibility(View.INVISIBLE);
        binding.editPhoneNum.setVisibility(View.VISIBLE);
    }

    @Override
    public void itemClick(Item item, int position) {
        viewModel.updateSoldItem(true, position);
        Timber.d("Item remove from advertisements");
        Toast.makeText(requireContext(), "Item remove from advertisements", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.action_mode_menu) {
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("ui_mode", Context.MODE_PRIVATE);
            boolean itemUIMode = sharedPreferences.getBoolean("ISCHECKED", false);
            setUIMode(!itemUIMode);
            return true;
        } else
            return false;
    }

    private void setUIMode(boolean isChecked) {
        if (isChecked) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            saveToSharedPrefs(true);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            saveToSharedPrefs(false);
        }
    }

    private void saveToSharedPrefs(boolean isChecked) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("ui_mode", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("ISCHECKED", isChecked);
        editor.apply();
    }
}