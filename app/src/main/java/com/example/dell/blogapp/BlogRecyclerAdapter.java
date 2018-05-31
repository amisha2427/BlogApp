package com.example.dell.blogapp;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder> {

   public List<BlogPost> blogList;
   public Context context;
   private FirebaseFirestore firebaseFirestore;
   private FirebaseAuth mAuth;

    public BlogRecyclerAdapter(List<BlogPost> blogList)
    {
        this.blogList=blogList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_list_item,parent,false);
       context=parent.getContext();
       firebaseFirestore = FirebaseFirestore.getInstance();
       mAuth = FirebaseAuth.getInstance();

       return new ViewHolder(view);
       }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        final String blogPostId = blogList.get(position).BlogPostId;
        final String currentUserId = mAuth.getCurrentUser().getUid();
        holder.setIsRecyclable(false);  // help to reduce delay while recycling views

        String desc_data = blogList.get(position).getDesc();
        holder.setDescText(desc_data);

        String image_url = blogList.get(position).getImage_url();
        String thumb_url = blogList.get(position).getImage_thumbs();
        holder.setBlogImage(image_url,thumb_url);

        String user_id = blogList.get(position).getUser_id();

        //User data will be retrieved here
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
            
                String userName = null,userImage;
            if(task.isSuccessful())

                 userName = task.getResult().getString("name");
                 userImage = task.getResult().getString("image");
                 holder.setBlogUserNameandImage(userName,userImage);

            }
        });


        long milliseconds = blogList.get(position).getTimestamp().getTime();
        String dateString = DateFormat.format("MM/dd/yyyy", new Date(milliseconds)).toString();
        holder.setTime(dateString);


        // Get Likes Count
        firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").addSnapshotListener((Activity) context,new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent( QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {

                if(!queryDocumentSnapshots.isEmpty()) {

                    int count =queryDocumentSnapshots.size();

                    holder.updateLikeCount(count);

                }else {

                    holder.updateLikeCount(0);
                }

            }
        });


        //GetLikes
        firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).addSnapshotListener((Activity) context, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                if (documentSnapshot.exists())
                {
                    holder.blogLikeBtn.setImageDrawable(context.getDrawable(R.mipmap.action_like_accent));

                } else {

                    holder.blogLikeBtn.setImageDrawable(context.getDrawable(R.mipmap.action_like_gray));
                }

            }
        });


        //Likes Feature
        holder.blogLikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // to check data from likes collection
                firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).get().addOnCompleteListener((Activity) context,new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (!task.getResult().exists())
                        { //Like
                            Map<String,Object> likesMap = new HashMap<>();
                            likesMap.put("timestamp", FieldValue.serverTimestamp());

                            firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).set(likesMap);


                        }
                        else
                        { //Unlike
                            firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).delete();


                        }

                    }
                });



            }
        });


    }

    @Override
    public int getItemCount() {
        return blogList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        private View mview;

        private TextView descView;
        private ImageView blogImageview;
        private TextView blogDate;
        private TextView blogUsername;
        private CircleImageView blogUserImage;

        private ImageView blogLikeBtn;
        private TextView blogLikeCount;


        public ViewHolder(View itemView) {
            super(itemView);
            mview = itemView;

            blogLikeBtn = mview.findViewById(R.id.blogLikeBtn);
        }

        public void setDescText(String descText){

            descView = mview.findViewById(R.id.blogDesc);
            descView.setText(descText);
        }

        public void setBlogImage(String downloadUri,String thumbUri)
        {
            blogImageview = mview.findViewById(R.id.blogImage);

            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.squareimage);

            Glide.with(context).applyDefaultRequestOptions(requestOptions).load(downloadUri).thumbnail(Glide.with(context).load(thumbUri)).into(blogImageview);

        }

        public void setTime(String date)
        {
            blogDate = mview.findViewById(R.id.blogDate);
            blogDate.setText(date);
        }

        public void setBlogUserNameandImage(String user_name,String user_image)
        {
            blogUsername = mview.findViewById(R.id.blogUserName);
            blogUsername.setText(user_name);

            blogUserImage = mview.findViewById(R.id.bloguserImage);
            
            RequestOptions placeHolderRequest = new RequestOptions();
            placeHolderRequest.placeholder(R.drawable.profileicon);
            
            Glide.with(context).applyDefaultRequestOptions(placeHolderRequest).load(user_image).into(blogUserImage);

        }

        public void updateLikeCount(int count)
        {

            blogLikeCount = mview.findViewById(R.id.blogLikeCount);
            blogLikeCount.setText(count + "Likes");
        }

    }

}
