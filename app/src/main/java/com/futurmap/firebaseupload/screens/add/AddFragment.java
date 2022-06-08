package com.futurmap.firebaseupload.screens.add;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.futurmap.firebaseupload.MainActivity;
import com.futurmap.firebaseupload.R;
import com.futurmap.firebaseupload.databinding.FragmentAddBinding;
import com.futurmap.firebaseupload.utils.FileUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class AddFragment extends Fragment {
    FirebaseDatabase database;
    FirebaseStorage storage;
    private final CharSequence[] options = { "Camera","Gallery", "Cancel"};
    FragmentAddBinding binding;
    ActivityResultLauncher galleryLauncher;
    ActivityResultLauncher<Intent> cameraLauncher;
    private String selectedImage;
    private Uri tempImage;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add, container, false);
        init();
        requirePermission();
        setSelectedImageToView();
        onClickListerner();
        return binding.getRoot();
    }

    private void init() {
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        database.getReference().child("image").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String image = snapshot.getValue((String.class));
                Picasso.get().load(image).into(binding.imgUp);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.d(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();

                        // Log and toast
                        Log.d(TAG, token);
                        Toast.makeText(getContext(), token, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setSelectedImageToView() {
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {

                        tempImage = result;
                        binding.imgUp.setImageURI(result);
                    }
                });
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                            binding.imgUp.setImageURI(tempImage);
                        if (result.getResultCode() == RESULT_OK) {
                            // There are no request codes

                        }
                    }
                });
    }

    public void onClickListerner() {
        binding.cardView.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Select Image");
            builder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (options[which].equals("Camera")) {
                        ContentValues values = new ContentValues();
                        values.put(MediaStore.Images.Media.TITLE, "New Picture");
                        values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
                        tempImage = requireContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                        Intent takePic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        takePic.putExtra(MediaStore.EXTRA_OUTPUT, tempImage);
                        cameraLauncher.launch(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));
                    } else if (options[which].equals("Gallery")) {
                        galleryLauncher.launch("image/*");
                    } else {
                        dialog.dismiss();
                    }
                }
            });

            builder.show();
        });

        binding.btnSubmit.setOnClickListener(v -> {
            StorageReference reference = storage.getReference().child("image");
            reference.putFile(tempImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            database.getReference().child("image")
                                    .setValue(uri.toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Toast.makeText(getContext(), "Success", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    });
                }
            });
        });
    }




    public void requirePermission() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, 1);
    }
}