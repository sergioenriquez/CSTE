package org.fern.rest.android.task;

import org.fern.rest.android.Duration;
import org.fern.rest.android.NumberUtils;
import org.fern.rest.android.R;
import org.fern.rest.android.dataObj.DataEventReceiver;
import org.fern.rest.android.dataObj.DataHandler;
import org.fern.rest.android.user.User;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

public class TaskEditActivity extends Activity {
	private Task mTask;
	private EditText mNameView;
	private EditText mStatusView;
	private SeekBar mProgressView;
	private EditText mProgressDiscreteView;
	private Spinner mPriorityView;
	private EditText mDetailsView;
	private MultiAutoCompleteTextView mTagsView;
	private Button mActivationDateButton;
	private Button mActivationTimeButton;
	private Button mExpirationDateButton;
	private Button mExpirationTimeButton;
	private Button mDurationDateButton;
	private Button mDurationTimeButton;
	private DatePickerDialog mActivationDateDialog;
	private DatePickerDialog mExpirationDateDialog;
	private DatePickerDialog mDurationDateDialog;
	private TimePickerDialog mActivationTimeDialog;
	private TimePickerDialog mExpirationTimeDialog;
	private TimePickerDialog mDurationTimeDialog;
	private SimpleDateFormat mDateFormatter;
	private SimpleDateFormat mTimeFormatter;
	private Calendar mActivationCalendar;
	private Calendar mExpirationCalendar;
	private Calendar mDurationCalendar;
	private Button mSubmit;
	private Intent mIntent;
	private User mUser;
	private ProgressDialog mProgressDialog;
	private DataHandler mDataHandler;
	private static final int ACTIVATION_DATE_PICKER_DIALOG = 0;
	private static final int ACTIVATION_TIME_PICKER_DIALOG = 1;
	private static final int EXPIRATION_DATE_PICKER_DIALOG = 2;
	private static final int EXPIRATION_TIME_PICKER_DIALOG = 3;
	private static final int DURATION_DATE_PICKER_DIALOG = 4;
	private static final int DURATION_TIME_PICKER_DIALOG = 5;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		mIntent = getIntent();

		mUser = (User) mIntent.getParcelableExtra("user");
		if (mUser == null)
			return; // user info needed to anthenticate with server

		mTask = (Task) mIntent.getParcelableExtra("task");
		if (mTask == null)
			mTask = new Task();

		mDateFormatter = new SimpleDateFormat("EE, MMM d, yyyy");

		mProgressDialog = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);
		mProgressDialog.setTitle("Saving settings");

		if (DateFormat.is24HourFormat(this)) {
			mTimeFormatter = new SimpleDateFormat("kk:mm");
		} else {
			mTimeFormatter = new SimpleDateFormat("h:mm aa");
		}

		setContentView(R.layout.task_edit_layout);

		mNameView = (EditText) findViewById(R.id.taskEditName);
		mStatusView = (EditText) findViewById(R.id.taskEditStatus);
		mProgressView = (SeekBar) findViewById(R.id.taskEditProgress);
		mProgressDiscreteView = (EditText) findViewById(R.id.taskEditProgressValue);
		mPriorityView = (Spinner) findViewById(R.id.taskEditPrioritySelected);
		mDetailsView = (EditText) findViewById(R.id.taskEditDetails);
		mTagsView = (MultiAutoCompleteTextView) findViewById(R.id.taskEditTags);
		mActivationDateButton = (Button) findViewById(R.id.taskEditActivationDate);
		mActivationTimeButton = (Button) findViewById(R.id.taskEditActivationTime);
		mExpirationDateButton = (Button) findViewById(R.id.taskEditExpirationDate);
		mExpirationTimeButton = (Button) findViewById(R.id.taskEditExpirationTime);
		mDurationDateButton = (Button) findViewById(R.id.taskEditDurationDate);
		mDurationTimeButton = (Button) findViewById(R.id.taskEditDurationTime);
		mSubmit = (Button) findViewById(R.id.taskEditComplete);
		
		mDataHandler = new DataHandler(this, new DataEventReceiver() {
			@Override
			public void onAddTask(Task task, CommandResult cr) {
				super.onAddTask(task, cr);
				mProgressDialog.dismiss();
				
				Intent intent = getIntent();
				intent.putExtra("task", task);
				if ( cr == CommandResult.SUCCESS){
					
				}else
					Toast.makeText(getApplicationContext(),"Unable to save changes to the server", Toast.LENGTH_SHORT).show();
				setResult(RESULT_OK, intent);
				finish();
			}
		});

		setupForm();

		mProgressView.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						if (fromUser) {
							mProgressDiscreteView.setText(Integer
									.toString(progress));
						}
					}
				});

		mProgressDiscreteView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						if (!hasFocus) {
							try {
								int value = Integer
										.parseInt(mProgressDiscreteView
												.getText().toString());
								value = NumberUtils.clamp(value, 0, 100);
								mProgressDiscreteView.setText(Integer
										.toString(value));
								mProgressView.setProgress(value);
							} catch (NumberFormatException ex) {
								mProgressDiscreteView.setText(Integer
										.toString(mProgressView.getProgress()));
							}
						}
					}
				});

		mActivationDateButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				showDialog(ACTIVATION_DATE_PICKER_DIALOG);
			}
		});

		mActivationTimeButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				showDialog(ACTIVATION_TIME_PICKER_DIALOG);
			}
		});

		mExpirationDateButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				showDialog(EXPIRATION_DATE_PICKER_DIALOG);
			}
		});

		mExpirationTimeButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				showDialog(EXPIRATION_TIME_PICKER_DIALOG);
			}
		});

		mDurationDateButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				showDialog(DURATION_DATE_PICKER_DIALOG);
			}
		});

		mDurationTimeButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				showDialog(DURATION_TIME_PICKER_DIALOG);
			}
		});

		mSubmit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				createNewTaskFromForm();
			}
		});

		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case ACTIVATION_DATE_PICKER_DIALOG:
			return mActivationDateDialog;
		case ACTIVATION_TIME_PICKER_DIALOG:
			return mActivationTimeDialog;
		case EXPIRATION_DATE_PICKER_DIALOG:
			return mExpirationDateDialog;
		case EXPIRATION_TIME_PICKER_DIALOG:
			return mExpirationTimeDialog;
		case DURATION_DATE_PICKER_DIALOG:
			return mDurationDateDialog;
		case DURATION_TIME_PICKER_DIALOG:
			return mDurationTimeDialog;
		default:
			return null;
		}
	}

	private void createNewTaskFromForm() {
		mTask.setName(mNameView.getText().toString() == "" ? null : mNameView.getText().toString());
		mTask.setStatus(mStatusView.getText().toString() == "" ? null: mStatusView.getText().toString());
		mTask.setDetails(mDetailsView.getText().toString() == "" ? null: mDetailsView.getText().toString());
		mTask.setPriority(mPriorityView.getSelectedItemPosition());
		mTask.setProgress(mProgressView.getProgress());

		if (mTask.getAdditionDate() == null)
			mTask.setAdditionDate(new Date());
		mTask.setModifiedDate(new Date());
		mTask.setActivatedDate(mActivationCalendar.getTime());
		mTask.setExpirationDate(mExpirationCalendar.getTime());
		mTask.setEstimatedCompletionTime(Duration.difference(mDurationCalendar,mActivationCalendar));
		mTask.clearTags();
		
		List<String> tagList = Arrays.asList(mTagsView.getText().toString().split(","));
		
		mTask.addAllTags(tagList);

		mDataHandler.saveTaskForUser(mUser, mTask);
		mProgressDialog.show();
	}

	private void setupForm() {
		mNameView.setText(mTask.getName());
		mStatusView.setText(mTask.getStatus());
		mDetailsView.setText(mTask.getDetails());
		mProgressView.setProgress(mTask.getProgress() == null ? 0 : mTask.getProgress());
		mProgressDiscreteView.setText(Integer.toString(mTask.getProgress() == null ? 0: mTask.getProgress()));
		mPriorityView.setSelection(mTask.getPriority().ordinal());
		mTagsView.setText(TextUtils.join(", ", mTask.getTags()));
		
		List<String> tagList = mDataHandler.getAllTags();
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, tagList);
		
		mTagsView.setAdapter(adapter);
		mTagsView.setThreshold(1);
		mTagsView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

		Date activationDate, expirationDate;
		Duration dur = mTask.getEstimatedCompletionTime();
		activationDate = mTask.getActivatedDate() == null ? new Date() : mTask.getActivatedDate();
		expirationDate = mTask.getExpirationDate() == null ? new Date(
				System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000)) : mTask.getExpirationDate();

		if (expirationDate.getYear() > 2099)
			expirationDate.setYear(2099);

		mDurationCalendar = Calendar.getInstance();
		if (dur == null) {
			mDurationCalendar.roll(Calendar.DATE, 7);
		} else {
			mDurationCalendar.roll(Calendar.YEAR, dur.getYears());
			mDurationCalendar.roll(Calendar.MONTH, dur.getMonths());
			mDurationCalendar.roll(Calendar.DATE, dur.getDays());
			mDurationCalendar.roll(Calendar.HOUR_OF_DAY, dur.getHours());
			mDurationCalendar.roll(Calendar.MINUTE, dur.getMinutes());
			mDurationCalendar.roll(Calendar.SECOND, dur.getSeconds());
		}

		mActivationCalendar = Calendar.getInstance();
		mExpirationCalendar = Calendar.getInstance();
		mActivationCalendar.setTime(activationDate);
		mExpirationCalendar.setTime(expirationDate);

		updateActivationButtonText(mActivationCalendar);
		updateExpirationButtonText(mExpirationCalendar);
		updateDurationButtonText(mDurationCalendar);

		mActivationDateDialog = new DatePickerDialog(this,
				new DatePickerDialog.OnDateSetListener() {

					@Override
					public void onDateSet(DatePicker view, int year,
							int monthOfYear, int dayOfMonth) {
						mActivationCalendar.set(Calendar.YEAR, year);
						mActivationCalendar.set(Calendar.MONTH, monthOfYear);
						mActivationCalendar.set(Calendar.DATE, dayOfMonth);
						updateActivationButtonText(mActivationCalendar);
					}
				}, mActivationCalendar.get(Calendar.YEAR),
				mActivationCalendar.get(Calendar.MONTH),
				mActivationCalendar.get(Calendar.DATE));

		mActivationTimeDialog = new TimePickerDialog(this,
				new TimePickerDialog.OnTimeSetListener() {

					@Override
					public void onTimeSet(TimePicker view, int hourOfDay,
							int minute) {
						mActivationCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
						mActivationCalendar.set(Calendar.MINUTE, minute);
						updateActivationButtonText(mActivationCalendar);
					}
				}, mActivationCalendar.get(Calendar.HOUR_OF_DAY),
				mActivationCalendar.get(Calendar.MINUTE),
				DateFormat.is24HourFormat(this));

		mExpirationDateDialog = new DatePickerDialog(this,
				new DatePickerDialog.OnDateSetListener() {
					@Override
					public void onDateSet(DatePicker view, int year,
							int monthOfYear, int dayOfMonth) {
						mExpirationCalendar.set(Calendar.YEAR, year);
						mExpirationCalendar.set(Calendar.MONTH, monthOfYear);
						mExpirationCalendar.set(Calendar.DATE, dayOfMonth);
						updateExpirationButtonText(mExpirationCalendar);
					}
				}, 2011 + 88, mExpirationCalendar.get(Calendar.MONTH),
				mExpirationCalendar.get(Calendar.DATE));

		mExpirationTimeDialog = new TimePickerDialog(this,
				new TimePickerDialog.OnTimeSetListener() {

					@Override
					public void onTimeSet(TimePicker view, int hourOfDay,
							int minute) {
						mExpirationCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
						mExpirationCalendar.set(Calendar.MINUTE, minute);
						updateExpirationButtonText(mExpirationCalendar);
					}
				}, mExpirationCalendar.get(Calendar.HOUR_OF_DAY),
				mExpirationCalendar.get(Calendar.MINUTE),
				DateFormat.is24HourFormat(this));

		mDurationDateDialog = new DatePickerDialog(this,
				new DatePickerDialog.OnDateSetListener() {

					@Override
					public void onDateSet(DatePicker view, int year,
							int monthOfYear, int dayOfMonth) {
						mDurationCalendar.set(Calendar.YEAR, year);
						mDurationCalendar.set(Calendar.MONTH, monthOfYear);
						mDurationCalendar.set(Calendar.DATE, dayOfMonth);
						updateDurationButtonText(mDurationCalendar);
					}
				}, mDurationCalendar.get(Calendar.YEAR),
				mDurationCalendar.get(Calendar.MONTH),
				mDurationCalendar.get(Calendar.DATE));

		mDurationTimeDialog = new TimePickerDialog(this,
				new TimePickerDialog.OnTimeSetListener() {

					@Override
					public void onTimeSet(TimePicker view, int hourOfDay,
							int minute) {
						mDurationCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
						mDurationCalendar.set(Calendar.MINUTE, minute);
						updateDurationButtonText(mDurationCalendar);
					}
				}, mDurationCalendar.get(Calendar.HOUR_OF_DAY),
				mDurationCalendar.get(Calendar.MINUTE),
				DateFormat.is24HourFormat(this));
		
	}

	private void updateActivationButtonText(Calendar activation) {
		mActivationDateButton.setText(mDateFormatter.format(activation.getTime()));
		mActivationTimeButton.setText(mTimeFormatter.format(activation.getTime()));
	}

	private void updateExpirationButtonText(Calendar expiration) {
		mExpirationDateButton.setText(mDateFormatter.format(expiration.getTime()));
		mExpirationTimeButton.setText(mTimeFormatter.format(expiration.getTime()));
	}

	private void updateDurationButtonText(Calendar duration) {
		mDurationDateButton.setText(mDateFormatter.format(duration.getTime()));
		mDurationTimeButton.setText(mTimeFormatter.format(duration.getTime()));
	}
}
