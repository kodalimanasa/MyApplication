package com.mytask;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.dropbox.sync.android.DbxAccount;
import com.dropbox.sync.android.DbxAccountInfo;
import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxFileSystem;

public class MainActivity extends Activity implements OnClickListener {
	private static final String appKey = "fco3n0d8irewjuc";
	private static final String appSecret = "x2uhdrms6lnq7tx";
	private static final int REQUEST_LINK_TO_DBX = 1;
	public static DbxAccountManager dbxAcctManager;
	public static DbxAccount dbxAcct;
	public static DbxAccountInfo dbxAcctInfo;
	public static DbxFileSystem dbxFileSystem;
	private ImageView login;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		login = (ImageView) findViewById(R.id.dropbox);
		login.setOnClickListener(this);

		dbxAcctManager = DbxAccountManager.getInstance(getApplicationContext(),appKey, appSecret);

		// if we are linked to a DropBox account already, execute the linking
		// code
		if (dbxAcctManager.hasLinkedAccount()) {
			onLinkToDropBox();
		}
	}
	
	

	private void onLinkToDropBox() {
		// executed once we have a confirmed link to dropbox
		dbxAcct = dbxAcctManager.getLinkedAccount();
		dbxAcctInfo = dbxAcct.getAccountInfo();
		// lblWelcomeMsg.setText("Welcome, " + dbxAcctInfo.displayName + ".");
		// get the file system information
		try {
			dbxFileSystem = DbxFileSystem.forAccount(dbxAcct);
		} catch (Exception e) {
			// unauthorized to use the file system
		}
	}

	@Override
	public void onClick(View v) {

		// first unlink an account if it already exists
		if (dbxAcctManager.hasLinkedAccount()) {
			dbxAcct.unlink();
		}
		// authenticate and link to the dropbox account
		dbxAcctManager.startLink((Activity) this, REQUEST_LINK_TO_DBX);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (requestCode == REQUEST_LINK_TO_DBX) {
			// we requested a link to the dropbox account
			if (resultCode == Activity.RESULT_OK) {
				// can allow other operations as we have a successful link
				onLinkToDropBox();
				startActivity(new Intent(MainActivity.this,PhotoListActivity.class));
			} 
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

}
