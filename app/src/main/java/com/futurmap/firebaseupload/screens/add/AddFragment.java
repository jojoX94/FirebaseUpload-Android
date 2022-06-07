package com.futurmap.firebaseupload.screens.add;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

import android.Manifest;
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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.futurmap.firebaseupload.R;
import com.futurmap.firebaseupload.databinding.FragmentAddBinding;
import com.futurmap.firebaseupload.utils.FileUtils;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class AddFragment extends Fragment {
    private final CharSequence[] options = {"Camera", "Gallery", "Cancel"};
    FragmentAddBinding binding;
    private String selectedImage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add, container, false);
        requirePermission();
        onClickListerner();
        return binding.getRoot();
    }

    public void onClickListerner() {
        binding.cardView.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Select Image");
            builder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (options[which].equals("Camera")) {
                        Intent takePic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(takePic, 0);
                    } else if (options[which].equals("Gallery")) {
                        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(gallery, 1);
                    } else {
                        dialog.dismiss();
                    }
                }
            });

            builder.show();
        });

        binding.btnSubmit.setOnClickListener(v -> {
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_CANCELED) {

            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK && data != null) {
                        Bitmap image = (Bitmap) data.getExtras().get("data");
                        selectedImage = FileUtils.getPath(getContext(), getImageUri(getContext(), image));
                        Log.i("paht", selectedImage);
                        binding.imgUp.setImageBitmap(image);
                    }
                    break;
                case 1:
                    if (resultCode == RESULT_OK && data != null) {

                        Uri image = data.getData();
                        selectedImage = FileUtils.getPath(getContext(), image);
                        Log.i("paht", selectedImage);
                        Picasso.get().load(image).into(binding.imgUp);
                    }
            }

        }
    }

    public Uri getImageUri(Context context, Bitmap bitmap) {
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "myImage", "");

        return Uri.parse(path);
    }


    public void requirePermission() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
    }
}