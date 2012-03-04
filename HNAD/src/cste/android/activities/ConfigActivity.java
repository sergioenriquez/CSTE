package cste.android.activities;

import cste.android.R;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class ConfigActivity extends Activity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.configlayout);
	}
}
