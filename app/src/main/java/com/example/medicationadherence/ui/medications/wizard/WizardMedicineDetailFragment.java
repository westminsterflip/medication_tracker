package com.example.medicationadherence.ui.medications.wizard;


import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.os.ConfigurationCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.medicationadherence.R;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

//TODO: https://developer.android.com/guide/topics/text/autofill
//TODO: disallow active before start, active after end
public class WizardMedicineDetailFragment extends Fragment implements RootWizardFragment.ErrFragment {
    private RootWizardViewModel model;
    private AutoCompleteTextView medName;
    private TextView medNameRequired;
    private Switch active;
    private TextView startDate;
    private TextView endDate;
    private AutoCompleteTextView perPillDosage;
    private Spinner dosageUnitSelector;
    private TextView dosageRequired;
    private TextInputEditText onHand;
    private TextInputEditText cost;
    private CheckBox asNeeded;
    private TextInputEditText instructions;
    private DatePickerDialog datePickerDialog;
    private boolean start = true;
    private TextView endBefore;
    private boolean exitable = false;
    private Spinner timeChooser;
    private ArrayList<String> doseUnits;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = new ViewModelProvider(Objects.requireNonNull(Objects.requireNonNull(getParentFragment()).getParentFragment())).get(RootWizardViewModel.class);
        datePickerDialog = model.getDatePickerDialog();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_wizard_medicine_detail, container, false);

        medName = root.findViewById(R.id.wizardMedName);
        medNameRequired = root.findViewById(R.id.wizardMedNameRequired);
        active = root.findViewById(R.id.wizardActive);
        Button setStart = root.findViewById(R.id.wizardSetStartDate);
        startDate = root.findViewById(R.id.wizardStartDate);
        Button setEnd = root.findViewById(R.id.wizardSetEndDate);
        endDate = root.findViewById(R.id.wizardEndDate);
        perPillDosage = root.findViewById(R.id.wizardPerPillDosage);
        dosageUnitSelector = root.findViewById(R.id.wizardDosageSpinner);
        dosageRequired = root.findViewById(R.id.wizardPillDoseRequired);
        onHand = root.findViewById(R.id.wizardOnHand);
        cost = root.findViewById(R.id.wizardCost);
        asNeeded = root.findViewById(R.id.wizardAsNeeded);
        instructions = root.findViewById(R.id.wizardInstructions);
        endBefore = root.findViewById(R.id.wizardEndDateBefore);
        timeChooser = root.findViewById(R.id.medicineLate);

        if(savedInstanceState != null) {
            medNameRequired.setVisibility((savedInstanceState.getBoolean("medNameRequiredVisible", false)) ? View.VISIBLE : View.INVISIBLE);
            dosageRequired.setVisibility((savedInstanceState.getBoolean("dosageRequiredVisible", false)) ? View.VISIBLE : View.INVISIBLE);
            endBefore.setVisibility((savedInstanceState.getBoolean("invalidEndVisible", false)) ? View.VISIBLE : View.INVISIBLE);
            if(savedInstanceState.getBoolean("dateDialogVisible", false) && datePickerDialog != null){
                final Calendar cal = Calendar.getInstance();
                if (savedInstanceState.getBoolean("datePickerStart", true)){
                    datePickerDialog = new DatePickerDialog(Objects.requireNonNull(getContext()), null, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
                    datePickerDialog.setOnCancelListener(null);
                    datePickerDialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            long currTime = cal.getTimeInMillis();
                            cal.clear();
                            cal.set(Calendar.YEAR, year);
                            cal.set(Calendar.MONTH, month);
                            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                            ((TextView)root.findViewById(R.id.wizardStartDate)).setText(new SimpleDateFormat("MM/dd/yyyy", ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()).get(0)).format(new Date(cal.getTimeInMillis())));
                            model.setStartDate(cal.getTimeInMillis());
                            if (cal.getTimeInMillis() > currTime) {
                                active.setChecked(false);
                                active.setEnabled(false);
                            }else {
                                active.setChecked(true);
                                active.setEnabled(true);
                            }
                        }
                    });
                    if (model.getEndDate() != -1)
                        datePickerDialog.getDatePicker().setMaxDate(model.getEndDate());
                    datePickerDialog.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis());
                } else {
                    datePickerDialog = new DatePickerDialog(Objects.requireNonNull(getContext()), null, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
                    datePickerDialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            long currTime = cal.getTimeInMillis();
                            cal.clear();
                            cal.set(Calendar.YEAR, year);
                            cal.set(Calendar.MONTH, month);
                            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                            if(cal.getTimeInMillis() < model.getStartDate() || cal.getTimeInMillis() < currTime){
                                model.setEndDate(-1);
                                endDate.setText("");
                                endBefore.setVisibility(View.VISIBLE);
                            } else {
                                endBefore.setVisibility(View.INVISIBLE);
                                model.setEndDate(cal.getTimeInMillis());
                                endDate.setText(new SimpleDateFormat("MM/dd/yyyy", ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()).get(0)).format(new Date(cal.getTimeInMillis())));
                            }
                        }
                    });
                    datePickerDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            model.setEndDate(-1);
                            endDate.setText("");
                            endBefore.setVisibility(View.INVISIBLE);
                        }
                    });
                    datePickerDialog.getDatePicker().setMinDate(model.getStartDate());
                }
                datePickerDialog.onRestoreInstanceState(savedInstanceState.getBundle("datePickerBundle"));
                datePickerDialog.show();
            }
            if (savedInstanceState.getStringArrayList("doseUnits") != null){
                ArrayList<String> doses = savedInstanceState.getStringArrayList("doseUnits");
                ArrayList<String> units = new ArrayList<>();
                ArrayList<String> numbers = new ArrayList<>();
                doseUnits = doses;
                for ( String t : doses){
                    String[] text = t.split(" ", 2);
                    try{
                        Double.parseDouble(text[0].replaceAll(",", ""));
                        if (!units.contains(text[1]))
                            units.add(text[1]);
                        numbers.add(text[0].replaceAll(",",""));
                    } catch (NumberFormatException | NullPointerException e) {
                        units.add(t);
                    }
                }
                if (units.size() != 0) {
                    String[] temp = new String[units.size()];
                    dosageUnitSelector.setAdapter(new ArrayAdapter<>(getContext(), R.layout.support_simple_spinner_dropdown_item, units.toArray(temp)));
                    if (units.size() == 1){
                        dosageUnitSelector.setEnabled(false);
                    }
                }
                if (numbers.size() == 1){
                    perPillDosage.setText(numbers.get(0).split(" ", 2 )[0]);
                }
                if (numbers.size() > 0){
                    String[] temp = new String[numbers.size()];
                    perPillDosage.setAdapter(new ArrayAdapter<String>(getContext(), R.layout.support_simple_spinner_dropdown_item, numbers.toArray(temp)));
                }
            }
        } else if (model.getMedicineFragmentBundle() != null){
            Bundle bundle = model.getMedicineFragmentBundle();
            if (bundle.getStringArrayList("doseUnits") != null){
                ArrayList<String> doses = bundle.getStringArrayList("doseUnits");
                ArrayList<String> units = new ArrayList<>();
                ArrayList<String> numbers = new ArrayList<>();
                doseUnits = doses;
                for ( String t : doses){
                    String[] text = t.split(" ", 2);
                    try{
                        Double.parseDouble(text[0].replaceAll(",", ""));
                        if (!units.contains(text[1]))
                            units.add(text[1]);
                        numbers.add(text[0].replaceAll(",",""));
                    } catch (NumberFormatException | NullPointerException e) {
                        units.add(t);
                    }
                }
                if (units.size() != 0) {
                    String[] temp = new String[units.size()];
                    dosageUnitSelector.setAdapter(new ArrayAdapter<>(getContext(), R.layout.support_simple_spinner_dropdown_item, units.toArray(temp)));
                    if (units.size() == 1){
                        dosageUnitSelector.setEnabled(false);
                    }
                }
                if (numbers.size() == 1){
                    perPillDosage.setText(numbers.get(0).split(" ", 2 )[0]);
                }
                if (numbers.size() > 0){
                    String[] temp = new String[numbers.size()];
                    perPillDosage.setAdapter(new ArrayAdapter<String>(getContext(), R.layout.support_simple_spinner_dropdown_item, numbers.toArray(temp)));
                }
            }
            model.setMedicineFragmentBundle(null);
        }

        setStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start = true;
                final Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY,0);
                cal.clear(Calendar.AM_PM);
                cal.clear(Calendar.MINUTE);
                cal.clear(Calendar.SECOND);
                cal.clear(Calendar.MILLISECOND);
                DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        long currTime = cal.getTimeInMillis();
                        cal.clear();
                        cal.set(Calendar.YEAR, year);
                        cal.set(Calendar.MONTH, month);
                        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        startDate.setText(new SimpleDateFormat("MM/dd/yyyy", ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()).get(0)).format(new Date(cal.getTimeInMillis())));
                        model.setStartDate(cal.getTimeInMillis());
                        if (cal.getTimeInMillis() > currTime)
                            active.setChecked(false);
                        else
                            active.setChecked(true);
                    }
                };
                datePickerDialog = new DatePickerDialog(Objects.requireNonNull(getContext()), listener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.setOnCancelListener(null);
                if (model.getEndDate() != -1)
                    datePickerDialog.getDatePicker().setMaxDate(model.getEndDate());
                datePickerDialog.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis());
                datePickerDialog.show();
            }
        });
        setEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start = false;
                final Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY,0);
                cal.clear(Calendar.AM_PM);
                cal.clear(Calendar.MINUTE);
                cal.clear(Calendar.SECOND);
                cal.clear(Calendar.MILLISECOND);
                DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        long currTime = cal.getTimeInMillis();
                        cal.clear();
                        cal.set(Calendar.YEAR, year);
                        cal.set(Calendar.MONTH, month);
                        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        if(cal.getTimeInMillis() < model.getStartDate() || cal.getTimeInMillis() < currTime){
                            model.setEndDate(-1);
                            endDate.setText("");
                            endBefore.setVisibility(View.VISIBLE);
                        } else {
                            endBefore.setVisibility(View.INVISIBLE);
                            model.setEndDate(cal.getTimeInMillis());
                            endDate.setText(new SimpleDateFormat("MM/dd/yyyy", ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()).get(0)).format(new Date(cal.getTimeInMillis())));
                        }
                    }
                };
                DatePickerDialog.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        model.setEndDate(-1);
                        endDate.setText("");
                        endBefore.setVisibility(View.INVISIBLE);
                    }
                };
                datePickerDialog = new DatePickerDialog(Objects.requireNonNull(getContext()), listener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.setOnCancelListener(cancelListener);
                datePickerDialog.getDatePicker().setMinDate(model.getStartDate());
                datePickerDialog.show();
            }
        });
        medName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().equals("")) {
                    model.setMedName(s.toString());
                    exitable = !Objects.requireNonNull(perPillDosage.getText()).toString().equals("")&&Double.parseDouble(perPillDosage.getText().toString())!=0;
                } else {
                    exitable = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        List<String> names = model.getMainViewModel().getRepository().getNames();
        String[] arr = new String[names.size()];
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(), R.layout.textview, names.toArray(arr));
        medName.setAdapter(arrayAdapter);
        medName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String s = parent.getItemAtPosition(position).toString();
                List<String> doses = model.getMainViewModel().getRepository().getDosages(s);
                doseUnits = (ArrayList<String>) doses;
                ArrayList<String> units = new ArrayList<>();
                ArrayList<String> numbers = new ArrayList<>();
                for ( String t : doses){
                    String[] text = t.split(" ", 2);
                    try{
                        Double.parseDouble(text[0].replaceAll(",", ""));
                        if (!units.contains(text[1]))
                            units.add(text[1]);
                        numbers.add(text[0].replaceAll(",",""));
                    } catch (NumberFormatException | NullPointerException e) {
                        units.add(t);
                    }
                }
                if (units.size() != 0) {
                    String[] temp = new String[units.size()];
                    dosageUnitSelector.setAdapter(new ArrayAdapter<>(getContext(), R.layout.support_simple_spinner_dropdown_item, units.toArray(temp)));
                    if (units.size() == 1){
                        dosageUnitSelector.setEnabled(false);
                    }
                }
                if (numbers.size() == 1){
                    perPillDosage.setText(numbers.get(0).split(" ", 2 )[0]);
                }
                if (numbers.size() > 0){
                    String[] temp = new String[numbers.size()];
                    perPillDosage.setAdapter(new ArrayAdapter<String>(getContext(), R.layout.support_simple_spinner_dropdown_item, numbers.toArray(temp)));
                }
            }
        });
        perPillDosage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                perPillDosage.showDropDown();
                return false;
            }
        });
        String unit;
        if(model.getMedDosage() != null && !dosageUnitSelector.getSelectedItem().equals(unit = model.getMedDosage().replaceAll("[\\d.]", "").replaceAll(" " ,"")))
            dosageUnitSelector.setSelection(Arrays.asList(getResources().getStringArray(R.array.medDosageUnits)).indexOf(unit));
        perPillDosage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().equals("") && Double.parseDouble(s.toString())!=0) {
                    model.setMedDosage(s.toString() + " " + dosageUnitSelector.getSelectedItem());
                    exitable = !Objects.requireNonNull(medName.getText()).toString().equals("");
                } else {
                    exitable = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return root;
    }

    @Override
    public void onResume() {
        if (model.getMedName() != null)
            medName.setText(model.getMedName());
        active.setChecked(model.isActive());
        active.setEnabled(model.isActiveEnabled());
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY,0);
        cal.clear(Calendar.AM_PM);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);
        if(model.getStartDate() == -1){
            model.setStartDate(cal.getTimeInMillis());
        }
        startDate.setText(new SimpleDateFormat("MM/dd/yyyy", ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()).get(0)).format(new Date(model.getStartDate())));
        if(model.getEndDate() != -1)
            endDate.setText(new SimpleDateFormat("MM/dd/yyyy", ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()).get(0)).format(new Date(model.getEndDate())));
        if(model.getMedDosage() != null)
            perPillDosage.setText(model.getMedDosage().split(" ",2)[0].replaceAll("[^\\d.]",""));
        if(model.getOnHand() != -1)
            onHand.setText(model.getOnHand());
        if (model.getCost() != -1)
            cost.setText((model.getCost()+""));
        asNeeded.setChecked(model.isAsNeeded());
        if(model.getInstructions() != null)
            instructions.setText(model.getInstructions());
        if(model.getThisList().isEmpty())
            model.getThisList().add(this);
        else if (model.getThisList().get(0) != this)
            model.getThisList().set(0, this);
        if (model.getLate() != null)
            timeChooser.setSelection(Arrays.asList(getResources().getStringArray(R.array.lateTimes)).indexOf(model.getLate()));
        super.onResume();
    }

    @Override
    public void onPause() {
        pause();
        model.setDatePickerDialog(datePickerDialog);
        model.setActiveEnabled(active.isEnabled());
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if(this.isVisible()){
            outState.putBoolean("medNameRequiredVisible", medNameRequired.getVisibility() == View.VISIBLE);
            outState.putBoolean("dosageRequiredVisible", dosageRequired.getVisibility() == View.VISIBLE);
            outState.putBoolean("invalidEndVisible", endBefore.getVisibility() == View.VISIBLE);
            if(datePickerDialog != null) {
                outState.putBoolean("dateDialogVisible", datePickerDialog.isShowing());
                outState.putBundle("datePickerBundle", datePickerDialog.onSaveInstanceState());
            }
            outState.putBoolean("datePickerStart", start);
            outState.putStringArrayList("doseUnits", doseUnits);
        }
        if(datePickerDialog != null)
            datePickerDialog.dismiss();
        super.onSaveInstanceState(outState);
    }

    @Override
    public void showErrors() {
        if(Objects.requireNonNull(medName.getText()).toString().equals(""))
            medNameRequired.setVisibility(View.VISIBLE);
        else
            medNameRequired.setVisibility(View.INVISIBLE);
        if(Objects.requireNonNull(perPillDosage.getText()).toString().equals("") || Double.parseDouble(perPillDosage.getText().toString()) == 0)
            dosageRequired.setVisibility(View.VISIBLE);
        else
            dosageRequired.setVisibility(View.INVISIBLE);
    }

    @Override
    public void pause() {
        model.setMedName(Objects.requireNonNull(medName.getText()).toString());
        model.setMedDosage(Objects.requireNonNull(perPillDosage.getText()).toString().replaceFirst("^0+(?!$)", "")+" "+dosageUnitSelector.getSelectedItem().toString());
        if(!Objects.requireNonNull(instructions.getText()).toString().equals(""))
            model.setInstructions(instructions.getText().toString());
        model.setActive(active.isChecked());
        if(!Objects.requireNonNull(onHand.getText()).toString().equals(""))
            model.setOnHand(Integer.parseInt(onHand.getText().toString()));
        if(!Objects.requireNonNull(cost.getText()).toString().equals(""))
            model.setCost(Double.parseDouble(cost.getText().toString()));
        model.setAsNeeded(asNeeded.isChecked());
        model.setLate((String)timeChooser.getSelectedItem());
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("doseUnits", doseUnits);
        model.setMedicineFragmentBundle(bundle);
    }

    @Override
    public boolean isExitable() {
        return exitable;
    }

    @Override
    public void prepareBack() {

    }
}
