package com.mytask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileInfo;
import com.dropbox.sync.android.DbxPath;

public class PhotoListActivity extends Activity {
	 private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	    private static String imageFilePath;
	    private ListView lstPhotos;
	    private ArrayList<DbxPath> filePathList;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photo_list);
		 lstPhotos = (ListView) findViewById(R.id.listview);
		 getListOfImages();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, 0, Menu.NONE,getString(R.string.hello_world)).setIcon(R.drawable.mycameraa).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		menu.add(Menu.NONE, 1, Menu.NONE,getString(R.string.hello_world)).setIcon(R.drawable.refresh).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == 0) {
			takeIamge();
		}
		else if(id==1) {
		
		refreshImageList();
	
		}
		return super.onOptionsItemSelected(item);
		
	}
	
	
	private void refreshImageList() {

    	//check if we are linked
    	if (MainActivity.dbxAcct.isLinked()) {
        	//refresh the list
    		
    		getListOfImages();
    	}
    	
    
		// TODO Auto-generated method stub
		
	}

	private void takeIamge() {
    	if (MainActivity.dbxAcctManager.hasLinkedAccount()) {
    		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    		imageFilePath = "manasa" + String.valueOf(System.currentTimeMillis()) + ".jpg";
    		Uri fileUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), imageFilePath));
    		imageFilePath = fileUri.getPath();
    		intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
    		startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    	} 
    }
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
    		if (resultCode == Activity.RESULT_OK) {
    			upLoadingImageToDropBox(imageFilePath);
    		}
	}
	}
	private void upLoadingImageToDropBox(String imageFilePath) {
    	if (!imageFilePath.equals("")) {
	    	String dbxPathString = imageFilePath;
	    	dbxPathString = dbxPathString.substring(dbxPathString.lastIndexOf("/"));
	    	
	    	DbxPath dbxPath = new DbxPath(dbxPathString);
	    	try {
				DbxFile dbxFile = MainActivity.dbxFileSystem.create(dbxPath);
				File imgFile = new File(imageFilePath);
				dbxFile.writeFromExistingFile(imgFile, false);
				dbxFile.close();
			} catch (Exception e) {
				//error in creating the dropbox file
			} 
	    	imageFilePath = "";	//reset the image path so we don't upload the image twice
	    	
    	}
    	
    	
    	
    	
    }
	
	 private void getListOfImages() {
	    	ArrayList<DbxFileInfo> fileList;
	    	try {
				fileList = (ArrayList<DbxFileInfo>) MainActivity.dbxFileSystem.listFolder(DbxPath.ROOT);
			} catch (DbxException e) {
				//error in getting access to the file list
				return;
			}
	    	//Populate an array of DbxPaths with the filenames
	    	filePathList = new ArrayList<DbxPath>();
	    	for (DbxFileInfo fileInfo : fileList) {
	    		filePathList.add(fileInfo.path);
	    	}
	    	//create the adapter that the list will use
	    	final ArrayAdapter<DbxPath> adapter = new ArrayAdapter<DbxPath>(this, android.R.layout.simple_list_item_1, filePathList);
	    	lstPhotos.setAdapter(adapter);
	    	//set the click listener to view the photo on click
	    	lstPhotos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	    		@Override
	    	    public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
	    			DbxPath dbxPath = (DbxPath) parent.getItemAtPosition(position);
	    			viewImage(dbxPath);
	    		}
			});
	    
	    }
	 
	 private void viewImage(DbxPath dbxPath) {
	    	//read the file and open the intent to view the image
	    	DbxFile imgFile;
	    	
	    	try {
				imgFile = MainActivity.dbxFileSystem.open(dbxPath);
			
		    	//read and copy from dropbox to local file system
		    	FileInputStream inputStream = imgFile.getReadStream();
		    	File tmpFile = new File(Environment.getExternalStorageDirectory(), "img_" + String.valueOf(System.currentTimeMillis()) + ".jpg");
		    	FileOutputStream outputStream = new FileOutputStream(tmpFile);
		    	int read = 0;
		    	byte[] bytes = new byte[1024];
		    	while ((read = inputStream.read(bytes)) != -1) {
		    		outputStream.write(bytes, 0, read);
		    	}
		    	inputStream.close();
		    	outputStream.close();
		    	imgFile.close();
		    	//now tmpFile contains the image. Launch the photo viewer
		    	Intent intent = new Intent();
		    	intent.setAction(Intent.ACTION_VIEW);
		    	intent.setDataAndType(Uri.fromFile(tmpFile), "image/*");
		    	startActivity(intent);
			} catch (DbxException e) {
				return;
			} catch (IOException e) {
				return;
			}
	    }
}
