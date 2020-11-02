package com.example.medicationadherence.ui.medications;


import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicationadherence.R;
import com.example.medicationadherence.adapter.MedicationListAdapter;
import com.example.medicationadherence.data.room.entities.Medication;
import com.example.medicationadherence.ui.MainViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

public class MedicationFragment extends Fragment implements Serializable {
    private MedicationViewModel model;
    private MainViewModel mainModel;
    private ArrayList<MedicationFragment> thisList = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = new ViewModelProvider(this).get(MedicationViewModel.class);
        mainModel = new ViewModelProvider(Objects.requireNonNull(getActivity())).get(MainViewModel.class);
        if (thisList.isEmpty())
            thisList.add(this);
        else
            thisList.set(0, this);
        model.setMedList(mainModel.getMedList());
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_medication, container, false);
        RecyclerView medRecyclerView = root.findViewById(R.id.medicationRecyclerView);
        medRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        model.setMedAdapter(new MedicationListAdapter(model.getMedList(), mainModel, thisList, getActivity()));
        medRecyclerView.setAdapter(model.getMedAdapter());
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeDeleteCallback(model.getMedAdapter()));
        itemTouchHelper.attachToRecyclerView(medRecyclerView);
        sort(mainModel.getMedSortMode());
        setHasOptionsMenu(true);
        FloatingActionButton addMed = root.findViewById(R.id.addMedButton);
        addMed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MedicationFragmentDirections.ActionNavMedicationsToRootWizardFragment action = MedicationFragmentDirections.actionNavMedicationsToRootWizardFragment(thisList);
                Navigation.findNavController(v).navigate(action);
            }
        });
        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.sort, menu);
        Toolbar toolbar = Objects.requireNonNull(getActivity()).findViewById(R.id.toolbar);
        Drawable drawable = getResources().getDrawable(R.drawable.ic_sort, null);
        switch (Objects.requireNonNull(getContext()).getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK){
            case Configuration.UI_MODE_NIGHT_NO:
                drawable.setTint(Color.parseColor("#FF000000"));
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                drawable.setTint(Color.parseColor("#FFFFFFFF"));
        }
        toolbar.setOverflowIcon(drawable);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sort_az_asc:
                sort(0);
                mainModel.setMedSortMode(0);
                break;
            case R.id.sort_az_desc:
                sort(1);
                mainModel.setMedSortMode(1);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void sort(int sortMode) {
        switch (sortMode) {
            case 0:
                sortAZAsc();
                break;
            case 1:
                sortAZDesc();
                break;
        }
        model.getMedAdapter().notifyDataSetChanged();
    }

    private void sortAZAsc() {
        Collections.sort(model.getMedList(), new Comparator<Medication>() {
            @Override
            public int compare(Medication o1, Medication o2) {
                return (o1.getName().compareToIgnoreCase(o2.getName()));
            }
        });
    }

    private void sortAZDesc() {
        Collections.sort(model.getMedList(), new Comparator<Medication>() {
            @Override
            public int compare(Medication o1, Medication o2) {
                return (o2.getName().compareToIgnoreCase(o1.getName()));
            }
        });
    }

//    private void sortStartAsc() {
//        Collections.sort(model.getMedList(), new Comparator<Medication>() {
//            @Override
//            public int compare(Medication o1, Medication o2) {
//                return Long.compare(o1.getStartDate(), o2.getStartDate());
//            }
//        });
//    }

//    private void sortStartDesc() {
//        Collections.sort(model.getMedList(), new Comparator<Medication>() {
//            @Override
//            public int compare(Medication o1, Medication o2) {
//                return Long.compare(o2.getStartDate(), o1.getStartDate());
//            }
//        });
//    }

//    private void sortEndAsc() {
//        Collections.sort(model.getMedList(), new Comparator<Medication>() {
//            @Override
//            public int compare(Medication o1, Medication o2) {
//                return Long.compare(o1.getEndDate(), o2.getEndDate());
//            }
//        });
//    }

//    private void sortEndDesc() {
//        Collections.sort(model.getMedList(), new Comparator<Medication>() {
//            @Override
//            public int compare(Medication o1, Medication o2) {
//                return Long.compare(o2.getEndDate(), o1.getEndDate());
//            }
//        });
//    }

//    private void sortActive() { //sorts active/inactive a-z
//        Collections.sort(model.getMedList(), new Comparator<Medication>() {
//            @Override
//            public int compare(Medication o1, Medication o2) {
//                int compared = Boolean.compare(o2.isStatus(), o1.isStatus());
//                return (compared == 0) ? (o1.getName().compareToIgnoreCase(o2.getName())) : compared;
//            }
//        });
//    }

    @Override
    public void onResume() {
        sort(mainModel.getMedSortMode());
        super.onResume();
    }

    private class SwipeDeleteCallback extends ItemTouchHelper.SimpleCallback{
        private MedicationListAdapter medicationListAdapter;
        private Drawable trashIcon;
        private ColorDrawable background;

        SwipeDeleteCallback(MedicationListAdapter medicationListAdapter) {
            super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            this.medicationListAdapter = medicationListAdapter;
            trashIcon = getResources().getDrawable(R.drawable.ic_delete_24px, null);
            background = new ColorDrawable(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.summaryMissed));
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int pos = viewHolder.getAdapterPosition();
            medicationListAdapter.delete(pos);
        }

        @Override
        public void onMoved(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, int fromPos, @NonNull RecyclerView.ViewHolder target, int toPos, int x, int y) {
            super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y);
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            View view = viewHolder.itemView;
            int offset = R.dimen.fab_margin;
            int iconMargin = (view.getHeight() - trashIcon.getIntrinsicHeight())/2;
            int iconTop = view.getTop() + iconMargin;
            int iconBottom = iconTop + trashIcon.getIntrinsicHeight();
            if (dX > 0) {
                trashIcon.setBounds(view.getLeft()+iconMargin, iconTop, view.getLeft() + iconMargin+trashIcon.getIntrinsicWidth(), iconBottom);
                background.setBounds(view.getLeft(), view.getTop() + 13, view.getLeft() + ((int)dX + offset), view.getBottom() - 13);
            } else if (dX < 0) {
                trashIcon.setBounds(view.getRight()-iconMargin-trashIcon.getIntrinsicWidth(), iconTop, view.getRight() -iconMargin, iconBottom);
                background.setBounds(view.getRight() + ((int)dX - offset), view.getTop() + 13, view.getRight(), view.getBottom() - 13);
            } else {
                background.setBounds(0,0,0,0);
            }
            background.draw(c);
            trashIcon.draw(c);
        }
    }
}
