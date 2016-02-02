package com.group.nearme.adapter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.group.nearme.ContactListActivity;
import com.group.nearme.R;
import com.group.nearme.adapter.GroupItemAdapter.ViewHolder;
import com.group.nearme.objects.ContactInfoObject;

public class ContactListAdapter extends BaseAdapter {

	ContactListActivity context;
	public static boolean[] itemChecked;
	public ArrayList<String> selectedPhoneNo,selectedItemList;
	LayoutInflater inflater;
	static int selectcount = 0;
	ViewHolder holder;
	ArrayList<ContactInfoObject> objectList,searchList;
	HashMap<String, Boolean> checkedMap;
	public ContactListAdapter(ContactListActivity context,ArrayList<ContactInfoObject> objectList1,HashMap<String, Boolean> checkedMap1) {
			
		this.context = context;
		this.objectList = objectList1;
		this.checkedMap=checkedMap1;
		this.searchList = new ArrayList<ContactInfoObject>();
		this.searchList.addAll(objectList);
		inflater = context.getLayoutInflater();
		selectedItemList=new ArrayList<String>();
		selectedPhoneNo=new ArrayList<String>();
	}

	@Override
	public int getCount() {
		return objectList.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	/* private view holder class */
	private class ViewHolder {
		ImageView profile_pic;
		TextView member_name,mobile_no;
		CheckBox chk;
		
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.contact_list_item, null);
			holder = new ViewHolder();

			holder.member_name = (TextView) convertView
					.findViewById(R.id.member_name);
			holder.profile_pic = (ImageView) convertView
					.findViewById(R.id.profile_pic);
			
			holder.chk = (CheckBox) convertView.findViewById(R.id.chk);
			holder.mobile_no=(TextView) convertView.findViewById(R.id.mobile_no);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
	/*	// add or setting data's to listItem row
		if (objectList.get(position).getContactImage() != null) {
			//System.out.println(Uri.parse(imageList.get(position)));
			try {
				Bitmap bitmap = MediaStore.Images.Media.getBitmap(
						context.getContentResolver(),
						Uri.parse(objectList.get(position).getContactImage()));
				holder.profile_pic.setImageBitmap(bitmap);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/

		// holder.profile_pic.setImageResource(row_pos.getProfile_pic_id());
		holder.member_name.setText(objectList.get(position).getContactName());
		holder.mobile_no.setText(objectList.get(position).getContactNo());
		// holder.status.setText(row_pos.getStatus());
	//	chk.setChecked(false);

		//if (itemChecked[position])
			//chk.setChecked(true);
		//else
		//	chk.setChecked(false);
		holder.chk.setOnCheckedChangeListener(null);
		holder.chk.setChecked(checkedMap.get(objectList.get(position).getContactName()));

		/*chk.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				if (chk.isChecked()) {
					itemChecked[position] = true;
					selectcount++;

				} else {
					
					itemChecked[position] = false;
					selectcount--;

				}
				
				if(itemChecked[position])
				{
					TextView txt=new TextView(context);
					LayoutParams param=new  LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
					param.topMargin=10;
					txt.setLayoutParams(param);
					txt.setText(nameList.get(position));
					context.layout.addView(txt);
					
				}
				else
				{
					//context.layout.removeViewAt(selectedItemList.indexOf(nameList.get(position)));
					selectedItemList.remove(objectList.get(position).getContactName());
					selectedPhoneNo.remove(objectList.get(position).getContactNo());
					assignSelectedContact();
				}

			}
		});*/
		
		holder.chk.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				checkedMap.put(objectList.get(position).getContactName(), isChecked);
				System.out.println("inside setOnCheckedChangeListener");
				
				if(isChecked)
				{
					if(!selectedItemList.contains(objectList.get(position).getContactName()))
					{
					selectedItemList.add(objectList.get(position).getContactName());
					selectedPhoneNo.add(objectList.get(position).getContactNo());
					assignSelectedContact();
					}
					
				}
				else
				{
					selectedItemList.remove(objectList.get(position).getContactName());
					selectedPhoneNo.remove(objectList.get(position).getContactNo());
					assignSelectedContact();
				}
				
				/*if(objectList.size()!=searchList.size())
				{
					objectList.clear();
					objectList.addAll(searchList);
					
					notifyDataSetChanged();
				}*/
			}
		});

		return convertView;
	}
	
	private void assignSelectedContact()
	{
		String result=""; 
		for(int i=0;i<selectedItemList.size();i++)
		{
			if(result.isEmpty())
				result=selectedItemList.get(i);
			else
				result=result+", "+selectedItemList.get(i);
		}
		context.selectedContactTxt.setText(result);
		
	}

	static int getItemSelectCount() {

		return selectcount;
	}

	// Filter Class
	public void filter(String cs) {
		cs = cs.toLowerCase(Locale.getDefault());
		objectList.clear();
		if (cs.length() == 0) {
			objectList.addAll(searchList);
		} else {
			for (int i = 0; i < searchList.size(); i++) {
				if ((searchList.get(i).getContactName().toLowerCase(Locale.getDefault()))
						.contains(cs)) {
					objectList.add(searchList.get(i));
				}
			}
		}
		notifyDataSetChanged();
	}

}
