package com.example.gyogyszertar;

import static androidx.core.content.PackageManagerCompat.LOG_TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegistrationActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    //LOG_TAG
    private static final String LOG_TAG=RegistrationActivity.class.getName();
    //Package név elkérése
    private static final String PREF_KEY=RegistrationActivity.class.getPackage().toString();

    EditText usernameEditText;
    EditText emailEditText;
    EditText passwordEditText;
    EditText passwordAgainEditText;
    EditText phoneEditText;
    EditText addressEditText;
    RadioGroup genderTypeGroup;
    //Spinner feltöltése
    Spinner spinner;
    //Sharedpreferences - ezt használom fel az onPause esetén
    private SharedPreferences preferences;
    //Firebase
    private FirebaseAuth fireauth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        //intent lekérése, ha nem egyezik a secret_key, meghívjuk a finish()-t
        int secret_key=getIntent().getIntExtra("SECRET_KEY",0);
        if(secret_key!=99){
            finish();
        }

        //regisztrációs oldalról
        usernameEditText=findViewById(R.id.usernameEditText);
        emailEditText=findViewById(R.id.emailEditText);
        passwordEditText=findViewById(R.id.passwordEditText);
        passwordAgainEditText=findViewById(R.id.passwordAgainEditText);
        phoneEditText=findViewById(R.id.phoneEditText);
        spinner=findViewById(R.id.phoneSpinner);
        addressEditText=findViewById(R.id.addressEditText);
        genderTypeGroup=findViewById(R.id.genderTypeGroup);
        //alapból legyen az Egyéb kiválasztva
        genderTypeGroup.check(R.id.anotherGenderRadioButton);

        //Lifecycle
        Log.i(LOG_TAG,"onCreate");

        //Sharedpreferences
        preferences=getSharedPreferences(PREF_KEY,MODE_PRIVATE);
        String email=preferences.getString("email","");

        //beállítom, amit kaptam a belépésnél, mint egy default email cím
        emailEditText.setText(email);

        //spinner kiválasztásánál
        spinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter=ArrayAdapter.createFromResource(this,R.array.phone_modes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        //Firebase inicializálás
        fireauth=FirebaseAuth.getInstance();
    }

    //regisztrációra rányomunk
    public void registration(View view) {
        String username=usernameEditText.getText().toString();
        String email=emailEditText.getText().toString();
        String password=passwordEditText.getText().toString();
        String passwordagain=passwordAgainEditText.getText().toString();
        String address=addressEditText.getText().toString();

        //jelszó megszorítások
        //jelszó és a megerősítő jelszó nem egyezik meg
        if(!password.equals(passwordagain)){
            Log.e(LOG_TAG,"Nem egyenlő a jelszó és a megerősítése");
            return;
        }


        //spinneres rész
        String phoneNumber=phoneEditText.getText().toString();
        String phoneType=spinner.getSelectedItem().toString();

        //melyik radiogroup van kiválasztva
        int checkedId = genderTypeGroup.getCheckedRadioButtonId();
        View radioButton = genderTypeGroup.findViewById(checkedId);
        int id = genderTypeGroup.indexOfChild(radioButton);
        String accountType =  ((RadioButton)genderTypeGroup.getChildAt(id)).getText().toString();

        Log.i(LOG_TAG, "Sikeresen regisztrált: "+username+", jelszó: "+password);
        //minden megvan:
        fireauth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, task -> {
            if(task.isSuccessful()){
                Log.d(LOG_TAG, "A felhasználó sikeresen létre lett hozva");
                startShopping();
            }else{
                Log.d(LOG_TAG, "A felhasználót nem sikerült létrehozni");
                Toast.makeText(RegistrationActivity.this,"A felhasználót nem sikerült létrehozni "+task.getException().getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    //bezárjuk az activityt
    public void cancel(View view) {
        finish();
    }

    //sikeres regisztráció után elkezdhetjük a vásárlást
    private void startShopping(){
      Intent intent=new Intent(this,ProductsActivity.class);
      startActivity(intent);
    }

    //TODO Lifecycle
    @Override
    protected void onStart() {
        super.onStart();
        Log.i(LOG_TAG, "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(LOG_TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "onDestroy");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(LOG_TAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "onResume");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(LOG_TAG, "onRestart");
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        String selectedItem=adapterView.getItemAtPosition(position).toString();
        Log.i(LOG_TAG,selectedItem);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}