package com.example.taptimingkeyboard;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.taptimingkeyboard.data.LayoutStringDbString;
import com.example.taptimingkeyboard.data.UserInfo;

import java.util.ArrayList;

public class UserInfoActivity extends AppCompatActivity {

    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText ageEditText;
    private Spinner sexSpinner;
    private Spinner handednessSpinner;
    private CheckBox diagnosedWithPDCheckbox;
    private Spinner symptomsAsymmetrySpinner;
    private CheckBox onMedicationCheckbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        firstNameEditText=findViewById(R.id.edit_text_name);
        lastNameEditText=findViewById(R.id.edit_text_last_name);
        ageEditText=findViewById(R.id.edit_text_age);
        sexSpinner=findViewById(R.id.spinner_sex);
        handednessSpinner=findViewById(R.id.spinner_handedness);
        diagnosedWithPDCheckbox=findViewById(R.id.check_box_diagnosed);
        symptomsAsymmetrySpinner =findViewById(R.id.spinner_diagnosed_asymmetry);
        onMedicationCheckbox=findViewById(R.id.check_box_on_medication);
        ArrayList<LayoutStringDbString> sexesList = new ArrayList<>(2);
        sexesList.add(new LayoutStringDbString(getSexResourceString(UserInfo.SEX_MALE),UserInfo.SEX_MALE));
        sexesList.add(new LayoutStringDbString(getSexResourceString(UserInfo.SEX_FEMALE),UserInfo.SEX_FEMALE));
        sexSpinner.setAdapter(new ArrayAdapter<>(this,R.layout.support_simple_spinner_dropdown_item,sexesList));
        ArrayList<LayoutStringDbString> handednessList = new ArrayList<>(2);
        handednessList.add(new LayoutStringDbString(getHandednessResourceString(UserInfo.HANDEDNESS_LEFT),UserInfo.HANDEDNESS_LEFT));
        handednessList.add(new LayoutStringDbString(getHandednessResourceString(UserInfo.HANDEDNESS_RIGHT),UserInfo.HANDEDNESS_RIGHT));
        handednessSpinner.setAdapter(new ArrayAdapter<>(this,R.layout.support_simple_spinner_dropdown_item,handednessList));
        ArrayList<LayoutStringDbString> symptomsAsymmetryList = new ArrayList<>(4);
        symptomsAsymmetryList.add(new LayoutStringDbString(getAsymmetryResourceString(UserInfo.ASYMMETRY_LEFT),UserInfo.ASYMMETRY_LEFT));
        symptomsAsymmetryList.add(new LayoutStringDbString(getAsymmetryResourceString(UserInfo.ASYMMETRY_RIGHT),UserInfo.ASYMMETRY_RIGHT));
        symptomsAsymmetryList.add(new LayoutStringDbString(getAsymmetryResourceString(UserInfo.ASYMMETRY_NO_ASYMMETRY),UserInfo.ASYMMETRY_NO_ASYMMETRY));
        symptomsAsymmetryList.add(new LayoutStringDbString(getAsymmetryResourceString(UserInfo.ASYMMETRY_NOT_SPECIFIED),UserInfo.ASYMMETRY_NOT_SPECIFIED));
        symptomsAsymmetrySpinner.setAdapter(new ArrayAdapter<>(this,R.layout.support_simple_spinner_dropdown_item,symptomsAsymmetryList));
    }

    private String getSexResourceString(String dbString) {
        switch (dbString) {
            case UserInfo.SEX_MALE:
                return getResources().getString(R.string.sex_male);
            case UserInfo.SEX_FEMALE:
                return getResources().getString(R.string.sex_female);
            default:
                throw new RuntimeException("unknown sex string: " + dbString);
        }
    }

    private String getHandednessResourceString(String dbString) {
        switch (dbString) {
            case UserInfo.HANDEDNESS_LEFT:
                return getResources().getString(R.string.handedness_left);
            case UserInfo.HANDEDNESS_RIGHT:
                return getResources().getString(R.string.handedness_right);
            default:
                throw new RuntimeException("unknown handedness string: " + dbString);
        }
    }

    private String getAsymmetryResourceString(String dbString) {
        switch (dbString) {
            case UserInfo.ASYMMETRY_LEFT:
                return getResources().getString(R.string.asymmetry_left);
            case UserInfo.ASYMMETRY_RIGHT:
                return getResources().getString(R.string.asymmetry_right);
            case UserInfo.ASYMMETRY_NOT_SPECIFIED:
                return getResources().getString(R.string.asymmetry_not_specified);
            case UserInfo.ASYMMETRY_NO_ASYMMETRY:
                return getResources().getString(R.string.asymmetry_no_asymmetry);
            default:
                throw new RuntimeException("unknown asymmetry string: " + dbString);
        }
    }

}
