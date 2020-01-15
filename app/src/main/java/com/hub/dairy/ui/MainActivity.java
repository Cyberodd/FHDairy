
package com.hub.dairy.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hub.dairy.R;
import com.hub.dairy.adapters.TabPagerAdapter;
import com.hub.dairy.fragments.AnimalFragment;
import com.hub.dairy.fragments.ReportsFragment;
import com.hub.dairy.fragments.TransactionDialog;
import com.hub.dairy.fragments.TransactionFragment;


public class MainActivity extends AppCompatActivity implements TransactionDialog.TransInterface {

    private static final String TAG = "MainActivity";
    private Toolbar mToolbar;
    private ViewPager mPager;
    private TabLayout mTabLayout;
    private FloatingActionButton mFab, mAddAnimal, mAddTransaction;
    private boolean isFabOpen = true;
    private TextView txtAddAnimal, txtTransaction;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        initViews();

        setSupportActionBar(mToolbar);
        mToolbar.setTitle("Dairy");

        TabPagerAdapter pagerAdapter = new TabPagerAdapter(getSupportFragmentManager(),
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        pagerAdapter.addFragments(new AnimalFragment(), "Animals");
        pagerAdapter.addFragments(new TransactionFragment(), "Transactions");
        pagerAdapter.addFragments(new ReportsFragment(), "Reports");
        mPager.setAdapter(pagerAdapter);
        mTabLayout.setupWithViewPager(mPager);

        mFab.setOnClickListener(v -> displayAnimations());

        mAddAnimal.setOnClickListener(v -> navigateToAddAnimal());

        mAddTransaction.setOnClickListener(v -> toTransaction());
    }

    private void toTransaction() {
        TransactionDialog dialog = new TransactionDialog();
        dialog.show(getSupportFragmentManager(), "TransactionDialog");
        closeFab();
    }

    private void navigateToAddAnimal() {
        Intent intent = new Intent(this, AnimalActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        closeFab();
    }

    private void displayAnimations() {
        if (!isFabOpen)
            closeFab();
        else
            openFab();
    }

    private void openFab() {
        isFabOpen = false;
        mAddAnimal.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        mAddTransaction.animate().translationY(-getResources().getDimension(R.dimen.standard_105));
        txtAddAnimal.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        txtTransaction.animate().translationY(-getResources().getDimension(R.dimen.standard_105));
        txtAddAnimal.setVisibility(View.VISIBLE);
        txtTransaction.setVisibility(View.VISIBLE);
    }

    private void closeFab() {
        isFabOpen = true;
        mAddAnimal.animate().translationY(0.0F);
        txtAddAnimal.animate().translationY(0.0F);
        mAddTransaction.animate().translationY(0.0F);
        txtTransaction.animate().translationY(0.0F);
        txtAddAnimal.setVisibility(View.GONE);
        txtTransaction.setVisibility(View.GONE);
    }

    private void initViews() {
        mToolbar = findViewById(R.id.homeToolbar);
        mPager = findViewById(R.id.homePager);
        mTabLayout = findViewById(R.id.mainTabLayout);
        mFab = findViewById(R.id.btnFab);
        mAddAnimal = findViewById(R.id.btnAddAnimal);
        txtAddAnimal = findViewById(R.id.txtAddAnimal);
        mAddTransaction = findViewById(R.id.btnAddTransaction);
        txtTransaction = findViewById(R.id.txtAddTransaction);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_log_out:
                logOut();
                return true;
            case R.id.action_search:
                Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_profile:
                toProfile();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void toProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    private void logOut() {
        if (mUser != null){
            mAuth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            Log.d(TAG, "logOut: User not logged in");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mUser == null){
            toLoginActivity();
        } else {
            Log.d(TAG, "onStart: User logged in");
        }
    }

    private void toLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void notifyInput(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
