package kidozen.client.analytics;

import android.app.*;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by christian on 10/22/14.
 */
public class Session {
    private String mUUID;
    private ArrayList<Event> mEvents;
    private String mFileName;
    private Context mContext;

    public Session(Context context){
        mContext = context;
        mUUID = UUID.randomUUID().toString();
        mFileName = String.format("%s.events", mUUID);
        mEvents = new ArrayList<Event>();
   }

    public String getUUID() {
        return mUUID;
    }

    public void setUUID(String mUUID) {
        this.mUUID = mUUID;
    }

    public void RemoveSavedEvents() {
        mContext.deleteFile(mFileName);
    }

    public void RemoveCurrentEvents() {
        mEvents.clear();
    }

    public void StartNew() {
        mUUID = UUID.randomUUID().toString();
        mFileName = String.format("%s.events",mUUID);
        mEvents = new ArrayList<Event>();
    }

    public void Save() throws IOException {
        FileOutputStream fos = mContext.openFileOutput(mFileName, Context.MODE_PRIVATE);
        ObjectOutputStream os = new ObjectOutputStream(fos);
        os.writeObject(this);
        os.close();
    }

    public void LogEvent(Event event) {
        mEvents.add(event);
    }

    public ArrayList<Event> getEvents() {
        return mEvents;
    }

    public void setEvents(ArrayList<Event> mEvents) {
        this.mEvents = mEvents;
    }

    public void LoadEventsFromDisk() throws IOException {
        FileInputStream fis = null;
        try {
            fis = mContext.openFileInput(mFileName);
            ObjectInputStream is = new ObjectInputStream(fis);
            mEvents =  (ArrayList<Event>) is.readObject();
            is.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


}
