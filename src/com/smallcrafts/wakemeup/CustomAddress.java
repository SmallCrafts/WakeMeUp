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
		
		maxLines = this.getMaxAddressLineIndex();
		
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
