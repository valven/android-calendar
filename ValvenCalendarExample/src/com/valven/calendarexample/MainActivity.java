package com.valven.calendarexample;

import java.text.DateFormat;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.valven.calendarexample.component.calendar.CustomCalendar;
import com.valven.calendarexample.component.calendar.CustomCalendar.DateChangeListener;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		((CustomCalendar) findViewById(R.id.calendar))
				.setOnDateChangeListener(new DateChangeListener() {

					@Override
					public void onSelectedDateChange(CustomCalendar cal,
							int day, int month, int year) {
						Toast.makeText(MainActivity.this,
								day + "-" + month + "-" + year,
								Toast.LENGTH_SHORT).show();
					}
				});

		findViewById(R.id.ok).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				CustomCalendar myCalendar = (CustomCalendar) findViewById(R.id.calendar);

				if (myCalendar.isDateSelected()) {
					Toast.makeText(
							MainActivity.this,
							DateFormat.getDateInstance().format(
									myCalendar.getDate()), Toast.LENGTH_SHORT)
							.show();
				} else {
					Toast.makeText(MainActivity.this, R.string.not_selected,
							Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
}
