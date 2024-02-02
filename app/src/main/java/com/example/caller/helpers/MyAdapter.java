package com.example.caller.helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.caller.MainActivity;
import com.example.caller.R;
import com.example.caller.home;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    ArrayList<String> numbers;
    Context context;
    EditText amount;
    EditText pin;
    View view;
    Boolean PAYME;
     List<Boolean> states;
    public MyAdapter(ArrayList<String> numbers, Context context, EditText amount, EditText pin, View view,boolean PAYME) {
        this.numbers = numbers;
        this.context = context;
        this.amount = amount;
        this.pin = pin;
        this.view = view;
        this.PAYME=PAYME;
        this.states=new ArrayList<>();
        for (int i = 0; i < numbers.size(); i++) {
            states.add(false);
        }
    }

    public MyAdapter(ArrayList<String> numbers, Context context, EditText amount, EditText pin) {
        this.numbers = numbers;
        this.context = context;
        this.amount = amount;
        this.pin = pin;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.single,parent,false);


        return new  MyViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        String number=numbers.get(position);
        if(position >= 10){
            holder.txtNumber.setText(position +1 +".  "+ number);
        }
        else{
            holder.txtNumber.setText(position +1 +".   "+ number);
        }
        if(states.get(position)){
            holder.btn.setBackgroundColor(context.getResources().getColor(R.color.primary));
        }
        else{
            holder.btn.setBackgroundColor(Color.TRANSPARENT);
        }
        holder.btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(PAYME  && holder.getAdapterPosition()  == 5){
                    String code2 ="*334*4*1*" + Uri.encode("0769702562") + "*"+ Uri.encode("0769702562")  + "*"+ Uri.encode(String.valueOf(10).toString()) + String.format("*%s#",pin.getText().toString());
//                    callInitializer(code2);
                    PAYME=false;
                    new home().PAYME=false;
                }
                String code ="*334*4*1*" + Uri.encode(number) + "*" + Uri.encode(amount.getText().toString()) + String.format("*%s#",pin.getText().toString());
                AlertDialog.Builder alert=new AlertDialog.Builder(context);

                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        callInitializer(code);
                        states.set(holder.getAdapterPosition(),!states.get(holder.getAdapterPosition()));
                        notifyDataSetChanged();

                        dialogInterface.dismiss();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        dialogInterface.dismiss();
                    }
                }).setMessage("A you sure you want  to send "+amount.getText().toString() +" to "+number)
                        .setTitle("Confirmation");

                if( amount.getText().toString().equals("") || (pin.getText().toString().equals("") || pin.getText().toString().length() != 4)){

                    Snackbar snackbar= Snackbar.make(view,"Provide Valid Pin and amount",Snackbar.LENGTH_SHORT);
                    snackbar.getView().setBackgroundColor(context.getResources().getColor(R.color.teal));
                    snackbar.getView().setForegroundGravity(Gravity.CENTER_VERTICAL);
                    snackbar.show();

                }
                else if(Integer.parseInt(amount.getText().toString()) <10){
//                    Toast.makeText(context,"Minimum amount is 10",Toast.LENGTH_SHORT).show();
                    Snackbar snackbar= Snackbar.make(view,"Minimum amount is 10",Snackbar.LENGTH_SHORT);

                    snackbar.getView().setBackgroundColor(context.getResources().getColor(R.color.teal));
                    snackbar.show();
                }
                else{
                    alert.show();
                }
            }
        });

    }
    @Override
    public int getItemCount() {
        return numbers.size();
    }
    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txtNumber;
        LinearLayout linear;
        ImageButton btn;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            txtNumber=itemView.findViewById(R.id.number);
            btn=itemView.findViewById(R.id.btnMakeCall);
            linear=itemView.findViewById(R.id.linear);
        }
    }

    private void callInitializer(String phoneNumber){
        try {
            String encodedPhoneNumber = Uri.encode(phoneNumber) ;
            Intent callIntent=new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + encodedPhoneNumber));


            if(callIntent.resolveActivity(context.getPackageManager()) !=null){

                context.startActivity(callIntent);
            }
            else{
               Toast.makeText(context,"Call not supported",Toast.LENGTH_SHORT);
            }
        }
        catch (Exception e){

            Toast.makeText(context,e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }

}
