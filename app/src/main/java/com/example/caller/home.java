package com.example.caller;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.caller.databinding.FragmentHomeBinding;
import com.example.caller.helpers.MyAdapter;
import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Timer;


public class home extends Fragment {

    Uri fileurl;
    ImageButton btnCallEdit;
    EditText amount, pin, txtNumber;
    ArrayList<String> numbers;
    Boolean caller = false;
    RecyclerView recy;
    Uri previous_uri =null;
    FragmentHomeBinding binding;
    public static Boolean PAYME=true;
    View root;
    int index=0;
    public home() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater,container,false);

//        variables
        txtNumber = binding.editNumber;
        amount = binding.amount;
        pin = binding.pin;
        recy = binding.recy;
        numbers = new ArrayList<>();
        root=binding.getRoot();
        binding.filePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseFile();
//                practice();
            }
        });
        binding.btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!caller && numbers.size() > 0){

                    if (!amount.getText().toString().equals("") && pin.getText().toString().length() == 4) {

                        AlertDialog.Builder alert=new AlertDialog.Builder(getActivity());
                        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        binding.btnCall.setText("STOP");
                                        binding.btnCall.setBackgroundColor(getResources().getColor(R.color.red_2));
                                        caller=true;
                                        placeCall(amount.getText().toString(), pin.getText().toString(),index);
                                        dialogInterface.dismiss();

                                    }
                                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        dialogInterface.dismiss();
                                    }
                                }).setMessage("A you sure you want  to do Bulk transactions of KSH "+amount.getText().toString() +" to "+numbers.size() +" Numbers")
                                .setTitle("Confirmation");

                        alert.show();

                    } else {
                        Toast.makeText(getContext(), "Amount required", Toast.LENGTH_SHORT).show();
                        amount.setError("Required");
                        amount.requestFocus();
                    }
                }
                else if(numbers.size() < 1){

                    showSnack("Load txt File ");
                }
                else{
                    Toast.makeText(getContext(),"Stopped",Toast.LENGTH_SHORT).show();
                    binding.btnCall.setText("BULK CALL");
                    binding.btnCall.setBackgroundColor(getResources().getColor(R.color.teal));
                    caller=false;
                }
            }
        });
        binding.btnCallEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (txtNumber.getText().toString().equals("")) {
                    Toast.makeText(getContext(), "Phone number required", Toast.LENGTH_SHORT);
                    txtNumber.requestFocus();
                } else if (txtNumber.getText().length() < 10) {
                    Toast.makeText(getContext(), "Phone number should be 10 digits", Toast.LENGTH_SHORT);
                    txtNumber.requestFocus();
                } else if (amount.getText().toString().equals("") || (pin.getText().toString().equals("") || pin.getText().toString().length() != 4)) {
                    Toast.makeText(getContext(), "Provide Valid Pin and amount", Toast.LENGTH_SHORT).show();
                    amount.requestFocus();
                } else if (Integer.parseInt(amount.getText().toString()) < 10) {
                    Toast.makeText(getContext(), "Minimum amount is 10", Toast.LENGTH_SHORT).show();

                    pin.requestFocus();
                } else {
                    String code = "*334*4*1*1*" + Uri.encode(txtNumber.getText().toString()) + "*" + Uri.encode(amount.getText().toString()) + String.format("*%s#", pin.getText().toString());

                    AlertDialog.Builder alert = new AlertDialog.Builder(getContext());

                    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    callInitializer(code);
                                    dialogInterface.dismiss();
                                }
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }).setMessage("A you sure you want  to send " + amount.getText().toString() + " to " + txtNumber.getText().toString())
                            .setTitle("Confirmation").create().show();

                }
            }
        });
        return root;
    }
    private void placeCall(String amount,String pin, int index1){

            try {
                if(index1 < numbers.size()) {
                    String number = numbers.get(index1);
                    if (number.length() == 10) {
                        String code = "*334*4*1*1*" + Uri.encode(number) + "*" + Uri.encode(amount) + String.format("*%s#", pin.toString());
                        if (caller) {
                           // int wait=5400;
                            //if(index1 ==1){
                            //    wait=10000;
                            }
                            new Handler().postDelayed(new Runnable() {
                                @Override
                               public void run() {
                                   callInitializer(code);
                                   MainActivity activity = (MainActivity) getActivity();
                                    if (activity != null) {
                                       activity.updateNotification(String.valueOf(index1 + 1));
                                    }
                                   index += 1;
                                    placeCall(amount, pin, index);
                                    Toast.makeText(getContext(), " Sending... " + amount +" to "+numbers.get(index1), Toast.LENGTH_SHORT).show();

                                }
                            }, 5400); // Delay of 2000 milliseconds

                            }
                    }
                else{
                    index=0;
                    showSnack("Transactions Completed");
                }
            } catch (Exception e) {
                Toast.makeText(getContext(), "Error making call" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        }

    private void showSnack(String text) {
        Snackbar snackbar= Snackbar.make(txtNumber,text,Snackbar.LENGTH_SHORT);
        snackbar.getView().setBackgroundColor(getResources().getColor(R.color.green));
        snackbar.show();
    }

    private void callInitializer(String phoneNumber){
        try {
            String encodedPhoneNumber = Uri.encode(phoneNumber) ;
            Intent callIntent=new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + encodedPhoneNumber));
            if(callIntent.resolveActivity(getContext().getPackageManager()) !=null){

                startActivity(callIntent);
            }
            else{
                Snackbar snackbar= Snackbar.make(txtNumber,"Phone call not permitted",Snackbar.LENGTH_SHORT);
                snackbar.getView().setBackgroundColor(getResources().getColor(R.color.teal));
                snackbar.show();
            }
        }
        catch (Exception e){
            Snackbar snackbar= Snackbar.make(txtNumber,e.getMessage(),Snackbar.LENGTH_SHORT);
            snackbar.getView().setBackgroundColor(getResources().getColor(R.color.teal));
            snackbar.show();
        }
    }
    private void chooseFile(){
        Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/plain");
        startActivityForResult(Intent.createChooser(intent,"Pick a file"),3);
    }
    private String getPhoneNumber(String [] parts){
        String number="";
        for(String part : parts){
            if(part.trim().length() > 7 ){
                number= part.replace(",","").replace(" ","");
                break;
            }
        }
        return number;
    }
    private void readFileContents(boolean PAYME){
        try {
            InputStream inp=getContext().getContentResolver().openInputStream(fileurl);
            if(inp !=null){

                StringBuffer bf=new StringBuffer();
                BufferedReader reader=new BufferedReader(new InputStreamReader(inp));
                String line;

                numbers=new ArrayList<>();
                while((line =reader.readLine()) !=null){
                    if(line.length() > 9){
                        String n=getPhoneNumber(line.split(" "));
                        if(!n.startsWith("0")){
                            n="0"+n;
                        }
                        numbers.add(n.substring(0,10));
                        bf.append(n +"\n");
                    }
                }
                MyAdapter adapter=new MyAdapter(numbers,getContext(),amount,pin,txtNumber,PAYME);
                recy.setLayoutManager(new LinearLayoutManager(getContext()));
                recy.setHasFixedSize(true);
                recy.setAdapter(adapter);
            }
        }
        catch (Exception e){
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
  @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data !=null){
            fileurl=data.getData();
            if(previous_uri != fileurl  && previous_uri !=null && !PAYME){
                PAYME=true;
                previous_uri=fileurl;
            }
            index=0;
            readFileContents(PAYME);
        }
    }

}