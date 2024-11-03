package com.example.nailment;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity {

    private RecyclerView manicuristRecyclerView;
    private List<Manicurist> manicuristList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        // Back button to go to the previous activity
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> finish());

        // RecyclerView setup
        manicuristRecyclerView = findViewById(R.id.manicuristRecyclerView);
        manicuristRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Sample manicurist data
        manicuristList = createManicuristList();

        // Adapter setup
        ManicuristAdapter adapter = new ManicuristAdapter(manicuristList, this::openProfileActivity);
        manicuristRecyclerView.setAdapter(adapter);

        // Bottom Navigation Bar
        findViewById(R.id.homeButton).setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        findViewById(R.id.settingsButton).setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
    }

    private List<Manicurist> createManicuristList() {
        List<Manicurist> list = new ArrayList<>();
        list.add(new Manicurist("Jane Doe", "Manicures, Pedicures", "Downtown Nail Spa", R.drawable.nailment_pro1));
        list.add(new Manicurist("Emily Smith", "Manicures", "Uptown Nails", R.drawable.nailment_pro2));
        list.add(new Manicurist("Sarah Johnson", "Pedicures", "East Side Nails", R.drawable.nailment_pro3));
        list.add(new Manicurist("Thomas Brown", "Manicures, Pedicures", "West End Nail Studio", R.drawable.nailment_pro4));
        list.add(new Manicurist("Samantha Lee", "Manicures", "North Point Nail Salon", R.drawable.nailment_pro5));
        return list;
    }

    private void openProfileActivity(Manicurist manicurist) {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("manicurist", manicurist);
        startActivity(intent);
    }

}
