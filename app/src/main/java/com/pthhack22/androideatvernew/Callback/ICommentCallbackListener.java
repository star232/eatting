package com.pthhack22.androideatvernew.Callback;

import com.pthhack22.androideatvernew.Model.CommentModel;

import java.util.List;

public interface ICommentCallbackListener {
    void onCommentLoadSuccess(List<CommentModel> commentModels);
        void onCommentLoadFailed(String message);

}
