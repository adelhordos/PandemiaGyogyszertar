package com.example.gyogyszertar;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Locale;

public class ProductItemAdapter extends RecyclerView.Adapter<ProductItemAdapter.ViewHolder> implements Filterable {

    private ArrayList<ProductItem> productItemsData;
    //szűrőhöz, ez minden terméket tartalmazni fog
    private ArrayList<ProductItem> productItemsDataAll;
    private Context mContext;
    private int lastPosition =-1;

    //konstruktor, productitemeket tárol
    ProductItemAdapter(Context context, ArrayList<ProductItem> itemsData){
        this.productItemsData=itemsData;
        this.productItemsDataAll=itemsData;
        this.mContext=context;
    }

    @NonNull
    @Override
    public ProductItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //itt adjuk meg a listitemünket
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.list,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ProductItemAdapter.ViewHolder holder, int position) {
        //aktuális pozícióból szedjük ki az elemet
        ProductItem currentItem= productItemsData.get(position);
        holder.bindTo(currentItem);

        //animáció hozzáadása
        if(holder.getAdapterPosition()>lastPosition){
            Animation animation= AnimationUtils.loadAnimation(mContext,R.anim.slide_in_row);
            holder.itemView.startAnimation(animation);
            lastPosition=holder.getAdapterPosition();
        }

    }

    //termékek száma
    @Override
    public int getItemCount() {
        return productItemsData.size();
    }

    @Override
    public Filter getFilter() {
        return productFilter;
    }
    private Filter productFilter=new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            //mikor beírunk a keresőbe valamit, hogyan történjen szűrés
            //elemek, amik a szűrésnek eleget fognak tenni
            ArrayList<ProductItem> filteredList=new ArrayList<>();
            FilterResults results=new FilterResults();
            //nem írtunk be semmit vagy nem akarunk semmire szűrni
            if(charSequence==null || charSequence.length()==0){
                results.count=productItemsDataAll.size();
                results.values=productItemsDataAll;
            }else{
                String filterPattern=charSequence.toString().toLowerCase().trim();
                for(ProductItem item:productItemsDataAll){
                    if(item.getName().toLowerCase().contains(filterPattern)){
                        filteredList.add(item);
                    }
                }
                results.count=filteredList.size();
                results.values=filteredList;
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            //a szűrés eredménye hogyan kerüljön visszaadásra
            productItemsData=(ArrayList)filterResults.values;
            //recyclerview értesítése, ha adatmódosítás történt
            notifyDataSetChanged();

        }
    };

    class ViewHolder extends RecyclerView.ViewHolder{
        //GUI-n keresztüli elemek elérése
        private TextView nameText;
        private TextView typeText;
        private TextView priceText;
        private ImageView itemImage;
        private RatingBar ratingBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            //hozzápárosítjuk a megfelelő layout-beli elemhez
            nameText=itemView.findViewById(R.id.name);
            typeText=itemView.findViewById(R.id.itemType);
            priceText=itemView.findViewById(R.id.price);
            itemImage=itemView.findViewById(R.id.itemImage);
            ratingBar=itemView.findViewById(R.id.rating);
        }

        public void bindTo(ProductItem currentItem) {
            nameText.setText(currentItem.getName());
            typeText.setText(currentItem.getItemtype());
            priceText.setText(currentItem.getPrice());
            ratingBar.setRating(currentItem.getRatedInfo());

            //imageViewba betesszük a resourcet
            Glide.with(mContext).load(currentItem.getImageResource()).into(itemImage);
            //kosárba gombhoz
            itemView.findViewById(R.id.cart).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((ProductsActivity) mContext).updateAlertIcon(currentItem);
                    Animation animscale= AnimationUtils.loadAnimation(mContext,R.anim.scale);
                    view.startAnimation(animscale);
                }
            });
            itemView.findViewById(R.id.delete).setOnClickListener(view -> ((ProductsActivity) mContext).deleteItem(currentItem));
        }
    }
}
