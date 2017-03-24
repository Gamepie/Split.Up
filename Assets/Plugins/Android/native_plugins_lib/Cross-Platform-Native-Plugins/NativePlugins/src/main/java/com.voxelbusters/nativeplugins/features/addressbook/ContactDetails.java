package com.voxelbusters.nativeplugins.features.addressbook;

import com.voxelbusters.nativeplugins.defines.Keys;

import java.util.ArrayList;
import java.util.HashMap;

// For storing Contact details for each contact
public class ContactDetails
{

	String				displayName			= "";
	String				familyName			= "";
	String				givenName			= "";

	ArrayList<String>	phoneList			= new ArrayList<String>();

	String				profilePicturePath	= "";

	ArrayList<String>	emailList			= new ArrayList<String>();

	public void setNames(String displayName, String familyName, String givenName)
	{
		this.displayName = displayName;
		this.familyName = familyName;
		this.givenName = givenName;
	}

	public void addPhoneNumber(String eachPhoneNumber, int numberType)
	{
		// Ignoring numberType
		// numberType //Phone.TYPE_HOME/Phone.TYPE_MOBILE/Phone.TYPE_WORK
		phoneList.add(eachPhoneNumber);
	}

	public void setPicturePath(String path)
	{
		profilePicturePath = path;
	}

	public void addEmail(String emailContact, int emailType)
	{
		// emailType is not stored here. Can be extended later here
		emailList.add(emailContact);
	}

	HashMap<String, Object> getHash()
	{
		HashMap<String, Object> map = new HashMap<String, Object>();

		map.put(Keys.AddressBook.DISPLAY_NAME, displayName);
		map.put(Keys.AddressBook.FAMILY_NAME, familyName);
		map.put(Keys.AddressBook.GIVEN_NAME, givenName);

		map.put(Keys.AddressBook.IMAGE_PATH, profilePicturePath);

		map.put(Keys.AddressBook.PHONE_NUM_LIST, phoneList);
		map.put(Keys.AddressBook.EMAIL_ID_LIST, emailList);

		return map;
	}
}