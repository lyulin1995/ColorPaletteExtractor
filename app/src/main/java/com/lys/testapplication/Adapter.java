package com.lys.testapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    private LayoutInflater layoutInflater;
    private List<Map<String, Object>> data;
    Map<String, Object> paletteObj;
    String title;
    String imagePath;
    private String TAG = "Adapter";

    StorageReference storageReference;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference userRef;
    Bitmap imageBitmap;
    private String uid;

    Adapter (Context context, List<Map<String, Object>> data){
        this.layoutInflater = LayoutInflater.from(context);
        this.data = data;
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        FirebaseUser loggedInUser = FirebaseAuth.getInstance().getCurrentUser();
        for (UserInfo profile : loggedInUser.getProviderData()){
            uid = profile.getUid();
        }
        userRef = db.collection("user").document(uid);
        storageReference = FirebaseStorage.getInstance().getReference();

        View view = layoutInflater.inflate(R.layout.custom_view, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

        // bind the textView with data received
        paletteObj = data.get(i);
        title = paletteObj.get("title").toString();
        imagePath = paletteObj.get("imagePath").toString();
        StorageReference ref = storageReference.child("pictures/" + imagePath);
        final long ONE_MEGABYTE = 1024 * 1024 * 5;
        ref.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                imageBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                viewHolder.textTitle.setText(title);
                viewHolder.paletteCardImageView.setImageBitmap(imageBitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView textTitle;
        ImageView paletteCardImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // set onClick Listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // start new intent
                    // Sending Data from recyclerView to Details Activity
                    Intent intent = new Intent(v.getContext(), Details.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("paletteObj", (Serializable) paletteObj); // sending title of cardview
                    v.getContext().startActivity(intent);  // starting next activity from view "v"
                }
            });
            textTitle = itemView.findViewById(R.id.paletteCardTextView);
            paletteCardImageView = itemView.findViewById(R.id.paletteCardImageView);
        }
    }
}
