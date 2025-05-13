package com.example.brightonshop;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class BuyFragment extends Fragment {

    private RecyclerView recyclerView;
    private ItemAdapter itemAdapter;
    private List<Product> productList;

    public BuyFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_buy, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        int numberOfColumns = 2;
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), numberOfColumns));


        productList = new ArrayList<>();
        itemAdapter = new ItemAdapter(productList, getContext(), this::showProductDetails);
        recyclerView.setAdapter(itemAdapter);

        loadAllProducts();

        return view;
    }

    private void loadAllProducts() {
        FirebaseFirestore.getInstance().collection("product-details")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    productList.clear();
                    for (DocumentSnapshot doc : querySnapshot) {
                        Product product = doc.toObject(Product.class);
                        if (product != null) {
                            productList.add(product);
                        }
                    }
                    itemAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // Log error if needed
                });
    }

    private void showProductDetails(Product product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_product_details, null);
        builder.setView(dialogView);

        ImageView image = dialogView.findViewById(R.id.detailImage);
        TextView title = dialogView.findViewById(R.id.detailTitle);
        TextView desc = dialogView.findViewById(R.id.detailDescription);
        TextView price = dialogView.findViewById(R.id.detailPrice);

        title.setText(product.getTitle());
        desc.setText(product.getDescription());
        price.setText("£" + product.getPrice());

        FirebaseStorage.getInstance().getReference()
                .child(product.getImage_url())
                .getDownloadUrl()
                .addOnSuccessListener(uri -> Glide.with(getContext()).load(uri).into(image));

        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}
