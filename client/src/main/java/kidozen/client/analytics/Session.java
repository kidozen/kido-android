package kidozen.client.analytics;

import android.content.Context;

import com.google.gson.Gson;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

/**
 * Created by christian on 10/22/14.
 */
public class Session {
    private String mUUID;
    private Collection mEvents;
    private String mFileName;
    private Context mContext;
    private Date mStartDateWithTimeout = null;
    private int mSessionTimeout = 1;

    public Session(Context context){
        mContext = context;
        mUUID = UUID.randomUUID().toString();
        mFileName = String.format("%s.events", mUUID);
        mEvents = new ArrayList<Event>();

        Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, mSessionTimeout);
        mStartDateWithTimeout = c.getTime();
    }

    public boolean ShouldUploadSessionUsingBackgroundDate() {
        Date now = Calendar.getInstance().getTime();
        return mStartDateWithTimeout !=null
                && mSessionTimeout > 0
                && mStartDateWithTimeout.compareTo(now) < 0;
    }

    public String getUUID() {
        return mUUID;
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

        Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, mSessionTimeout);
        mStartDateWithTimeout = c.getTime();
    }

    public void Save() throws IOException {
        if (ShouldUploadSessionUsingBackgroundDate()) {
            FileOutputStream fos = mContext.openFileOutput(mFileName, Context.MODE_PRIVATE);
            Gson gson = new Gson();
            fos.write(gson.toJson(mEvents).getBytes("UTF-8"));
            fos.close();
        }
    }

    public void LogEvent(Event event) {
        mEvents.add(event);
        try {
            Save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String LoadEventsFromDisk() throws IOException {
        FileInputStream fis = null;
        StringBuilder sb = new StringBuilder();
        try {
            fis = mContext.openFileInput(mFileName);
            int content;
            while ((content = fis.read()) != -1) {
                sb.append((char)content);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        finally {
            return  sb.toString();
        }
    }


}
