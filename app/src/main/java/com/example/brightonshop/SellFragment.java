package com.example.brightonshop;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.*;

public class SellFragment extends Fragment {
    private RecyclerView recyclerViewItems;
    private ItemAdapter itemAdapter;
    private List<Product> itemsList;
    private FloatingActionButton fabAddItem;
    private Uri selectedImageUri;

    private ImageView imagePreview;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (imagePreview != null) {
                        imagePreview.setImageURI(selectedImageUri);
                    }
                }
            });



    public SellFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sell, container, false);

        recyclerViewItems = view.findViewById(R.id.recyclerViewItems);
        fabAddItem = view.findViewById(R.id.fabAddItem);

        int numberOfColumns = 2;
        recyclerViewItems.setLayoutManager(new GridLayoutManager(getContext(), numberOfColumns));
        itemsList = new ArrayList<>();
        itemAdapter = new ItemAdapter(itemsList, getContext(), this::showProductDetails);
        recyclerViewItems.setAdapter(itemAdapter);

        fabAddItem.setOnClickListener(v -> showUploadDialog());

        fetchUserItems();

        return view;
    }

    private void fetchUserItems() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance().collection("product-details")
                .whereEqualTo("user", user.getEmail())
                .get()
                .addOnSuccessListener(snapshot -> {
                    itemsList.clear();
                    for (DocumentSnapshot doc : snapshot) {
                        Product product = doc.toObject(Product.class);
                        if (product != null) {
                            itemsList.add(product);
                        }
                    }
                    itemAdapter.notifyDataSetChanged();
                });
    }

    private void showUploadDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_upload_item, null);
        builder.setView(dialogView);

        imagePreview = dialogView.findViewById(R.id.imagePreview);
        Button chooseImageBtn = dialogView.findViewById(R.id.buttonChooseImage);
        EditText titleInput = dialogView.findViewById(R.id.editTitle);
        EditText descInput = dialogView.findViewById(R.id.editDescription);
        EditText priceInput = dialogView.findViewById(R.id.editPrice);
        Button uploadBtn = dialogView.findViewById(R.id.buttonUpload);

        AlertDialog dialog = builder.create();
        dialog.show();

        chooseImageBtn.setOnClickListener(v -> {
            Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(pickIntent);
        });

        uploadBtn.setOnClickListener(v -> {
            String title = titleInput.getText().toString().trim();
            String desc = descInput.getText().toString().trim();
            String price = priceInput.getText().toString().trim();

            if (title.isEmpty() || desc.isEmpty() || price.isEmpty() || selectedImageUri == null) {
                Toast.makeText(getContext(), "Please fill in all fields and choose an image.", Toast.LENGTH_SHORT).show();
                return;
            }

            uploadItem(title, desc, price, dialog);
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


    private void uploadItem(String title, String description, String price, AlertDialog dialog) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String docId = UUID.randomUUID().toString();
        String fileName = "item.jpg";
        String imagePath = "images/" + docId + "/" + fileName;

        StorageReference imageRef = FirebaseStorage.getInstance().getReference().child(imagePath);
        imageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("title", title);
                    data.put("description", description);
                    data.put("price", price);
                    data.put("user", user.getEmail());
                    data.put("timestamp", FieldValue.serverTimestamp());
                    data.put("image_url", imagePath);

                    FirebaseFirestore.getInstance().collection("product-details")
                            .document(docId)
                            .set(data)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(getContext(), "Item uploaded!", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                fetchUserItems(); // refresh list
                                selectedImageUri = null;
                                imagePreview = null;
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
