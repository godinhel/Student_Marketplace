package com.example.brightonshop;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {
    private List<Product> productList;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Product product);
    }


    public ItemAdapter(List<Product> productList, Context context, OnItemClickListener listener) {
        this.productList = productList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemAdapter.ViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.titleText.setText(product.getTitle());
        holder.priceText.setText("£" + product.getPrice());


        holder.itemView.setOnClickListener(v -> listener.onItemClick(product));

        // Load image from Firebase Storage
        if (product.getImage_url() != null && !product.getImage_url().isEmpty()) {
            FirebaseStorage.getInstance().getReference()
                    .child(product.getImage_url())
                    .getDownloadUrl()
                    .addOnSuccessListener(uri -> {
                        Glide.with(context)
                                .load(uri)
                                .into(holder.imageView);
                    })
                    .addOnFailureListener(e -> {
                        // Optionally set a placeholder image on failure
                        holder.imageView.setImageResource(R.drawable.placeholder_image);
                    });
        } else {
            holder.imageView.setImageResource(R.drawable.placeholder_image);
        }
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleText, priceText;
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.itemTitle);
            priceText = itemView.findViewById(R.id.itemPrice);
            imageView = itemView.findViewById(R.id.itemImage);
        }
    }
}
