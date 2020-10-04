package com.pluralsight.courses.utility;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pluralsight.courses.AddEventActivty;
import com.pluralsight.courses.R;
import com.pluralsight.courses.ScheduleEventActivity;
import com.pluralsight.courses.ShowScheduledEventActivity;
import com.pluralsight.courses.users.ScheduledEvent;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;


@RequiresApi(api = Build.VERSION_CODES.N)
public class ScheduledEventAdapter extends ArrayAdapter<ScheduledEvent> {
    public static final String NOTIFICATION_CHANNEL_ID = "10001" ;
    private final static String default_notification_channel_id = "default" ;
    private int mLayoutResource;
    private Context mContext;
    final Calendar myCalendar = Calendar.getInstance();
    String eventId;
    private LayoutInflater mInflater;
    public ScheduledEventAdapter(@NonNull Context context, int resource, @NonNull List<ScheduledEvent> objects) {
        super(context, resource, objects);
        mContext = context;
        mLayoutResource = resource;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }
    public static class ViewHolder{
        TextView name, details,address,date;
        ImageView mEventImage, interestedImage;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final  ViewHolder holder;
        if(convertView == null){
            convertView = mInflater.inflate(mLayoutResource, parent, false);
            holder = new ScheduledEventAdapter.ViewHolder();

            holder.address = convertView.findViewById(R.id.scheduledAddress);
            holder.details = convertView.findViewById(R.id.scheduledDetails);
            holder.name = convertView.findViewById(R.id.scheduledNAme);
            holder.mEventImage = convertView.findViewById(R.id.scheduledImage);
            holder.interestedImage = convertView.findViewById(R.id.interested);
            holder.date = convertView.findViewById(R.id.date);
        }else{
            holder = (ScheduledEventAdapter.ViewHolder) convertView.getTag();
        }
        try {
            holder.address.setText(getItem(position).getAddress());
            holder.name.setText(getItem(position).getName());
            holder.details.setText(getItem(position).getDetails());
            holder.date.setText(getItem(position).getDate());
            if(getItem(position).getImage_description_uri()!=null){
                Picasso.with(getContext()).load(getItem(position).getImage_description_uri()).into(holder.mEventImage);
            }
            if(getItem(position).getIntersted_users().contains(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                holder.interestedImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_baseline_check_24));
            }else{
                holder.interestedImage.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onClick(View view) {
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                        reference.child("scheduled_events")
                                .child(getItem(position).getScheduled_event_id())
                                .child("intersted_users")
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        eventId = getItem(position).getScheduled_event_id();
                        String myFormat = "dd/MM/yy" ; //In which you need put here
                        SimpleDateFormat sdf = new SimpleDateFormat(myFormat , Locale. getDefault ()) ;
                        Date date = myCalendar .getTime() ;
                        long delay = getDelay(getItem(position).getDate());

                        scheduleNotification(getNotification(getItem(position).getName()) , delay-date.getTime());
                    }
                });
            }

        }catch  (NullPointerException e){
            Log.e(TAG, "getView: NullPointerException: ", e.getCause() );
        }
        return convertView;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private long getDelay(String date) {

        DateTimeFormatter dateFormatter
                = DateTimeFormatter.ofPattern("dd/M/yyyy", Locale.ENGLISH);
        long millisecondsSinceEpoch = LocalDate.parse(date, dateFormatter)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli();
        return millisecondsSinceEpoch;
    }

    private void scheduleNotification (Notification notification , long delay) {
        Intent notificationIntent = new Intent( getContext(), BroadCast.class ) ;
        notificationIntent.putExtra(BroadCast.NOTIFICATION_ID , 1 ) ;
        notificationIntent.putExtra(BroadCast.EVENT_ID,eventId);
        notificationIntent.putExtra(BroadCast. NOTIFICATION , notification) ;
        PendingIntent pendingIntent = PendingIntent. getBroadcast ( getContext(), 0 , notificationIntent , PendingIntent. FLAG_UPDATE_CURRENT ) ;
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context. ALARM_SERVICE);
        Toast.makeText(mContext,String.valueOf( delay), Toast.LENGTH_SHORT).show();
        assert alarmManager != null;
        alarmManager.set(AlarmManager. ELAPSED_REALTIME_WAKEUP , 1000 , pendingIntent) ;
    }
    private Notification getNotification (String content) {
        Intent intent = new Intent(getContext(), AddEventActivty.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getContext(),1 ,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder( getContext(), default_notification_channel_id ) ;
        builder.setContentTitle( "Scheduled Notification" ) ;
        builder.setContentText("You have " +content+" scheduled for today") ;
        builder.setSmallIcon(R.drawable.ic_add_alert_24 ) ;
        builder.setAutoCancel( true );
        builder.setChannelId( NOTIFICATION_CHANNEL_ID)
        .setContentIntent(pendingIntent);
        return builder.build();
    }
}
