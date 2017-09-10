package ti.fcm;

// from 
// https://github.com/googlesamples/google-services/blob/master/android/gcm/app/src/main/java/gcm/play/android/samples/com/gcmquickstart/MyGcmListenerService.java#L43-L69
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.appcelerator.titanium.TiApplication;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.gcm.GcmListenerService;

public class MyGcmListenerService extends GcmListenerService {
	
	private FcmModule module = FcmModule.getModule();

	@Override
	public void onMessageReceived(String from, Bundle data) {
		FcmModule.log(">>>>>>>>> got notification from " + from);
		for (String key : data.keySet()) {
			Object value = data.get(key);
			FcmModule.log(key + " :: " + value.getClass().getName());
		}
		JSONObject json = new JSONObject();
		Set<String> keys = data.keySet();
		for (String key : keys) {
			try {
				json.put(key, JSONObject.wrap(data.get(key)));
			} catch (JSONException e) {
			}
		}
		GCMQueue.insertMessage(data.getString("google.message_id"),
				data.getLong("google.sent_time"), json);
		Boolean isAppInBackground = !testIfActivityIsTopInList()
				.getIsForeground();
		Boolean sendMessage = !isAppInBackground;
		Boolean showNotification = isAppInBackground;
		if (!isAppInBackground) {
			FcmModule
					.log("!isAppInBackground  => depending on force_show_in_foreground: ");
			if (json != null && json.has("force_show_in_foreground")) {
				Boolean forceShowInForeground = false;
				try {
					forceShowInForeground = json
							.getBoolean("force_show_in_foreground");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				showNotification = (forceShowInForeground == true);
			} else {
				showNotification = false;
			}
		}

		if (sendMessage && module != null) {
			FcmModule.log(" IntentServioce tries to sendback to JS via module");
			module.sendMessage(json.toString(), isAppInBackground);
		}
		if (showNotification) {
			FcmModule.log("showNotification will call");
			MyNotification.create(json);
		} else {
			FcmModule.log("Show Notification: FALSE");
		}
	}

	
      static public TaskTestResult testIfActivityIsTopInList() {
		try {
			TaskTestResult result = new ForegroundCheck().execute(
					TiApplication.getInstance().getApplicationContext()).get();
			return result;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		} catch (ExecutionException e) {
			e.printStackTrace();
			return null;
		}
	}

	
}
