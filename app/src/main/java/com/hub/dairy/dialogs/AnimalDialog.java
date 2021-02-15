package com.hub.dairy.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hub.dairy.R;
import com.hub.dairy.models.Animal;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;
import static com.hub.dairy.helpers.Constants.ANIMALS;
import static com.hub.dairy.helpers.Constants.UPLOADS;

public class AnimalDialog extends AppCompatDialogFragment {

    private static final String TAG = "AnimalDialog";
    private EditText name, gender, location, breed, category, status, regDate, availability;
    private CircleImageView animalImg;
    private TextView txtUpdate;
    private Button btnUpdate;
    private int IMAGE_REQUEST_CODE = 1002;
    private Animal mAnimal;
    private CollectionReference animalRef;
    private boolean isClicked = false;
    private Uri imageUri;
    private ProgressBar uploadProgress;
    private String mDownloadUrl;
    private StorageReference mStorageReference;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        LayoutInflater inflater = getActivity().getLayoutInflater();
        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.animal_dialog, null);

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        mStorageReference = FirebaseStorage.getInstance().getReference(UPLOADS);
        animalRef = database.collection(ANIMALS);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mAnimal = bundle.getParcelable("animal");
        } else {
            Log.d(TAG, "onCreateDialog: No Animal passed");
        }

        initViews(view);

        showInfoInViews(view);

        builder.setView(view);
        AlertDialog alertDialog = builder.create();

        btnUpdate.setOnClickListener(v -> updateInfo());

        txtUpdate.setOnClickListener(v -> enableViews());

        animalImg.setOnClickListener(v -> openGallery());

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                            WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }

        return alertDialog;
    }

    private void openGallery() {
        if (isClicked) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, IMAGE_REQUEST_CODE);
        } else {
            Toast.makeText(getContext(), "Update button not clicked", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileExtension(Uri uri) {
        return MimeTypeMap.getFileExtensionFromUrl(uri.toString());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            animalImg.setImageURI(imageUri);
            uploadImage();
        }
    }

    private void uploadImage() {
        uploadProgress.setVisibility(View.VISIBLE);
        if (imageUri != null) {
            StorageReference fileRef = mStorageReference.child(mAnimal.getAnimalId())
                    .child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
            fileRef.putFile(imageUri).addOnProgressListener(taskSnapshot -> {
                double progress = (100.0 * taskSnapshot.getBytesTransferred() /
                        taskSnapshot.getTotalByteCount());
                uploadProgress.incrementProgressBy((int) progress);
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        mDownloadUrl = uri.toString();
                        uploadProgress.setVisibility(View.GONE);
                    });
                } else {
                    uploadProgress.setVisibility(View.GONE);
                    Toast.makeText(requireActivity(), "Please try again", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                uploadProgress.setVisibility(View.GONE);
                Toast.makeText(requireActivity(), "Something went wrong", Toast.LENGTH_SHORT).show();
            });
        } else {
            uploadProgress.setVisibility(View.GONE);
        }
    }

    private void enableViews() {
        isClicked = true;
        btnUpdate.setVisibility(View.VISIBLE);
        name.setEnabled(true);
        location.setEnabled(true);
        breed.setEnabled(true);
        status.setEnabled(true);
    }

    private void showInfoInViews(View view) {
        name.setText(mAnimal.getAnimalName());
        gender.setText(mAnimal.getGender());
        location.setText(mAnimal.getLocation());
        breed.setText(mAnimal.getAnimalBreed());
        category.setText(mAnimal.getCategory());
        status.setText(mAnimal.getStatus());
        regDate.setText(mAnimal.getRegDate());
        availability.setText(mAnimal.getAvailability());

        Glide.with(view.getContext()).load(mAnimal.getImageUrl()).into(animalImg);
    }

    private void updateInfo() {
        if (!name.isEnabled() || !location.isEnabled() || !breed.isEnabled()) {
            Toast.makeText(getContext(), R.string.unable_to_update, Toast.LENGTH_SHORT).show();
        } else {
            String updateName = name.getText().toString().trim();
            String updateLoc = location.getText().toString().trim();
            String updateBreed = breed.getText().toString().trim();

            Map<String, Object> updateInfo = new HashMap<>();
            updateInfo.put("animalName", updateName);
            updateInfo.put("location", updateLoc);
            updateInfo.put("animalBreed", updateBreed);

            if (mDownloadUrl != null) {
                updateInfo.put("imageUrl", mDownloadUrl);
            }

            animalRef.document(mAnimal.getAnimalId())
                    .set(updateInfo, SetOptions.merge()).addOnSuccessListener(aVoid -> {
                name.setEnabled(false);
                location.setEnabled(false);
                breed.setEnabled(false);
                status.setEnabled(false);
                btnUpdate.setVisibility(View.GONE);
                isClicked = false;
                Toast.makeText(getContext(), R.string.success_msg, Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void initViews(View view) {
        animalImg = view.findViewById(R.id.animalImg);
        name = view.findViewById(R.id.animalName);
        gender = view.findViewById(R.id.animalGender);
        location = view.findViewById(R.id.animalLocation);
        breed = view.findViewById(R.id.animalBreed);
        category = view.findViewById(R.id.animalCategory);
        status = view.findViewById(R.id.animalStatus);
        regDate = view.findViewById(R.id.animalRegDate);
        availability = view.findViewById(R.id.animalAvailability);
        btnUpdate = view.findViewById(R.id.btnUpdateInfo);
        txtUpdate = view.findViewById(R.id.txtUpdate);
        uploadProgress = view.findViewById(R.id.uploadProgress);
    }
}
