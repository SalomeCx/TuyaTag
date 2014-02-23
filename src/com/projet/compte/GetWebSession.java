package com.projet.tuyatag.compte;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.projet.tuyatag.R;

public class GetWebSession extends Activity {
    /** Called when the activity is first created. */
   
	private EditText user;
	private EditText password;
	private Button loginBtn;
	private Button signupBtn;
	
	//主要是记录用户会话过程中的一些用户的基本信息
	private HashMap<String, String> session =new HashMap<String, String>();
	
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ecran_co);
        
        user=(EditText)findViewById(R.id.editTextUserNameToLogin);
        password=(EditText)findViewById(R.id.editTextPasswordToLogin);
        
        loginBtn=(Button)findViewById(R.id.buttonSignIn);
        loginBtn.setOnClickListener(loginClick);
        
        
        signupBtn=(Button)findViewById(R.id.createaccountinlogin);
        signupBtn.setOnClickListener(signupClick);
        
        
    }
    
    OnClickListener loginClick=new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			
			if(checkUser()){
				Toast.makeText(v.getContext(), "Login Successfully", Toast.LENGTH_SHORT).show();
				
				Context context = v.getContext();
				
				//传递session参数,在用户登录成功后为session初始化赋值,即传递HashMap的值
				Bundle map = new Bundle();				
				map.putSerializable("sessionid", session);
				
				//context.startActivity(intent); // 跳转到成功页面	
				
			}
			else
				Toast.makeText(v.getContext(), "Login Failed", Toast.LENGTH_SHORT).show();
			
		}
	};
    
    OnClickListener signupClick=new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent= new Intent(GetWebSession.this, SignUPActivity.class);
			startActivity(intent)	;	
		}
	};
    
	
    private boolean checkUser(){
    	
    	String username=user.getText().toString();
    	String pass=password.getText().toString();
    	
    	DefaultHttpClient mHttpClient = new DefaultHttpClient();
		HttpPost mPost = new HttpPost("http://tuyatag.irisa.fr/php/login.php");
		
		List<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();
		pairs.add(new BasicNameValuePair("user", username));
		pairs.add(new BasicNameValuePair("mdp", pass));

		try {
			mPost.setEntity(new UrlEncodedFormEntity(pairs, HTTP.UTF_8));
		} catch (UnsupportedEncodingException e) {
			
			e.printStackTrace();
		}
    			
		try {
			HttpResponse response = mHttpClient.execute(mPost);
			int res = response.getStatusLine().getStatusCode();
			
			if (res == 200) {
				HttpEntity entity = response.getEntity();
						
				if (entity != null) {
					String info = EntityUtils.toString(entity);
					System.out.println("info-----------"+info);
					//以下主要是对服务器端返回的数据进行解析
					
					JSONObject jsonObject=null;
					//flag为登录成功与否的标记,从服务器端返回的数据
					String flag="";					
					String name="";
					String userid="";
					String sessionid="";
					try {
						jsonObject = new JSONObject(info);
						flag = jsonObject.getString("flag");
						name = jsonObject.getString("name");
						userid = jsonObject.getString("userid");
						sessionid = jsonObject.getString("sessionid");				
					
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//根据服务器端返回的标记,判断服务端端验证是否成功						
					
					if(flag.equals("success")){
						//为session传递相应的值,用于在session过程中记录相关用户信息
						session.put("s_userid", userid);
						session.put("s_username", name);						
						session.put("s_sessionid", sessionid);
						return true;
					}
					else{
						return false;
					}
				}
				else{
					
					return false;
				}
					
			}
			
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return false;
    }
	
	
    
}