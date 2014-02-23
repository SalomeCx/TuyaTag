package com.projet.tuyatag.compte;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
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

import com.projet.tuyatag.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SignUPActivity extends Activity
{
   
            EditText editTextUserName,editTextPassword,editTextConfirmPassword,editTextEmail;
            Button btnCreateAccount;
           
            
            @Override
            protected void onCreate(Bundle savedInstanceState)
            {
                    super.onCreate(savedInstanceState);
                    setContentView(R.layout.inscription);
                   
                    // get Instance  of Database Adapter
                   
                   
                    // Get Refferences of Views
                    editTextUserName=(EditText)findViewById(com.example.testtuyatag.R.id.editTextUserName);
                    editTextPassword=(EditText)findViewById(R.id.editTextPassword);
                    editTextConfirmPassword=(EditText)findViewById(R.id.editTextConfirmPassword);
                    editTextEmail=(EditText)findViewById(R.id.editTextEmail);
                   
                    btnCreateAccount=(Button)findViewById(R.id.buttonCreateAccount);
                   
                   
                    btnCreateAccount.setOnClickListener(new View.OnClickListener() {
                       
                        @Override
						public void onClick(View v) {
                            // TODO Auto-generated method stub
                           int flag=0;
                            String userName=editTextUserName.getText().toString();
                            String password=editTextPassword.getText().toString();
                            String confirmPassword=editTextConfirmPassword.getText().toString();
                           
                            // check if any of the fields are vaccant
                            if(userName.equals("")||password.equals("")||confirmPassword.equals(""))
                            {
                                    Toast.makeText(getApplicationContext(), "Field Vaccant", Toast.LENGTH_LONG).show();
                                    
                                    //return;
                            }
                            // check if both password matches
                            else if(!password.equals(confirmPassword))
                            {
                                Toast.makeText(getApplicationContext(), "Password Does Not Matches", Toast.LENGTH_LONG).show();
                               // return;
                            }
                            else
                            {
                                    // Save the Data in Database
                                   // loginDataBaseAdapter.insertEntry(userName, password);
                            		flag=Signup();
                            		if(flag==1)
                            			Toast.makeText(getApplicationContext(), "Account Successfully Created ", Toast.LENGTH_LONG).show();
                            		else if(flag==2)
                            			Toast.makeText(getApplicationContext(), "Account is already exist", Toast.LENGTH_LONG).show();
                            		else
                            			Toast.makeText(getApplicationContext(), "Creating account failed, flag: "+flag, Toast.LENGTH_LONG).show();
                            }
                           
                           
                        }

						
                    });
            }
            
            private int Signup() {
            	String userName=this.editTextUserName.getText().toString();
            	String email=this.editTextEmail.getText().toString();
            	String password = this.editTextPassword.getText().toString();
            	
            	DefaultHttpClient mHttpClient = new DefaultHttpClient();
        		HttpPost mPost = new HttpPost("http://tuyatag.irisa.fr/php/signup.php");
        		
        		List<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();
        		pairs.add(new BasicNameValuePair("username", userName));
        		pairs.add(new BasicNameValuePair("mdp", password));
        		pairs.add(new BasicNameValuePair("email", email));

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
        						return 1;
        					}
        					else if (flag.equals("exist")){
        						return 2;
        					}
        				}
        				else{
        					
        					return 0;
        				}
        					
        			}
        			
        		} catch (ClientProtocolException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        		} catch (IOException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        		}
            	
            	return -1;

			
				// TODO Auto-generated method stub
				
			}
     

}