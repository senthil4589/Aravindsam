package com.group.nearme;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.group.nearme.settings.PreferenceSettings;
import com.group.nearme.util.Constants;
import com.group.nearme.util.Utility;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

public class NewGroupInfoActivity extends Activity implements android.view.View.OnClickListener{
	private ImageView mBackImg;//,mGroupImgView;
	private String mGroupId="",mGroupName="",mGroupImage,mMobileNo="",mGroupType="";
	private ParseImageView mGroupImgView;
	private TextView groupProfile,groupLocation,groupPermission,groupMembers,groupSettings;
	private TextView exitGroup,inviteMember,pendingApproval,mGroupNameTxtView,groupRules,myPost,pendingPost;
	ParseObject groupObject;
	boolean memberApproval;
	ProgressBar progressBar;
	Dialog mDialog;
	List<String> adminList=new ArrayList<String>();
	TextView adminSettingsLabel,myLabel,groupInfoLabel,invitationLabel;
	//View locationLine,accesPermissionLine,pendingApprovalLine,memberLine,groupSettingsLine,pendingPostLine;
	ArrayList<String> pendingInvitesIDList=new ArrayList<>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.group_info);
		Utility.getTracker(this, "GROUP INFO SCREEN");
		initViews();
		groupProfile.setOnClickListener(this);
		groupLocation.setOnClickListener(this);
		groupPermission.setOnClickListener(this);
		exitGroup.setOnClickListener(this);
		inviteMember.setOnClickListener(this);
		pendingApproval.setOnClickListener(this);
		groupMembers.setOnClickListener(this);
		groupSettings.setOnClickListener(this);
		groupRules.setOnClickListener(this);
		myPost.setOnClickListener(this);
		pendingPost.setOnClickListener(this);
	}
	
	private void initViews() {
		mBackImg=(ImageView) findViewById(R.id.back);
		mGroupImgView=(ParseImageView) findViewById(R.id.group_image);
		mGroupNameTxtView=(TextView) findViewById(R.id.group_name);
		groupMembers=(TextView) findViewById(R.id.members);
		groupProfile=(TextView) findViewById(R.id.group_profile);
		groupLocation=(TextView) findViewById(R.id.group_location);
		exitGroup=(TextView) findViewById(R.id.exit_group);
		pendingApproval=(TextView) findViewById(R.id.pending_approvals);
		groupPermission=(TextView) findViewById(R.id.group_access_permission);
		inviteMember=(TextView) findViewById(R.id.invite_members);
		groupSettings=(TextView) findViewById(R.id.group_settings);
		progressBar=(ProgressBar) findViewById(R.id.progressBar);
		
		groupRules=(TextView) findViewById(R.id.group_rules);
		myPost=(TextView) findViewById(R.id.my_post);
		pendingPost=(TextView) findViewById(R.id.pending_post);
		
		adminSettingsLabel=(TextView) findViewById(R.id.admin_settings_label);
		myLabel=(TextView) findViewById(R.id.my_label);
		groupInfoLabel=(TextView) findViewById(R.id.group_info_label);
		invitationLabel=(TextView) findViewById(R.id.invitation_label);
		
		myLabel.setTypeface(Utility.getTypeface(), Typeface.BOLD);
		groupInfoLabel.setTypeface(Utility.getTypeface(), Typeface.BOLD);
		invitationLabel.setTypeface(Utility.getTypeface(), Typeface.BOLD);
		adminSettingsLabel.setTypeface(Utility.getTypeface(), Typeface.BOLD);
		
		//locationLine=(View) findViewById(R.id.location_line);
		//pendingApprovalLine=(View) findViewById(R.id.pending_approval_line);
		//memberLine=(View) findViewById(R.id.members_line);
		//accesPermissionLine=(View) findViewById(R.id.access_permission_line);
		//groupSettingsLine=(View) findViewById(R.id.group_settings_line);
		
		mBackImg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				/*if(Utility.isRefershNeed)
				{
					startActivity(new Intent(NewGroupInfoActivity.this,TabGroupPostActivity.class));
				}*/
				finish();
				overridePendingTransition( R.anim.left_to_right_in, R.anim.left_to_right_out );
			}
		});
		
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition( R.anim.left_to_right_in, R.anim.left_to_right_out );
	/*	if(Utility.isRefershNeed)
		{
			startActivity(new Intent(NewGroupInfoActivity.this,TabGroupPostActivity.class));
			overridePendingTransition( R.anim.left_to_right_in, R.anim.left_to_right_out );
		}
		*/
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.group_profile:
			startActivity(new Intent(NewGroupInfoActivity.this,GroupProfileActivity.class).putExtra("flag", false));
			overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
			break;
		case R.id.group_location:
			startActivity(new Intent(NewGroupInfoActivity.this,GroupLocationActivity.class));
			overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
			break;		
		case R.id.group_access_permission:
			startActivity(new Intent(NewGroupInfoActivity.this,AccessPermissionActivity.class));
			overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
			break;
		case R.id.pending_approvals:
			/*if(Utility.getInvitationList().size() <=0)
			{
				Toast.makeText(NewGroupInfoActivity.this, "No Pending Approvals", Toast.LENGTH_LONG).show();
			}
			else
			{*/
				startActivity(new Intent(NewGroupInfoActivity.this,PendingApprovalActivity.class).putExtra(Constants.GROUP_ID, mGroupId));
				overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
			//}
			break;
		case R.id.invite_members:
			/*if(mGroupType.equals("Secret") || (!memberApproval && !adminList.contains(PreferenceSettings.getMobileNo())))
			{
				startActivity(new Intent(NewGroupInfoActivity.this,InviteSecretGroupActivity.class));
				overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
			}*/
			if(mGroupType.equals("Private") && adminList.contains(PreferenceSettings.getMobileNo()))
			{
				startActivity(new Intent(NewGroupInfoActivity.this,ContactListActivity.class));
				overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
			}
			else
			{
				startActivity(new Intent(NewGroupInfoActivity.this,ContactListActivity.class));
				overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
			}
			break;
		case R.id.exit_group:
			/*if(mGroupType.equals("Public"))
			{
				exitGroup.setEnabled(false);
				exitMember();
			}*/
			if(adminList.contains(PreferenceSettings.getMobileNo())) // if admin
			{
				if(groupObject.getInt(Constants.MEMBER_COUNT)>1 && adminList.size() ==1)
				{
					showDialog("You can not exit this group as you are the only admin. Make someone else the admin and then try again.","makeAdmin");
				}
				else if(groupObject.getInt(Constants.MEMBER_COUNT)>1 && adminList.size() >1)
				{
					showDialog("Are you sure you want to exit this group?","exitAdmin");
				}
				else if(groupObject.getInt(Constants.MEMBER_COUNT)==1 && adminList.size() ==1)
				{
					showDialog("Exiting this group will result in this group being deleted. Are you sure you want to exit?","deleteGroup");
				}
				
			}
			else // if member
			{
				showDialog("Are you sure you want to exit this group?","exitMember");
			}
			
			break;
		case R.id.members:
			startActivity(new Intent(NewGroupInfoActivity.this,MemberListActivity.class));
			overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
			break;
			
		case R.id.group_settings:
			startActivity(new Intent(NewGroupInfoActivity.this,GroupSettingsActivity.class));
			overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
			break;
			
		case R.id.group_rules:
			callGroupRulesActivity();	
			break;
			
		case R.id.my_post:
			openMyPosts();
			break;
			
		case R.id.pending_post:
			openPendingPost();
			break;

		default:
			break;
		}
		
	}
	
	private void openPendingPost()
	{
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
		query.whereEqualTo(Constants.GROUP_ID, mGroupId);
		query.whereEqualTo(Constants.POST_STATUS, "Pending");
		//query.whereEqualTo(Constants.POST_TYPE, "Pending");
		query.include(Constants.USER_ID);
		query.fromPin(mGroupId);
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> list, ParseException e) {
					if (e == null) 
					{
						if(list.size() > 0)
						{
							ArrayList<String> feedIdList=new ArrayList<String>();
							for(int i=0;i<list.size();i++)
							{
								feedIdList.add(list.get(i).getObjectId());
							}
							
							Intent i=new Intent(NewGroupInfoActivity.this,HashTagFeedActivity.class);
							i.putStringArrayListExtra(Constants.TAGGED_GROUP_FEED_ID_ARRAY,feedIdList);
							i.putExtra("hash_tags", "Pending Posts");
							startActivity(i);
							overridePendingTransition(R.anim.right_to_left_in,R.anim.right_to_left_out);
						}
						else
						{
							Utility.showToastMessage(NewGroupInfoActivity.this, "No Pending Posts");
						}
					}
			}});
	}
	
	private void callGroupRulesActivity()
	{
		progressBar.setVisibility(View.VISIBLE);
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
		query.whereEqualTo(Constants.GROUP_ID, mGroupId);
		//query.whereEqualTo(Constants.POST_STATUS, "InActive");
		query.whereEqualTo(Constants.POST_TYPE, "Rule");
		//query.fromPin(mGroupId);
		query.getFirstInBackground(new GetCallback<ParseObject>() {
			public void done(ParseObject object, ParseException e) {
				if (object != null) {
					progressBar.setVisibility(View.GONE);
					String str = object.getString(Constants.POST_TEXT);
					Intent i = new Intent(NewGroupInfoActivity.this, GroupRulesActivity.class);
					i.putExtra("Rules", str);
					i.putExtra("isFirstTime", false);
					i.putExtra(Constants.OBJECT_ID, object.getObjectId());
					startActivity(i);
					overridePendingTransition(R.anim.right_to_left_in, R.anim.right_to_left_out);

				} else {
					progressBar.setVisibility(View.GONE);
					String str = getResources().getString(R.string.group_rules);
					Intent i = new Intent(NewGroupInfoActivity.this, GroupRulesActivity.class);
					i.putExtra("Rules", str);
					i.putExtra("isFirstTime", true);
					startActivity(i);
					overridePendingTransition(R.anim.right_to_left_in, R.anim.right_to_left_out);

				}
			}
		});

		
	}

	
	private void openMyPosts()
	{
		ArrayList<String> typeList=new ArrayList<String>();
		typeList.add("Member");
		typeList.add("Invitation");
		typeList.add("Pending");
		
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
		query.whereEqualTo(Constants.GROUP_ID, mGroupId);
		query.whereEqualTo(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
		query.whereEqualTo(Constants.POST_STATUS, "Active");
		query.whereNotContainedIn(Constants.POST_TYPE, typeList);
		query.include(Constants.USER_ID);
		query.fromPin(mGroupId);
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> list, ParseException e) {
				if (e == null) {
					if (list.size() > 0) {
						ArrayList<String> feedIdList = new ArrayList<String>();
						for (int i = 0; i < list.size(); i++) {
							feedIdList.add(list.get(i).getObjectId());
						}

						Intent i = new Intent(NewGroupInfoActivity.this, HashTagFeedActivity.class);
						i.putStringArrayListExtra(Constants.TAGGED_GROUP_FEED_ID_ARRAY, feedIdList);
						i.putExtra("hash_tags", "My Posts");
						startActivity(i);
						overridePendingTransition(R.anim.right_to_left_in, R.anim.right_to_left_out);
					} else {
						Utility.showToastMessage(NewGroupInfoActivity.this, "No post available for slected tag");
					}
				}
			}
		});

	}
	
	private void showDialog(String msg,final String exitOption)
	{
		mDialog = new Dialog(NewGroupInfoActivity.this);
		mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mDialog.setContentView(R.layout.two_btn_dialog);
		mDialog.setCancelable(true);
		mDialog.setCanceledOnTouchOutside(true);
		
		WindowManager.LayoutParams windowManager = mDialog.getWindow().getAttributes();
		windowManager.gravity = Gravity.CENTER;
		Button yes=(Button) mDialog.findViewById(R.id.yes);
		Button no=(Button) mDialog.findViewById(R.id.no);
		TextView message=(TextView) mDialog.findViewById(R.id.msg);
		
		message.setText(msg);
		
		if(exitOption.equals("makeAdmin"))
		{
			no.setVisibility(View.GONE);
			yes.setText("OK");
		}
		else
		{
			no.setVisibility(View.VISIBLE);
		}
		
		yes.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mDialog.dismiss();

				if (exitOption.equals("exitMember")) {
					getPendingInvites();
					exitMember();
				} else if (exitOption.equals("exitAdmin")) {
					getPendingInvites();
					exitAdmin();
				} else if (exitOption.equals("deleteGroup")) {
					getPendingInvites();
					deleteGroup();
				} else if (exitOption.equals("makeAdmin")) {
					startActivity(new Intent(NewGroupInfoActivity.this, MemberListActivity.class));
					overridePendingTransition(R.anim.right_to_left_in, R.anim.right_to_left_out);
				}

			}
		});
		
		no.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mDialog.dismiss();
			}
		});
		mDialog.show();
	}

	private void getPendingInvites()
	{
		ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.INVITATION_TABLE);
		query.whereEqualTo(Constants.TO_USER, PreferenceSettings.getMobileNo());
		query.whereEqualTo(Constants.INVITATION_STATUS, "Active");
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> list, ParseException e) {
				pendingInvitesIDList=new ArrayList<String>();
				if (e == null) {
					if (list.size() > 0) {

						for (int i = 0; i < list.size(); i++) {
							pendingInvitesIDList.add(list.get(i).get(Constants.GROUP_ID).toString());
						}
					}
				}}});
	}
	
	private void setValues()
	{
		groupObject=Utility.getGroupObject();
		mGroupId=groupObject.getObjectId();
		mGroupName=groupObject.get(Constants.GROUP_NAME).toString();
		mGroupType=groupObject.get(Constants.GROUP_TYPE).toString();
		if(groupObject.getParseFile(Constants.THUMBNAIL_PICTURE)==null)
			mGroupImage=groupObject.getParseFile(Constants.GROUP_PICTURE).getUrl();
		else
			mGroupImage=groupObject.getParseFile(Constants.THUMBNAIL_PICTURE).getUrl();
//		mMobileNo=groupObject.get(Constants.MOBILE_NO).toString();
		
		memberApproval=groupObject.getBoolean(Constants.MEMBERSHIP_APPROVAL);
		
		adminList=groupObject.getList(Constants.ADMIN_ARRAY);
		
		if(adminList.contains(PreferenceSettings.getMobileNo()))
		{
			groupSettings.setVisibility(View.VISIBLE);
			//groupSettingsLine.setVisibility(View.VISIBLE);
			pendingPost.setVisibility(View.VISIBLE);
			//pendingApprovalLine.setVisibility(View.VISIBLE);
			adminSettingsLabel.setVisibility(View.VISIBLE);
		}
		else
		{
			groupSettings.setVisibility(View.GONE);
			//groupSettingsLine.setVisibility(View.VISIBLE);
			pendingPost.setVisibility(View.GONE);
			adminSettingsLabel.setVisibility(View.GONE);
			//pendingApprovalLine.setVisibility(View.GONE);
		}
		
		if(mGroupType.equals("Open"))
		{
			pendingApproval.setVisibility(View.GONE);
			groupPermission.setVisibility(View.GONE);
			//pendingApprovalLine.setVisibility(View.GONE);
			//accesPermissionLine.setVisibility(View.GONE);
		}
		else if(mGroupType.equals("Public"))
		{
			pendingApproval.setVisibility(View.GONE);
			groupPermission.setVisibility(View.GONE);
			groupMembers.setVisibility(View.GONE);
			//pendingApprovalLine.setVisibility(View.GONE);
			//accesPermissionLine.setVisibility(View.GONE);
			//memberLine.setVisibility(View.GONE);
			
		}
		else if(mGroupType.equals("Secret"))
		{
			groupPermission.setVisibility(View.GONE);
			groupLocation.setVisibility(View.GONE);
			pendingApproval.setVisibility(View.GONE);
			pendingPost.setVisibility(View.GONE);
			//pendingApprovalLine.setVisibility(View.GONE);
			//accesPermissionLine.setVisibility(View.GONE);
			//locationLine.setVisibility(View.GONE);
			
		}
		else if(mGroupType.equals("Private"))
		{
			if(memberApproval && !adminList.contains(PreferenceSettings.getMobileNo()))
			{
				groupPermission.setVisibility(View.GONE);
				pendingApproval.setVisibility(View.GONE);
				
				//pendingApprovalLine.setVisibility(View.GONE);
				//accesPermissionLine.setVisibility(View.GONE);
			}
			
			else if(!memberApproval && !adminList.contains(PreferenceSettings.getMobileNo()))
			{
				groupPermission.setVisibility(View.GONE);
				//accesPermissionLine.setVisibility(View.GONE);
			}
		}
		else
		{
			pendingApproval.setVisibility(View.GONE);
			groupPermission.setVisibility(View.GONE);
			//pendingApprovalLine.setVisibility(View.GONE);
			//accesPermissionLine.setVisibility(View.GONE);
		}
		/*else if(mGroupType.equals("Public"))
		{
			pendingApproval.setVisibility(View.GONE);
			groupPermission.setVisibility(View.GONE);
			inviteMember.setVisibility(View.GONE);
		}*/
		if(groupObject.getString(Constants.VISIBILITY)!=null)
		{
		if(groupObject.getString(Constants.VISIBILITY).equals("City"))
		{
			groupLocation.setVisibility(View.GONE);
			//locationLine.setVisibility(View.GONE);
		}
		}
		
		groupPermission.setVisibility(View.GONE);
		//accesPermissionLine.setVisibility(View.GONE);
		mGroupNameTxtView.setText(mGroupName);
		Picasso.with(this).load(mGroupImage).into(mGroupImgView);
	}
	
	
	private void exitMember()
	{
			progressBar.setVisibility(View.VISIBLE);
			
			ParseQuery<ParseObject> query1 = ParseQuery.getQuery(Constants.MEMBER_DETAIL_TABLE);
			query1.whereEqualTo(Constants.MEMBER_NO, PreferenceSettings.getMobileNo());
			query1.whereEqualTo(Constants.GROUP_ID, mGroupId);
			query1.getFirstInBackground(new GetCallback<ParseObject>() {
				public void done(ParseObject memberObject, ParseException e) {
					if (e == null) 
					{
						memberObject.put(Constants.MEMBER_STATUS, "InActive");
						memberObject.put(Constants.LEAVE_DATE, Utility.getCurrentDate());
						memberObject.put(Constants.EXIT_GROUP, true);
						memberObject.put(Constants.EXITED_BY, "Own");
						memberObject.saveInBackground();
					}
					
				}});
			
			ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.USER_TABLE);
			query.whereEqualTo(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
			query.getFirstInBackground(new GetCallback<ParseObject>() {
				public void done(ParseObject object, ParseException e) {
					if (e == null) 
					{
								List<String> myGroupList=object.getList(Constants.MY_GROUP_ARRAY);
									if(myGroupList.contains(mGroupId))
										myGroupList.remove(mGroupId);
									object.put(Constants.MY_GROUP_ARRAY, myGroupList);
									object.increment(Constants.BADGE_POINT, -100);
									object.pinInBackground(Constants.USER_LOCAL_DATA_STORE);
									object.saveInBackground(new SaveCallback() {
  							          public void done(ParseException e) {
 							                 if (e == null) {
 							                	 groupObject.increment(Constants.MEMBER_COUNT, -1);
 							                	 List<String> myGroupList=groupObject.getList(Constants.GROUP_MEMBERS);
 							                	if(myGroupList.contains(PreferenceSettings.getMobileNo()))
 													myGroupList.remove(PreferenceSettings.getMobileNo());
 							                	groupObject.put(Constants.GROUP_MEMBERS, myGroupList);
 							                	groupObject.pinInBackground(Constants.MY_GROUP_LOCAL_DATA_STORE);
 							                	groupObject.saveInBackground(new SaveCallback() {
 				  							          public void done(ParseException e) {
 			 							                 if (e == null) {
 			 							                	progressBar.setVisibility(View.GONE);
 			 							                	startActivity(new Intent(NewGroupInfoActivity.this,TabGroupActivity.class).putStringArrayListExtra("pending_invites_id",pendingInvitesIDList).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
 			 							                	finish();
 			 							                	/*ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
 			 							                	query.whereEqualTo(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
 			 							                	query.whereEqualTo(Constants.GROUP_ID, mGroupId);
 			 							                	query.findInBackground(new FindCallback<ParseObject>() {
 			 							      				public void done(List<ParseObject> list, ParseException e) {
 			 							      						if (e == null) 
 			 							      						{
 			 							      							if(list.size() > 0)
 			 							      							{
 			 							      								for(int i=0;i<list.size();i++)
 			 							      								{
 			 							      									list.get(i).put(Constants.POST_STATUS, "InActive");
 			 							      								}
 			 							      								ParseObject.pinAllInBackground(mGroupId,list);
 			 							      								ParseObject.saveAllInBackground(list, new SaveCallback() {
			 							   										public void done(ParseException e) {
			 							   											if (e == null) {
			 							   											progressBar.setVisibility(View.GONE);
			 			 					            							//Toast.makeText(NewGroupInfoActivity.this, "Group exited successfuly", Toast.LENGTH_LONG).show();
			 			 					            							startActivity(new Intent(NewGroupInfoActivity.this,TabGroupActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
			 			 					            							//overridePendingTransition( R.anim.left_to_right_in, R.anim.left_to_right_out );
			 			 					            							finish();
			 							   											}
			 							   										}});
 			 							      							}
 			 							      						}
 			 							      				}
 			 							      			});*/
 			 							                 }
 			 							                 else
 			 							                	progressBar.setVisibility(View.GONE);
 				  							          }});
 							                	
 							                 }
 							                else
	 							                progressBar.setVisibility(View.GONE);
  							          }});
							}
						else
			                progressBar.setVisibility(View.GONE);
						}
				
			});
	}
	
	private void deleteGroup()
	{
		progressBar.setVisibility(View.VISIBLE);
		ParseQuery<ParseObject> query1 = ParseQuery.getQuery(Constants.MEMBER_DETAIL_TABLE);
		query1.whereEqualTo(Constants.MEMBER_NO, PreferenceSettings.getMobileNo());
		query1.whereEqualTo(Constants.GROUP_ID, mGroupId);
		query1.getFirstInBackground(new GetCallback<ParseObject>() {
			public void done(ParseObject memberObject, ParseException e) {
				if (e == null) 
				{
					memberObject.deleteInBackground();
				}
			}});
			groupObject.deleteInBackground(new DeleteCallback() {
	          public void done(ParseException e) {
	                 if (e == null) {
	                	ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.USER_TABLE);
						query.whereEqualTo(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
						query.getFirstInBackground(new GetCallback<ParseObject>() {
							public void done(ParseObject object, ParseException e) {
								if (e == null) 
									{
											List<String> myGroupList=object.getList(Constants.MY_GROUP_ARRAY);
												if(myGroupList.contains(mGroupId))
													myGroupList.remove(mGroupId);
												object.put(Constants.MY_GROUP_ARRAY, myGroupList);
												object.increment(Constants.BADGE_POINT, -1000);
												object.pinInBackground(Constants.USER_LOCAL_DATA_STORE);
												object.saveInBackground(new SaveCallback() {
		      							          public void done(ParseException e) {
			 							                 if (e == null) {
			 							                	progressBar.setVisibility(View.GONE);
					            							//Toast.makeText(NewGroupInfoActivity.this, "Group exited successfuly", Toast.LENGTH_LONG).show();
					            							startActivity(new Intent(NewGroupInfoActivity.this, TabGroupActivity.class).putStringArrayListExtra("pending_invites_id", pendingInvitesIDList).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
					            							finish();
			 							                 }
		      							          }});
										}
									
							}
						});
	                 }
	          }
		});			
		
	}
	
	private void exitAdmin()
	{
		progressBar.setVisibility(View.VISIBLE);
		
		ParseQuery<ParseObject> query1 = ParseQuery.getQuery(Constants.MEMBER_DETAIL_TABLE);
		query1.whereEqualTo(Constants.MEMBER_NO, PreferenceSettings.getMobileNo());
		query1.whereEqualTo(Constants.GROUP_ID, mGroupId);
		query1.getFirstInBackground(new GetCallback<ParseObject>() {
			public void done(ParseObject memberObject, ParseException e) {
				if (e == null) 
				{
					memberObject.put(Constants.MEMBER_STATUS, "InActive");
					memberObject.put(Constants.LEAVE_DATE, Utility.getCurrentDate());
					memberObject.put(Constants.EXIT_GROUP, true);
					memberObject.put(Constants.EXITED_BY, "Own");
					memberObject.saveInBackground();
				}
				
			}});
		
		 groupObject.increment(Constants.MEMBER_COUNT, -1);
     	 List<String> myGroupList=groupObject.getList(Constants.GROUP_MEMBERS);
     	 if(myGroupList.contains(PreferenceSettings.getMobileNo()))
				myGroupList.remove(PreferenceSettings.getMobileNo());
     	 List<String> adminArray=groupObject.getList(Constants.ADMIN_ARRAY);
    	 if(adminArray.contains(PreferenceSettings.getMobileNo()))
    		 adminArray.remove(PreferenceSettings.getMobileNo());
     	 groupObject.put(Constants.ADMIN_ARRAY,adminArray);
     	 groupObject.put(Constants.GROUP_MEMBERS, myGroupList);
     	groupObject.pinInBackground(Constants.MY_GROUP_LOCAL_DATA_STORE);
     	 groupObject.saveInBackground(new SaveCallback() {
		          public void done(ParseException e) {
                 if (e == null) {
                	 ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.USER_TABLE);
         			query.whereEqualTo(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
         			query.getFirstInBackground(new GetCallback<ParseObject>() {
						public void done(ParseObject object, ParseException e) {
							if (e == null)
         						{
         							{
         								List<String> myGroupList=object.getList(Constants.MY_GROUP_ARRAY);
         									if(myGroupList.contains(mGroupId))
         										myGroupList.remove(mGroupId);
         									object.put(Constants.MY_GROUP_ARRAY, myGroupList);
         									object.increment(Constants.BADGE_POINT, -1000);
         									object.pinInBackground(Constants.USER_LOCAL_DATA_STORE);
         									object.saveInBackground(new SaveCallback() {
           							          public void done(ParseException e) {
          							                 if (e == null) {
          							                	progressBar.setVisibility(View.GONE);
					            							//Toast.makeText(NewGroupInfoActivity.this, "Group exited successfuly", Toast.LENGTH_LONG).show();
					            							startActivity(new Intent(NewGroupInfoActivity.this,TabGroupActivity.class).putStringArrayListExtra("pending_invites_id",pendingInvitesIDList).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
					            							//overridePendingTransition( R.anim.left_to_right_in, R.anim.left_to_right_out );
					            							finish();
				   											
          							                	/*ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.GROUP_FEED_TABLE);
			 							                	query.whereEqualTo(Constants.MOBILE_NO, PreferenceSettings.getMobileNo());
			 							                	query.whereEqualTo(Constants.GROUP_ID, mGroupId);
			 							                	query.findInBackground(new FindCallback<ParseObject>() {
			 							      				public void done(List<ParseObject> list, ParseException e) {
			 							      						if (e == null) 
			 							      						{
			 							      							if(list.size() > 0)
			 							      							{
			 							      								for(int i=0;i<list.size();i++)
			 							      								{
			 							      									list.get(i).put(Constants.POST_STATUS, "InActive");
			 							      								}
			 							      								ParseObject.pinAllInBackground(mGroupId,list);
			 							      								ParseObject.saveAllInBackground(list, new SaveCallback() {
		 							   										public void done(ParseException e) {
		 							   											if (e == null) {
		 							   											progressBar.setVisibility(View.GONE);
		 			 					            							//Toast.makeText(NewGroupInfoActivity.this, "Group exited successfuly", Toast.LENGTH_LONG).show();
		 			 					            							startActivity(new Intent(NewGroupInfoActivity.this,TabGroupActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
		 			 					            							//overridePendingTransition( R.anim.left_to_right_in, R.anim.left_to_right_out );
		 			 					            							finish();
		 							   											}
		 							   										}});
			 							      							}
			 							      						}
			 							      				}
			 							      			});
*/          							                 }
           							          }});
         							}
         						}
         				}});
                 }
		      }});
     	 
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		setValues();
	}
	
	 @Override
		protected void onStart() {
			super.onStart();
			GoogleAnalytics.getInstance(this).reportActivityStart(this);
		}

		@Override
		protected void onStop() {
			GoogleAnalytics.getInstance(this).reportActivityStop(this);
			super.onStop();
		}
		
	
	
}
