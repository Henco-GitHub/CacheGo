package com.example.cachego;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.cachego.databinding.FragmentAccountBinding;
import com.example.cachego.databinding.FragmentMycachesBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link fragment_account#newInstance} factory method to
 * create an instance of this fragment.
 */
public class fragment_account extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    //Our own Variables
    private FragmentAccountBinding binding;
    Fragment OverlayFragment;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public fragment_account() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment fragment_account.
     */
    // TODO: Rename and change types and number of parameters
    public static fragment_account newInstance(String param1, String param2) {
        fragment_account fragment = new fragment_account();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Our own code
        binding = FragmentAccountBinding.inflate(inflater,container,false);
        View view = binding.getRoot();

        binding.rlMyCaches.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getParentFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                OverlayFragment = new fragment_mycaches();
                transaction.replace(R.id.overlay_fragment, OverlayFragment);
                transaction.commit();
            }
        });

        return view;
    }
}