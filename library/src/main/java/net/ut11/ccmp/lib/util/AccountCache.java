package net.ut11.ccmp.lib.util;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import net.ut11.ccmp.lib.LibApp;
import net.ut11.ccmp.lib.db.Account;
import net.ut11.ccmp.lib.db.AccountsDb;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.util.HashMap;

/**
 * Created by dwohlmuth on 19.03.14.
 */
public class AccountCache {

    public static final String INTENT_ACTION_ACCOUNT_DATA_UPDATED = "net.ut11.ccmp.lib.INTENT_ACTION_ACCOUNT_DATA_UPDATED";
    private static HashMap<Long, Account> cache = new HashMap<Long, Account>();

    public static Account getAccount(long accountId) {
        Account account = cache.get(accountId);

        if (account == null) {
            account = AccountsDb.getAccount(accountId);

            if (account != null) {
                cache.put(accountId, account);
            }
        }

        if (account != null && account.getAvatar() == null) {
            new DownloadAvatarTask(accountId, account.getAvatarUrl()).execute();
        }

        return account;
    }

    public static void removeFromCache(long accountId) {
        cache.remove(accountId);
    }

    private static class DownloadAvatarTask extends AsyncTask<Void, Void, Boolean> {

        private long accountId;
        private String url;

        public DownloadAvatarTask(long accountId, String url) {
            this.accountId = accountId;
            this.url = url;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                DefaultHttpClient mHttpClient = new DefaultHttpClient();
                HttpGet mHttpGet = new HttpGet(url);
                HttpResponse mHttpResponse = null;
                mHttpResponse = mHttpClient.execute(mHttpGet);
                if (mHttpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    HttpEntity entity = mHttpResponse.getEntity();
                    if (entity != null) {
                        byte[] avatar = EntityUtils.toByteArray(entity);
                        AccountsDb.addAvatar(accountId, avatar);

                        Account account = cache.get(accountId);
                        if (account != null) {
                            account.setAvatar(BitmapFactory.decodeByteArray(avatar, 0, avatar.length));
                            LibApp.getContext().sendBroadcast(new Intent(INTENT_ACTION_ACCOUNT_DATA_UPDATED));
                        }
                    }
                }
            } catch (Exception e) {
                return false;
            }

            return true;
        }
    }
}
