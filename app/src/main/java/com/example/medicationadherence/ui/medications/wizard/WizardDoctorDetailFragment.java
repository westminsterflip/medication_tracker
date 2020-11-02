package com.example.medicationadherence.ui.medications.wizard;


import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.medicationadherence.R;
import com.example.medicationadherence.data.room.entities.Doctor;
import com.example.medicationadherence.ui.MainViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


//TODO: https://developers.google.com/places/android-sdk/autocomplete
//TODO: https://developer.android.com/guide/topics/text/autofill
public class WizardDoctorDetailFragment extends Fragment implements RootWizardFragment.ErrFragment {
    private RootWizardViewModel model;
    Spinner doctorChooser;
    private TextInputLayout doctorNameLayout;
    private TextInputEditText doctorName;
    private TextView doctorNameRequired;
    private TextInputLayout practiceNameLayout;
    private TextInputEditText practiceName;
    private TextInputLayout practiceAddressLayout;
    private TextInputEditText practiceAddress;
    private TextInputLayout phoneLayout;
    private TextInputEditText phone;
    private List<Doctor> doctorList;
    private MainViewModel mainModel;
    private CheckBox scheduleAfter;
    private Button update;
    private Button remove;
    private boolean exitable = true;
    private boolean fromWiz = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(fromWiz = (Objects.requireNonNull(getParentFragment()).getParentFragment() instanceof RootWizardFragment)) {
            model = new ViewModelProvider(Objects.requireNonNull(Objects.requireNonNull(getParentFragment()).getParentFragment())).get(RootWizardViewModel.class);
            if (model.getThisList().size() == 2)
                model.getThisList().add(this);
            else if (model.getThisList().get(2) != this)
                model.getThisList().set(2, this);
        }
        mainModel = new ViewModelProvider(Objects.requireNonNull(getActivity())).get(MainViewModel.class);
        doctorList = mainModel.getRepository().getDoctors();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_wizard_doctor_detail, container, false);
        Objects.requireNonNull(((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar()).setTitle(fromWiz ? "Add Medication" : "Edit Doctors");

        doctorChooser = root.findViewById(R.id.wizardDoctorChooser);
        doctorNameLayout = root.findViewById(R.id.textInputDoctorName);
        doctorName = root.findViewById(R.id.wizardDoctorName);
        doctorNameRequired = root.findViewById(R.id.wizardDocNameRequired);
        practiceNameLayout = root.findViewById(R.id.textInputPracticeName);
        practiceName = root.findViewById(R.id.wizardPracticeName);
        practiceAddressLayout = root.findViewById(R.id.textInputAddress);
        practiceAddress = root.findViewById(R.id.wizardPracticeAddress);
        phoneLayout = root.findViewById(R.id.textInputPhone);
        phone = root.findViewById(R.id.wizardPhone);
        scheduleAfter = root.findViewById(R.id.wizardScheduleAfter);
        update = root.findViewById(R.id.update_doctor);
        remove = root.findViewById(R.id.remove_doctor);

        ArrayList<String> doctors = fromWiz ? new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.doctorChooserItems))) : new ArrayList<String>();
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(Objects.requireNonNull(getContext()), R.layout.support_simple_spinner_dropdown_item, doctors);
        for(Doctor doctor : doctorList){
            adapter.add(doctor.getName());
        }
        doctorChooser.setAdapter(adapter);
        doctorChooser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(fromWiz && position == 0){
                    doctorNameLayout.setVisibility(View.GONE);
                    doctorNameRequired.setVisibility(View.GONE);
                    practiceNameLayout.setVisibility(View.GONE);
                    practiceAddressLayout.setVisibility(View.GONE);
                    phoneLayout.setVisibility(View.GONE);
                    doctorName.setText("");
                    practiceName.setText("");
                    practiceAddress.setText("");
                    phone.setText("");
                    if(fromWiz)
                        model.setDoctorName(null);
                    exitable = true;
                } else {
                    if(!fromWiz || position != 1){
                        doctorNameRequired.setVisibility(View.INVISIBLE);
                        exitable = true;
                        doctorName.setText(doctorList.get(position-(fromWiz?2:0)).getName());
                        practiceName.setText(doctorList.get(position-(fromWiz?2:0)).getPracticeName());
                        practiceAddress.setText(doctorList.get(position-(fromWiz?2:0)).getAddress());
                        phone.setText(doctorList.get(position-(fromWiz?2:0)).getPhone());
                        if (fromWiz)
                            model.setDoctorID(doctorList.get(position-2).getDoctorID());
                    } else {
                        if (model.getSpinnerSelection() != position) {
                            doctorName.setText("");
                            practiceName.setText("");
                            practiceAddress.setText("");
                            phone.setText("");
                        }
                        exitable = !Objects.requireNonNull(doctorName.getText()).toString().equals("");

                    }
                    doctorNameLayout.setVisibility(View.VISIBLE);
                    if(savedInstanceState == null)
                        doctorNameRequired.setVisibility(View.INVISIBLE);
                    else if (savedInstanceState.getBoolean("showErrors") && position == 1)
                        showErrors();
                    practiceNameLayout.setVisibility(View.VISIBLE);
                    practiceAddressLayout.setVisibility(View.VISIBLE);
                    phoneLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //This shouldn't happen
            }
        });
        doctorName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                exitable = !s.toString().equals("");
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        scheduleAfter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    Objects.requireNonNull(model.getDestinations().getValue()).add(R.id.editScheduleFragment2);
                    model.getDestinations().getValue().add(R.id.editScheduleCardFragment2);
                } else {
                    Objects.requireNonNull(model.getDestinations().getValue()).remove((Integer)R.id.editScheduleFragment2);
                    model.getDestinations().getValue().remove((Integer)R.id.editScheduleCardFragment2);
                }
                model.getDestinations().postValue(model.getDestinations().getValue());
            }
        });
        if (!fromWiz) {
            scheduleAfter.setVisibility(View.GONE);
            update.setVisibility(View.VISIBLE);
            update.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!exitable){
                        showErrors();
                    } else {
                        int pos = -1;
                        for (int i = 0; i < doctorList.size(); i++) {
                            if (doctorList.get(i).getDoctorID().equals(doctorList.get(doctorChooser.getSelectedItemPosition()).getDoctorID())) {
                                pos = i;
                                break;
                            }
                        }
                        if (pos != -1)
                            mainModel.updateDoctor(doctorList.get(pos).getDoctorID(), Objects.requireNonNull(doctorName.getText()).toString(), Objects.requireNonNull(practiceName.getText()).toString(), Objects.requireNonNull(practiceAddress.getText()).toString(), Objects.requireNonNull(phone.getText()).toString());
                        adapter.clear();
                        doctorList.get(pos).setName(doctorName.getText().toString());
                        doctorList.get(pos).setPracticeName(practiceName.getText().toString());
                        doctorList.get(pos).setAddress(practiceAddress.getText().toString());
                        doctorList.get(pos).setPhone(phone.getText().toString());
                        adapter.addAll(getNames(doctorList));
                        adapter.notifyDataSetChanged();
                    }
                }
            });
            remove.setVisibility(View.VISIBLE);
            remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for(int i = 0; i < doctorList.size(); i++){
                        if (doctorList.get(i).getDoctorID().equals(doctorList.get(doctorChooser.getSelectedItemPosition()).getDoctorID())){
                            mainModel.remove(doctorList.get(i));
                            adapter.remove(doctorList.get(i).getName());
                            adapter.notifyDataSetChanged();
                            doctorList.remove(i);
                            break;
                        }
                    }
                    if (doctorList.size() == 0){
                        doctorNameLayout.setVisibility(View.GONE);
                        doctorNameRequired.setVisibility(View.GONE);
                        practiceNameLayout.setVisibility(View.GONE);
                        practiceAddressLayout.setVisibility(View.GONE);
                        phoneLayout.setVisibility(View.GONE);
                        update.setEnabled(false);
                        remove.setEnabled(false);
                    } else {
                        doctorChooser.setSelection(0);
                    }
                }
            });
            if (doctorList.size() == 0){
                update.setEnabled(false);
                remove.setEnabled(false);
            }
        }
        if(fromWiz && model.getsMedID() != -1){
            model.setScheduleAfter(true);
            scheduleAfter.setVisibility(View.GONE);
            int i;
            for (i = 0; i < doctorList.size(); i++){
                if (Objects.equals(doctorList.get(i).getDoctorID(), model.getDoctorID())){
                    doctorChooser.setSelection(i + 2);
                    break;
                }
            }
        }
        return root;
    }

    @Override
    public void showErrors() {
        doctorNameRequired.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        if(fromWiz) {
            doctorChooser.setSelection(model.getSpinnerSelection());
            scheduleAfter.setChecked(model.isScheduleAfter());
            if (model.getDoctorName() != null)
                doctorName.setText(model.getDoctorName());
            if (model.getPracticeName() != null)
                practiceName.setText(model.getPracticeName());
            if (model.getPracticeAddress() != null)
                practiceAddress.setText(model.getPracticeAddress());
            if (model.getPhone() != null)
                phone.setText(model.getPhone());
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        pause();
        super.onPause();
    }

    @Override
    public void pause() {
        if (fromWiz) {
            model.setSpinnerSelection(doctorChooser.getSelectedItemPosition());
            if (!Objects.requireNonNull(doctorName.getText()).toString().equals(""))
                model.setDoctorName(doctorName.getText().toString());
            if (!Objects.requireNonNull(practiceName.getText()).toString().equals(""))
                model.setPracticeName(practiceName.getText().toString());
            if (!Objects.requireNonNull(practiceAddress.getText()).toString().equals(""))
                model.setPracticeAddress(practiceAddress.getText().toString());
            if (!Objects.requireNonNull(phone.getText()).toString().equals(""))
                model.setPhone(phone.getText().toString());
            model.setScheduleAfter(scheduleAfter.isChecked());
        } else {
            InputMethodManager manager = (InputMethodManager) Objects.requireNonNull(getActivity()).getSystemService(Context.INPUT_METHOD_SERVICE);
            if (getActivity().getCurrentFocus() != null) {
                Objects.requireNonNull(manager).hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    @Override
    public boolean isExitable() {
        return exitable;
    }

    @SuppressWarnings("unchecked")
    private ArrayList<String> getNames(List<Doctor> doctorList){
        ArrayList<String> names = new ArrayList();
        for (Doctor d : doctorList){
            names.add(d.getName());
        }
        return names;
    }
}
