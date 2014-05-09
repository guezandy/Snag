package com.example.snagtag;

/*
 * Important notes on NFC the Samgsung Galaxy S4 is not compatible with the classic Mifare Tags because it 
 * uses the Broadcom NFC chip.
 * NTAG203 tags are fully compatible with all nfc phones including the galaxy s4
 */



import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

@TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
public class SnagActivity extends FragmentActivity {

	//parameters used to store data internally
	public static final String ENTITY_DELIM = "|";
	public static final String DETAIL_DELIM = "~";
	public static final String CURRENT_SNAG = "current";
	
	//initialize nfcadapter object
	private NfcAdapter mNfcAdapter;
	private ProgressBar mProgressBar;	
	
//Used to write to nfc commenting out for now!
	//private Button mEnableWriteButton;
	//private EditText mTextField;

	@SuppressLint("CutPasteId")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_snag);
		Parse.initialize(this, "QoNDrz4pfuyMua3gxRCAKgUq5jW5Fl7HISCMXjOF", "nsgiu5WCMMBTrkCgOJkrcTwJoF2isRHJPemaiYy3");

		//SharedPreferences prefs = this.getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);
		//InternalData.addItem(prefs, CURRENT_SNAG, "");
		mProgressBar = (ProgressBar) findViewById(R.id.pgWriteToTag);
		mProgressBar.setVisibility(View.GONE);
		
//Used to write to nfc tags
/*//		mTextField = (EditText) findViewById(R.id.nfcWriteString);		
		mEnableWriteButton = (Button) findViewById(R.id.writeToTagButton);
		
		mEnableWriteButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setTagWriteReady(!isWriteReady);
				mProgressBar.setVisibility(isWriteReady ? View.VISIBLE : View.GONE);
			}
		});
		*/
		
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (mNfcAdapter == null) {//no nfc on phone
			Toast.makeText(this, "Sorry, NFC is not available on this device", Toast.LENGTH_SHORT).show();
			finish();
		}
		
	}
	//read or write?
	private boolean isWriteReady = false;

	/**
	 * Enable this activity to write to a tag
	 * 
	 * @param isWriteReady
	 */
	public void setTagWriteReady(boolean isWriteReady) {
		this.isWriteReady = isWriteReady;
		if (isWriteReady) {
			IntentFilter[] writeTagFilters = new IntentFilter[] { new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED) };
			mNfcAdapter.enableForegroundDispatch(SnagActivity.this, NfcUtils.getPendingIntent(SnagActivity.this),
					writeTagFilters, null);
		} else {
			// Disable dispatch if not writing tags
			mNfcAdapter.disableForegroundDispatch(SnagActivity.this);
		}
		mProgressBar.setVisibility(isWriteReady ? View.VISIBLE : View.GONE);
	}

	@Override
	public void onNewIntent(Intent intent) {
		// onResume gets called after this to handle the intent
		setIntent(intent);
	}

	
//nfc intent launches onResume!
	@Override
	public void onResume() {
		super.onResume();
		Bundle bundle = getIntent().getExtras();
		if (isWriteReady && NfcAdapter.ACTION_TAG_DISCOVERED.equals(getIntent().getAction())) {
			processWriteIntent(getIntent());
		} else if (!isWriteReady
				&& (NfcAdapter.ACTION_TAG_DISCOVERED.equals(getIntent().getAction()) || NfcAdapter.ACTION_NDEF_DISCOVERED
						.equals(getIntent().getAction()))) {
			//mTextField.setVisibility(View.GONE);
			//mEnableWriteButton.setVisibility(View.GONE);
			processReadIntent(getIntent());
		} else if(bundle.getString("nfcId")!=null) {
			
//this will be called when trying to open zip activity not through nfc tag
			//get clothing item because nfcId in the bundle
			//mTextField.setVisibility(View.GONE);
			//mEnableWriteButton.setVisibility(View.GONE);
		}
	}

	private static final String MIME_TYPE = "application/com.tapped.nfc.tag";

	/**
	 * Write to an NFC tag; reacting to an intent generated from foreground
	 * dispatch requesting a write
	 * 
	 * @param intent
	 */
	public void processWriteIntent(Intent intent) {
		if (isWriteReady && NfcAdapter.ACTION_TAG_DISCOVERED.equals(getIntent().getAction())) {

			Tag detectedTag = getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);
/*
		//	String tagWriteMessage = mTextField.getText().toString();
		//	byte[] payload = new String(tagWriteMessage).getBytes();

			if (detectedTag != null && NfcUtils.writeTag(
					NfcUtils.createMessage(MIME_TYPE, payload), detectedTag)) {
				
			//	Toast.makeText(this, "Wrote '" + tagWriteMessage + "' to a tag!", 
			//			Toast.LENGTH_LONG).show();
				setTagWriteReady(false);
			} else {
				Toast.makeText(this, "Write failed. Please try again.", Toast.LENGTH_LONG).show();
			}*/
		}
	}

	public void processReadIntent(Intent intent) {
		List<NdefMessage> intentMessages = NfcUtils.getMessagesFromIntent(intent);
		List<String> payloadStrings = new ArrayList<String>(intentMessages.size());

		for (NdefMessage message : intentMessages) {
			for (NdefRecord record : message.getRecords()) {
				byte[] payload = record.getPayload();
				String payloadString = new String(payload);

				if (!TextUtils.isEmpty(payloadString))
					payloadStrings.add(payloadString);
			}
		}

		if (!payloadStrings.isEmpty()) {
			String content =  TextUtils.join(",", payloadStrings);
			Toast.makeText(SnagActivity.this, "Read from tag: " + content,
					Toast.LENGTH_LONG).show();
			System.out.println(content);
			System.out.println("We got a tag.");
			
			ParseObject gameScore = new ParseObject("GameScore");
			gameScore.put("score", 1337);
			gameScore.put("playerName", "Sean Plott");
			gameScore.put("cheatMode", false);
			gameScore.saveInBackground();
			
/*			ParseQuery<ParseObject> query = ParseQuery.getQuery("GameScore");
			
			try {
				System.out.println(query.get("0j1RzmAFBH"));
			} catch (ParseException e1) {
				System.out.println("Caught exception");
				e1.printStackTrace();
			}
			query.getInBackground("0j1RzmAFBH", new GetCallback<ParseObject>() {
				@Override
				public void done(ParseObject item, ParseException e) {
				    if (e == null) {
				        // item will be your game score
				    	Toast.makeText(SnagActivity.this, item.getString("name").toString() , Toast.LENGTH_SHORT).show();
				      } else {
					    	Toast.makeText(SnagActivity.this, e.getMessage() , Toast.LENGTH_SHORT).show();
 
				        // something went wrong
				      }
				}
			});*/
		}
	}
		public void addEntity(View v){
			//SharedPreferences prefs = this.getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);
			//String report = prefs.getString(CURRENT_SNAG, "");
	        //InternalData.addItem(prefs, CART_KEY, "Lacoste~$89.5~http://slimages.macys.com/is/image/MCY/products/8/optimized/1242258_fpx.tif|");

			//if( !report.isEmpty()){
				//InternalData.addItem(prefs, InternalData.CART_KEY, report);
				//System.out.println("Just added to internal data");
			//}
		
	}	
}
