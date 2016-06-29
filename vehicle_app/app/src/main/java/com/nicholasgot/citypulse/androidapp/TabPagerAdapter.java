package com.nicholasgot.citypulse.androidapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class TabPagerAdapter extends FragmentStatePagerAdapter { 
	public TabPagerAdapter(FragmentManager fm) {
    super(fm);
    // TODO Auto-generated constructor stub
  }
 
  @Override
  public Fragment getItem(int i) {
    switch (i) {
        case 0:
            //Fragment for Route Tab
            return new TravelPlannerActivity();
        case 1:
            //Fragment for Parking Place Tab
//            return new ParkingPlacePlannerActivity();
        case 2:
            //Fragment for Settings Tab
            return new Settings();
        }
    return null;

  }
 
  @Override
  public int getCount() {
    // TODO Auto-generated method stub
    return 3; //No of Tabs
  }
}
