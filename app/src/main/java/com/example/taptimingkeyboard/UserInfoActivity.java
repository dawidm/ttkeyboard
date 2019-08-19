package com.example.taptimingkeyboard;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.room.Database;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.taptimingkeyboard.activity.TestSessionActivity;
import com.example.taptimingkeyboard.data.LayoutStringDbString;
import com.example.taptimingkeyboard.data.TapTimingDatabase;
import com.example.taptimingkeyboard.data.UserInfo;

import java.util.ArrayList;

public class UserInfoActivity extends AppCompatActivity {

    private ConstraintLayout mainLayout;
    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText ageEditText;
    private Spinner sexSpinner;
    private Spinner handednessSpinner;
    private CheckBox diagnosedWithPDCheckbox;
    private Spinner symptomsAsymmetrySpinner;
    private CheckBox onMedicationCheckbox;
    private Button buttonOk;
    private Button buttonLoad;
    private ListView usersListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        mainLayout=findViewById(R.id.main_layout);
        firstNameEditText=findViewById(R.id.edit_text_name);
        lastNameEditText=findViewById(R.id.edit_text_last_name);
        ageEditText=findViewById(R.id.edit_text_age);
        sexSpinner=findViewById(R.id.spinner_sex);
        handednessSpinner=findViewById(R.id.spinner_handedness);
        diagnosedWithPDCheckbox=findViewById(R.id.check_box_diagnosed);
        symptomsAsymmetrySpinner =findViewById(R.id.spinner_diagnosed_asymmetry);
        onMedicationCheckbox=findViewById(R.id.check_box_on_medication);
        buttonOk=findViewById(R.id.button_ok);
        buttonLoad=findViewById(R.id.button_load);
        usersListView=findViewById(R.id.list_view_users);
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
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!validateFields())
                    return;
                mainLayout.setEnabled(false);
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        UserInfo userInfo = new UserInfo(firstNameEditText.getText().toString(),
                                lastNameEditText.getText().toString(),
                                Integer.parseInt(ageEditText.getText().toString()),
                                ((LayoutStringDbString)sexSpinner.getSelectedItem()).getDbValue(),
                                ((LayoutStringDbString)handednessSpinner.getSelectedItem()).getDbValue(),
                                diagnosedWithPDCheckbox.isSelected(),
                                ((LayoutStringDbString)symptomsAsymmetrySpinner.getSelectedItem()).getDbValue(),
                                onMedicationCheckbox.isSelected());
                        final long userId = TapTimingDatabase.instance(getApplicationContext()).userInfoDao().insert(userInfo);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                startTestSessionActivity(userId);
                            }
                        });
                    }
                });
            }
        });
        buttonLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        final UserInfo[] userInfos = TapTimingDatabase.instance(getApplicationContext()).userInfoDao().getAll();
                        if(userInfos==null)
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(UserInfoActivity.this, getResources().getString(R.string.warning_no_existing_users),Toast.LENGTH_LONG);
                                }
                            });
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                usersListView.setVisibility(View.VISIBLE);
                                usersListView.setAdapter(new ArrayAdapter<>(UserInfoActivity.this,android.R.layout.simple_list_item_1,userInfos));
                                usersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                        startTestSessionActivity(((UserInfo)adapterView.getItemAtPosition(i)).getId());
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
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

    private boolean validateFields() {
        if(firstNameEditText.getText().length()<1 || lastNameEditText.getText().length()<1 || ageEditText.getText().length()<1) {
            Toast.makeText(this, getResources().getString(R.string.warning_fill_all_fields), Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void startTestSessionActivity(long id) {
        Intent intent = new Intent(this, TestSessionActivity.class);
        intent.putExtra("user_id",id);
        startActivity(intent);
        finish();
    }

}
