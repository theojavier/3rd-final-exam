package com.example.a3rd.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.a3rd.databinding.FragmentHomeBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Connect to Firebase "examSchedule"
        DatabaseReference dbSchedule = FirebaseDatabase.getInstance().getReference("examSchedule");
        dbSchedule.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    binding.txtSubject.setText(snapshot.child("exam1").child("subject").getValue(String.class));
                    binding.txtDate1.setText(snapshot.child("exam1").child("date").getValue(String.class));
                    binding.txtTime1.setText(snapshot.child("exam1").child("time").getValue(String.class));
                    binding.txtStatus1.setText(snapshot.child("exam1").child("status").getValue(String.class));

                    binding.txtSubject2.setText(snapshot.child("exam2").child("subject").getValue(String.class));
                    binding.txtDate2.setText(snapshot.child("exam2").child("date").getValue(String.class));
                    binding.txtTime2.setText(snapshot.child("exam2").child("time").getValue(String.class));
                    binding.txtStatus2.setText(snapshot.child("exam2").child("status").getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle Firebase error
            }
        });

        // Connect to Firebase "results"
        DatabaseReference dbResults = FirebaseDatabase.getInstance().getReference("results");
        dbResults.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    binding.txtResultSubject1.setText(snapshot.child("result1").child("subject").getValue(String.class));
                    binding.txtResultScore1.setText(snapshot.child("result1").child("score").getValue(String.class));
                    binding.txtResultIntegrity1.setText(snapshot.child("result1").child("integrity").getValue(String.class));

                    binding.txtResultSubject2.setText(snapshot.child("result2").child("subject").getValue(String.class));
                    binding.txtResultScore2.setText(snapshot.child("result2").child("score").getValue(String.class));
                    binding.txtResultIntegrity2.setText(snapshot.child("result2").child("integrity").getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle Firebase error
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
