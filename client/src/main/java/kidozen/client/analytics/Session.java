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
    private String mEventsFileName;
    private Context mContext;
    private Date mStartDateWithTimeout = null;
    private int mSessionTimeout = 1;

    private SessionDetails mSessionDetails;
    private String mCurrentSessionInfoFilename;

    public int getSessionTimeout() {
        return mSessionTimeout;
    }

    public void setSessionTimeout(int timeout) {
        mSessionTimeout = timeout;
    }

    public Session(Context context) {
        mContext = context;
        this.StartNew();
    }

    public boolean ShouldUploadSessionAfterTimeout() {
        Date now = Calendar.getInstance().getTime();
        return mStartDateWithTimeout != null
                && mSessionTimeout > 0
                && mStartDateWithTimeout.compareTo(now) < 0;
    }

    public String getUUID() {
        return mUUID;
    }

    public void RemoveSavedEvents() {
        mContext.deleteFile(mEventsFileName);
    }

    public void RemoveCurrentEvents() {
        mEvents.clear();
    }

    public void StartNew() {
        mUUID = UUID.randomUUID().toString();
        mEventsFileName = String.format("%s.events", mUUID);
        mEvents = new ArrayList<Event>();

        Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, mSessionTimeout);
        mStartDateWithTimeout = c.getTime();

        // persists current session information for later usage
        mCurrentSessionInfoFilename = String.format("%s.session", mUUID);
        mSessionDetails = new SessionDetails(mUUID,mContext);
        try {
            FileOutputStream fos = mContext.openFileOutput(mCurrentSessionInfoFilename, Context.MODE_PRIVATE);
            Gson gson = new Gson();
            fos.write(gson.toJson(mSessionDetails).getBytes("UTF-8"));

            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void Save() throws IOException {
        FileOutputStream fos = mContext.openFileOutput(mEventsFileName, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        fos.write(gson.toJson(mEvents).getBytes("UTF-8"));
        fos.close();
    }

    public String GetEventsSerializedAsJson() {
        if (mEvents.size()<=0) {
            return "";
        }
        else {
            Gson gson = new Gson();
            return gson.toJson(mEvents);
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
        return readFile(mEventsFileName);
    }

    public String LoadSessionInformationFromDisk() throws IOException {
        return readFile(mCurrentSessionInfoFilename);
    }

    private String readFile(String filename) throws IOException {
        FileInputStream fis = null;
        StringBuilder sb = new StringBuilder();
        try {
            fis = mContext.openFileInput(filename);
            int content;
            while ((content = fis.read()) != -1) {
                sb.append((char) content);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            return sb.toString();
        }
    }

    public void RemoveCurrentSession() {
        mContext.deleteFile(mCurrentSessionInfoFilename);
    }

    public void Reset() {
        this.RemoveSavedEvents();
        this.RemoveCurrentEvents();
        this.RemoveCurrentSession();
        this.StartNew();
    }

}


