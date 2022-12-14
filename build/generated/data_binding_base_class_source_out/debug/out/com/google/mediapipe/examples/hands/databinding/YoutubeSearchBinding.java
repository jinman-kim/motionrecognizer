// Generated by view binder compiler. Do not edit!
package com.google.mediapipe.examples.hands.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.google.mediapipe.examples.hands.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class YoutubeSearchBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final SearchView SearchView;

  @NonNull
  public final RecyclerView recyclerview;

  private YoutubeSearchBinding(@NonNull ConstraintLayout rootView, @NonNull SearchView SearchView,
      @NonNull RecyclerView recyclerview) {
    this.rootView = rootView;
    this.SearchView = SearchView;
    this.recyclerview = recyclerview;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static YoutubeSearchBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static YoutubeSearchBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.youtube_search, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static YoutubeSearchBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.SearchView;
      SearchView SearchView = ViewBindings.findChildViewById(rootView, id);
      if (SearchView == null) {
        break missingId;
      }

      id = R.id.recyclerview;
      RecyclerView recyclerview = ViewBindings.findChildViewById(rootView, id);
      if (recyclerview == null) {
        break missingId;
      }

      return new YoutubeSearchBinding((ConstraintLayout) rootView, SearchView, recyclerview);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
