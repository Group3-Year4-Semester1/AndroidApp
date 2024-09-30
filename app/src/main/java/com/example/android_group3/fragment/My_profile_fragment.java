package com.example.android_group3.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.android_group3.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class My_profile_fragment extends Fragment {

    private TextView edt_user_name;
    private TextView edt_address;
    private TextView edt_dob;
    private TextView edt_phone;
    private TextView edt_sosphone;
    private TextView edt_model;
    private TextView edt_brand;
    private TextView edt_color;
    private TextView edt_license;
    private TextView edt_description;
    private Button edt_bt;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_profile, container, false);

        edt_user_name = view.findViewById(R.id.tv_nameuser);
        edt_address = view.findViewById(R.id.tv_address);
        edt_dob = view.findViewById(R.id.tv_dob);
        edt_phone = view.findViewById(R.id.tv_phone);
        edt_sosphone = view.findViewById(R.id.tv_sosphone);
        edt_model = view.findViewById(R.id.tv_model);
        edt_brand = view.findViewById(R.id.tv_brand);
        edt_color = view.findViewById(R.id.tv_color);
        edt_license = view.findViewById(R.id.tv_license);
        edt_description = view.findViewById(R.id.tv_description);
        edt_bt = view.findViewById(R.id.edt_button);


        readDatabase();


        // Set click listener for the button to navigate to Update_profile_fragment
        edt_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), Update_profile_fragment.class);
                startActivity(intent);
            }
        });


        return view;
    }


    private void readDatabase() {
        // Get the current authenticated user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }

        // Extract email and username
        String email = user.getEmail();
        if (email == null || !email.contains("@")) {
            Toast.makeText(getContext(), "Invalid email address.", Toast.LENGTH_SHORT).show();
            return;
        }
        String username = email.split("@")[0];

        // Get reference to Firebase Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userInformationRef = database.getReference(username + "/information");

        // Add a listener to read data
        userInformationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Retrieve the "Name" field
                String name = dataSnapshot.child("Name").getValue(String.class);
                    edt_user_name.setText(name);
                String address = dataSnapshot.child("Address").getValue(String.class);
                    edt_address.setText(address);
                Long phone = dataSnapshot.child("Phone").getValue(Long.class);
                    edt_phone.setText(String.valueOf(phone));
                Long sosphone = dataSnapshot.child("SoSPhone").getValue(Long.class);
                    edt_sosphone.setText(String.valueOf(sosphone));
                String DoB = dataSnapshot.child("DateOfBirth").getValue(String.class);
                    edt_dob.setText(DoB);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to read data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });





        DatabaseReference vehicleRef = database.getReference(username + "/device");

        // Add a listener to read data
        vehicleRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Retrieve the "Name" field
                String model = dataSnapshot.child("Name").getValue(String.class);
                edt_model.setText(model);
                String brand = dataSnapshot.child("Brand").getValue(String.class);
                edt_brand.setText(brand);
                String color = dataSnapshot.child("Color").getValue(String.class);
                edt_color.setText(color);
                String license = dataSnapshot.child("LicensePlate").getValue(String.class);
                edt_license.setText(license);
                String description = dataSnapshot.child("Description").getValue(String.class);
                edt_description.setText(description);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to read data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



}
