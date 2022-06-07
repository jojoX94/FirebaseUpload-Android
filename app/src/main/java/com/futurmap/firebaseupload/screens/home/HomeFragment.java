package com.futurmap.firebaseupload.screens.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.futurmap.firebaseupload.R;
import com.futurmap.firebaseupload.databinding.FragmentHomeBinding;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {
    FragmentHomeBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false);
        onClickListener();
        return binding.getRoot();
    }

    public void onClickListener() {
        binding.btnAdd.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.action_homeFragment_to_addFragment));
    }


}