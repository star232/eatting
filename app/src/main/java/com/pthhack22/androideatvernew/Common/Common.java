package com.pthhack22.androideatvernew.Common;

import com.pthhack22.androideatvernew.Model.CategoryModel;
import com.pthhack22.androideatvernew.Model.FoodModel;
import com.pthhack22.androideatvernew.Model.UserModel;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class Common {
    public static final String USER_REFERENCESER ="Users" ;
    public static final String POPULAR_CATEGORY_REF ="MostPopular" ;
    public static final String BEST_DEAL_REF ="BestDeals" ;
    public static final int DEFAULT_COLUMN_COUNT =0 ;
    public static final int FULL_WIDTH_COLUMN = 1;
    public static final String CATEGORY_REF ="Category" ;
    public static final String COMMENT_REF ="Comments" ;
    public static UserModel currentUser;
    public static CategoryModel categorySelected;
    public static FoodModel selectedFood;

    public static String formatPrice(double price) {
        if(price !=0)
        {
            DecimalFormat df = new DecimalFormat( "#,##0.00" );
            df.setRoundingMode( RoundingMode.UP );
            String finalPrice = new StringBuilder( df.format( price ) ).toString();
            return finalPrice.replace( ".","," );
        }
        else
            return "0.00";
    }
}
