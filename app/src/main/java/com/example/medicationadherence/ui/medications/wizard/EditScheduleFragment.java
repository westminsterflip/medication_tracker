package com.example.medicationadherence.ui.medications.wizard;

import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicationadherence.R;
import com.example.medicationadherence.adapter.StartEndDaysScheduleAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

public class EditScheduleFragment extends Fragment implements RootWizardFragment.ErrFragment {
    private RootWizardViewModel wizardModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wizardModel = new ViewModelProvider(Objects.requireNonNull(Objects.requireNonNull(getParentFragment()).getParentFragment())).get(RootWizardViewModel.class);
        wizardModel.getSchedules();
        wizardModel.getScheduleDays();
        if ( wizardModel.getThisList().size() < 4)
            wizardModel.getThisList().add(this);
        else if(!wizardModel.getThisList().get(3).equals(this))
            wizardModel.getThisList().set(3,this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_edit_schedule, container, false);
        RecyclerView recyclerView = root.findViewById(R.id.scheduleEditorRecyclerView);
        FloatingActionButton fab = root.findViewById(R.id.scheduleAddButton);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditScheduleFragmentDirections.ActionEditScheduleFragment2ToEditScheduleCardFragment2 action = EditScheduleFragmentDirections.actionEditScheduleFragment2ToEditScheduleCardFragment2(0);
                wizardModel.getNavController().navigate(action);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        StartEndDaysScheduleAdapter adapter = new StartEndDaysScheduleAdapter(wizardModel.getScheduleDays(), wizardModel, getActivity());
        recyclerView.setAdapter(adapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeDeleteCallback(adapter));
        itemTouchHelper.attachToRecyclerView(recyclerView);
        ((RootWizardFragment) Objects.requireNonNull(Objects.requireNonNull(getParentFragment()).getParentFragment())).setHasLast(true);
        ((RootWizardFragment) Objects.requireNonNull(getParentFragment().getParentFragment())).setHasNext(false);
        Objects.requireNonNull(((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar()).setTitle("Schedule");
        return root;
    }

    @Override
    public void showErrors() {

    }

    @Override
    public void pause() {

    }

    @Override
    public boolean isExitable() {
        return true;
    }

    private class SwipeDeleteCallback extends ItemTouchHelper.SimpleCallback{
        private final StartEndDaysScheduleAdapter daysScheduleAdapter;
        private final Drawable trashIcon;
        private final ColorDrawable background;

        SwipeDeleteCallback(StartEndDaysScheduleAdapter daysScheduleAdapter) {
            super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            this.daysScheduleAdapter = daysScheduleAdapter;
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
            daysScheduleAdapter.delete(pos);
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
