package com.example.gyogyszertar;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Notification;
import android.app.NotificationChannel;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class ProductsActivity extends AppCompatActivity {

    private static final String LOG_TAG=ProductsActivity.class.getName();
    private FirebaseUser user;
    private FirebaseAuth fireauth;
    private FirebaseFirestore mFirestore;
    private CollectionReference mItems;
    private RecyclerView recyclerView;
    private ArrayList<ProductItem> itemList;
    private ProductItemAdapter adapter;
    //piros kör
    private FrameLayout redCircle;
    private TextView contentTextView;
    //kocsi
    private int cartItems=0;
    //oszlopok száma alapesetben
    private int gridNumber=1;
    //nézet váltása
    private boolean viewRow=true;
    //limit
    private int queryLimit=10;
    //értesítés
    private NotificationHelper mNotificationHelper;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_products);

        fireauth=FirebaseAuth.getInstance();

        //felhasználó adatainak lekérdezése
        user=FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null){
            Log.d(LOG_TAG,"Azonosított felhasználó");
        }else{
            Log.d(LOG_TAG,"Nem azonosított felhasználó");
            finish();
        }
        recyclerView=findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this,gridNumber));
        itemList=new ArrayList<>();
        adapter=new ProductItemAdapter(this,itemList);
        recyclerView.setAdapter(adapter);
        mFirestore=FirebaseFirestore.getInstance(); //példányosítás
        mItems=mFirestore.collection("Items");
        queryData();

        //töltés alatt
        IntentFilter filter=new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        this.registerReceiver(powerReceiver,filter);

        //értesítés inicializálása
        mNotificationHelper = new NotificationHelper(this);
    }
    BroadcastReceiver powerReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if(action==null){
                return;
            }
            switch (action){
                case  Intent.ACTION_POWER_CONNECTED:
                    queryLimit=10;
                    break;
                case Intent.ACTION_POWER_DISCONNECTED:
                    queryLimit=4;
                    queryData();
                    break;
            }

        }
    };
    private void queryData(){
        itemList.clear();
        //rendezzük a termékeket az alapján, melyiket tettük be legtöbbször a kosárba
        mItems.orderBy("cartCounter", Query.Direction.DESCENDING).limit(queryLimit).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for(QueryDocumentSnapshot document: queryDocumentSnapshots){
                ProductItem item=document.toObject(ProductItem.class);
                item.setId(document.getId());
                itemList.add(item);
            }
            //ha nem volt adat
            if(itemList.size()==0){
                initializeData();
                queryData();
            }
            adapter.notifyDataSetChanged();
        });

    }

    private void initializeData() {
        String[]itemsList=getResources().getStringArray(R.array.medicine_item_names);
        String[]itemType=getResources().getStringArray(R.array.medicine_item_types);
        String[]itemsPrice=getResources().getStringArray(R.array.medicine_item_prices);
        TypedArray itemsImageResource=getResources().obtainTypedArray(R.array.medicine_item_images);
        TypedArray itemsRate=getResources().obtainTypedArray(R.array.medicine_item_rates);
        //litához adom hozzá az itemeket
        for (int i = 0; i < itemsList.length; i++) {
            //feltöltés a cloudba
            mItems.add(new ProductItem(
                    itemsList[i],
                    itemType[i],
                    itemsPrice[i],
                    itemsRate.getFloat(i, 0),
                    itemsImageResource.getResourceId(i, 0),
                    0));
        }
        itemsImageResource.recycle();
    }

    //törlés
    public void deleteItem(ProductItem item){
        DocumentReference ref=mItems.document(item._getId());
        ref.delete().addOnSuccessListener(success->{
            Log.d(LOG_TAG, "Az elem sikeresen ki lett törölve"+item._getId());
        }).addOnFailureListener(fail->{
            Toast.makeText(this,"A "+item._getId()+" elemet nem lehet kitörölni!",Toast.LENGTH_LONG).show();
        });
        queryData();
        mNotificationHelper.cancel();//ha kitörölt termékhez van értesítés, akkor kilövi
    }

    //menü beállítása
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.products_menu,menu);
        MenuItem menuItem=menu.findItem(R.id.search_bar);
        SearchView searchView=(SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //szöveg változik
                Log.d(LOG_TAG,s);
                adapter.getFilter().filter(s);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                finish();
                return true;
            case R.id.settings:
                FirebaseAuth.getInstance().signOut();
                finish();
                return true;
            case R.id.cart:
                return true;
            case R.id.view_selector:
                if(viewRow){
                    changeSpan(item,R.drawable.icon_view_grid,1);
                }else{
                    changeSpan(item,R.drawable.icon_view_row,2);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void changeSpan(MenuItem item, int drawableId, int i) {
        viewRow=!viewRow;
        item.setIcon(drawableId);
        GridLayoutManager layoutManager=(GridLayoutManager) recyclerView.getLayoutManager();
        layoutManager.setSpanCount(i);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final MenuItem alertMenuItem = menu.findItem(R.id.cart);
        FrameLayout rootView = (FrameLayout) alertMenuItem.getActionView();
        redCircle = (FrameLayout) rootView.findViewById(R.id.view_alert_red_circle);
        contentTextView = (TextView) rootView.findViewById(R.id.view_alert_count_textview);

        //rootview-ra való klikkelés
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //ha valaki rákattint, meghívódik az onOptionsItemSelected-ben a megfelelő case opció
                onOptionsItemSelected(alertMenuItem);
            }
        });

        return super.onPrepareOptionsMenu(menu);
    }
    public void updateAlertIcon(ProductItem item){
        cartItems=(cartItems+1);
        if(0<cartItems){
            contentTextView.setText(String.valueOf(cartItems));
        }else{
            contentTextView.setText("");
        }
        redCircle.setVisibility((cartItems>0) ? VISIBLE:GONE);
        mItems.document(item._getId()).update("cartCounter",item.getCartCounter()+1).addOnFailureListener(fail->{
            Toast.makeText(this,"A "+item._getId()+" elemet nem lehet megváltoztatni!",Toast.LENGTH_LONG).show();
        });
        mNotificationHelper.send(item.getName());
        queryData();
    }

    //vége van az Activitynek, akkor leregisztrálom a receivert
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(powerReceiver);
    }
}