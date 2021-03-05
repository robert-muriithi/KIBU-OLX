package com.ifixhubke.kibu_olx.ui.fragments.home;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ifixhubke.kibu_olx.R;
import com.ifixhubke.kibu_olx.adapters.AllItemsAdapter;
import com.ifixhubke.kibu_olx.data.Favourites;
import com.ifixhubke.kibu_olx.data.Item;
import com.ifixhubke.kibu_olx.databinding.FragmentHomeBinding;
import com.ifixhubke.kibu_olx.others.ItemClickListener;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import timber.log.Timber;

public class HomeFragment extends Fragment implements ItemClickListener {
    FragmentHomeBinding binding;
    private AllItemsAdapter adapter;
    private DatabaseReference databaseReference;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();


        databaseReference = FirebaseDatabase.getInstance().getReference();

        setHasOptionsMenu(true);

        initializeRecycler();

        binding.searchForItem.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                //filter(s.toString());
            }
        });

        binding.floatingActionButton.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_homeFragment2_to_sellFragmentOne));

        return view;
    }

    /*public void filter(String itemName){
        ArrayList<Item> filterItemsList = new ArrayList<>();

        for(Item item : itemsList){
            if(item.getItemName().toLowerCase().contains(itemName.toLowerCase())){
                filterItemsList.add(item);
            }
        }
        adapter = new AllItemsAdapter(filterItemsList);
        binding.allItemsRecyclerview.setAdapter(adapter);
    }*/

    private void initializeRecycler() {

        Query query = databaseReference.child("all_items");

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    //Toast.makeText(requireContext(),"data exists",Toast.LENGTH_SHORT).show();
                    Timber.d("data exists");
                    binding.shimmerFrameLayout.setVisibility(View.INVISIBLE);
                    binding.allItemsRecyclerview.setVisibility(View.VISIBLE);
                } else {
                    //Toast.makeText(requireContext(),"No data exists",Toast.LENGTH_SHORT).show();
                    binding.imageView2.setVisibility(View.VISIBLE);
                    binding.textView.setVisibility(View.VISIBLE);
                    binding.shimmerFrameLayout.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        FirebaseRecyclerOptions<Item> options = new FirebaseRecyclerOptions.Builder<Item>()
                .setQuery(query, Item.class)
                .build();

        adapter = new AllItemsAdapter(options, this);
        binding.allItemsRecyclerview.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.shimmerFrameLayout.stopShimmer();
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.shimmerFrameLayout.startShimmer();
    }

    @Override
    public void addItemToFavorites(Item item, int position) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("favoriteitems");
        databaseReference.child(UUID.randomUUID().toString()).setValue(item).addOnSuccessListener(aVoid -> Toast.makeText(requireContext(), item.getItemName() + " to favorites successfully", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void clickCard(Favourites favourites, int position) {
        
    }
}