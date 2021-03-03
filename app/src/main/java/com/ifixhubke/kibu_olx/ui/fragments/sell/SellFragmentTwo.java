package com.ifixhubke.kibu_olx.ui.fragments.sell;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ifixhubke.kibu_olx.R;
import com.ifixhubke.kibu_olx.data.Sell;
import com.ifixhubke.kibu_olx.databinding.FragmentSellTwoBinding;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;
import java.util.UUID;
import timber.log.Timber;

public class SellFragmentTwo extends Fragment {
    FragmentSellTwoBinding binding;
    DatabaseReference databaseReference;
    StorageReference storageReference;
    private ArrayList<Uri> imagesList = new ArrayList<>();
    private final ArrayList<String> imagesUrls = new ArrayList<>();
    String imageUrl1;
    String imageUrl2;
    String imageUrl3;
    Sell sellArgs;
    String f_name;
    String s_name;
    String lastSeen;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSellTwoBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        sellArgs = SellFragmentTwoArgs.fromBundle(getArguments()).getSellTwoArguments();

        imagesList = sellArgs.getImagesList();

        storageReference = FirebaseStorage.getInstance().getReference("images");
        databaseReference = FirebaseDatabase.getInstance().getReference("all_images");

        getCurrentUserDetails();

        binding.postAdButton.setOnClickListener(v -> {
            if (TextUtils.isEmpty(binding.productNameEditText.getText().toString())) {
                binding.productNameEditText.setError("Field can't be empty!");
                return;
            } else if (TextUtils.isEmpty(binding.conditionEditTtext.getText().toString())) {
                binding.conditionEditTtext.setError("Field can't be empty!");
                return;
            } else if (TextUtils.isEmpty(binding.priceEditText.getText().toString())) {
                binding.priceEditText.setError("Field can't be empty!");
                return;
            } else if (TextUtils.isEmpty(binding.phoneNumberEditText.getText().toString())) {
                binding.phoneNumberEditText.setError("Field can't be empty!");
                return;
            } else if (TextUtils.isEmpty(binding.itemDescription.getText().toString())) {
                binding.itemDescription.setError("Field can't be empty!");
            }
            uploadFirebase();
        });


        return view;

    }

    public void uploadFirebase() {
        Timber.d("upload method called");

            if (imagesList != null) {
                ProgressDialog pd = new ProgressDialog(requireContext());
                pd.setTitle("Uploading Item...");
                pd.setCancelable(false);
                pd.show();

                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("items_image");
                for (int counter=0;counter<imagesList.size();counter++) {

                    Uri individualImage = imagesList.get(counter);
                    final StorageReference fileStorageReference = storageReference.child(UUID.randomUUID() +""+individualImage.getLastPathSegment());

                    UploadTask uploadTask = fileStorageReference.putFile(individualImage);

                    uploadTask.continueWithTask(task -> {
                        if (!task.isSuccessful()) {
                            throw Objects.requireNonNull(task.getException());
                        }
                        return fileStorageReference.getDownloadUrl();

                    }).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            assert downloadUri != null;
                            imagesUrls.add(downloadUri.toString());
                            Timber.d("%s", imagesUrls.size());
                            if (imagesUrls.size() == imagesList.size()){
                                Timber.d(" Now about to store data to FireB %s", (imagesUrls.size() == imagesList.size()));
                                storeUrl();
                                pd.dismiss();
                                Navigation.findNavController(requireView()).navigate(R.id.action_sellFragmentTwo_to_homeFragment2);
                            }
                        }

                    }).addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to upload", Toast.LENGTH_SHORT).show());
                }
            }
            else{
                Toast.makeText(requireContext(), "It seems you did not select images", Toast.LENGTH_SHORT).show();
            }

}

    public void storeUrl() {
        traverseList();
        Timber.d("method to store url called");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("all_items");
        String date = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("userProfile", Context.MODE_PRIVATE);

        Sell sell = new Sell(
                sellArgs.getCategory(),
                sellArgs.getLocation(),
                binding.productNameEditText.getText().toString(),
                binding.priceEditText.getText().toString(),
                binding.conditionEditTtext.getText().toString(),
                binding.phoneNumberEditText.getText().toString(),
                date,
                imageUrl2,
                imageUrl1,
                imageUrl3,
                false,
                binding.itemDescription.getText().toString(),
                sharedPreferences.getString("USERNAME", "default"),
                "Thursday 2020");

        databaseReference.push().setValue(sell);
    }

    private void traverseList(){
        Timber.d("Method to retrieve each image url called");
        for (int i=0;i<imagesUrls.size();i++){
            if (i==0){
                imageUrl1 = imagesUrls.get(0);
            }
            else if (i==1){
                imageUrl2 = imagesUrls.get(1);
            }
            else if (i==2){
                imageUrl3 = imagesUrls.get(2);
            }
        }
    }

    private void getCurrentUserDetails(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        String userid = user.getUid();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        reference.child(userid).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                f_name = Objects.requireNonNull(dataSnapshot.child("f_Name").getValue()).toString();
                s_name = Objects.requireNonNull(dataSnapshot.child("l_Name").getValue()).toString();

                SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("userProfile", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("USERNAME", f_name+" "+s_name);
                editor.apply();
                editor.commit();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

}
