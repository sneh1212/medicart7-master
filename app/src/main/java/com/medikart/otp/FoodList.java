package com.medikart.otp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.medikart.otp.Common.Common;
import com.medikart.otp.Interface.ItemClickListener;
import com.medikart.otp.Model.Food;
import com.medikart.otp.ViewHolder.FoodViewHolder;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class FoodList extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference foodList;

    String CategoryId="";
    FirebaseRecyclerAdapter<Food,FoodViewHolder> adapter;

    // Search functionality

    FirebaseRecyclerAdapter<Food,FoodViewHolder> searchAdapter;
    List<String> suggestList = new ArrayList<>();
    MaterialSearchBar materialSearchBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);

        database = FirebaseDatabase.getInstance();
        foodList = database.getReference("Foods");

        recyclerView = findViewById(R.id.recyler_food);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // get intent here
        if(getIntent()!=null)
            CategoryId = getIntent().getStringExtra("CategoryId");

        if(!CategoryId.isEmpty() && CategoryId !=null)
        {
            if (Common.isConnectedToInternet(getBaseContext()))

                loadListFood(CategoryId);

            else
            {
                Toast.makeText(FoodList.this, "No Internet Connection !!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        //Search
        materialSearchBar = (MaterialSearchBar)findViewById(R.id.searchBar);
        materialSearchBar.setHint("Enter your medicine name");
       //  materialSearchBar.setSpeechMode(false); no need we have already defined it
        loadSuggest(); // Write fucntion load suggest from firebase
        materialSearchBar.setLastSuggestions(suggestList);
        materialSearchBar.setCardViewElevation(10);
        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                // when user type their text,we will change suggest list

                List<String> suggest = new ArrayList<String>();
                for (String search:suggestList) // loop in suggest list
                {

                    if(search.toLowerCase().contains(materialSearchBar.getText().toLowerCase()))
                          suggest.add(search);
                }
                materialSearchBar.setLastSuggestions(suggest);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                // when search Bar is close
                //Restore original suggest adapter
                if (!enabled)
                    recyclerView.setAdapter(adapter);

            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                //when search finish
                //Restore original adapter
                startSearch(text);

            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });

    }

    private void startSearch(CharSequence text) {

        searchAdapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(
                Food.class,
                R.layout.food_item,
                FoodViewHolder.class,
                foodList.orderByChild("Name").equalTo(text.toString()) // compare name
        ) {
            @Override
            protected void populateViewHolder(FoodViewHolder viewHolder, Food model, int position) {
                viewHolder.food_name.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage())
                        .into(viewHolder.food_image);

                final Food loacal = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public Void onClick(View view, int position, boolean isLongClick) {

                        // start new activity

                        Intent fooddetail = new Intent(FoodList.this, FoodDetail.class);
                        fooddetail.putExtra("FoodId", searchAdapter.getRef(position).getKey());// send food id to new activity
                        startActivity(fooddetail);
                        return null;

                    }
                });
            }
        };
                recyclerView.setAdapter(searchAdapter);// set Adapter for recycler view is a search result
    }

    private void loadSuggest() {

        foodList.orderByChild("MenuId").equalTo(CategoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot postSnapShot:dataSnapshot.getChildren())
                        {
                            Food item = postSnapShot.getValue(Food.class);
                            suggestList.add(item.getName()); // Add name of food to suggest list
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void loadListFood(String categoryId) {
                                                                                                                                                    // like: select* from Foods Where MenuId =
        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(Food.class,R.layout.food_item,FoodViewHolder.class,foodList.orderByChild("MenuId").equalTo(categoryId)) {
            @Override
            protected void populateViewHolder(FoodViewHolder viewHolder, Food model, int position) {

                viewHolder.food_name.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage())
                        .into(viewHolder.food_image);

                final Food loacal = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public Void onClick(View view, int position, boolean isLongClick) {

                        // start new activity

                        Intent fooddetail = new Intent(FoodList.this,FoodDetail.class);
                        fooddetail.putExtra("FoodId",adapter.getRef(position).getKey());// send food id to new activity
                        startActivity(fooddetail);
                        return null;
                    }
                });

            }
        };

        // set adapter
        recyclerView.setAdapter(adapter);
    }
}
