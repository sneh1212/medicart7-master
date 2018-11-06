package com.medikart.otp;

import android.content.Intent;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.medikart.otp.Common.Common;
import com.medikart.otp.Database.Database;
import com.medikart.otp.Model.Food;
import com.medikart.otp.Model.Order;
import com.medikart.otp.Model.Rating;
import com.squareup.picasso.Picasso;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import java.util.Arrays;

public class FoodDetail extends AppCompatActivity implements RatingDialogListener {

     TextView food_name,food_price,food_description;
     ImageView food_image;
     CollapsingToolbarLayout collapsingToolbarLayout;
     FloatingActionButton btnRating;
     CounterFab btncart;
     ElegantNumberButton numberButton;
     RatingBar ratingBar;

     String foodId="";

     FirebaseDatabase database;
     DatabaseReference foods;
     DatabaseReference ratingTbl;

     Food currentFood;


     @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        database = FirebaseDatabase.getInstance();

        foods = database.getReference("Foods");
        ratingTbl = database.getReference("Rating");
        // Init view

        numberButton = (ElegantNumberButton)findViewById(R.id.number_button);
        btncart = (CounterFab) findViewById(R.id.btncart);
        btnRating = (FloatingActionButton)findViewById(R.id.btn_rating);
        ratingBar = (RatingBar)findViewById(R.id.ratingBar);

        btnRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRatingDailog();
            }
        });

        btncart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Database(getBaseContext()).addToCart(new Order(
                        Common.currentUser.getPhone(),
                        foodId,
                        currentFood.getName(),
                        numberButton.getNumber(),
                        currentFood.getPrice(),
                        currentFood.getDiscount(),
                        currentFood.getImage()
                ));
                Toast.makeText(FoodDetail.this, "Added to Cart", Toast.LENGTH_SHORT).show();

            }
        });

        btncart.setCount(new Database(this).getCountCart(Common.currentUser.getPhone()));

        food_description = (TextView)findViewById(R.id.food_description);
        food_name = (TextView)findViewById(R.id.food_name);
        food_price = (TextView)findViewById(R.id.food_price);
        food_image = (ImageView)findViewById(R.id.img_food);

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collasping);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppbar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppbar);

        // get food id from intent

        if(getIntent()!=null)
            foodId = getIntent().getStringExtra("foodId");

          if(!foodId.isEmpty())
          {
              if (Common.isConnectedToInternet(getBaseContext())) {
                  getDetailFood(foodId);
                  getRatingFood(foodId);
              }
              else
              {
                  Toast.makeText(FoodDetail.this, "No Internet Connection !!", Toast.LENGTH_SHORT).show();
                  return;
              }
          }





    }

    private void getRatingFood(String foodId) {

         Query foodRating = ratingTbl.orderByChild("foodId").equalTo(foodId);

        foodRating.addValueEventListener(new ValueEventListener() {

            int count=0,sum=0;
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapshot:dataSnapshot.getChildren())
                {
                    Rating item = postSnapshot.getValue(Rating.class);
                    sum+= Integer.parseInt(item.getRateValue());
                    count++;
                }

                if (count !=0) {
                    float average = sum / count;
                    ratingBar.setRating(average);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showRatingDailog() {

         new AppRatingDialog.Builder()
                 .setPositiveButtonText("Submit")
                 .setNegativeButtonText("Cancel")
                 .setNoteDescriptions(Arrays.asList("Very Bad","Not Good","Quite Ok","Very Good","Excellent"))
                 .setDefaultRating(1)
                 .setTitle("Rate this Medicine")
                 .setDescription("Please select some star and give some feedback")
                 .setTitleTextColor(R.color.colorPrimary)
                 .setDescriptionTextColor(R.color.colorPrimary)
                 .setHint("Please write your comment here....")
                 .setHintTextColor(R.color.colorgreen)
                 .setCommentTextColor(android.R.color.white)
                 .setCommentBackgroundColor(R.color.colorPrimaryDark)
                 .setWindowAnimation(R.style.RatingDialogFadeAnim)
                 .create(FoodDetail.this)
                 .show();


    }

    private void getDetailFood(String foodId) {

        foods.child(foodId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                currentFood =dataSnapshot.getValue(Food.class);

                // set image

                Picasso.with(getBaseContext()).load(currentFood.getImage())
                        .into(food_image);

                collapsingToolbarLayout.setTitle(currentFood.getName());

                food_price.setText(currentFood.getPrice());
                food_name.setText(currentFood.getName());
                food_description.setText(currentFood.getDescription());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onNegativeButtonClicked() {


    }

    @Override
    public void onPositiveButtonClicked(int value, String comments) {


        // get Rating and upload to firebase

        final Rating rating = new Rating(Common.currentUser.getPhone(),
                foodId,
                String.valueOf(value),
                comments);
           ratingTbl.child(Common.currentUser.getPhone()).addListenerForSingleValueEvent(new ValueEventListener() {
               @Override
               public void onDataChange(DataSnapshot dataSnapshot) {

                   if (dataSnapshot.child(Common.currentUser.getPhone()).child(foodId).exists())
                   {
                       //Remove old value (You can delete or let it be-useless function
                       ratingTbl.child(Common.currentUser.getPhone()).child(foodId).removeValue();
                       //Update new Value
                       ratingTbl.child(Common.currentUser.getPhone()).child(foodId).setValue(rating);
                       Toast.makeText(FoodDetail.this, "Thank you for submit rating", Toast.LENGTH_SHORT).show();
                       finish();

                   }
                   else
                   {
                       //Update new Value
                       ratingTbl.child(Common.currentUser.getPhone()).child(foodId).setValue(rating);
                       Toast.makeText(FoodDetail.this, "Thank you for submit rating", Toast.LENGTH_SHORT).show();
                       finish();

                   }

               }

               @Override
               public void onCancelled(DatabaseError databaseError) {

               }
           });



    }
}
