package com.smallcrafts.wakemeup;

import java.util.Locale;

import android.location.Address;
import android.util.Log;

public class CustomAddress extends Address{
	
	public CustomAddress(Locale locale) {
		super(locale);
	}
	
	public CustomAddress(Address a){
		super(a.getLocale());
		
		Log.d("CUSTOMADDRESS", "Address Start----------------------------------");
		
		int maxLines = a.getMaxAddressLineIndex();
		if (a.getAddressLine(0) != null)
			this.setAddressLine(0, a.getAddressLine(0));
		if (a.getAddressLine(1) != null)
			this.setAddressLine(1, a.getAddressLine(1));
		if (a.getAddressLine(2) != null)
			this.setAddressLine(2, a.getAddressLine(2));
		
//		Log.d("CUSTOMADDRESS", "Source Lines: "+ Integer.toString(maxLines));
//		for (int i = 0; i < maxLines; i++){
//			this.setAddressLine(i, a.getAddressLine(i));
//			if ( i == 0){
//				for (int j = 1; j < maxLines; j++){
//					this.setAddressLine(j, a.getAddressLine(j));
//				}
//			}
//			Log.d("CUSTOMADDRESS", "AddressLine " + Integer.toString(i) + " : " + a.getAddressLine(i));
//		}
		
		maxLines = this.getMaxAddressLineIndex();

		Log.d("CUSTOMADDRESS", "NEW Lines: "+ Integer.toString(maxLines));
		
		for (int i = 0; i < maxLines; i++){
			Log.d("CUSTOMADDRESS", "AddressLine " + Integer.toString(i) + " : " + getAddressLine(i));
		}
		
		
		this.setAdminArea(a.getAdminArea());
		this.setCountryCode(a.getCountryCode());
		this.setCountryName(a.getCountryName());
		this.setExtras(a.getExtras());
		this.setFeatureName(a.getFeatureName());
		this.setLatitude(a.getLatitude());
		this.setLocality(a.getLocality());
		this.setLongitude(a.getLongitude());
		this.setPhone(a.getPhone());
		this.setPostalCode(a.getPostalCode());
		this.setPremises(a.getPremises());
		this.setSubAdminArea(a.getSubAdminArea());
		this.setSubLocality(a.getSubLocality());
		this.setSubThoroughfare(a.getSubThoroughfare());
		this.setThoroughfare(a.getThoroughfare());
		this.setUrl(a.getUrl());
		
		if (getAdminArea() != null)
			Log.d("CUSTOMADDRESS", getAdminArea());
		if (getCountryCode() != null)
			Log.d("CUSTOMADDRESS", getCountryCode());
		if (getCountryName() != null)
			Log.d("CUSTOMADDRESS", getCountryName());
		if (getFeatureName() != null)
			Log.d("CUSTOMADDRESS", getFeatureName());
		if (getLatitude() != 0)
			Log.d("CUSTOMADDRESS", Double.toString(getLatitude()));
		if (getLocality() != null)
			Log.d("CUSTOMADDRESS", getLocality());
		if (getLongitude() != 0)
			Log.d("CUSTOMADDRESS", Double.toString(getLongitude()));
		if (getPhone() != null)
			Log.d("CUSTOMADDRESS", getPhone());
		if (getPostalCode() != null)
			Log.d("CUSTOMADDRESS", getPostalCode());
		if (getPremises() != null)
			Log.d("CUSTOMADDRESS", getPremises());
		if (getSubAdminArea() != null)
			Log.d("CUSTOMADDRESS", getSubAdminArea());
		if (getSubLocality() != null)
			Log.d("CUSTOMADDRESS", getSubLocality());
		if (getSubThoroughfare() != null)
			Log.d("CUSTOMADDRESS", getSubThoroughfare());
		if (getThoroughfare() != null)
			Log.d("CUSTOMADDRESS", getThoroughfare());
		if (getUrl() != null)
			Log.d("CUSTOMADDRESS", getUrl());
		
	}
	
	@Override
	public String toString(){
		if (this.getAddressLine(0) == null){
			return this.getFeatureName() + ", " + this.getLocality()  + ", " + this.getCountryName();
		} else {
			if ( this.getFeatureName().length() < this.getAddressLine(0).length()){
				return this.getAddressLine(0) + ", " + this.getLocality() + ", " + this.getCountryName();
			} else {
				return this.getFeatureName() + ", " + this.getLocality()  + ", " + this.getCountryName();
			}
		}
	}
}
