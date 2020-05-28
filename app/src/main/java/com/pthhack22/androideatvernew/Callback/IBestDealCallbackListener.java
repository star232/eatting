package com.pthhack22.androideatvernew.Callback;

import com.pthhack22.androideatvernew.Model.BestDealModel;
import com.pthhack22.androideatvernew.Model.PopularCategoryModel;

import java.util.List;

public interface IBestDealCallbackListener {
    void onBestDealLoadSuccess(List<BestDealModel> bestDealModels);
    void onBestDealLoadFailed(String message);
}
