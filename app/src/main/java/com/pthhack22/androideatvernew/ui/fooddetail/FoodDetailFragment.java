package com.pthhack22.androideatvernew.ui.fooddetail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.andremion.counterfab.CounterFab;
import com.bumptech.glide.Glide;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.pthhack22.androideatvernew.Common.Common;
import com.pthhack22.androideatvernew.Model.CommentModel;
import com.pthhack22.androideatvernew.Model.FoodModel;
import com.pthhack22.androideatvernew.Model.SizeModel;
import com.pthhack22.androideatvernew.R;
import com.pthhack22.androideatvernew.ui.comments.CommentFragment;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

public class FoodDetailFragment extends Fragment {

    private FoodDetailViewModel foodDetailViewModel;

    private Unbinder unbinder;
    private android.app.AlertDialog waitingDialog;

    @BindView( R.id.img_food )
    ImageView img_food;
    @BindView( R.id.btnCart )
    CounterFab btnCart;
    @BindView( R.id.btn_rating )
    FloatingActionButton btn_rating;
    @BindView( R.id.food_name )
    TextView food_name;
    @BindView( R.id.food_description )
    TextView food_description;
    @BindView( R.id.food_pride )
    TextView food_pride;
    @BindView( R.id.number_button )
    ElegantNumberButton number_button;
    @BindView( R.id.ratingBar )
    RatingBar ratingBar;
    @BindView( R.id.btnShowComment )
    Button btnShowComment;
    @BindView( R.id.rdi_group_size )
    RadioGroup rdi_group_size;

    @OnClick(R.id.btn_rating)
    void onRatingButtonClick()
    {
        showDiaLogRating();
    }

    @OnClick(R.id.btnShowComment)
    void  onShowCommentButtonClick(){
        CommentFragment commentFragment = CommentFragment.getInstance();
        commentFragment.show(getActivity().getSupportFragmentManager(),"CommentFragment");
    }


    private void showDiaLogRating() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder( getContext() );
        builder.setTitle( "Rating Food" );
        builder.setMessage( "Please fill information" );

        View itemView = LayoutInflater.from( getContext() ).inflate( R.layout.layout_rating,null );

        RatingBar ratingBar = (RatingBar)itemView.findViewById( R.id.rating_bar );
        EditText edt_comment = itemView.findViewById( R.id.edt_comment );

        builder.setView( itemView );

        builder.setNegativeButton( "CANCEL", (dialogInterface, i) -> {
            dialogInterface.dismiss();
        } );
        builder.setPositiveButton( "OK", (dialogInterface, i) -> {
            CommentModel commentModel = new CommentModel();
            commentModel.setName( Common.currentUser.getName() );
            commentModel.setUid( Common.currentUser.getUid() );
            commentModel.setComment( edt_comment.getText().toString() );
            commentModel.setRatingValue( ratingBar.getRating() );
            Map<String,Object> serverTimeStamp = new HashMap<>(  );
            serverTimeStamp.put( "timeStamp", ServerValue.TIMESTAMP );
            commentModel.setCommentTimeStamp( serverTimeStamp );

            foodDetailViewModel.setCommentModel( commentModel );


        } );
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        foodDetailViewModel =
                ViewModelProviders.of( this ).get( FoodDetailViewModel.class );
        View root = inflater.inflate( R.layout.fragment_food_detail, container, false );
        unbinder = ButterKnife.bind( this,root );
        initViews();
        foodDetailViewModel.getMutableLiveDataFood().observe( getViewLifecycleOwner(), foodModel -> {
            displayInto(foodModel);
        } );

        foodDetailViewModel.getMutableLiveDataComment().observe( getViewLifecycleOwner(),commentModel ->  {
            submitRatingToFirebase(commentModel);
        });
        return root;
    }

    private void initViews() {
        waitingDialog = new SpotsDialog.Builder().setCancelable( false ).setContext( getContext() ).build();
    }

    private void submitRatingToFirebase(CommentModel commentModel) {
        waitingDialog.show();
        //FIrt
        FirebaseDatabase.getInstance()
                .getReference(Common.COMMENT_REF)
                .child( Common.selectedFood.getId() )
                .push()
                .setValue( commentModel )
                .addOnCompleteListener( task -> {
                    if (task.isSuccessful())
                    {
                        //After submit to CommentRef
                        addRatingFood(commentModel.getRatingValue());
                    }
                    waitingDialog.dismiss();
                } );

    }

    private void addRatingFood(float ratingValue) {
        FirebaseDatabase.getInstance()
                .getReference(Common.CATEGORY_REF)
                .child( Common.categorySelected.getMenu_id() )
        .child( "foods" )
        .child( Common.selectedFood.getKey() )
        .addListenerForSingleValueEvent( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    FoodModel foodModel = dataSnapshot.getValue(FoodModel.class);
                    foodModel.setKey( Common.selectedFood.getKey() );

                    //Apply rating
                    if (foodModel.getRatingValue() == null)
                        foodModel.setRatingValue( 0d );
                    if(foodModel.getRatingCount() == null)
                        foodModel.setRatingCount( 0l );
                    double sumRating = foodModel.getRatingValue()+ratingValue;
                    long ratingCount = foodModel.getRatingCount()+1;
                    double result = sumRating/ratingCount;

                    Map<String,Object> updateData = new HashMap<>(  );
                    updateData.put( "ratingValue", result );
                    updateData.put( "ratingCount", ratingCount);

                    //Update
                    foodModel.setRatingValue(result  );
                    foodModel.setRatingCount( ratingCount );


                    dataSnapshot.getRef()
                            .updateChildren( updateData )
                            .addOnCompleteListener( task -> {
                                if(task.isSuccessful())
                                {
                                    waitingDialog.dismiss();
                                    if(task.isSuccessful())
                                    {
                                        Toast.makeText( getContext(), "Thank you !", Toast.LENGTH_SHORT ).show();
                                        Common.selectedFood = foodModel;
                                        foodDetailViewModel.setFoodModel(foodModel);
                                    }
                                }
                            } );
                }
                else
                    waitingDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                waitingDialog.dismiss();
                Toast.makeText( getContext(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT ).show();
            }
        } );
    }

    private void displayInto(FoodModel foodModel) {
        Glide.with( getContext() ).load( foodModel.getImage() ).into( img_food );
        food_name.setText( new StringBuilder( foodModel.getName() ) );
        food_description.setText( new StringBuilder( foodModel.getDescription() ) );
        food_pride.setText( new StringBuilder( foodModel.getPrice().toString() ) );

        if(foodModel.getRatingValue() !=null)
            ratingBar.setRating( foodModel.getRatingValue().floatValue() );
        ((AppCompatActivity)getActivity())
                .getSupportActionBar()
                .setTitle( Common.selectedFood.getName() );
        //Size
        for(SizeModel sizeModel: Common.selectedFood.getSize())
        {
            RadioButton radioButton = new RadioButton( getContext() );
            radioButton.setOnCheckedChangeListener( (compoundButton, b) -> {
                if(b)
                    Common.selectedFood.setUserSelectedSize(sizeModel);
                calculateTotalPrice(); // Update price

            } );
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams( 0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1.0f);
            radioButton.setLayoutParams(params);
            radioButton.setText( sizeModel.getName());
            radioButton.setTag( sizeModel.getPrice());

            rdi_group_size.addView( radioButton );
        }
        if (rdi_group_size.getChildCount()>0)
        {
            RadioButton radioButton =(RadioButton)rdi_group_size.getChildAt( 0 );
            radioButton.setChecked( true ); //Default first select
        }

        calculateTotalPrice();
    }

    private void calculateTotalPrice() {
        double totalPrice = Double.parseDouble( Common.selectedFood.getPrice().toString() ),displayPrice=0.0;
        //Size
        totalPrice +=Double.parseDouble( Common.selectedFood.getUserSelectedSize().getPrice().toString() );

        displayPrice = totalPrice *(Integer.parseInt( number_button.getNumber() ));
        displayPrice = Math.round( displayPrice*100.0/100.0 );

        food_pride.setText( new StringBuilder( "" ).append( Common.formatPrice(displayPrice) ).toString() );
    }
}
