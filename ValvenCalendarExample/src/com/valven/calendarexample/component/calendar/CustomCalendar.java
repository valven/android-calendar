package com.valven.calendarexample.component.calendar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.valven.calendarexample.R;
import com.valven.calendarexample.component.listener.SwipeListener;
import com.valven.calendarexample.component.listener.SwipeListener.SwipeCallback;

@SuppressLint("SimpleDateFormat")
@TargetApi(3)
public class CustomCalendar extends LinearLayout implements OnClickListener {

	public CustomCalendar(Context context) {
		this(context, null);
	}

	public CustomCalendar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public CustomCalendar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs);
	}

	private static final String tag = Calendar.class.getSimpleName();

	private static final String KEY_SELECTED = "SELECTED";
	private static final String KEY_TODAY = "TODAY";
	private static final String KEY_DISABLED = "DISABLED";
	private static final String KEY_DEFAULT = "DEFAULT";
	private static final String KEY_OTHER = "OTHER";

	private TextView currentMonth;
	private ImageView prevMonth;
	private ImageView nextMonth;
	private GridView calendarView;
	private GridCellAdapter adapter;
	private Calendar _calendar;
	private int start = 1;
	@SuppressLint("NewApi")
	private int day, month, year;
	private int selectedMonth, selectedDay, selectedYear;
	private int thisDay, thisMonth, thisYear;
	private DateChangeListener dateChangeListener;
	private LayoutInflater inflater;
	private boolean dateSelected;
	private boolean canSelectBeforeToday;
	private int todayBackgroundColor;
	private int todayTextColor;
	private int selectedBackgroundColor;
	private int selectedTextColor;
	private int defaultTextColor;
	private int disabledTextColor;
	private int otherTextColor;

	@SuppressLint({ "NewApi", "NewApi", "NewApi", "NewApi" })
	private final DateFormat dateFormatter = new DateFormat();
	private static final String dateTemplate = "MMMM yyyy";

	private final int[] daysOfMonth = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31,
			30, 31 };
	private final String[] weekdays = new String[7];
	private final String[] months = new String[12];

	private static final long DAY = 24 * 60L * 60L * 1000L;

	public static long diffDays(long time) {
		Calendar then = Calendar.getInstance();
		then.setTimeInMillis(time);
		then.set(Calendar.MILLISECOND, 0);
		then.set(Calendar.SECOND, 0);
		then.set(Calendar.MINUTE, 0);
		then.set(Calendar.HOUR_OF_DAY, 0);

		Calendar now = Calendar.getInstance();
		now.set(Calendar.MILLISECOND, 0);
		now.set(Calendar.SECOND, 0);
		now.set(Calendar.MINUTE, 0);
		now.set(Calendar.HOUR_OF_DAY, 0);

		return (then.getTimeInMillis() / DAY - now.getTimeInMillis() / DAY);
	}

	private SwipeListener swipeListener = new SwipeListener(
			new SwipeCallback() {

				@Override
				public void onTopToBottomSwipe(View view) {
				}

				@Override
				public void onRightToLeftSwipe(View view) {
					nextMonth.performClick();
				}

				@Override
				public void onLeftToRightSwipe(View view) {
					prevMonth.performClick();
				}

				@Override
				public void onBottomToTopSwipe(View view) {
				}
			});

	/** Called when the activity is first created. */
	private void init(AttributeSet attrs) {
		inflater = LayoutInflater.from(getContext());
		inflater.inflate(R.layout.calendar, this, true);

		// init variables
		for (int i = 0; i < 12; i++) {
			months[i] = DateUtils.getMonthString(i, DateUtils.LENGTH_MEDIUM);
		}

		for (int i = 0; i < 7; i++) {
			weekdays[i] = DateUtils.getDayOfWeekString(i + 1,
					DateUtils.LENGTH_MEDIUM);
		}

		_calendar = Calendar.getInstance(Locale.getDefault());
		dateSelected = true; // today is selected by default
		thisDay = selectedDay = day = _calendar.get(Calendar.DAY_OF_MONTH);
		thisMonth = selectedMonth = month = _calendar.get(Calendar.MONTH) + 1;
		thisYear = selectedYear = year = _calendar.get(Calendar.YEAR);
		Log.d(tag, "Calendar Instance:= " + "Month: " + month + " " + "Year: "
				+ year);

		// init UI
		prevMonth = (ImageView) this.findViewById(R.id.prevMonth);
		prevMonth.setOnClickListener(this);

		currentMonth = (TextView) this.findViewById(R.id.currentMonth);
		currentMonth.setText(DateFormat.format(dateTemplate,
				_calendar.getTime()));

		nextMonth = (ImageView) this.findViewById(R.id.nextMonth);
		nextMonth.setOnClickListener(this);

		if (attrs != null) {
			TypedArray a = getContext().obtainStyledAttributes(attrs,
					R.styleable.Calendar, 0, 0);

			Resources res = getResources();

			this.canSelectBeforeToday = a.getBoolean(
					R.styleable.Calendar_selectBeforeToday, true);

			this.todayBackgroundColor = (a.getInt(
					R.styleable.Calendar_todayBackgroundColor,
					res.getColor(R.color.lightgray)));
			this.todayTextColor = (a.getInt(
					R.styleable.Calendar_todayTextColor,
					res.getColor(R.color.blue)));

			this.selectedBackgroundColor = (a.getInt(
					R.styleable.Calendar_selectedBackgroundColor,
					res.getColor(R.color.darkorange)));
			this.selectedTextColor = (a.getInt(
					R.styleable.Calendar_selectedTextColor,
					res.getColor(R.color.white)));

			this.defaultTextColor = (a.getInt(
					R.styleable.Calendar_defaultTextColor,
					res.getColor(R.color.black)));

			this.disabledTextColor = (a.getInt(
					R.styleable.Calendar_disabledTextColor,
					res.getColor(R.color.lightgray)));

			this.otherTextColor = (a.getInt(
					R.styleable.Calendar_otherTextColor,
					res.getColor(R.color.lightgray)));
			a.recycle();
		}

		calendarView = (GridView) this.findViewById(R.id.days);

		adapter = new GridCellAdapter(getContext().getApplicationContext(),
				R.id.calendar_day_gridcell, month, year);
		adapter.notifyDataSetChanged();
		calendarView.setAdapter(adapter);
	}

	public void setOnDateChangeListener(DateChangeListener listener) {
		this.dateChangeListener = listener;
	}

	public void setDate(long timestamp) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp);
		selectedDay = day = cal.get(Calendar.DAY_OF_MONTH);
		selectedMonth = month = cal.get(Calendar.MONTH) + 1;
		selectedYear = year = cal.get(Calendar.YEAR);

		if (!canSelectBeforeToday) {
			if (diffDays(cal.getTimeInMillis()) < 0) {
				selectedDay = thisDay;
				selectedMonth = thisMonth;
				selectedYear = thisYear;
			}
		}

		setGridCellAdapterToDate(selectedDay, selectedMonth, selectedYear);
	}

	public Date getDate() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, selectedDay);
		cal.set(Calendar.MONTH, selectedMonth - 1);
		cal.set(Calendar.YEAR, selectedYear);
		return cal.getTime();
	}

	private void setGridCellAdapterToDate(int day, int month, int year) {

		adapter = new GridCellAdapter(getContext().getApplicationContext(),
				R.id.calendar_day_gridcell, month, year);
		_calendar.set(year, month - 1, day);
		currentMonth.setText(DateFormat.format(dateTemplate,
				_calendar.getTime()));
		adapter.notifyDataSetChanged();
		calendarView.setAdapter(adapter);
	}

	@Override
	public void onClick(View v) {
		if (v == prevMonth) {
			if (month == thisMonth && year == thisYear) {
				return;
			}
			if (month <= 1) {
				month = 12;
				year--;
			} else {
				month--;
			}
			Log.d(tag, "Setting Prev Month in GridCellAdapter: " + "Month: "
					+ month + " Year: " + year);
			setGridCellAdapterToDate(day, month, year);
		}
		if (v == nextMonth) {
			if (month > 11) {
				month = 1;
				year++;
			} else {
				month++;
			}
			Log.d(tag, "Setting Next Month in GridCellAdapter: " + "Month: "
					+ month + " Year: " + year);
			setGridCellAdapterToDate(day, month, year);
		}
	}

	private String getMonthAsString(int i) {
		return months[i];
	}

	private String getWeekDayAsString(int i) {
		return weekdays[i];
	}

	private int getNumberOfDaysOfMonth(int i) {
		return daysOfMonth[i];
	}

	// Inner Class
	public class GridCellAdapter extends BaseAdapter implements OnClickListener {
		private static final String tag = "GridCellAdapter";

		private final List<String> list;
		private static final int DAY_OFFSET = 1;

		private int daysInMonth;
		private int currentDayOfMonth;
		private int currentWeekDay;
		private Button gridcell;
		private final SimpleDateFormat dateFormatter = new SimpleDateFormat(
				"dd-MM-yyyy");

		// Days in Current Month
		public GridCellAdapter(Context context, int textViewResourceId,
				int month, int year) {
			super();

			this.list = new ArrayList<String>();

			Log.d(tag, "==> Passed in Date FOR Month: " + month + " "
					+ "Year: " + year);
			Calendar calendar = Calendar.getInstance();
			setCurrentDayOfMonth(calendar.get(Calendar.DAY_OF_MONTH));
			setCurrentWeekDay(calendar.get(Calendar.DAY_OF_WEEK));
			Log.d(tag, "New Calendar:= " + calendar.getTime().toString());
			Log.d(tag, "CurrentDayOfWeek :" + getCurrentWeekDay());
			Log.d(tag, "CurrentDayOfMonth :" + getCurrentDayOfMonth());

			// Print Month
			displayMonth(month, year);

		}

		public String getItem(int position) {
			return list.get(position);
		}

		@Override
		public int getCount() {
			return list.size();
		}

		/**
		 * Shows given month in given year
		 * 
		 * @param mm
		 * @param yy
		 */
		private void displayMonth(int mm, int yy) {
			Log.d(tag, "==> displayMonth: mm: " + mm + " " + "yy: " + yy);
			int trailingSpaces = 0;
			int daysInPrevMonth = 0;
			int prevMonth = 0;
			int prevYear = 0;
			int nextMonth = 0;
			int nextYear = 0;

			int currentMonth = mm - 1;
			String currentMonthName = getMonthAsString(currentMonth);
			daysInMonth = getNumberOfDaysOfMonth(currentMonth);

			Log.d(tag, "Current Month: " + " " + currentMonthName + " having "
					+ daysInMonth + " days.");

			GregorianCalendar cal = new GregorianCalendar(yy, currentMonth, 1);
			Log.d(tag, "Gregorian Calendar:= " + cal.getTime().toString());

			if (currentMonth == 11) {
				prevMonth = currentMonth - 1;
				daysInPrevMonth = getNumberOfDaysOfMonth(prevMonth);
				nextMonth = 0;
				prevYear = yy;
				nextYear = yy + 1;
				Log.d(tag, "*->PrevYear: " + prevYear + " PrevMonth:"
						+ prevMonth + " NextMonth: " + nextMonth
						+ " NextYear: " + nextYear);
			} else if (currentMonth == 0) {
				prevMonth = 11;
				prevYear = yy - 1;
				nextYear = yy;
				daysInPrevMonth = getNumberOfDaysOfMonth(prevMonth);
				nextMonth = 1;
				Log.d(tag, "**--> PrevYear: " + prevYear + " PrevMonth:"
						+ prevMonth + " NextMonth: " + nextMonth
						+ " NextYear: " + nextYear);
			} else {
				prevMonth = currentMonth - 1;
				nextMonth = currentMonth + 1;
				nextYear = yy;
				prevYear = yy;
				daysInPrevMonth = getNumberOfDaysOfMonth(prevMonth);
				Log.d(tag, "***---> PrevYear: " + prevYear + " PrevMonth:"
						+ prevMonth + " NextMonth: " + nextMonth
						+ " NextYear: " + nextYear);
			}

			int currentWeekDay = cal.get(Calendar.DAY_OF_WEEK) - 1;
			trailingSpaces = currentWeekDay - start;

			Log.d(tag, "Week Day:" + currentWeekDay + " is "
					+ getWeekDayAsString(currentWeekDay));
			Log.d(tag, "No. Trailing space to Add: " + trailingSpaces);
			Log.d(tag, "No. of Days in Previous Month: " + daysInPrevMonth);

			if (cal.isLeapYear(cal.get(Calendar.YEAR)))
				if (mm == 2)
					++daysInMonth;
				else if (mm == 3)
					++daysInPrevMonth;

			// header
			for (int i = start; i < 7 + start; i++) {
				list.add(getWeekDayAsString(i % 7));
			}

			// Trailing Month days
			for (int i = 0; i < trailingSpaces; i++) {
				Log.d(tag,
						"PREV MONTH:= "
								+ prevMonth
								+ " => "
								+ getMonthAsString(prevMonth)
								+ " "
								+ String.valueOf((daysInPrevMonth
										- trailingSpaces + DAY_OFFSET)
										+ i));
				list.add(String
						.valueOf((daysInPrevMonth - trailingSpaces + DAY_OFFSET)
								+ i)
						+ "-"
						+ KEY_OTHER
						+ "-"
						+ (prevMonth + 1)
						+ "-"
						+ prevYear);
			}

			// Current Month Days
			for (int i = 1; i <= daysInMonth; i++) {
				// Log.d(currentMonthName,
				// i+" "+selectedDay+" - "+currentMonth+" "+selectedMonth+" "+thisMonth);
				if (i == selectedDay && currentMonth == selectedMonth - 1
						&& yy == selectedYear) {
					list.add(String.valueOf(i) + "-" + KEY_SELECTED + "-"
							+ (currentMonth + 1) + "-" + yy);
				} else if (currentMonth == thisMonth - 1 && yy == thisYear) {
					if (i == thisDay) {
						list.add(String.valueOf(i) + "-" + KEY_TODAY + "-"
								+ (currentMonth + 1) + "-" + yy);
					} else if (!canSelectBeforeToday && i < thisDay) {
						list.add(String.valueOf(i) + "-" + KEY_DISABLED + "-"
								+ (currentMonth + 1) + "-" + yy);
					} else {
						list.add(String.valueOf(i) + "-" + KEY_DEFAULT + "-"
								+ (currentMonth + 1) + "-" + yy);
					}
				} else {
					list.add(String.valueOf(i) + "-" + KEY_DEFAULT + "-"
							+ (currentMonth + 1) + "-" + yy);
				}
			}

			// Leading Month days
			for (int i = 0; i < list.size() % 7; i++) {
				Log.d(tag, "NEXT MONTH:= " + getMonthAsString(nextMonth));
				list.add(String.valueOf(i + 1) + "-" + KEY_OTHER + "-"
						+ (nextMonth + 1) + "-" + nextYear);
			}
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		private View getHeaderView(int position, View view, ViewGroup parent) {
			Button cell = (Button) view.findViewById(R.id.header_cell);
			cell.setText(getWeekDayAsString((position + start) % 7));
			return view;
		}

		private View getDayView(int position, View view, ViewGroup parent) {
			// Get a reference to the Day gridcell
			gridcell = (Button) view.findViewById(R.id.calendar_day_gridcell);
			gridcell.setOnClickListener(this);

			String[] day_color = list.get(position).split("-");
			String theday = day_color[0];
			String themonth = day_color[2];
			String theyear = day_color[3];

			// Set the Day GridCell
			gridcell.setText(theday);
			gridcell.setTag(theday + "-" + themonth + "-" + theyear);

			if (day_color[1].equals(KEY_DISABLED)) {
				gridcell.setTextColor(disabledTextColor);
			} else if (day_color[1].equals(KEY_OTHER)) {
				gridcell.setTextColor(otherTextColor);
			} else if (day_color[1].equals(KEY_DEFAULT)) {
				gridcell.setTextColor(defaultTextColor);
			} else if (day_color[1].equals(KEY_TODAY)) {
				gridcell.setBackgroundColor(todayBackgroundColor);
				gridcell.setTextColor(todayTextColor);
			} else if (day_color[1].equals(KEY_SELECTED)) {
				gridcell.setBackgroundColor(selectedBackgroundColor);
				gridcell.setTextColor(selectedTextColor);
			}
			return view;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			// Log.d(tag,
			// "getView : "+position+" "+getItemViewType(position)+" "+list.get(position));
			if (getItemViewType(position) == 1) {
				if (row == null) {
					row = inflater.inflate(R.layout.calendar_header, parent,
							false);
				}
				return getHeaderView(position, row, parent);
			} else {
				if (row == null) {
					row = inflater.inflate(R.layout.calendar_cell, parent,
							false);
				}
				return getDayView(position, row, parent);
			}
		}

		@Override
		public int getItemViewType(int position) {
			if (position < 7) {
				return 1;
			} else {
				return 2;
			}
		}

		public int getViewTypeCount() {
			return 2;
		}

		@Override
		public void onClick(View view) {
			String date_month_year = (String) view.getTag();
			Log.e("Selected date", date_month_year);
			try {
				Date parsedDate = dateFormatter.parse(date_month_year);
				Calendar cal = Calendar.getInstance();
				cal.setTime(parsedDate);
				int day = cal.get(Calendar.DAY_OF_MONTH);
				int month = cal.get(Calendar.MONTH) + 1;
				int year = cal.get(Calendar.YEAR);
				Log.d(tag, "Parsed Date: " + parsedDate.toString() + " " + day
						+ " " + month + " " + year);
				if (selectedDay == day && selectedMonth == month
						&& selectedYear == year) {
					selectedDay = selectedMonth = selectedYear = 0;
					dateSelected = false;
					setGridCellAdapterToDate(thisDay, thisMonth, thisYear);
				} else {
					dateSelected = true;
					if (canSelectBeforeToday || diffDays(cal.getTimeInMillis()) >= 0) {
						selectedDay = day;
						selectedMonth = month;
						selectedYear = year;
						Log.d(tag, "diff >= 0 " + cal.getTime());
						if (dateChangeListener != null) {
							dateChangeListener.onSelectedDateChange(
									CustomCalendar.this, day, month, year);
						}
						setGridCellAdapterToDate(day, month, year);
					}
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		public int getCurrentDayOfMonth() {
			return currentDayOfMonth;
		}

		private void setCurrentDayOfMonth(int currentDayOfMonth) {
			this.currentDayOfMonth = currentDayOfMonth;
		}

		public void setCurrentWeekDay(int currentWeekDay) {
			this.currentWeekDay = currentWeekDay;
		}

		public int getCurrentWeekDay() {
			return currentWeekDay;
		}
	}

	public interface DateChangeListener {
		public void onSelectedDateChange(CustomCalendar cal, int day,
				int month, int year);
	}

	public boolean isDateSelected() {
		return dateSelected;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		swipeListener.onTouch(this, event);
		return false;
	}
}