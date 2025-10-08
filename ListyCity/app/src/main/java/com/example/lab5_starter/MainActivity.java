package com.example.lab5_starter;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements CityDialogFragment.CityDialogListener {

    private Button addCityButton;
    private Button deleteCityButton;
    private FloatingActionButton confirmDeleteButton;
    private ListView cityListView;

    private ArrayList<City> cityArrayList;
    private ArrayAdapter<City> cityArrayAdapter;

    private FirebaseFirestore db;

    private CollectionReference citiesRef;

    private boolean readyToDelete;
    private int stagedToDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        db = FirebaseFirestore.getInstance();
        citiesRef = db.collection("cities");

        citiesRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
            }
            if (value != null && !value.isEmpty()) {
                cityArrayList.clear();
                for (QueryDocumentSnapshot snapshot : value) {
                    String name = snapshot.getString("name");
                    String province = snapshot.getString("province");

                    cityArrayList.add(new City(name, province));

                    Log.d("Firestore", "DocumentSnapshot successfully written!");

                }
                cityArrayAdapter.notifyDataSetChanged();
            }
        });

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set views
        addCityButton = findViewById(R.id.buttonAddCity);
        deleteCityButton = findViewById(R.id.buttonDeleteCity);
        cityListView = findViewById(R.id.listviewCities);

        addCityButton.setBackgroundColor(getResources().getColor(R.color.regular_button));
        deleteCityButton.setBackgroundColor(getResources().getColor(R.color.regular_button));

        // create city array
        cityArrayList = new ArrayList<>();
        cityArrayAdapter = new CityArrayAdapter(this, cityArrayList);
        cityListView.setAdapter(cityArrayAdapter);

        readyToDelete = false;
        stagedToDelete = -1;

        confirmDeleteButton = findViewById(R.id.confirmDeleteButton);

        if (!readyToDelete){
            confirmDeleteButton.hide();
        }

        // addDummyData();

        // set listeners
        addCityButton.setOnClickListener(view -> {
            CityDialogFragment cityDialogFragment = new CityDialogFragment();
            cityDialogFragment.show(getSupportFragmentManager(),"Add City");
        });

        deleteCityButton.setOnClickListener(view -> {
            if (readyToDelete) {
                Log.e("debug", "ready to delete");
                deleteCityButton.setBackgroundColor(getResources().getColor(R.color.regular_button));
                confirmDeleteButton.hide();
                stagedToDelete = -1;
                cityListView.setBackgroundColor(Color.WHITE);
            } else {
                Log.e("debug", "not ready to delete");
                deleteCityButton.setBackgroundColor(getResources().getColor(R.color.selected_button));
            }

            readyToDelete = !readyToDelete;
        });

        confirmDeleteButton.setOnClickListener(view -> {
            if (readyToDelete && stagedToDelete != -1){
                deleteCity(stagedToDelete);
                readyToDelete = false;
                stagedToDelete = -1;
                confirmDeleteButton.hide();
                deleteCityButton.setBackgroundColor(getResources().getColor(R.color.regular_button));
                cityListView.setBackgroundColor(Color.WHITE);
            }
        });

        cityListView.setOnItemClickListener((adapterView, view, i, l) -> {
            if (!readyToDelete) {
                City city = cityArrayAdapter.getItem(i);
                CityDialogFragment cityDialogFragment = CityDialogFragment.newInstance(city);
                cityDialogFragment.show(getSupportFragmentManager(), "City Details");

            } else {
                stagedToDelete = i;
                confirmDeleteButton.show();
                cityListView.setBackgroundColor(Color.WHITE);
                view.setBackgroundColor(Color.LTGRAY);
            }
        });

    }

    @Override
    public void updateCity(City city, String title, String year) {
        DocumentReference docRef = citiesRef.document(city.getName());
        docRef.delete();

        DocumentReference docRef_2 = citiesRef.document(title);

        city.setName(title);
        city.setProvince(year);
        cityArrayAdapter.notifyDataSetChanged();

        docRef_2.set(city);

        // Updating the database using delete + addition
    }

    @Override
    public void addCity(City city){
        cityArrayList.add(city);
        cityArrayAdapter.notifyDataSetChanged();

        DocumentReference docRef = citiesRef.document(city.getName());
        docRef.set(city);
    }

    public void deleteCity(int position){
        DocumentReference docRef = citiesRef.document(cityArrayList.get(position).getName());
        docRef.delete();

        cityArrayList.remove(position);
        cityArrayAdapter.notifyDataSetChanged();
    }
    public void addDummyData(){
        City m1 = new City("Edmonton", "AB");
        City m2 = new City("Vancouver", "BC");
        cityArrayList.add(m1);
        cityArrayList.add(m2);
        cityArrayAdapter.notifyDataSetChanged();
    }
}