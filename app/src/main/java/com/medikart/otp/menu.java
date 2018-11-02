    package com.medikart.otp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.medikart.otp.Common.Common;
import com.medikart.otp.Database.Database;
import com.medikart.otp.Interface.ItemClickListener;
import com.medikart.otp.Model.Category;
import com.medikart.otp.Service.ListenOrder;
import com.medikart.otp.ViewHolder.MenuViewHolder;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

public class menu extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    FirebaseDatabase database;
    DatabaseReference category;

    TextView txtfullname;
    RecyclerView recycler_menu;
    RecyclerView.LayoutManager layoutManager;
    CounterFab fab;

    FirebaseRecyclerAdapter<Category,MenuViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Menu");
        setSupportActionBar(toolbar);



        // Init Firebase
        database = FirebaseDatabase.getInstance();
        category = database.getReference("Category");

        Paper.init(this);


         fab = (CounterFab) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cartIntent = new Intent(menu.this,Cart.class);
                startActivity(cartIntent);
            }
        });

        fab.setCount(new Database(this).getCountCart());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // set name for user

        View headerView = navigationView.getHeaderView(0);

        txtfullname =  headerView.findViewById(R.id.fullname);
        txtfullname.setText(Common.currentUser.getName());

        //Load Menu

          recycler_menu = (RecyclerView)findViewById(R.id.recyler_menu);
          recycler_menu.setHasFixedSize(true);
          //layoutManager = new LinearLayoutManager(this);
          //recycler_menu.setLayoutManager(layoutManager);

         recycler_menu.setLayoutManager(new GridLayoutManager(this,2));

          if (Common.isConnectedToInternet(this))

              loadMenu();
          else
          {
              Toast.makeText(this, "No Internet Connection !!", Toast.LENGTH_SHORT).show();
              return;
          }

          //Register services

        Intent service = new Intent(menu.this,ListenOrder.class);
        startService(service);
    }

    //ctrl+o


    @Override
    protected void onResume() {

        super.onResume();
        fab.setCount(new Database(this).getCountCart());
        //Fix Click back button from food and dont see category

    }

    private void loadMenu() {

        adapter = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(Category.class,R.layout.menu_item,MenuViewHolder.class,category) {
            @Override
            protected void populateViewHolder(MenuViewHolder viewHolder, Category model, int position) {

                viewHolder.txtMenuName.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage())
                        .into(viewHolder.imageView);

                final Category clickItem = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public Void onClick(View view, int position, boolean isLongClick) {

                        // get category id and send to new action

                        Intent foodList = new Intent(menu.this,FoodList.class);

                        //because category id is key so we just get key of the item
                        foodList.putExtra("CategoryId",adapter.getRef(position).getKey());
                        startActivity(foodList);
                        return null;
                    }
                });

            }
        };

        recycler_menu.setAdapter(adapter);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId()== R.id.refresh)
            loadMenu();
          switch (item.getItemId())
          {
              case R.id.menulogut:

                  // Delete Remeber  and user and paasword

                  Paper.book().destroy();
                  //FirebaseAuth.getInstance().signOut();
                  //finish();
                  //startActivity(new Intent(menu.this,MainActivity.class));

                  Intent signout = new Intent(menu.this,MainActivity.class);
                  signout.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                  startActivity(signout);
          }

          return true;


    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        {
            if (id == R.id.pre) {



                startActivity(new Intent(menu.this,MainActivity.class));
            } else if (id == R.id.ayu) {

                Intent Cartintent = new Intent(menu.this,Cart.class);
                startActivity(Cartintent);

            } else if (id == R.id.spot) {
                Intent Orderintent = new Intent(menu.this,OrderStatus.class);
                startActivity(Orderintent);

            } else if (id == R.id.nav_change_password) {

                showChangePasswordDailog();

            } else if (id == R.id.home) {

            } else if (id == R.id.up) {

            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showChangePasswordDailog() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(menu.this);
        alertDialog.setTitle("CHANGE PASSWORD");
        alertDialog.setMessage("Please fill all information");

        LayoutInflater inflater = LayoutInflater.from(this);
        View layout_pwd = inflater.inflate(R.layout.change_password_layout,null);

        final MaterialEditText edtPassword = (MaterialEditText)layout_pwd.findViewById(R.id.edtPassword);
        final MaterialEditText edtNewPassword = (MaterialEditText)layout_pwd.findViewById(R.id.edtNewPassword);
        final MaterialEditText edtRepeatPassword = (MaterialEditText)layout_pwd.findViewById(R.id.edtRepeatPassword);

        alertDialog.setView(layout_pwd);

        //Button
        alertDialog.setPositiveButton("CHANGE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //Change password here

                // For use SpotsDialog pls use AlertDialog from android .app , not from V7 like above  Aleart Dialog
                final android.app.AlertDialog waitingDialog = new SpotsDialog(menu.this);
                waitingDialog.show();

                //Check old Password
                if (edtPassword.getText().toString().equals(Common.currentUser.getPassword()))
                {
                    // check new password and repeat password

                    if (edtNewPassword.getText().toString().equals(edtRepeatPassword.getText().toString()))
                    {
                        Map<String,Object> passwordUpdate = new HashMap<>();
                        passwordUpdate.put("password",edtNewPassword.getText().toString());

                        //Make Update
                        DatabaseReference user = FirebaseDatabase.getInstance().getReference("Users");
                        user.child(Common.currentUser.getPhone())
                                .updateChildren(passwordUpdate)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        waitingDialog.dismiss();
                                        Toast.makeText(menu.this, "Password was update", Toast.LENGTH_SHORT).show();

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(menu.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                });
                    }
                    else
                    {
                        waitingDialog.dismiss();
                        Toast.makeText(menu.this, "New password doesn't match", Toast.LENGTH_SHORT).show();
                    }


                }
                else
                {
                    waitingDialog.dismiss();
                    Toast.makeText(menu.this, "Wrong old Password", Toast.LENGTH_SHORT).show();
                }

            }
        });

        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

            }
        });

        alertDialog.show();



    }
}
