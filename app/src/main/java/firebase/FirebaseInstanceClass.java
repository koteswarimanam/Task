package firebase;


import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;

public class FirebaseInstanceClass extends FirebaseMessagingService {
    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        String token = s;
    }

    /* @Override
    public void onTokenRefresh() {
       // super.onTokenRefresh();

        String token = FirebaseInstanceId.getInstance().toString();

    }*/
}
