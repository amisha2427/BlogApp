package com.example.dell.blogapp;

import io.reactivex.annotations.NonNull;

public class BlogPostId {

    public String BlogPostId;

    public <T extends BlogPostId> T withId(@NonNull final String id){

        this.BlogPostId = id;
        return (T) this;
    }


}
