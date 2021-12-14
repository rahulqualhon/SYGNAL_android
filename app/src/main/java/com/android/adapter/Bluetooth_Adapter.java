package com.android.adapter;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.android.R;
import com.android.models.BluetoothDeviceModel;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Random;

public class Bluetooth_Adapter extends RecyclerView.Adapter<Bluetooth_Adapter.ViewHolder> {

    private ArrayList<BluetoothDeviceModel> bluetoothDeviceModels;
    private Context context;


    public Bluetooth_Adapter(Context context, ArrayList<BluetoothDeviceModel> bluetoothDeviceModels) {
        this.bluetoothDeviceModels = bluetoothDeviceModels;
        this.context = context;
    }

    @NonNull
    @Override
    public Bluetooth_Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_bluetooth, parent, false);
        return new Bluetooth_Adapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Bluetooth_Adapter.ViewHolder holder, int position) {

        holder.nameTV.setText("Device Name : " + bluetoothDeviceModels.get(position).bluetooth_name);
        holder.gasScaleTv.setText("Gas Scale : " + bluetoothDeviceModels.get(position).gasScale + " %");
        holder.minAlertTv.setText("Min Alert Value : " + bluetoothDeviceModels.get(position).minValue);
        holder.maxAlertTv.setText("Alert Value : " + bluetoothDeviceModels.get(position).maxValue);
        holder.distanceTV.setText("Distance : " + bluetoothDeviceModels.get(position).distance);

     //   playAlertTone(bluetoothDeviceModels.get(position).isDistance);
        if(bluetoothDeviceModels.get(position).isDistance){
            holder.warningIv.setVisibility(View.GONE);
        }else{
            holder.warningIv.setVisibility(View.VISIBLE);
        }

//        if (bluetoothDeviceModels.get(position).isDistance) {
//            holder.warningIv.setVisibility(View.GONE);
//            if(player!=null){
//                player.release();
//            }
//
//        }else{
//
//
//
//        }
//        try {
//            Uri rawPathUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.beep_09);
//            Ringtone r = RingtoneManager.getRingtone(context, rawPathUri);
//            if (bluetoothDeviceModels.get(position).isDistance) {
//                holder.warningIv.setVisibility(View.GONE);
//                player.release();
//                player.stop();
//                //r.stop();
//
//            } else {
//                holder.warningIv.setVisibility(View.VISIBLE);
//                player.release();
//                player.stop();
//                if (r.isPlaying()) {
//                    r.stop();
//                }
//                playAlertTone(context);
//                //r.play();
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }


    }

    @Override
    public int getItemCount() {
        // return bluetoothDeviceModels.size();
        return 1;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView nameTV, distanceTV, gasScaleTv, minAlertTv, maxAlertTv;
        private ImageView warningIv;

        ViewHolder(@NonNull View itemView) {
            super(itemView);


            nameTV = itemView.findViewById(R.id.nameTV);
            distanceTV = itemView.findViewById(R.id.distanceTV);
            gasScaleTv = itemView.findViewById(R.id.gasScaleTv);
            minAlertTv = itemView.findViewById(R.id.minAlertTv);
            maxAlertTv = itemView.findViewById(R.id.maxAlertTv);
            warningIv = itemView.findViewById(R.id.warningIv);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }
    }



//call it like this from your activity' any method

}