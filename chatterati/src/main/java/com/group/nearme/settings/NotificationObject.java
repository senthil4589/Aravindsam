package com.group.nearme.settings;

public class NotificationObject 
{
	private String text,image,groupId,feedId,groupType,mobileNo,type,time;//,flag;
	
	public NotificationObject(String text,String imgUrl,String feedid,String groupid,String grouptype,String no,
			String type,String time)
	{
		this.text=text;
		this.image=imgUrl;
		this.feedId=feedid;
		this.groupId=groupid;
		this.groupType=grouptype;
		this.mobileNo=no;
		this.type=type;
		this.time=time;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getFeedId() {
		return feedId;
	}

	public void setFeedId(String feedId) {
		this.feedId = feedId;
	}

	public String getGroupType() {
		return groupType;
	}

	public void setGroupType(String groupType) {
		this.groupType = groupType;
	}

	public String getMobileNo() {
		return mobileNo;
	}

	public void setMobileNo(String mobileNo) {
		this.mobileNo = mobileNo;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

}
