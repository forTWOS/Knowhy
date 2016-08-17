package com.example.apple.knowhy;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Rawght Steven on 8/9/16, 14.
 * Email:rawghtsteven@gmail.com
 */
@SuppressLint("ValidFragment")
public class ArticalActivity extends AppCompatActivity{

    private int id;
    private ImageView imageView;
    private TextView textView;
    private WebView webView;
    private Button back, share, comment, like;
    private RequestQueue queue;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.artical_activity);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        imageView = (ImageView) findViewById(R.id.article_image);
        textView = (TextView) findViewById(R.id.article_title);
        webView = (WebView) findViewById(R.id.article_web);
        back = (Button) findViewById(R.id.back);
        share = (Button) findViewById(R.id.share);
        comment = (Button) findViewById(R.id.comment);
        like = (Button) findViewById(R.id.like);
        queue = Volley.newRequestQueue(this);

        Intent intent = getIntent();
        id = intent.getIntExtra("id",0);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        String url = "http://news-at.zhihu.com/api/4/news/"+id;
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("RESPONSE",response);
                Gson gson = new Gson();
                ArticalBean bean = gson.fromJson(response,ArticalBean.class);
                display(bean);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("ERROR!",error.getMessage());
            }
        });
        queue.add(request);
    }


    private void display(ArticalBean bean) {
        String body = bean.getBody();
        String image_source = bean.getImage_source();
        String title = bean.getTitle();
        String image = bean.getImage();
        final String share_url = bean.getShare_url();
        //ArticalBean.Section section = bean.getSection();
        //String thumbnail = section.getThumbnail();
        //String name = section.getName();

        WebSettings settings= webView.getSettings(); // webView: 类WebView的实例
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webView.loadDataWithBaseURL(null,body,"text/html","utf-8",null);
        Typeface Segoe = Typeface.createFromAsset(getAssets(),"fonts/Segoe WP.TTF");
        textView.setTypeface(Segoe);
        textView.setText(title);
        ImageRequest imageRequest = new ImageRequest(image, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                imageView.setImageBitmap(response);
            }
        }, 0, 0, Bitmap.Config.RGB_565, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("IMAGE ERROR!",error.getMessage());
                imageView.setImageResource(R.drawable.xperia3);
            }
        });
        queue.add(imageRequest);

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra("String",share_url);
                intent.setType("text/html");
                startActivity(Intent.createChooser(intent,"选择你要分享的应用"));
            }
        });

        comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(),CommentActivity.class);
                intent.putExtra("id",id);
                startActivity(intent);
            }
        });

        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "http://news-at.zhihu.com/api/4/story-extra/"+id;
                StringRequest request = new StringRequest(url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int likes = jsonObject.getInt("popularity");
                            Toast.makeText(getApplication(),"点赞数:"+likes,Toast.LENGTH_SHORT).show();
                            like.setBackgroundResource(R.drawable.liked);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("LIKE ERROR","");
                    }
                });
                queue.add(request);
            }
        });
    }
}