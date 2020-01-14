package com.hub.dairy.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.hub.dairy.R;
import com.hub.dairy.adapters.AnimalAdapter;
import com.hub.dairy.models.Animal;
import com.hub.dairy.ui.DetailActivity;

import java.util.ArrayList;
import java.util.List;

import static com.hub.dairy.helpers.Constants.ANIMALS;
import static com.hub.dairy.helpers.Constants.REG_DATE;
import static com.hub.dairy.helpers.Constants.USER_ID;

public class AnimalFragment extends Fragment implements AnimalAdapter.AnimalClick {

    private static final String TAG = "AnimalFragment";
    private String userId;
    private RecyclerView animalRv;
    private List<Animal> mAnimals = new ArrayList<>();
    private CollectionReference animalRef;
    private AnimalAdapter mAnimalAdapter;
    private ProgressBar progress;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_animal, container, false);
        initViews(view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        animalRef = database.collection(ANIMALS);
        if (user != null) {
            userId = user.getUid();
        } else {
            Log.d(TAG, "onActivityCreated: User not logged in");
        }

        mAnimalAdapter = new AnimalAdapter(mAnimals, this);

        loadAnimals();
    }

    private void loadAnimals() {
        progress.setVisibility(View.VISIBLE);
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        animalRv.setHasFixedSize(true);
        animalRv.setLayoutManager(manager);

        Query animalQuery = animalRef.whereEqualTo(USER_ID, userId);
        animalQuery
                .orderBy(REG_DATE, Query.Direction.DESCENDING)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                mAnimals.clear();
                mAnimals.addAll(queryDocumentSnapshots.toObjects(Animal.class));
                animalRv.setAdapter(mAnimalAdapter);
                mAnimalAdapter.notifyDataSetChanged();
                progress.setVisibility(View.GONE);
            } else {
                progress.setVisibility(View.GONE);
                Toast.makeText(getContext(), "No animals yet", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initViews(View view) {
        animalRv = view.findViewById(R.id.animalRecycler);
        progress = view.findViewById(R.id.progress);
    }

    @Override
    public void onAnimalClick(Animal animal) {
        Intent intent = new Intent(getContext(), DetailActivity.class);
        intent.putExtra("animal", animal);
        startActivity(intent);
    }
}
