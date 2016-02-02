package com.group.nearme.objects;

import java.util.HashMap;

public class ContactInfoObject {
	
	private String contactName,contactNo,contactImage;
	private HashMap<String, Boolean> selectedContactMap;

	public HashMap<String, Boolean> getSelectedContactMap() {
		return selectedContactMap;
	}

	public void setSelectedContactMap(HashMap<String, Boolean> selectedContactMap) {
		this.selectedContactMap = selectedContactMap;
	}

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public String getContactNo() {
		return contactNo;
	}

	public void setContactNo(String contactNo) {
		this.contactNo = contactNo;
	}

	public String getContactImage() {
		return contactImage;
	}

	public void setContactImage(String contactImage) {
		this.contactImage = contactImage;
	}
	

}
