package com.example.dell.blogapp;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {


    private RecyclerView blogListView;
    private List<BlogPost> blogList;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private BlogRecyclerAdapter blogRecyclerAdapter;

    private DocumentSnapshot lastVisible;
    private Boolean isFirstPageFirstLoaded = true;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        blogList = new ArrayList<>();
        blogListView = view.findViewById(R.id.blogListview);

        blogRecyclerAdapter = new BlogRecyclerAdapter(blogList);
        blogListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        blogListView.setAdapter(blogRecyclerAdapter);

        mAuth = FirebaseAuth.getInstance();

        // Retrieving Data from firebase
        if(mAuth.getCurrentUser() != null) {

            firebaseFirestore = FirebaseFirestore.getInstance();

            blogListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    Boolean reachedBottom = !recyclerView.canScrollVertically(1);

                    if(reachedBottom){

                        String desc = lastVisible.getString("desc");
                        Toast.makeText(container.getContext(),"Reached " + desc ,Toast.LENGTH_LONG).show();

                        loadMorePost();

                    }

                }
            });


            Query firstQuery =firebaseFirestore.collection("Posts")
                    .orderBy("timestamp",Query.Direction.DESCENDING)
                    .limit(3);


            firstQuery.addSnapshotListener(getActivity(),new EventListener<QuerySnapshot>()
            {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    if (!queryDocumentSnapshots.isEmpty())
                    {
                        if(isFirstPageFirstLoaded)
                        {
                            lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                        }

                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges())
                        {
                            // it will check if document was added or not,if added then start the process
                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                String blogPostId = doc.getDocument().getId();
                                BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);

                                if(isFirstPageFirstLoaded) {
                                    blogList.add(blogPost);
                                }
                                else {
                                    blogList.add(0,blogPost);
                                }

                                blogRecyclerAdapter.notifyDataSetChanged();
                            }


                        }
                        isFirstPageFirstLoaded = false;
                    }

                }
            });

        }

        // Inflate the layout for this fragment
        return view;
    }

    // this will run the second query
     public void loadMorePost() {

         Query nextQuery =firebaseFirestore.collection("Posts")
                 .orderBy("timestamp",Query.Direction.DESCENDING)
                 .startAfter(lastVisible)
                 .limit(3);

         nextQuery.addSnapshotListener(getActivity(),new EventListener<QuerySnapshot>() {
             @Override
             public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                 if (!queryDocumentSnapshots.isEmpty())
                 {
                         lastVisible = queryDocumentSnapshots.getDocuments()
                                 .get(queryDocumentSnapshots.size() - 1);


                     for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges())
                     {
                         // it will check if document was added or not,if added then start the process
                         if (doc.getType() == DocumentChange.Type.ADDED)
                         {
                             String blogPostId = doc.getDocument().getId();
                             BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);
                             blogList.add(blogPost);

                             blogRecyclerAdapter.notifyDataSetChanged();
                         }


                     }
                 }

             }
         });

     }

}
