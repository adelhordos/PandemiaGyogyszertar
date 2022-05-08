package com.example.gyogyszertar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String> {
    //Logoláshoz
    private static final String LOG_TAG=MainActivity.class.getName();
    //Package név elkérése
    private static final String PREF_KEY=MainActivity.class.getPackage().toString();
    //Google-ös
    private static final int SIGN_IN = 23;
    //bejelentkezős oldalról
    EditText emailET;
    EditText passwordET;
    //Sharedpreferences - ezt használom fel az onPause esetén
    private SharedPreferences preferences;
    //Firebase
    private FirebaseAuth fireAuth;
    //Google-ös bejelentkezés
    private GoogleSignInClient mGoogleSignInClient;
    //SECRET_KEY
    private static final int SECRET_KEY = 99;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //permission
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)){
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }else{
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
        //bejelentkezős oldalról
        emailET=findViewById(R.id.emailEditText);
        passwordET=findViewById(R.id.passwordEditText);
        //Sharedpreferences
        preferences=getSharedPreferences(PREF_KEY,MODE_PRIVATE);
        //Firebase inicializálás
        fireAuth=FirebaseAuth.getInstance();
        //Google-ös bejelentkezés
        GoogleSignInOptions gso=new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("121314299106-p6lkomdek6d3koadadbehiuabnojgl5e.apps.googleusercontent.com").
                        requestEmail().build();
        mGoogleSignInClient=GoogleSignIn.getClient(this,gso);
        //Lifecycle
        Log.i(LOG_TAG, "onCreate");
        //RandomAsyncLoader
        getSupportLoaderManager().restartLoader(0,null,this);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==SIGN_IN){
            Task<GoogleSignInAccount> task=GoogleSignIn.getSignedInAccountFromIntent(data);
            try{
                GoogleSignInAccount account=task.getResult(ApiException.class);
                Log.d(LOG_TAG,"Azonosítás Google fiókkal"+account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            }catch (ApiException e){
                Log.w(LOG_TAG, "Sikertelen a Google bejelentkezés",e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken){
        AuthCredential credential= GoogleAuthProvider.getCredential(idToken, null);
        fireAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if(task.isSuccessful()){
                Log.d(LOG_TAG, "Sikeres bejelentkezés Google fiókkal");
                startShopping();
            }else{
                Log.w(LOG_TAG, "Hiba a Google fiókos bejelentkezés során",task.getException());
            }
        });
    }

    //bejelentkezés
    public void login(View view) {

        String email=emailET.getText().toString();
        String password=passwordET.getText().toString();
        Log.i(LOG_TAG, "Sikeresen bejelentkezett: "+email+", jelszó: "+password);

        //felhasználó beléptetése
        fireAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this, task -> {
            if(task.isSuccessful()){
                Log.d(LOG_TAG, "A felhasználó sikeresen bejelentkezett");
                startShopping();
            }else{
                Log.d(LOG_TAG, "Hiba a bejelentkezés során");
                Toast.makeText(MainActivity.this,"Hiba a bejelentkezés során"+task.getException().getMessage(),Toast.LENGTH_LONG).show();
            }
        });

    }
    private void startShopping(){
        Intent intent=new Intent(this,ProductsActivity.class);
        startActivity(intent);
    }

    //regisztráció
    public void registration(View view) {
        //RegistrationActivity megnyitásához Intent
        Intent intent=new Intent(this,RegistrationActivity.class);
        intent.putExtra("SECRET_KEY",SECRET_KEY);
        startActivity(intent);
    }
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
        SharedPreferences.Editor editor=preferences.edit();
        editor.putString("email",emailET.getText().toString());
        editor.apply();
        Log.i(LOG_TAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "onResume");
        Toast.makeText(MainActivity.this,"Üdvözöljük újra!",Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(LOG_TAG, "onRestart");
    }

    public void loginAsGuest(View view) {
        fireAuth.signInAnonymously().addOnCompleteListener(this, task -> {
            if(task.isSuccessful()){
                Log.d(LOG_TAG, "Anonim felhasználó sikeresen bejelentkezett");
                startShopping();
            }else{
                Log.d(LOG_TAG, "Hiba az anonim bejelentkezés során");
                Toast.makeText(MainActivity.this,"Hiba a bejelentkezés során"+task.getException().getMessage(),Toast.LENGTH_LONG).show();

            }
        });
    }
    public void loginWithGoogle(View view) {
        Intent signInIntent=mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent,SIGN_IN);
    }

    @NonNull
    @Override
    public Loader<String> onCreateLoader(int id, @Nullable Bundle args) {
        //akkor lesz meghívva, mikor a loaderünk példáníosítva lesz
        return new RandomAsyncLoader(this);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String data) {
        //akkor lesz meghívva, mikor a task befejeződik
        Button button=findViewById(R.id.guestLoginButton);
        button.setText(data);

    }

    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {

    }
    //permission
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }
}