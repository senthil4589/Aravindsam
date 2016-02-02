package com.group.nearme.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import com.parse.ParseFile;
import com.parse.ParseGeoPoint;

public class GroupInfo{
	
	private String groupName="",groupDes="",groupType="";
	private ParseFile groupLargeImage,groupThumbnailImage;
	private String groupPurpose="",groupCategory="";
	private ArrayList<String> tagsList=new ArrayList<String>();
	private ArrayList<String> rangeList=new ArrayList<String>();
	private boolean postapproval,memberInvitation,membershipApproval;
	private int whoCanPost,whoCanComment;
	private Date groupVisibleTilDate=new Date();
	private ParseGeoPoint locationPoint= new ParseGeoPoint();

	/* for business group */
	private boolean ifBusinessGroup;
	private String businessName,businessBranch,businessAddress,businessPhoneNo;
	private ParseGeoPoint businessLocationPoint= new ParseGeoPoint();

	public ParseGeoPoint getBusinessLocationPoint() {
		return businessLocationPoint;
	}

	public void setBusinessLocationPoint(ParseGeoPoint businessLocationPoint) {
		this.businessLocationPoint = businessLocationPoint;
	}

	public String getBusinessPhoneNo() {
		return businessPhoneNo;
	}

	public void setBusinessPhoneNo(String businessPhoneNo) {
		this.businessPhoneNo = businessPhoneNo;
	}

	public String getBusinessAddress() {
		return businessAddress;
	}

	public void setBusinessAddress(String businessAddress) {
		this.businessAddress = businessAddress;
	}

	public String getBusinessBranch() {
		return businessBranch;
	}

	public void setBusinessBranch(String businessBranch) {
		this.businessBranch = businessBranch;
	}

	public String getBusinessName() {
		return businessName;
	}

	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}

	public boolean isIfBusinessGroup() {
		return ifBusinessGroup;
	}

	public void setIfBusinessGroup(boolean ifBusinessGroup) {
		this.ifBusinessGroup = ifBusinessGroup;
	}






	
	public boolean isPostapproval() {
		return postapproval;
	}
	public void setPostapproval(boolean postapproval) {
		this.postapproval = postapproval;
	}
	public boolean isMemberInvitation() {
		return memberInvitation;
	}
	public void setMemberInvitation(boolean memberInvitation) {
		this.memberInvitation = memberInvitation;
	}
	public boolean isMembershipApproval() {
		return membershipApproval;
	}
	public void setMembershipApproval(boolean membershipApproval) {
		this.membershipApproval = membershipApproval;
	}
	public int getWhoCanPost() {
		return whoCanPost;
	}
	public void setWhoCanPost(int whoCanPost) {
		this.whoCanPost = whoCanPost;
	}
	public int getWhoCanComment() {
		return whoCanComment;
	}
	public void setWhoCanComment(int whoCanComment) {
		this.whoCanComment = whoCanComment;
	}
	public Date getGroupVisibleTilDate() {
		return groupVisibleTilDate;
	}
	public void setGroupVisibleTilDate(Date groupVisibleTilDate) {
		this.groupVisibleTilDate = groupVisibleTilDate;
	}
	public ParseGeoPoint getLocationPoint() {
		return locationPoint;
	}
	public void setLocationPoint(ParseGeoPoint locationPoint) {
		this.locationPoint = locationPoint;
	}
	
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public String getGroupDes() {
		return groupDes;
	}
	public void setGroupDes(String groupDes) {
		this.groupDes = groupDes;
	}
	public String getGroupType() {
		return groupType;
	}
	public void setGroupType(String groupType) {
		this.groupType = groupType;
	}
	public ParseFile getGroupLargeImage() {
		return groupLargeImage;
	}
	public void setGroupLargeImage(ParseFile groupLargeImage) {
		this.groupLargeImage = groupLargeImage;
	}
	public ParseFile getGroupThumbnailImage() {
		return groupThumbnailImage;
	}
	public void setGroupThumbnailImage(ParseFile groupThumbnailImage) {
		this.groupThumbnailImage = groupThumbnailImage;
	}
	public String getGroupPurpose() {
		return groupPurpose;
	}
	public void setGroupPurpose(String groupPurpose) {
		this.groupPurpose = groupPurpose;
	}
	public String getGroupCategory() {
		return groupCategory;
	}
	public void setGroupCategory(String groupCategory) {
		this.groupCategory = groupCategory;
	}
	public ArrayList<String> getTagsList() {
		return tagsList;
	}
	public void setTagsList(ArrayList<String> tagsList) {
		this.tagsList = tagsList;
	}
	public ArrayList<String> getRangeList() {
		return rangeList;
	}
	public void setRangeList(ArrayList<String> rangeList) {
		this.rangeList = rangeList;
	}
	

}
