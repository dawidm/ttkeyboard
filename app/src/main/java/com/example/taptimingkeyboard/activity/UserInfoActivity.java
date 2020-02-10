package com.example.taptimingkeyboard.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.taptimingkeyboard.FireBaseLoginActivity;
import com.example.taptimingkeyboard.R;
import com.example.taptimingkeyboard.data.LayoutStringEnum;
import com.example.taptimingkeyboard.data.TapTimingDatabase;
import com.example.taptimingkeyboard.data.UserInfo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

/**
 * An activity for choosing keyboard user or creating new user
 */
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
    private ConstraintLayout usersListViewContainer;
    private ListView usersListView;
    private Boolean startedFromPreferences;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(getIntent().getExtras()!=null) {
            this.startedFromPreferences = getIntent().getExtras().getBoolean("started_from_preferences");
        }
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
        usersListViewContainer=findViewById(R.id.list_view_users_container);
        usersListView=findViewById(R.id.list_view_users);
        ArrayList<LayoutStringEnum> sexesList = new ArrayList<>(2);
        sexesList.add(new LayoutStringEnum(getSexResourceString(UserInfo.Sex.MALE), UserInfo.Sex.MALE));
        sexesList.add(new LayoutStringEnum(getSexResourceString(UserInfo.Sex.FEMALE),UserInfo.Sex.FEMALE));
        sexSpinner.setAdapter(new ArrayAdapter<>(this,R.layout.spinner_item,sexesList));
        ArrayList<LayoutStringEnum> handednessList = new ArrayList<>(2);
        handednessList.add(new LayoutStringEnum(getHandednessResourceString(UserInfo.Handedness.RIGHT),UserInfo.Handedness.RIGHT));
        handednessList.add(new LayoutStringEnum(getHandednessResourceString(UserInfo.Handedness.LEFT),UserInfo.Handedness.LEFT));
        handednessSpinner.setAdapter(new ArrayAdapter<>(this,R.layout.spinner_item,handednessList));
        ArrayList<LayoutStringEnum> symptomsAsymmetryList = new ArrayList<>(4);
        symptomsAsymmetryList.add(new LayoutStringEnum(getAsymmetryResourceString(UserInfo.Asymmetry.RIGHT),UserInfo.Asymmetry.RIGHT));
        symptomsAsymmetryList.add(new LayoutStringEnum(getAsymmetryResourceString(UserInfo.Asymmetry.LEFT),UserInfo.Asymmetry.LEFT));
        symptomsAsymmetryList.add(new LayoutStringEnum(getAsymmetryResourceString(UserInfo.Asymmetry.NO_ASYMMETRY),UserInfo.Asymmetry.NO_ASYMMETRY));
        symptomsAsymmetryList.add(new LayoutStringEnum(getAsymmetryResourceString(UserInfo.Asymmetry.NOT_SPECIFIED),UserInfo.Asymmetry.NOT_SPECIFIED));
        symptomsAsymmetrySpinner.setAdapter(new ArrayAdapter<>(this,R.layout.spinner_item,symptomsAsymmetryList));
        symptomsAsymmetrySpinner.setEnabled(false);
        onMedicationCheckbox.setEnabled(false);
        diagnosedWithPDCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean diagnosed) {
                if(diagnosed) {
                    symptomsAsymmetrySpinner.setEnabled(true);
                    onMedicationCheckbox.setEnabled(true);
                } else {
                    symptomsAsymmetrySpinner.setEnabled(false);
                    onMedicationCheckbox.setEnabled(false);
                }
            }
        });
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveForm();
            }
        });
        buttonLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPreviousEntryList();
            }
        });
        usersListViewContainer.bringToFront();
        checkDataForEmail();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(getString(R.string.text_logout));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(this, FireBaseLoginActivity.class);
                startActivity(intent);
                finish();
                break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(usersListViewContainer.getVisibility()==View.VISIBLE)
            usersListViewContainer.setVisibility(View.GONE);
    }

    private void checkDataForEmail() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                if (firebaseUser!=null) {
                    buttonLoad.setVisibility(View.GONE);
                    UserInfo[] userInfos = TapTimingDatabase.instance(getApplicationContext()).userInfoDao().getByEmail(firebaseUser.getEmail());
                    if (userInfos.length>0) {
                        startTestSessionActivity(userInfos[0].getId());
                    }
                }
            }
        });
    }

    /**
     * Get layout string in correct language Sex enum.
     * @param sex Sex enum.
     * @return Layout string taken from application's strings resources.
     */
    private String getSexResourceString(UserInfo.Sex sex) {
        switch (sex) {
            case MALE:
                return getResources().getString(R.string.sex_male);
            case FEMALE:
                return getResources().getString(R.string.sex_female);
            default:
                throw new RuntimeException("unknown sex enum: " + sex.name());
        }
    }

    /**
     * Get layout string in correct language for Handedness enum
     * @param handedness Handedness enum.
     * @return Layout string taken from application's strings resources.
     */
    private String getHandednessResourceString(UserInfo.Handedness handedness) {
        switch (handedness) {
            case LEFT:
                return getResources().getString(R.string.handedness_left);
            case RIGHT:
                return getResources().getString(R.string.handedness_right);
            default:
                throw new RuntimeException("unknown handedness string: " + handedness.name());
        }
    }

    /**
     * Get layout string in correct language for Asymmetry enum.
     * @param asymmetry Asymmetry enum.
     * @return Layout string taken from application's strings resources.
     */
    private String getAsymmetryResourceString(UserInfo.Asymmetry asymmetry) {
        switch (asymmetry) {
            case LEFT:
                return getResources().getString(R.string.asymmetry_left);
            case RIGHT:
                return getResources().getString(R.string.asymmetry_right);
            case NOT_SPECIFIED:
                return getResources().getString(R.string.asymmetry_not_specified);
            case NO_ASYMMETRY:
                return getResources().getString(R.string.asymmetry_no_asymmetry);
            default:
                throw new RuntimeException("unknown asymmetry string: " + asymmetry.name());
        }
    }

    /**
     * Check user input and save data to the database if correct.
     */
    private void saveForm() {
        if(!validateFields())
            return;
        mainLayout.setEnabled(false);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                UserInfo.Asymmetry symptomsAsymmetry;
                Boolean onMedication;
                if(!diagnosedWithPDCheckbox.isChecked()) {
                    symptomsAsymmetry=null;
                    onMedication=null;
                } else {
                    symptomsAsymmetry = (UserInfo.Asymmetry)((LayoutStringEnum)symptomsAsymmetrySpinner.getSelectedItem()).getEnumValue();
                    onMedication=onMedicationCheckbox.isChecked();
                }
                UserInfo userInfo = new UserInfo(firstNameEditText.getText().toString().trim(),
                        lastNameEditText.getText().toString().trim(),
                        Integer.parseInt(ageEditText.getText().toString()),
                        (UserInfo.Sex)((LayoutStringEnum)sexSpinner.getSelectedItem()).getEnumValue(),
                        (UserInfo.Handedness)((LayoutStringEnum)handednessSpinner.getSelectedItem()).getEnumValue(),
                        diagnosedWithPDCheckbox.isChecked(),
                        symptomsAsymmetry,
                        onMedication,
                        firebaseUser!=null ? firebaseUser.getEmail() : null);
                final long userId = TapTimingDatabase.instance(getApplicationContext()).userInfoDao().insert(userInfo);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(startedFromPreferences!=null&&startedFromPreferences.booleanValue()==true)
                            returnResultToPrefrences(userId);
                        else
                            startTestSessionActivity(userId);
                    }
                });
            }
        });
    }

    /**
     * Get all {@link UserInfo} from the database, show on a list, run {@link TestSessionActivity} for clicked position.
     */
    private void showPreviousEntryList() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final UserInfo[] userInfos = TapTimingDatabase.instance(getApplicationContext()).userInfoDao().getAllButDefault();
                if(userInfos==null || userInfos.length==0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(UserInfoActivity.this, getResources().getString(R.string.warning_no_existing_users), Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        usersListViewContainer.setVisibility(View.VISIBLE);
                        usersListView.setAdapter(new ArrayAdapter<>(UserInfoActivity.this,android.R.layout.simple_list_item_1,userInfos));
                        usersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                long userId=((UserInfo)adapterView.getItemAtPosition(i)).getId();
                                if(startedFromPreferences!=null&&startedFromPreferences.booleanValue()==true)
                                    returnResultToPrefrences(userId);
                                else
                                    startTestSessionActivity(userId);
                            }
                        });
                    }
                });
            }
        });
    }

    /**
     * Validate's form's fields.
     * @return True if all fields are filled correctly, false if not.
     */
    private boolean validateFields() {
        if(firstNameEditText.getText().toString().trim().length()<1
                || lastNameEditText.getText().toString().trim().length()<1
                || ageEditText.getText().toString().trim().length()<1) {
            Toast.makeText(this, getResources().getString(R.string.warning_fill_all_fields), Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    /**
     * Starts test session activity for specified user.
     * @param id User id.
     */
    private void startTestSessionActivity(long id) {
        Intent intent = new Intent(this, TestSessionActivity.class);
        intent.putExtra("user_id",id);
        startActivity(intent);
        finish();
    }

    /**
     * If started by {@link PreferencesActivity} sends back chosen or created user's id.
     * @param userId User id.
     */
    private void returnResultToPrefrences(long userId) {
        Intent intent = new Intent();
        intent.putExtra("user_id",userId);
        setResult(PreferencesActivity.CODE_RESULT_USER_ID,intent);
        finish();
    }

    /**
     * Hide users list if visible or go back.
     */
    @Override
    public void onBackPressed() {
        if(usersListViewContainer.getVisibility()==View.VISIBLE)
            usersListViewContainer.setVisibility(View.GONE);
        else
        if(startedFromPreferences!=null&&startedFromPreferences.booleanValue()==true)
            finish();
        else
            super.onBackPressed();
    }
}
