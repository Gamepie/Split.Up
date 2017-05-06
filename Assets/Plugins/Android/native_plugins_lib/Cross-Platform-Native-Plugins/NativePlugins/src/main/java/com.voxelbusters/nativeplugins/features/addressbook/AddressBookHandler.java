package com.voxelbusters.nativeplugins.features.addressbook;

import android.annotation.SuppressLint;
import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;

import com.voxelbusters.nativeplugins.NativePluginHelper;
import com.voxelbusters.nativeplugins.defines.CommonDefines;
import com.voxelbusters.nativeplugins.defines.Keys;
import com.voxelbusters.nativeplugins.defines.UnityDefines;
import com.voxelbusters.nativeplugins.utilities.ApplicationUtility;
import com.voxelbusters.nativeplugins.utilities.Debug;
import com.voxelbusters.nativeplugins.utilities.FileUtility;
import com.voxelbusters.nativeplugins.utilities.JSONUtility;
import com.voxelbusters.nativeplugins.utilities.StringUtility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// <uses-permission android:name="android.permission.READ_CONTACTS" />
public class AddressBookHandler
{
	static final Uri					CONTACT_CONTENT_URI			= ContactsContract.Contacts.CONTENT_URI;
	static final String					CONTACT_IN_VISIBLE_GROUP	= ContactsContract.Contacts.IN_VISIBLE_GROUP;

	static final String					ROOT_CONTACT_ID				= ContactsContract.Contacts._ID;
	static final String					CONTENT_ITEM_TYPE			= ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE;

	// For getting name of contact
	@SuppressLint("InlinedApi")
	static final String					DISPLAY_NAME				= Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? ContactsContract.Contacts.DISPLAY_NAME_PRIMARY : ContactsContract.Contacts.DISPLAY_NAME;
	static final String					GIVEN_NAME					= ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME;
	static final String					FAMILY_NAME					= ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME;

	// For phone numbers
	static final String					PHONE_NUMBER				= ContactsContract.CommonDataKinds.Phone.NUMBER;
	static final String					PHONE_TYPE					= ContactsContract.CommonDataKinds.Phone.TYPE;
	static final String					PHONE_DISPLAY_NAME			= ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME;
	static final Uri					PHONE_CONTENT_URI			= ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
	static final String					PHONE_CONTACT_ID			= ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
	static final String					HAS_PHONE_NUMBER			= ContactsContract.Contacts.HAS_PHONE_NUMBER;

	// http://developer.android.com/reference/android/provider/ContactsContract.Contacts.Photo.html
	static final String					PHOTO_URI					= Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? ContactsContract.CommonDataKinds.Phone.PHOTO_URI : ContactsContract.CommonDataKinds.Phone.PHOTO_ID;
	static final String					PHOTO_CONTENT_DIRECTORY		= ContactsContract.Contacts.Photo.CONTENT_DIRECTORY;

	// Email details
	static final String					EMAIL_DATA					= ContactsContract.CommonDataKinds.Email.DATA;
	static final String					EMAIL_TYPE					= ContactsContract.CommonDataKinds.Email.TYPE;
	static final Uri					EMAIL_CONTENT_URI			= ContactsContract.CommonDataKinds.Email.CONTENT_URI;
	static final String					EMAIL_CONTACT_ID			= ContactsContract.CommonDataKinds.Email.CONTACT_ID;

	// Create singleton instance
	private static AddressBookHandler	INSTANCE;

	public static AddressBookHandler getInstance()
	{
		if (INSTANCE == null)
		{
			INSTANCE = new AddressBookHandler();
		}
		return INSTANCE;
	}

	// Make constructor private for making singleton interface
	private AddressBookHandler()
	{
	}

	public boolean isAuthorized()
	{
		return ApplicationUtility.hasPermission(NativePluginHelper.getCurrentContext(), Keys.Permission.READ_CONTACTS);
	}

	public void readContacts()
	{
		Runnable runnable = new Runnable()
			{
				@Override
				public void run()
				{
					readContactsInBackground();
				}
			};

		//Execute this thread in background
		new Thread(runnable).start();
	}

	public void addContact(String contactDataJsonStr)
	{
		final JSONObject json = JSONUtility.getJSON(contactDataJsonStr);

		Runnable runnable = new Runnable()
			{

				@Override
				public void run()
				{
					addContactInternal(json);
				}
			};

		//Execute this thread in background
		new Thread(runnable).start();
	}

	@SuppressWarnings("rawtypes")
	private void readContactsInBackground()
	{
		Cursor cursor = null;
		String authStatus;
		ArrayList<HashMap> detailsHashList = null;
		Map<String, ContactDetails> contactsMap;
		ArrayList<ContactDetails> contactsList = new ArrayList<ContactDetails>();
		try
		{

			//Fetch the context
			Context context = NativePluginHelper.getCurrentContext();

			// Get content resolver
			ContentResolver contentResolver = context.getContentResolver();

			String[] projectionFields = new String[] { ROOT_CONTACT_ID, PHOTO_URI };

			// Qurery for contacts content for the items which are visible in contacts app
			cursor = contentResolver.query(CONTACT_CONTENT_URI, projectionFields, CONTACT_IN_VISIBLE_GROUP + " = '1'", null, DISPLAY_NAME + " ASC");

			boolean contactsExist = cursor.moveToFirst();

			detailsHashList = new ArrayList<HashMap>();
			contactsMap = new HashMap<String, ContactDetails>(cursor.getCount());

			if (contactsExist)
			{
				do
				{
					// Create contact details instance
					ContactDetails details = new ContactDetails();
					String contactID = getCursorString(cursor, ROOT_CONTACT_ID);

					String nameSelection = ContactsContract.Data.MIMETYPE + " = ? AND " + ContactsContract.CommonDataKinds.StructuredName.CONTACT_ID + " = ?";
					String[] nameSelectionParams = new String[] { CONTENT_ITEM_TYPE, contactID };

					projectionFields = new String[] { DISPLAY_NAME, FAMILY_NAME, GIVEN_NAME };
					Cursor nameCursor = contentResolver.query(ContactsContract.Data.CONTENT_URI, projectionFields, nameSelection, nameSelectionParams, GIVEN_NAME);
					nameCursor.moveToFirst();

					String displayName = getCursorString(nameCursor, DISPLAY_NAME);
					String familyName = getCursorString(nameCursor, FAMILY_NAME);
					String givenName = getCursorString(nameCursor, GIVEN_NAME);

					// Update names here
					details.setNames(displayName, familyName, givenName);

					nameCursor.close();

					// Get contact picture path
					String pictureUriString = getCursorString(cursor, PHOTO_URI);
					if (!StringUtility.isNullOrEmpty(pictureUriString))
					{
						String absolutePath = FileUtility.getSavedLocalFileFromUri(context, Uri.parse(pictureUriString), "contacts", contactID);

						if (absolutePath == null)
						{
							Debug.error(CommonDefines.ADDRESS_BOOK_TAG, "Unable to load profile image for below details");
							Debug.log(CommonDefines.ADDRESS_BOOK_TAG, "contactID : " + contactID);
							Debug.log(CommonDefines.ADDRESS_BOOK_TAG, "pictureUriString : " + pictureUriString);
						}

						if (absolutePath != null)
						{
							details.setPicturePath(absolutePath);
						}
					}

					contactsMap.put(contactID, details);
					contactsList.add(details);

				} while (cursor.moveToNext());

				// Fetch phone numbers
				Cursor phonesCursor = contentResolver.query(PHONE_CONTENT_URI, null, null, null, null);
				while (phonesCursor.moveToNext())
				{
					String eachPhoneNumber = getCursorString(phonesCursor, PHONE_NUMBER);
					int numberType = getCursorInt(phonesCursor, PHONE_TYPE);

					String contactIdString = getCursorString(phonesCursor, PHONE_CONTACT_ID);

					ContactDetails details = contactsMap.get(contactIdString);

					if (details == null)
					{
						continue;
					}

					// Add this detail here
					details.addPhoneNumber(eachPhoneNumber, numberType);
				}

				// Close the cursor once done
				phonesCursor.close();

				// Get Email list
				Cursor emailCursor = contentResolver.query(EMAIL_CONTENT_URI, null, null, null, null);
				while (emailCursor.moveToNext())
				{
					String emailContact = getCursorString(emailCursor, EMAIL_DATA);
					if (emailContact != null)
					{
						int emailType = getCursorInt(emailCursor, EMAIL_TYPE);
						String contactIdString = getCursorString(emailCursor, EMAIL_CONTACT_ID);
						ContactDetails details = contactsMap.get(contactIdString);
						if (details == null)
						{
							continue;
						}
						details.addEmail(emailContact, emailType);
					}
				}

				//Close cursor once done
				emailCursor.close();

				for (int i = 0; i < contactsList.size(); i++)
				{
					//Get Hashmap details from details object
					HashMap<String, Object> detailsMap = contactsList.get(i).getHash();

					//Add to collection
					detailsHashList.add(detailsMap);
				}

			}

			//Call to clean up.
			System.gc();
			authStatus = Keys.AddressBook.ACCESS_AUTHORIZED;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			String error = e.getMessage();
			Debug.error(CommonDefines.ADDRESS_BOOK_TAG, error);
			authStatus = Keys.AddressBook.ACCESS_RESTRICTED;
			detailsHashList = null;
		}
		finally
		{
			if (cursor != null)
			{
				//Close cursor once done
				cursor.close();
			}
		}

		HashMap<String, Object> packedData = new HashMap<String, Object>();
		packedData.put(Keys.AddressBook.AUTH_STATUS, authStatus);
		packedData.put(Keys.AddressBook.CONTACTS_LIST, detailsHashList);

		//Send message to Host Platform
		NativePluginHelper.sendMessage(UnityDefines.AddressBook.READ_CONTACTS_FINISED, packedData);

	}

	private void addContactInternal(JSONObject json)
	{
		String familyName = json.optString(Keys.AddressBook.FAMILY_NAME);
		String givenName = json.optString(Keys.AddressBook.GIVEN_NAME);
		String imagePath = json.optString(Keys.AddressBook.IMAGE_PATH, null);
		byte[] imageByteStream = null;

		if (!StringUtility.isNullOrEmpty(imagePath))
		{
			ByteArrayOutputStream stream = FileUtility.getBitmapStream(imagePath);
			if (stream != null)
			{
				imageByteStream = stream.toByteArray();
			}
		}

		JSONArray emailIdList = json.optJSONArray(Keys.AddressBook.EMAIL_ID_LIST);
		JSONArray phoneNumberList = json.optJSONArray(Keys.AddressBook.PHONE_NUM_LIST);

		//Fetch the context
		Context context = NativePluginHelper.getCurrentContext();
		// Get content resolver
		ContentResolver contentResolver = context.getContentResolver();

		ArrayList<ContentProviderOperation> contactOperation = new ArrayList<ContentProviderOperation>();
		int contactIndex = contactOperation.size();
		Debug.error("Test", "count : " + contactIndex);

		Builder builder = ContentProviderOperation.newInsert(RawContacts.CONTENT_URI);
		builder.withValue(RawContacts.ACCOUNT_TYPE, null);
		builder.withValue(RawContacts.ACCOUNT_NAME, null);
		contactOperation.add(builder.build());

		//Now add data
		builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
		builder.withValueBackReference(Data.RAW_CONTACT_ID, contactIndex);
		builder.withValue(StructuredName.FAMILY_NAME, familyName);
		builder.withValue(StructuredName.GIVEN_NAME, givenName);
		builder.withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
		contactOperation.add(builder.build());

		if (imageByteStream != null)
		{
			builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
			builder.withValueBackReference(Data.RAW_CONTACT_ID, contactIndex);
			builder.withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, imageByteStream);
			builder.withValue(Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE);
			contactOperation.add(builder.build());
		}

		if (emailIdList != null)
		{

			for (int index = 0; index < emailIdList.length(); index++)
			{

				String eachEmail = null;
				try
				{
					eachEmail = (String) emailIdList.get(index);
				}
				catch (JSONException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (!StringUtility.isNullOrEmpty(eachEmail))
				{
					builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
					builder.withValueBackReference(Data.RAW_CONTACT_ID, contactIndex);
					builder.withValue(Email.DATA, eachEmail);
					builder.withValue(Email.TYPE, Email.TYPE_OTHER);
					builder.withValue(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);
					contactOperation.add(builder.build());
				}

			}

		}

		if (phoneNumberList != null)
		{
			for (int index = 0; index < phoneNumberList.length(); index++)
			{
				String eachPhoneNumber = null;
				try
				{
					eachPhoneNumber = (String) phoneNumberList.get(index);
				}
				catch (JSONException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (!StringUtility.isNullOrEmpty(eachPhoneNumber))
				{
					builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
					builder.withValueBackReference(Data.RAW_CONTACT_ID, contactIndex);
					builder.withValue(Phone.NUMBER, eachPhoneNumber);
					builder.withValue(Phone.TYPE, Phone.TYPE_OTHER);
					builder.withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
					contactOperation.add(builder.build());
				}
			}
		}

		ContentProviderResult[] result = null;
		try
		{
			result = contentResolver.applyBatch(ContactsContract.AUTHORITY, contactOperation);
		}
		catch (RemoteException e)
		{
			e.printStackTrace();
		}
		catch (OperationApplicationException e)
		{
			e.printStackTrace();
		}

		NativePluginHelper.sendMessage(UnityDefines.AddressBook.ADD_CONTACTS_FINISHED, result == null ? "false" : "true");
	}

	//Helpers
	String getCursorString(Cursor cursor, String columnName)
	{
		int columnIndex;
		String string = null;
		try
		{
			//This can throw error if this columnName doesn't exist
			columnIndex = cursor.getColumnIndex(columnName);
			if (columnIndex != -1)
			{
				string = cursor.getString(columnIndex);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return string;
	}

	int getCursorInt(Cursor cursor, String columnName)
	{
		return cursor.getInt(cursor.getColumnIndex(columnName));
	}
}
