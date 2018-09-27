package com.virtu.kunal.myapplication;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import android.util.Base64;

public class MainActivity extends AppCompatActivity {

    EditText et_hostel_name;
    EditText et_owner_name;
    EditText et_mobile_no;
    EditText et_address_1;
    EditText et_address_2;
    EditText et_holding_no;
    EditText et_ward_no;
    EditText et_num_beds;

    CheckBox cb_fire_extinguisher;
    CheckBox cb_cctv;
    CheckBox cb_parking;
    CheckBox cb_water_harvesting;

    String hostel_name;
    String owner_name;
    String mobile_no;
    String address_1;
    String address_2;
    String holding_no;
    String ward_no;
    String num_beds;

    Boolean fire_extinguisher;
    Boolean cctv;
    Boolean parking;
    Boolean water_harvesting;

    Button b_take_photo;
    Button b_submit;

    ProgressDialog dialog = null;
    JSONObject jsonObject;
    ArrayList<Uri> imagesUriList;
    ArrayList<String> encodedImageList;
    String imageURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        et_address_1 = (EditText) findViewById(R.id.tv_address_line_1);
        et_address_2 = (EditText) findViewById(R.id.tv_address_line2);
        et_hostel_name = (EditText) findViewById(R.id.tv_name_hostel);
        et_owner_name = (EditText) findViewById(R.id.tv_name_owner);
        et_mobile_no = (EditText) findViewById(R.id.tv_mobile);
        et_holding_no = (EditText) findViewById(R.id.tv_holding);
        et_ward_no = (EditText) findViewById(R.id.tv_ward);
        et_num_beds = (EditText) findViewById(R.id.tv_beds);

        cb_cctv = (CheckBox) findViewById(R.id.cb_cctv);
        cb_fire_extinguisher = (CheckBox) findViewById(R.id.cb_fire_extinguisher);
        cb_parking = (CheckBox) findViewById(R.id.cb_parking);
        cb_water_harvesting = (CheckBox) findViewById(R.id.cb_water_harvesting);

        b_take_photo = (Button) findViewById(R.id.b_take_photo);
        b_submit = (Button) findViewById(R.id.b_submit);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading Image...");
        dialog.setCancelable(false);

        jsonObject = new JSONObject();
        encodedImageList = new ArrayList<>();

        b_take_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Choose application"), Utils.REQCODE);

            }
        });

        b_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upload();
            }
        });

    }

    void upload() {

        if (encodedImageList.size()!=5){
            Toast.makeText(this, "Please select 5 images"+encodedImageList.size(), Toast.LENGTH_LONG).show();
            return;
        }

        this.dialog.show();

        JSONArray jsonArray = new JSONArray();

        for (String encoded: encodedImageList){
            jsonArray.put(encoded);
        }

        try {
            jsonObject.put("hostel_name",hostel_name);
            jsonObject.put("owner_name",owner_name);
            jsonObject.put("hostel_mobile",mobile_no);
            jsonObject.put("ward_no",ward_no);
            jsonObject.put("holding_no",holding_no);
            jsonObject.put("number_beds",num_beds);
            jsonObject.put("hostel_address",address_1+";"+address_2);

            jsonObject.put("fire_extinguisher",fire_extinguisher);
            jsonObject.put("cctv",cctv);
            jsonObject.put("parking",parking);
            jsonObject.put("water_harvesting",water_harvesting);

            jsonObject.put(Utils.imageName, System.currentTimeMillis()+"_"+hostel_name);
            jsonObject.put(Utils.imageList, jsonArray);
        } catch (JSONException e) {
            Log.e("JSONObject Here", e.toString());
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Utils.urlUpload, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        Log.e("Message from server", jsonObject.toString());
                        dialog.dismiss();
                       // messageText.setText("Images Uploaded Successfully");
                        Toast.makeText(getApplication(), "Images Uploaded Successfully", Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e("Message from server", volleyError.toString());
                Toast.makeText(getApplication(), "Error Occurred", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy( 200*30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        try {
            if (requestCode == Utils.REQCODE && resultCode == RESULT_OK
                    && null != data) {

                String[] filePathColumn = { MediaStore.Images.Media.DATA };
                imagesUriList = new ArrayList<Uri>();
                encodedImageList.clear();
                if(data.getData()!=null){

                    Uri mImageUri=data.getData();

                    Cursor cursor = getContentResolver().query(mImageUri,
                            filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    imageURI  = cursor.getString(columnIndex);
                    cursor.close();

                }else {
                    if (data.getClipData() != null) {
                        ClipData mClipData = data.getClipData();
                        ArrayList<Uri> mArrayUri = new ArrayList<Uri>();
                        for (int i = 0; i < mClipData.getItemCount(); i++) {

                            ClipData.Item item = mClipData.getItemAt(i);
                            Uri uri = item.getUri();
                            mArrayUri.add(uri);
                            Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
                            cursor.moveToFirst();

                            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                            imageURI  = cursor.getString(columnIndex);
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                            String encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
                            encodedImageList.add(encodedImage);
                            cursor.close();

                        }
                    }
                }
            } else {
                Toast.makeText(this, "You haven't picked Image",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
