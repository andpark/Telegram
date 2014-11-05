package com.teamjihu;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.telegram.ui.ChatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by OOO on 2014-11-04.
 */
public class DownloadZzalTask extends AsyncTask<String, Void, ArrayList<String>> {
    ChatActivity chatActivity;
    String urldisplay;

    public DownloadZzalTask(ChatActivity _chatActivity) {
        chatActivity = _chatActivity;
    }

    protected ArrayList<String> doInBackground(String... urls) {
        urldisplay = urls[0];
        Bitmap mIcon11 = null;

        File tempFile = null;
        ArrayList<String> photos = new ArrayList<String>();
        Bitmap bmp;

        try {
            tempFile = File.createTempFile("tempEmojiFile.jpg", null, chatActivity.getParentActivity().getCacheDir());
            OutputStream outputStream = new FileOutputStream(tempFile);

            InputStream is = new URL( urldisplay ).openStream();
            String ext = MimeTypeMap.getFileExtensionFromUrl( urldisplay );
            bmp = BitmapFactory.decodeStream(is);
            is.close();

            if ( ext.equals("png") )
                bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            else
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

            outputStream.flush();
            outputStream.close();

            bmp.recycle();
            bmp = null;

            photos.add(tempFile.getPath());


            String[] urlParse = urldisplay.split("/");

            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://2runzzal.com/themegram/increaseTelegramCount");
            ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("name", urlParse[urlParse.length - 1]));

            UrlEncodedFormEntity ent;
            try {
                ent = new UrlEncodedFormEntity(params, HTTP.UTF_8);
                post.setEntity(ent);
                HttpResponse responsePOST = client.execute(post);
                HttpEntity resEntity = responsePOST.getEntity();

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch ( IOException ie ) {
            ie.printStackTrace();
        }

        return photos;
    }

    protected void onPostExecute(ArrayList<String> photos) {
        if(photos.size() > 0) {
            chatActivity.didSelectPhotos(photos);
        } else {
            Toast.makeText(chatActivity.getParentActivity(), "짤 전송에 실패하였습니다.", Toast.LENGTH_SHORT).show();
        }
    }
}
