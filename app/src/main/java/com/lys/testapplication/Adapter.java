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
    String title;
    String imagePath;
    Bitmap imageBitmap;
    private String TAG = "Adapter";

    StorageReference storageReference;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference userRef;
    private String uid;

    Adapter (Context context, List<Map<String, Object>> data){
        this.layoutInflater = LayoutInflater.from(context);
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = layoutInflater.inflate(R.layout.custom_view, viewGroup, false);

        FirebaseUser loggedInUser = FirebaseAuth.getInstance().getCurrentUser();
        for (UserInfo profile : loggedInUser.getProviderData()){
            uid = profile.getUid();
        }
        userRef = db.collection("user").document(uid);
        storageReference = FirebaseStorage.getInstance().getReference();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        // bind the textView with data received
        final Map<String, Object> paletteObj = data.get(i);
        imagePath = paletteObj.get("imagePath").toString();
         StorageReference ref = storageReference.child("pictures/" + imagePath);
        final long ONE_MEGABYTE = 1024 * 1024 * 10 ;
        ref.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                title = paletteObj.get("title").toString();
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
                    Map<String, Object> currPaletteObj =data.get(getAdapterPosition());
                    Intent intent = new Intent(v.getContext(), Details.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("paletteObj", (Serializable) currPaletteObj); // sending title of cardview
                    v.getContext().startActivity(intent);  // starting next activity from view "v"
                }
            });
            textTitle = itemView.findViewById(R.id.paletteCardTextView);
            paletteCardImageView = itemView.findViewById(R.id.paletteCardImageView);
        }
    }
}
